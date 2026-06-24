package controller.repository;

import model.plants.Plant;
import model.plants.PlantFamily;
import model.plants.Tag;
import model.plants.plantAbilities.*;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.plants.upgradeEffect.BehaviorEffect;
import model.plants.upgradeEffect.BehaviorKey;
import model.plants.upgradeEffect.StatEffect;
import model.plants.upgradeEffect.StatKey;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlantRepository implements AssetRepository<Plant> {
    private final Map<String, Plant> plantDataMap = new HashMap<>();

    private static final String TAG_SEP = ";";
    private static final String PARAM_SEP = ";";
    private static final String PARAM_KV_SEP = "=";
    private static final Pattern STAT_VALUE = Pattern.compile("^([+-]?\\d+(?:\\.\\d+)?)(.*)$");

    @Override
    public void load(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = splitCsvLine(line);
                Plant plant = parseRow(cols);

                plantDataMap.put(plant.getName(), plant);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading file " + filePath, e);
        }
    }

    @Override
    public Plant get(String id) {
        return plantDataMap.get(id);
    }

    private Plant parseRow(String[] c) {
        Plant plant = new Plant();

        plant.setId(parseInt(c[0]));
        plant.setName(c[1]);
        plant.setFamily(PlantFamily.valueOf(c[2]));
        plant.setTags(parseTags(c[3]));
        plant.setSunCost(parseInt(c[4]));
        plant.setBaseHp(parseInt(c[5]));
        plant.setDamage(c[6]);

        String abilityClass = c[7];
        Map<String, String> abilityParams = parseParams(c[8]);
        plant.setBaseAbility(instantiateAbility(abilityClass));
        plant.setAbilityParams(abilityParams);

        plant.setPlantFoodMode(parsePlantFoodMode(c[10]));

        List<StatEffect>[] statUpgrades = new List[3];
        List<BehaviorEffect>[] behaviorUpgrades = new List[3];

        String[] upgradeCells = {c[12], c[13], c[14]};
        for (int i = 0; i < 3; i++) {
            statUpgrades[i] = new ArrayList<>();
            behaviorUpgrades[i] = new ArrayList<>();
            parseUpgradeCell(upgradeCells[i], statUpgrades[i], behaviorUpgrades[i]);
        }
        plant.setStatUpgrades(statUpgrades);
        plant.setBehaviorUpgrades(behaviorUpgrades);

        plant.setActionInterval(parseNullableDouble(c[15]));
        plant.setRechargeTime(parseDouble(c[16]));

        plant.setLevel(1);

        return plant;
    }

    private BaseAbility instantiateAbility(String abilityClass) {
        switch (abilityClass) {
            case "Shooters": return new Shooters();
            case "Lobber": return new Lobber();
            case "Explosive": return new Explosive();
            case "MeleeAttackers": return new MeleeAttackers();
            case "WallNut": return new WallNut();
            case "SunProducers": return new SunProducers();
            case "PlantFooder": return new PlantFooder();
            case "Modifier": return new Modifier();
            default:
                throw new IllegalArgumentException("Unknown abilityClass: " + abilityClass);
        }
    }

    private Map<String, String> parseParams(String raw) {
        Map<String, String> params = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return params;
        for (String pair : raw.split(PARAM_SEP)) {
            if (pair.isBlank()) continue;
            int idx = pair.indexOf(PARAM_KV_SEP);
            if (idx < 0) continue;
            params.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return params;
    }

    private PlantFoodMode parsePlantFoodMode(String raw) {
        if (raw == null || raw.isBlank()) return PlantFoodMode.NONE;
        return PlantFoodMode.valueOf(raw.trim());
    }

    private void parseUpgradeCell(String cell, List<StatEffect> stats, List<BehaviorEffect> behaviors) {
        if (cell == null || cell.isBlank()) return;
        String[] parts = cell.split(":", 3);
        if (parts.length < 2) return;

        String kind = parts[0];
        if ("STAT".equals(kind) && parts.length == 3) {
            StatKey key = StatKey.valueOf(parts[1]);
            Matcher m = STAT_VALUE.matcher(parts[2]);
            if (m.matches()) {
                double amount = Double.parseDouble(m.group(1));
                String unit = m.group(2);
                stats.add(new StatEffect(key, amount, unit));
            }
        } else if ("BEHAVIOR".equals(kind)) {
            BehaviorKey key = BehaviorKey.valueOf(parts[1]);
            behaviors.add(new BehaviorEffect(key));
        }
    }

    private EnumSet<Tag> parseTags(String raw) {
        EnumSet<Tag> tags = EnumSet.noneOf(Tag.class);
        if (raw == null || raw.isBlank()) return tags;
        for (String t : raw.split(TAG_SEP)) {
            if (!t.isBlank()) {
                tags.add(Tag.valueOf(t.trim()));
            }
        }
        return tags;
    }

    private int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    private double parseDouble(String s) {
        return Double.parseDouble(s.trim());
    }

    private Double parseNullableDouble(String s) {
        if (s == null || s.isBlank()) return null;
        return Double.parseDouble(s.trim());
    }

    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    fields.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }

    public Map<String, Plant> getPlantDataMap() {
        return plantDataMap;
    }
}
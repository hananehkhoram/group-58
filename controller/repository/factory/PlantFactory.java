package controller.repository.factory;

import controller.repository.DataManager;
import model.plants.Plant;

import java.util.LinkedHashMap;

public class PlantFactory extends BaseFactory<Plant> {

    public PlantFactory(DataManager dm) {
        super(dm);
    }

    @Override
    public Plant create(String id) {
        Plant template = dataManager.plants.get(id);
        if (template == null) {
            throw new IllegalArgumentException("Plant template not found in repository: " + id);
        }

        Plant newPlant = new Plant();

        newPlant.setId(template.getId());
        newPlant.setName(template.getName());
        newPlant.setFamily(template.getFamily());
        newPlant.setTags(template.getTags());
        newPlant.setSunCost(template.getSunCost());
        newPlant.setBaseHp(template.getBaseHp());
        newPlant.setDamage(template.getDamage());

        if (template.getBaseAbility() != null) {
            String abilityClassName = template.getBaseAbility().getClass().getSimpleName();
            newPlant.setBaseAbility(recreateAbility(abilityClassName));
        }

        newPlant.setAbilityParams(new LinkedHashMap<>(template.getAbilityParams()));
        newPlant.setPlantFoodMode(template.getPlantFoodMode());

        newPlant.setStatUpgrades(template.getStatUpgrades());
        newPlant.setBehaviorUpgrades(template.getBehaviorUpgrades());
        newPlant.setActionInterval(template.getActionInterval());
        newPlant.setRechargeTime(template.getRechargeTime());

        newPlant.setLevel(1);

        return newPlant;
    }

    private model.plants.plantAbilities.BaseAbility recreateAbility(String className) {
        switch (className) {
            case "Shooters": return new model.plants.plantAbilities.Shooters();
            case "Lobber": return new model.plants.plantAbilities.Lobber();
            case "Explosive": return new model.plants.plantAbilities.Explosive();
            case "MeleeAttackers": return new model.plants.plantAbilities.MeleeAttackers();
            case "WallNut": return new model.plants.plantAbilities.WallNut();
            case "SunProducers": return new model.plants.plantAbilities.SunProducers();
            case "PlantFooder": return new model.plants.plantAbilities.PlantFooder();
            case "Modifier": return new model.plants.plantAbilities.Modifier();
            default: return null;
        }
    }
}
package controller.repository;

import controller.repository.factory.LevelFactory;
import model.season.*;
import model.season.miniGameSeason.IzombieSeason;
import model.season.miniGameSeason.beghouledSeason;
import model.season.miniGameSeason.vaseSeason;
import model.season.miniGameSeason.wallnutsSeason;

import java.util.*;

public class SeasonRepository implements AssetRepository<Season> {
    private final Map<String, Season> seasonMap = new HashMap<>();
    private final List<Season> orderedSeasons = new ArrayList<>();
    private final List<Season> allSeasons = new ArrayList<>(orderedSeasons);

    @Override
    public void load(String filePath) {
        Season egypt = new AncientEgypt(LevelFactory.buildAncientEgyptLevels());
        Season frozen = new FrozenCaveChapter(LevelFactory.buildFrozenCaveLevels());
        Season beach = new BigWaveBeachSeason(LevelFactory.buildBigWaveBeachLevels());
        Season darkAges = new DarkAgesSeason(LevelFactory.buildDarkAgesLevels());

        Season wallnut = new wallnutsSeason(LevelFactory.buildWallnutsLevels());
        Season vase = new vaseSeason(LevelFactory.buildVaseLevels());
        Season izombie = new IzombieSeason(LevelFactory.buildIzombieLevels());
        Season beghouled = new beghouledSeason(LevelFactory.buldBeghouledLevels());

        orderedSeasons.add(egypt);
        orderedSeasons.add(frozen);
        orderedSeasons.add(beach);
        orderedSeasons.add(darkAges);

        allSeasons.add(wallnut);
        allSeasons.add(vase);
        allSeasons.add(izombie);
        allSeasons.add(beghouled);

        for (Season s : orderedSeasons) {
            seasonMap.put(s.getName(), s);
        }
        for (Season s : allSeasons) {
            seasonMap.put(s.getName(), s);
        }
    }

    @Override
    public Season get(String id) {
        return seasonMap.get(id);
    }

    public Collection<Season> getAll() {
        return seasonMap.values();
    }
    public List<Season> getMainChapters() {
        return orderedSeasons;
    }

    public int getChapterNumber(Season season) {
        return orderedSeasons.indexOf(season) + 1;   // ۱-پایه: مصر=۱, یخی=۲, ساحل=۳, تاریک=۴
    }

    public Season getNextSeason(Season current) {
        int idx = orderedSeasons.indexOf(current);
        if (idx == -1 || idx + 1 >= orderedSeasons.size()) {
            return null;   // یا فصل پیدا نشد، یا این آخرین فصل بود (Dark Ages)
        }
        return orderedSeasons.get(idx + 1);
    }

    public List<Season> getOrderedSeasons() {
        return orderedSeasons;
    }

    public List<Season> getAllSeasons() {
        return allSeasons;
    }
}
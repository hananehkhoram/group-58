package controller.repository;

import controller.repository.factory.LevelFactory;
import model.season.*;
import model.season.miniGameSeason.wallnutsSeason;

import java.util.HashMap;
import java.util.Map;

public class SeasonRepository implements AssetRepository<Season> {
    private final Map<String, Season> seasonMap = new HashMap<>();

    @Override
    public void load(String filePath) {
        Season egypt = new AncientEgypt(LevelFactory.buildAncientEgyptLevels());
        Season frozen = new FrozenCaveChapter(LevelFactory.buildFrozenCaveLevels());
        Season beach = new BigWaveBeachSeason(LevelFactory.buildBigWaveBeachLevels());
        Season darkAges = new DarkAgesSeason(LevelFactory.buildDarkAgesLevels());
        Season wallnut = new wallnutsSeason(LevelFactory.buildWallnutsLevels());

        seasonMap.put(egypt.getName(), egypt);
        seasonMap.put(frozen.getName(), frozen);
        seasonMap.put(beach.getName(), beach);
        seasonMap.put(darkAges.getName(), darkAges);
        seasonMap.put(wallnut.getName(), wallnut);
    }

    @Override
    public Season get(String id) {
        return seasonMap.get(id);
    }
}
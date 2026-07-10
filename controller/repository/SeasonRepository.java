package controller.repository;

import controller.repository.factory.LevelFactory;
import model.season.*;

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

        seasonMap.put(egypt.getName(), egypt);
        seasonMap.put(frozen.getName(), frozen);
        seasonMap.put(beach.getName(), beach);
        seasonMap.put(darkAges.getName(), darkAges);
    }

    @Override
    public Season get(String id) {
        return seasonMap.get(id);
    }
}
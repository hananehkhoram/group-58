package controller.repository;

import controller.repository.factory.LevelFactory;
import model.season.*;
import model.season.miniGameSeason.wallnutsSeason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeasonRepository implements AssetRepository<Season> {
    private final Map<String, Season> seasonMap = new HashMap<>();
    private final List<Season> orderedSeasons = new ArrayList<>();

    @Override
    public void load(String filePath) {
        Season egypt = new AncientEgypt(LevelFactory.buildAncientEgyptLevels());
        Season frozen = new FrozenCaveChapter(LevelFactory.buildFrozenCaveLevels());
        Season beach = new BigWaveBeachSeason(LevelFactory.buildBigWaveBeachLevels());
        Season darkAges = new DarkAgesSeason(LevelFactory.buildDarkAgesLevels());
        Season wallnut = new wallnutsSeason(LevelFactory.buildWallnutsLevels());

        orderedSeasons.add(egypt);
        orderedSeasons.add(frozen);
        orderedSeasons.add(beach);
        orderedSeasons.add(darkAges);
        orderedSeasons.add(wallnut);

        for (Season s : orderedSeasons) {
            seasonMap.put(s.getName(), s);
        }
    }

    @Override
    public Season get(String id) {
        return seasonMap.get(id);
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
}
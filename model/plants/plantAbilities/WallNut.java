package model.plants.plantAbilities;

import model.GameContext;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

import java.util.ArrayList;
import java.util.List;

public class WallNut implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }

    public void triggerAbility(WallNutType wallNutType, int damage, Plant self, GameEngine engine) {

        int pRow = self.getRow();
        int pCol = self.getCol();

        List<int[]> areaTiles = new ArrayList<>();

        switch (wallNutType) {

            case BLOCKER:


            case TALL_BLOCKER:

            case REFLECTIVE:

            case LANE_REDIRECT:

            case LANE_ATTRACT:

            case STACKABLE_COVER:

            case SUN_GENERATING:
        }
    }

    public void wall(WallNutType wallNutType, Plant plant, GameContext ctx) {


    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // GRANT_ARMOR: temporary or permanent bonus HP/armor
    }
}

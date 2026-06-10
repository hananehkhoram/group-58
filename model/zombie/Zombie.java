package model.zombie;

import model.season.Season;
import model.zombie.behavior.Behaviors;

import java.util.List;
import java.util.Map;

public class Zombie {
    private int x;
    private int y;
    private int hp;
    private double speed;
    private double damage;
    private String name;
    private Map<String, Behaviors> behaviors;
    private List<Effects> effects;
    private List<Season> seasonsAvailable;

    public String zombiesInfo() { return null;
    }// to show zombie infos

    public void update(){} //runs in each loop

    public boolean idDead(){return false;}
}
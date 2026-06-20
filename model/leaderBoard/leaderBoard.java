package model.leaderBoard;

import model.user.Gender;
import model.user.User;

import java.util.ArrayList;

public class leaderBoard extends User{

    public leaderBoard(String username, String password, String nickName, String email, Gender gender) {
        super(username, password, nickName, email, gender);
    }

    @Override
    public int getLastLevel() {
        return super.getLastLevel();
    }

    @Override
    public int getLastSeason() {
        return super.getLastSeason();
    }
}

package model.MiniGame.VaseGame;

public class Vase {
    private VaseContent content;
    private String hiddenEntityName;
    private boolean isBroken;

    public Vase(VaseContent content, String hiddenEntityName) {
        this.content = content;
        this.hiddenEntityName = hiddenEntityName;
        this.isBroken = false;
    }

    public VaseContent getContent() {
        return content;
    }
    public String getHiddenEntityName() {
        return hiddenEntityName;
    }

    public boolean isBroken() {
        return isBroken;
    }

    public void setBroken(boolean broken) {
        isBroken = broken;
    }
}

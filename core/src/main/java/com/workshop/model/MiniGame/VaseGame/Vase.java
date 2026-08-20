package com.workshop.model.MiniGame.VaseGame;

public class Vase {
    private VaseContent content;
    private String hiddenEntityName;
    private boolean isBroken;
    private VaseType type;

    public Vase(
        VaseContent content,
        String hiddenEntityName,
        VaseType type
    ) {
        this.content = content;
        this.hiddenEntityName = hiddenEntityName;
        this.type = type;
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

    public VaseType getType() {
        return type;
    }
}

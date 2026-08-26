package com.workshop.view.gameplay;

public final class ProjectileAnimationSpec {

    private final String pamPath;
    private final String clip;
    private final float scale;
    private final float offsetX;
    private final float offsetY;

    public ProjectileAnimationSpec(
        String pamPath,
        String clip,
        float scale,
        float offsetX,
        float offsetY
    ) {
        this.pamPath = pamPath;
        this.clip = clip;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public String getPamPath() {
        return pamPath;
    }

    public String getClip() {
        return clip;
    }

    public float getScale() {
        return scale;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }
}

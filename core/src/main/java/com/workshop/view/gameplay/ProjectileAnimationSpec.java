package com.workshop.view.gameplay;

public final class ProjectileAnimationSpec {

    private final String pamPath;
    private final String clip;
    private final String part;
    private final String imageResourceId;
    private final float scale;
    private final float offsetX;
    private final float offsetY;
    private final boolean freezeFrame;

    public ProjectileAnimationSpec(
        String pamPath,
        String clip,
        float scale,
        float offsetX,
        float offsetY
    ) {
        this(pamPath, clip, null, null, scale, offsetX, offsetY, false);
    }

    public ProjectileAnimationSpec(
        String pamPath,
        String clip,
        String part,
        String imageResourceId,
        float scale,
        float offsetX,
        float offsetY,
        boolean freezeFrame
    ) {
        this.pamPath = pamPath;
        this.clip = clip;
        this.part = part;
        this.imageResourceId = imageResourceId;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.freezeFrame = freezeFrame;
    }

    public String getPamPath() {
        return pamPath;
    }

    public String getClip() {
        return clip;
    }

    public String getPart() {
        return part;
    }

    public String getImageResourceId() {
        return imageResourceId;
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

    public boolean isFreezeFrame() {
        return freezeFrame;
    }
}

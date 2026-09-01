package com.workshop.model.mechanisms;

public final class ExplosionFx {

    public enum Kind {
        POTATO(10f, 0.28f),
        PRIMAL_POTATO(14f, 0.32f),
        CHERRY(16f, 0.40f),
        GRAPESHOT(16f, 0.40f),
        JALAPENO(14f, 0.38f),
        DOOM(28f, 0.55f),
        GENERIC(12f, 0.30f),
        ICEAGE_MISSILE(14f, 0.32f);

        public final float shakeIntensity;
        public final float shakeDuration;

        Kind(float shakeIntensity, float shakeDuration) {
            this.shakeIntensity = shakeIntensity;
            this.shakeDuration = shakeDuration;
        }
    }

    public final int row;
    public final int col;
    public final Kind kind;

    public ExplosionFx(int row, int col, Kind kind) {
        this.row = row;
        this.col = col;
        this.kind = kind;
    }
}

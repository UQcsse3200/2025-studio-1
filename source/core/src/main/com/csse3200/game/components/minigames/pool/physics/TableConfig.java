package com.csse3200.game.components.minigames.pool.physics;

public class TableConfig {
    private final float tableW, tableH;
    private final float railX, railY;
    private final float ballR;
    private final float pocketRScale;
    private final float pocketInsetScaleX, pocketInsetScaleY;
    private final float pocketFunnelScale;

    private TableConfig(float tableW, float tableH, float railX, float railY,
                        float ballR, float pocketRScale,
                        float insetX, float insetY, float funnelScale) {
        this.tableW = tableW;
        this.tableH = tableH;
        this.railX = railX;
        this.railY = railY;
        this.ballR = ballR;
        this.pocketRScale = pocketRScale;
        this.pocketInsetScaleX = insetX;
        this.pocketInsetScaleY = insetY;
        this.pocketFunnelScale = funnelScale;
    }

    // ---- Builder ----
    public static Builder builder() {
        return new Builder();
    }

    public float tableW() {
        return tableW;
    }

    public float tableH() {
        return tableH;
    }

    public float railX() {
        return railX;
    }

    public float railY() {
        return railY;
    }

    public float ballR() {
        return ballR;
    }

    public float pocketR() {
        return ballR * pocketRScale;
    }

    public float pocketInsetX() {
        return railX * pocketInsetScaleX;
    }

    public float pocketInsetY() {
        return railY * pocketInsetScaleY;
    }

    public float pocketFunnel() {
        return ballR * pocketFunnelScale;
    }

    public static class Builder {
        private float tableW, tableH, railX, railY, ballR;
        private float pocketRScale = 1.9f, insetX = 1f, insetY = 1f, funnel = 0.6f;

        public Builder tableSize(float w, float h) {
            this.tableW = w;
            this.tableH = h;
            return this;
        }

        public Builder railThickness(float x, float y) {
            this.railX = x;
            this.railY = y;
            return this;
        }

        public Builder ballRadius(float r) {
            this.ballR = r;
            return this;
        }

        public Builder pocketRadiusScale(float s) {
            this.pocketRScale = s;
            return this;
        }

        public Builder pocketInsetScaleX(float s) {
            this.insetX = s;
            return this;
        }

        public Builder pocketInsetScaleY(float s) {
            this.insetY = s;
            return this;
        }

        public Builder pocketFunnelScale(float s) {
            this.funnel = s;
            return this;
        }

        public TableConfig build() {
            return new TableConfig(tableW, tableH, railX, railY, ballR, pocketRScale, insetX, insetY, funnel);
        }
    }
}

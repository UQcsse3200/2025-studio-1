package com.csse3200.game.components.minigames.pool.physics;

/**
 * Defines the physical dimensions and scale factors for the pool table setup.
 * <p>
 * The {@code TableConfig} provides geometric parameters such as table size,
 * rail thickness, ball radius, and pocket layout scales. These values are
 * consumed by components like {@link TableBuilder} and {@link BallFactory}
 * to construct the simulation accurately.
 * <p>
 * Instances of this class are created using the {@link Builder} for clarity
 * and flexibility.
 */
public class TableConfig {
    private final float tableW, tableH;
    private final float railX, railY;
    private final float ballR;
    private final float pocketRScale;
    private final float pocketInsetScaleX, pocketInsetScaleY;
    private final float pocketFunnelScale;

    /**
     * Creates a new immutable table configuration.
     *
     * @param tableW       table width (world units)
     * @param tableH       table height (world units)
     * @param railX        rail thickness along the X axis
     * @param railY        rail thickness along the Y axis
     * @param ballR        ball radius (world units)
     * @param pocketRScale scale factor for pocket radius relative to ball radius
     * @param insetX       horizontal inset scale for pocket position
     * @param insetY       vertical inset scale for pocket position
     * @param funnelScale  scale factor for pocket funnel offset
     */
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


    /**
     * Returns a new {@link Builder} for constructing a {@code TableConfig}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }


    /**
     * @return table width in world units
     */
    public float tableW() {
        return tableW;
    }

    /**
     * @return table height in world units
     */
    public float tableH() {
        return tableH;
    }

    /**
     * @return rail thickness along the X axis
     */
    public float railX() {
        return railX;
    }

    /**
     * @return rail thickness along the Y axis
     */
    public float railY() {
        return railY;
    }

    /**
     * @return radius of each pool ball in world units
     */
    public float ballR() {
        return ballR;
    }

    /**
     * @return pocket radius in world units (scaled from ball radius)
     */
    public float pocketR() {
        return ballR * pocketRScale;
    }

    /**
     * @return horizontal pocket inset distance from the rail edge
     */
    public float pocketInsetX() {
        return railX * pocketInsetScaleX;
    }

    /**
     * @return vertical pocket inset distance from the rail edge
     */
    public float pocketInsetY() {
        return railY * pocketInsetScaleY;
    }

    /**
     * @return pocket funnel offset, used to smooth the corner entry
     */
    public float pocketFunnel() {
        return ballR * pocketFunnelScale;
    }

    /**
     * Fluent builder for constructing immutable {@link TableConfig} instances.
     * <p>
     * All units are expressed in world coordinates.
     */
    public static class Builder {
        private float tableW, tableH, railX, railY, ballR;
        private float pocketRScale = 1.9f, insetX = 1f, insetY = 1f, funnel = 0.6f;

        /**
         * Sets the table size.
         *
         * @param w table width in world units
         * @param h table height in world units
         * @return this builder for chaining
         */
        public Builder tableSize(float w, float h) {
            this.tableW = w;
            this.tableH = h;
            return this;
        }

        /**
         * Sets the rail thickness in both axes.
         *
         * @param x horizontal rail thickness
         * @param y vertical rail thickness
         * @return this builder for chaining
         */
        public Builder railThickness(float x, float y) {
            this.railX = x;
            this.railY = y;
            return this;
        }

        /**
         * Sets the ball radius.
         *
         * @param r ball radius in world units
         * @return this builder for chaining
         */
        public Builder ballRadius(float r) {
            this.ballR = r;
            return this;
        }

        /**
         * Sets the pocket radius scale relative to the ball radius.
         *
         * @param s scale factor (default = 1.9)
         * @return this builder for chaining
         */
        public Builder pocketRadiusScale(float s) {
            this.pocketRScale = s;
            return this;
        }

        /**
         * Sets the horizontal inset scale for pocket placement.
         *
         * @param s scale factor relative to {@code railX}
         * @return this builder for chaining
         */
        public Builder pocketInsetScaleX(float s) {
            this.insetX = s;
            return this;
        }

        /**
         * Sets the vertical inset scale for pocket placement.
         *
         * @param s scale factor relative to {@code railY}
         * @return this builder for chaining
         */
        public Builder pocketInsetScaleY(float s) {
            this.insetY = s;
            return this;
        }

        /**
         * Sets the funnel scale, controlling the pocket entry offset.
         *
         * @param s scale factor relative to {@code ballR}
         * @return this builder for chaining
         */
        public Builder pocketFunnelScale(float s) {
            this.funnel = s;
            return this;
        }

        /**
         * Builds the {@link TableConfig} instance using the configured parameters.
         *
         * @return a new {@code TableConfig} object
         */
        public TableConfig build() {
            return new TableConfig(tableW, tableH, railX, railY, ballR, pocketRScale, insetX, insetY, funnel);
        }
    }
}
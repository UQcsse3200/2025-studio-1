package com.csse3200.game.components;

public class AmmoStatsComponent extends Component {

    private int ammo;

    public AmmoStatsComponent(int ammo) {

        this.ammo = ammo;
    }

    /**
     * Gets the player's current amount off ammo (bullets they can fire)
     *
     * @return player's ammo.
     */
    public int getAmmo() {

        return this.ammo;
    }

    /**
     * Sets the player's current ammo count to a new value
     *
     * @param ammo the desired amount  ammo for the player to carry
     */
    public void setAmmo(int ammo) {

        this.ammo = ammo;
    }

}

package com.csse3200.game.components;


import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * Stores and manages a given weapon's magazine information
 */

public class MagazineComponent extends Component{

    private int currentAmmo;
    private int maxAmmo;
    private float timeSinceLastReload;

    /**
     * Constructs an magazine component
     * @param maxAmmo the maximum amount of ammunition that can be held by the weapon
     */

    public MagazineComponent(int maxAmmo) {

        this.maxAmmo = maxAmmo;
        this.currentAmmo = maxAmmo;
        this.timeSinceLastReload = 1.51f;
    }

    /**
     * Updates the reload cooldown
     */

    public void update() {

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timeSinceLastReload += dt;
    }


    /**
     * Returns current amount of ammo in magazine
     * @return ammo in magazine
     */

    public int getCurrentAmmo() {

        return this.currentAmmo;
    }

    /**
     * Sets the current ammo to a new value
     * @param newAmmo ammunition to be stored in the magazine
     */

    public void setCurrentAmmo(int newAmmo) {

        this.currentAmmo = newAmmo;
    }

    /**
     * Reloads the magazine from player's current ammo supply
     * @return true if reload is successful and adds ammunition to the magazine, false otherwise
     */

    public boolean reload(Entity player) {

        if (currentAmmo == maxAmmo) {

            return false;
        }

        AmmoStatsComponent ammoStats =  player.getComponent(AmmoStatsComponent.class);
        int ammoReserves = ammoStats.getAmmo();
        if (ammoReserves <= 0) {

            return false;
        }

        if (ammoReserves < maxAmmo) {

            this.setCurrentAmmo(currentAmmo + ammoReserves);
            ammoStats.setAmmo(0);
        }

        else {
            int reloadedAmmo = maxAmmo - currentAmmo;
            this.setCurrentAmmo(maxAmmo);
            ammoStats.setAmmo(ammoReserves - reloadedAmmo);
        }

        timeSinceLastReload = 0;
        return true;
    }

    /**
     * Determines if the weapon is currently reloading
     * @return true if the weapon is undergoing a reload, false otherwise
     */

    public boolean reloading() {

        float reloadDuration = 1.5f;
        return this.timeSinceLastReload <= reloadDuration;
    }

    /**
     * Sets the time since last reload, used for testing purposes
     * @param timeSinceLastReload designated time since last reload
     */

    public void setTimeSinceLastReload(float timeSinceLastReload) {

        this.timeSinceLastReload = timeSinceLastReload;
    }

}

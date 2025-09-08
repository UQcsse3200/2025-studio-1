package com.csse3200.game.components;


import com.csse3200.game.entities.Entity;

/**
 * Stores and manages a given weapon's magazine information
 */

public class MagazineComponent extends Component{

    private int currentAmmo;
    private int maxAmmo;

    /**
     * Constructs an magazine component
     * @param maxAmmo the maximum amount of ammunition that can be held by the weapon
     */

    public MagazineComponent(int maxAmmo) {

        this.maxAmmo = maxAmmo;
        this.currentAmmo = maxAmmo;
    }

    /**
     * Adds listener for reload event
     */
    @Override
    public void create() {

        entity.getEvents().addListener("reload", this::reload);
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

    public boolean reload() {

        if (currentAmmo == maxAmmo) {

            return false;
        }

        CombatStatsComponent combatStats =  entity.getComponent(CombatStatsComponent.class);
        int ammoReserves = combatStats.getAmmo();
        if (ammoReserves <= 0) {

            return false;
        }

        if (ammoReserves < maxAmmo) {

            this.setCurrentAmmo(currentAmmo + ammoReserves);
            combatStats.setAmmo(0);
            return true;
        }

        int reloadedAmmo = maxAmmo - currentAmmo;
        this.setCurrentAmmo(maxAmmo);
        combatStats.setAmmo(ammoReserves - reloadedAmmo);
        return true;
    }

}

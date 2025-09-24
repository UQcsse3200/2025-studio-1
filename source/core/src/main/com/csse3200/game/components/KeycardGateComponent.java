package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * A gate that requires a specific keycard level to pass through.
 * The gate will remain locked until the player collides with it AND
 * has the required keycard level in their InventoryComponent.
 */
public class KeycardGateComponent extends Component {

    private final String KEYCARD_GATE = "KeycardGate";

    /**
     * Global cheat: when true, all keycard checks are bypassed.
     */
    private static volatile boolean GLOBAL_OVERRIDE = false;

    /**
     * Enable/disable the global keycard-gate override.
     */
    public static void setGlobalOverride(boolean enabled) {
        GLOBAL_OVERRIDE = enabled;
    }

    /**
     * @return true if the global override is enabled.
     */
    public static boolean isGlobalOverride() {
        return GLOBAL_OVERRIDE;
    }

    // --- existing fields ---
    private final int requiredLevel;
    private final Runnable onUnlock;
    private boolean unlocked = false;

    public KeycardGateComponent(int requiredLevel, Runnable onUnlock) {
        this.requiredLevel = requiredLevel;
        this.onUnlock = onUnlock;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        Object meUd = me.getBody().getUserData();
        Object otherUd = other.getBody().getUserData();
        if (!(meUd instanceof BodyUserData) || !(otherUd instanceof BodyUserData)) return;

        Entity meEntity = ((BodyUserData) meUd).entity;
        Entity otherEntity = ((BodyUserData) otherUd).entity;
        if (meEntity == null || otherEntity == null) return;
        if (meEntity != this.entity) return;

        // === cheat bypass ===
        if (GLOBAL_OVERRIDE) {
            if (!unlocked) {
                unlock();
                Gdx.app.log(KEYCARD_GATE, "Override enabled: gate unlocked (bypassing level " + requiredLevel + ")");
                if (onUnlock != null) {
                    Gdx.app.postRunnable(onUnlock);
                }
            }
            return;
        }

        // === normal keycard check ===
        InventoryComponent inventory = otherEntity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            int level = inventory.getKeycardLevel();
            Gdx.app.log(KEYCARD_GATE, "Gate sees keycard level: " + level);
            if (level >= requiredLevel) {
                if (!unlocked) {
                    unlock();
                    Gdx.app.log(KEYCARD_GATE, "Gate unlocked. Allowing passage.");
                    if (onUnlock != null) {
                        Gdx.app.postRunnable(onUnlock);
                    }
                }
            } else {
                Gdx.app.log(KEYCARD_GATE, "Gate locked. Requires keycard level " + requiredLevel);
            }
        }
    }

    public void unlock() {
        unlocked = true;
        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider != null) {
            collider.setSensor(true);
        }
    }

    public boolean isUnlocked() {
        return unlocked;
    }
}

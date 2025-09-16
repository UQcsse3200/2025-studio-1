package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/**
 * Cocoon Spawner Component - Spawns white cocoons when Boss health drops to 30%
 * Works together with existing BossDefenseComponent for invulnerability
 * Only handles cocoon spawning and tracking, not Boss defense state
 */
public class CocoonSpawnerComponent extends Component {

    private final float healthThreshold; // Health percentage threshold to spawn cocoons (0.3f = 30%)
    private final Vector2[] cocoonPositions; // Spawn positions for cocoons
    private final List<Entity> activeCocoons; // List of currently alive cocoons

    private boolean cocoonsSpawned = false; // Whether cocoons have been spawned
    private boolean shouldSpawnCocoons = false; // Flag to spawn cocoons in next update cycle
    private int maxHealth; // Boss's maximum health

    /**
     * Constructor
     * @param healthThreshold Health percentage threshold to spawn cocoons (0.0-1.0)
     * @param cocoonPositions Array of spawn positions for cocoons
     */
    public CocoonSpawnerComponent(float healthThreshold, Vector2[] cocoonPositions) {
        this.healthThreshold = healthThreshold;
        this.cocoonPositions = cocoonPositions.clone();
        this.activeCocoons = new ArrayList<>();
    }

    @Override
    public void create() {
        super.create();
        // Get Boss's maximum health
        CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
        if (combatStats != null) {
            this.maxHealth = combatStats.getMaxHealth();
        }

        System.out.println("CocoonSpawnerComponent created and ready!");

        // Listen for Boss health update events
        entity.getEvents().addListener("updateHealth", this::onBossHealthUpdate);
    }

    @Override
    public void update() {
        super.update();

        // Check if we should spawn cocoons in this update cycle (safe from Box2D locks)
        if (shouldSpawnCocoons) {
            shouldSpawnCocoons = false;
            createCocoonsNow();
        }
    }

    /**
     * Handler for when Boss health is updated
     */
    private void onBossHealthUpdate(int currentHealth) {
        System.out.println("Boss health updated! Current: " + currentHealth + "/" + maxHealth + ", spawned: " + cocoonsSpawned);

        if (cocoonsSpawned) return;

        float currentHealthPercent = (float) currentHealth / maxHealth;
        System.out.println("Boss health percentage: " + (currentHealthPercent * 100) + "%");

        // Spawn cocoons when health drops below threshold
        if (currentHealthPercent <= healthThreshold) {
            System.out.println("Health threshold reached! Flagging for cocoon spawning...");
            cocoonsSpawned = true;
            shouldSpawnCocoons = true; // Flag for next update cycle
        } else {
            System.out.println("Health threshold not reached yet. Need: " + (healthThreshold * 100) + "%");
        }
    }

    /**
     * Actually create the cocoons (called from update cycle, safe from Box2D locks)
     */
    private void createCocoonsNow() {
        System.out.println("Creating cocoons now in safe update cycle...");

        for (Vector2 position : cocoonPositions) {
            Entity cocoon = createCocoonEntity(position);
            activeCocoons.add(cocoon);

            // Add cocoon to game world
            ServiceLocator.getEntityService().register(cocoon);

            // Set up death notification for this specific cocoon
            setupCocoonDeathListener(cocoon);
        }

        // Trigger cocoon spawned event (can be used for effects/sounds)
        entity.getEvents().trigger("cocoonsSpawned", activeCocoons.size());

        System.out.println("Successfully spawned " + activeCocoons.size() + " white cocoons!");
    }

    /**
     * Create a cocoon entity
     */
    private Entity createCocoonEntity(Vector2 position) {
        Entity cocoon = new Entity()
                .addComponent(new com.csse3200.game.physics.components.PhysicsComponent())
                .addComponent(new com.csse3200.game.physics.components.ColliderComponent())
                .addComponent(new com.csse3200.game.physics.components.HitboxComponent().setLayer(com.csse3200.game.physics.PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(20)) // Cocoon's health
                .addComponent(new com.csse3200.game.rendering.TextureRenderComponent("images/white_cocoon.png"))
                .addComponent(new IndividualCocoonComponent()); // Individual cocoon-specific component

        // Set cocoon position
        cocoon.setPosition(position);

        // Scale cocoon size
        cocoon.getComponent(com.csse3200.game.rendering.TextureRenderComponent.class).scaleEntity();
        cocoon.setScale(new Vector2(1.0f, 1.0f));

        // Set collider
        com.csse3200.game.physics.PhysicsUtils.setScaledCollider(cocoon, 0.8f, 0.8f);

        cocoon.getComponent(com.csse3200.game.physics.components.PhysicsComponent.class)
                .setBodyType(BodyDef.BodyType.StaticBody);

        return cocoon;
    }

    /**
     * Set up death listener for a specific cocoon
     */
    private void setupCocoonDeathListener(Entity cocoon) {
        cocoon.getEvents().addListener("death", () -> {
            onCocoonDestroyed(cocoon);
        });
    }

    /**
     * Handler for when a cocoon is destroyed
     */
    private void onCocoonDestroyed(Entity destroyedCocoon) {
        activeCocoons.remove(destroyedCocoon);

        System.out.println("Cocoon destroyed! Remaining cocoons: " + activeCocoons.size());

        // Trigger event when all cocoons are destroyed
        if (activeCocoons.isEmpty() && cocoonsSpawned) {
            entity.getEvents().trigger("allCocoonsDestroyed");
            System.out.println("All cocoons destroyed!");
        }
    }

    /**
     * Check if cocoons have been spawned
     */
    public boolean areCocoonsSpawned() {
        return cocoonsSpawned;
    }

    /**
     * Get remaining cocoons count
     */
    public int getRemainingCocoonsCount() {
        return activeCocoons.size();
    }

    /**
     * Get list of active cocoons (for debugging or special effects)
     */
    public List<Entity> getActiveCocoons() {
        return new ArrayList<>(activeCocoons);
    }

    /**
     * Force cleanup all cocoons (for debugging or special cases)
     */
    public void forceCleanupCocoons() {
        // Clean up all remaining cocoons
        for (Entity cocoon : new ArrayList<>(activeCocoons)) {
            cocoon.dispose();
        }
        activeCocoons.clear();
    }

    @Override
    public void dispose() {
        // Clean up resources
        forceCleanupCocoons();
        super.dispose();
    }
}
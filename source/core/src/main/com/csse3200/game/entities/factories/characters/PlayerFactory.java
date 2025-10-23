package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.*;
import com.csse3200.game.components.player.*;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.characters.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create a player entity.
 *
 * <p>Predefined player properties are loaded from a config stored as a json file and should have
 * the properties stored in 'PlayerConfig'.
 */
public class PlayerFactory {
    private static PlayerConfig stats;

    private PlayerFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     *
     * @return the player config
     */
    public static PlayerConfig getStats() {
        if (stats == null) {
            stats = safeLoadPlayerConfig();
        }
        return stats;
    }

    /**
     * Sets the stats
     * @param pc PlayerConfig
     */
    public static void setStats(PlayerConfig pc) {
        stats = pc;
    }

    /**
     * resets the stats (for testing)
     */
    public static void reset() {
        stats = null;
    }

    private static PlayerConfig safeLoadPlayerConfig() {
        String path = "configs/player.json";
        return FileLoader.read(PlayerConfig.class, path, FileLoader.Location.INTERNAL)
                .orElseGet(() -> {
                    PlayerConfig cfg = new PlayerConfig();
                    cfg.gold = 0;
                    cfg.health = 100;
                    cfg.baseAttack = 10;
                    return cfg;
                });
    }

    /**
     * Create a player entity.
     *
     * @return entity
     */
    public static Entity createPlayer() {
        InputComponent inputComponent =
                ServiceLocator.getInputService().getInputFactory().createForPlayer();
        stats = safeLoadPlayerConfig();
        InventoryComponent playerInventory = new InventoryComponent(stats.gold);

        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService()
                        .getAsset(AvatarRegistry.get().atlas(), TextureAtlas.class));
        add_animations(animator);

        ColliderComponent groundCollider = new ColliderComponent().setLayer(PhysicsLayer.PLAYER);
        PlayerHurtboxComponent playerCollider = new PlayerHurtboxComponent();

        Entity player =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(groundCollider)
                        .addComponent(playerCollider)
                        .addComponent(new PlayerActions())
                        .addComponent(new CombatStatsComponent(AvatarRegistry.get().baseHealth()))
                        .addComponent(new WeaponsStatsComponent(AvatarRegistry.get().baseDamage()))
                        .addComponent(new AmmoStatsComponent(1000))
                        .addComponent(playerInventory)
                        .addComponent(new ItemPickUpComponent(playerInventory))
                        .addComponent(inputComponent)
                        .addComponent(new PlayerStatsDisplay())
                        .addComponent(new PlayerInventoryDisplay(playerInventory))
                        .addComponent(new StaminaComponent())
                        .addComponent(animator)
                        .addComponent(new PowerupComponent())
                        .addComponent(new PlayerAnimationController())
                        .addComponent(new PlayerEquipComponent())
                        .addComponent(new ArmourEquipComponent())
                        .addComponent(new PlayerActionValidator())
                        .addComponent(new InteractComponent().setLayer(PhysicsLayer.DEFAULT));
        // Ensure global player reference is up-to-date for transitions
        ServiceLocator.registerPlayer(player);

        // Scale Player Sprite
        player.getComponent(AnimationRenderComponent.class).scaleEntity(2f);

        // Scale and set up Player Colliders
        PhysicsUtils.setScaledCollider(player, groundCollider,0.15f, 0.1f);
        PhysicsUtils.setScaledCollider(player, playerCollider, 0.2f, 0.4f);
        playerCollider.setDensity(1.5f);
        playerCollider.setFriction(0f);
        PhysicsUtils.setScaledCollider(player, player.getComponent(InteractComponent.class), 1f, 0.75f);

        player.getComponent(WeaponsStatsComponent.class).setCoolDown(0.2f);
        player.addComponent(new PowerComponent(player));
        return player;
    }

    /**
     * Add player animations to animation render component.
     *
     * @param animator animation render component for the player
     */
    private static void add_animations(AnimationRenderComponent animator) {
        animator.addAnimation("right_run", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("left_run", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("right_jump", 0.1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("left_jump", 0.1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("right_stand", 0.2f, Animation.PlayMode.LOOP);
        animator.addAnimation("left_stand", 0.2f, Animation.PlayMode.LOOP);
        animator.addAnimation("right_walk", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("left_walk", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("right_crouch", 0.2f, Animation.PlayMode.LOOP);
        animator.addAnimation("left_crouch", 0.2f, Animation.PlayMode.LOOP);
        animator.addAnimation("right_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("left_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("right_fall", 0.1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("left_fall", 0.1f, Animation.PlayMode.NORMAL);
        animator.startAnimation("right_stand");
    }

    /**
     * Create a full-featured player entity that uses arrow keys for movement,
     * matching the main player visuals/animations.
     */
    public static Entity createPlayerWithArrowKeys() {
        InputComponent inputComponent = new ArrowKeysPlayerInputComponent();
        InventoryComponent playerInventory = new InventoryComponent(stats.gold);

        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService()
                        .getAsset("images/player.atlas", TextureAtlas.class));
        add_animations(animator);

        Entity player =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                        .addComponent(new PlayerActions())
                        .addComponent(new CombatStatsComponent(stats.health))
                        .addComponent(new WeaponsStatsComponent(stats.baseAttack))
                        .addComponent(playerInventory)
                        .addComponent(new ItemPickUpComponent(playerInventory))
                        .addComponent(inputComponent)
                        .addComponent(new PlayerStatsDisplay())
                        .addComponent(new PlayerInventoryDisplay(playerInventory))
                        .addComponent(new StaminaComponent())
                        .addComponent(animator)
                        .addComponent(new PlayerAnimationController());

        player.getComponent(AnimationRenderComponent.class).scaleEntity(2f);
        PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
        player.getComponent(ColliderComponent.class).setDensity(1.5f);
        PhysicsUtils.setScaledCollider(player, 0.3f, 0.5f);
        player.getComponent(WeaponsStatsComponent.class).setCoolDown(0.2f);
        return player;
    }

}

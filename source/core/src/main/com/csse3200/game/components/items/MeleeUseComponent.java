package com.csse3200.game.components.items;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Handles melee weapon usage and attack logic for entities.
 * <p>
 * Checks cooldowns, detects nearby enemies, applies damage, and triggers attack animations and sounds.
 */
public class MeleeUseComponent extends ItemActionsComponent {
    /** Last time (in ms) a melee attack occurred. */
    private float lastAttackTime = -9999f;

    /** Maximum attack range in world units. */
    private static final float ATTACK_RANGE = 3f;

    /** Sound file path for melee impact effects. */
    private static final String IMPACT_SOUND_PATH = "sounds/Impact4.ogg";

    /**
     * Performs a melee attack from the player entity if the cooldown has elapsed.
     *
     * @param player The entity performing the attack.
     */
    public void use(Entity player) {
        if (ServiceLocator.getTimeSource().isPaused()) {
            return;
        }

        WeaponsStatsComponent weaponStats = entity.getComponent(WeaponsStatsComponent.class);
        if (weaponStats == null) {
            return;
        }

        float currentTime = ServiceLocator.getTimeSource().getTime();
        float cooldownMillis = weaponStats.getCoolDown() * 1000f;

        // Enforce weapon cooldown
        if (currentTime - lastAttackTime < cooldownMillis) {
            return;
        }

        attackNearbyEnemies(player, weaponStats.getBaseAttack());
        lastAttackTime = currentTime;
        triggerWeaponAnimation();
    }

    /**
     * Iterates through all entities and applies melee damage to nearby enemies.
     *
     * @param player      The attacking entity.
     * @param baseDamage  The amount of damage to deal per hit.
     */
    private void attackNearbyEnemies(Entity player, int baseDamage) {
        for (Entity target : ServiceLocator.getEntityService().getEntities()) {
            if (target == player) continue;

            CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
            HitboxComponent targetHitbox = target.getComponent(HitboxComponent.class);

            if (targetStats == null || targetHitbox == null) continue;
            if (targetHitbox.getLayer() != PhysicsLayer.NPC) continue;

            float distance = target.getCenterPosition().dst(player.getCenterPosition());
            if (distance > ATTACK_RANGE) continue;

            targetStats.takeDamage(baseDamage);
            playImpactSound();
        }
    }

    /** Plays the melee impact sound effect. */
    private void playImpactSound() {
        Sound attackSound = ServiceLocator.getResourceService()
                .getAsset(IMPACT_SOUND_PATH, Sound.class);
        if (attackSound != null) {
            attackSound.play();
        }
    }

    /** Triggers the weapon’s animation if it has one. */
    void triggerWeaponAnimation() {
        AnimationRenderComponent anim = entity.getComponent(AnimationRenderComponent.class);
        if (anim != null) {
            entity.getEvents().trigger("anim");
        }
    }
}

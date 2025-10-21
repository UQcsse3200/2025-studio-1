package com.csse3200.game.components.items;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class MeleeUseComponent extends ItemActionsComponent {
    private float timeSinceLastAttack = -9999f;

    public void use(Entity player) {
        // Melee Attack Implementation Sourced From PlayerActions (Shouldn't check all entities but not my work so)
        if (ServiceLocator.getTimeSource().isPaused()) return;
        WeaponsStatsComponent meleeStats = entity.getComponent(WeaponsStatsComponent.class);
        if (meleeStats == null) {
            return;
        }
        float coolDown = meleeStats.getCoolDown();
        float curTime = ServiceLocator.getTimeSource().getTime();
        if ((curTime - timeSinceLastAttack) < coolDown * 1000) {
            return;
        }

        float attackRange = 3f;

        for (Entity enemy : ServiceLocator.getEntityService().getEntities()) {
            if (enemy != player) {
                CombatStatsComponent enemyStats = enemy.getComponent(CombatStatsComponent.class);
                HitboxComponent enemyHitBox = enemy.getComponent(HitboxComponent.class);

                if (enemyStats != null && enemyHitBox != null) {
                    if (enemyHitBox.getLayer() == PhysicsLayer.NPC) {
                        float distance = enemy.getCenterPosition().dst(player.getCenterPosition());
                        if (distance <= attackRange) {
                            enemyStats.takeDamage(meleeStats.getBaseAttack());
                            Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
                            attackSound.play();
                        }
                    }
                }
            }
        }

        timeSinceLastAttack = ServiceLocator.getTimeSource().getTime();
        weaponAnimation();
    }

    private void weaponAnimation() {
        if (entity.getComponent(AnimationRenderComponent.class) != null) {
            entity.getEvents().trigger("anim");
        }
    }
}

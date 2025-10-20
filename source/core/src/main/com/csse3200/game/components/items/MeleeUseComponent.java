package com.csse3200.game.components.items;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class MeleeUseComponent extends ItemActionsComponent {
    private float timeSinceLastAttack;

    public void update() {
        timeSinceLastAttack += ServiceLocator.getTimeSource().getDeltaTime();
    }

    public void use(Entity player) {
        // Melee Attack Implementation Sourced From PlayerActions (Shouldn't check all entities but not my work so)
        if (ServiceLocator.getTimeSource().isPaused()) return;
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        Entity melee = inventory.getCurrSlot();
        if (melee == null) {
            return;
        }
        WeaponsStatsComponent meleeStats = melee.getComponent(WeaponsStatsComponent.class);
        if (meleeStats == null) {
            return;
        }
        float coolDown = meleeStats.getCoolDown();
        if (this.timeSinceLastAttack < coolDown) {
            return;
        }
        Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
        attackSound.play();

        float attackRange = 3f;

        for (Entity enemy : ServiceLocator.getEntityService().getEntities()) {
            if (enemy != entity) {
                CombatStatsComponent enemyStats = enemy.getComponent(CombatStatsComponent.class);
                WeaponsStatsComponent attackStats = entity.getComponent(WeaponsStatsComponent.class);
                HitboxComponent enemyHitBox = enemy.getComponent(HitboxComponent.class);

                if (enemyStats != null && attackStats != null && enemyHitBox != null) {
                    if (enemyHitBox.getLayer() == PhysicsLayer.NPC) {
                        float distance = enemy.getCenterPosition().dst(entity.getCenterPosition());
                        if (distance <= attackRange) {
                            enemyStats.takeDamage(attackStats.getBaseAttack());
                        }
                    }
                }
            }
        }

        timeSinceLastAttack = 0;
        weaponAnimation();
    }

    private void weaponAnimation() {
        if (entity.getComponent(AnimationRenderComponent.class) != null) {
            entity.getEvents().trigger("anim");
        }
    }
}

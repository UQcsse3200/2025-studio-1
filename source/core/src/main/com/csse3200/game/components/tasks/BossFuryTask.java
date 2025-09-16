package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.components.enemy.BossStatusDisplay;

/** Boss's fury form */
public class BossFuryTask extends Component {
    private final float thresholdRatio;
    private final float speedMultiplier;
    private final int   damageBonus;
    private final Float newCooldown;

    private boolean triggered = false;
    private int maxHealth = -1;

    public BossFuryTask(float thresholdRatio,
                              float speedMultiplier,
                              int damageBonus,
                              Float newCooldown) {
        this.thresholdRatio = thresholdRatio;
        this.speedMultiplier = speedMultiplier;
        this.damageBonus = damageBonus;
        this.newCooldown = newCooldown;
    }

    @Override
    public void create() {
        CombatStatsComponent cs = entity.getComponent(CombatStatsComponent.class);
        if (cs != null) {
            maxHealth = cs.getMaxHealth() > 0 ? cs.getMaxHealth() : cs.getHealth();
        }
    }

    @Override
    public void update() {
        if (triggered) return;
        CombatStatsComponent cs = entity.getComponent(CombatStatsComponent.class);
        if (cs == null || maxHealth <= 0) return;

        if (cs.getHealth() <= maxHealth * thresholdRatio) {
            enterFury();
            triggered = true;
        }
    }

    private void enterFury() {
        entity.getEvents().trigger("boss:enraged");
        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null && animator.hasAnimation("fury")) {
            animator.startAnimation("fury");
        }

        WeaponsStatsComponent weapon = entity.getComponent(WeaponsStatsComponent.class);
        if (weapon != null) {
            weapon.setBaseAttack(weapon.getBaseAttack() + damageBonus);
            if (newCooldown != null) {
                weapon.setCoolDown(newCooldown);
            }
        }

        PhysicsMovementComponent move = entity.getComponent(PhysicsMovementComponent.class);
        if (move != null) {
            move.setSpeed(new Vector2(2.5f * speedMultiplier, 2.5f * speedMultiplier));
        }

        final float furyScale = 1.5f;
        Vector2 sc = entity.getScale();
        entity.setScale(sc.x * furyScale, sc.y * furyScale);

        BossStatusDisplay display = entity.getComponent(BossStatusDisplay.class);
        if (display != null) {
            display.setPhase("FURY");
        }
    }
}


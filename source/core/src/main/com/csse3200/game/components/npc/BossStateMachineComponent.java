package com.csse3200.game.components.npc;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;

public class BossStateMachineComponent extends Component {
    public enum State { WANDER, CHASE, ATTACK, ENRAGED, DEAD }

    private final float aggroRange;
    private final float attackRange;
    private final float attackCooldown;
    private final float enrageThresholdRatio;

    private State state = State.WANDER;
    private float cd = 0f;
    private CombatStatsComponent combat;
    private int maxHp = 1;
    private Entity target;

    public BossStateMachineComponent(float aggroRange, float attackRange,
                                     float attackCooldown, float enrageThresholdRatio) {
        this.aggroRange = aggroRange;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.enrageThresholdRatio = enrageThresholdRatio;
    }

    public BossStateMachineComponent withTarget(Entity target) {
        this.target = target;
        return this;
    }

    @Override
    public void create() {
        combat = entity.getComponent(CombatStatsComponent.class);
        if (combat != null) {
            maxHp = Math.max(1, combat.getHealth());
        }
        enter(State.WANDER);
    }

    @Override
    public void update() {
        if (combat != null && combat.getHealth() <= 0) {
            enter(State.DEAD);
            return;
        }

        cd -= Gdx.graphics.getDeltaTime();

        float dist = (target != null)
                ? entity.getPosition().dst(target.getPosition())
                : Float.MAX_VALUE;

        boolean enraged = (combat != null) && combat.getHealth() <= maxHp * enrageThresholdRatio;

        switch (state) {
            case WANDER:
                if (enraged) { enter(State.ENRAGED); break; }
                if (dist <= attackRange && cd <= 0f) { enter(State.ATTACK); }
                else if (dist <= aggroRange) { enter(State.CHASE); }
                break;

            case CHASE:
                if (enraged) { enter(State.ENRAGED); break; }
                if (dist <= attackRange && cd <= 0f) { enter(State.ATTACK); }
                else if (dist > aggroRange * 1.2f) { enter(State.WANDER); }
                break;

            case ENRAGED:
                if (dist <= attackRange && cd <= 0f) { enter(State.ATTACK); }
                break;

            case ATTACK:
                entity.getEvents().trigger("boss:doAttack");
                cd = attackCooldown;

                if (enraged) enter(State.ENRAGED);
                else if (dist <= aggroRange) enter(State.CHASE);
                else enter(State.WANDER);
                break;

            case DEAD:
                break;
        }
    }

    private void enter(State next) {
        if (state == next) return;
        state = next;

        switch (state) {
            case WANDER:
                entity.getEvents().trigger("wanderStart");
                break;
            case CHASE:
                entity.getEvents().trigger("chaseStart");
                break;
            case ENRAGED:
                entity.getEvents().trigger("boss:enraged");
                entity.getEvents().trigger("chaseStart");
                break;
            case ATTACK:
                break;
            case DEAD:
                entity.getEvents().trigger("death");
                break;
        }
    }
}



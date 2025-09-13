package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

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
    private boolean enragedOnce = false;
    private float furyCooldownMul = 0.6f;

    // 平台“同层”高度容忍阈值（按你关卡平台高度调）
    private static final float SAME_LEVEL_EPS = 0.6f;

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
            // 若有 getMaxHealth() 可换成 max；当前取初始生命作为“最大值”
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

        // 冷却递减并夹紧到 0
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        cd = Math.max(0f, cd - dt);

        // —— 只看水平距离 + 同层判定 ——
        float dx = Float.MAX_VALUE;
        float dy = Float.MAX_VALUE;
        if (target != null) {
            dx = Math.abs(entity.getPosition().x - target.getPosition().x);
            dy = Math.abs(entity.getPosition().y - target.getPosition().y);
        }
        boolean sameLevel = dy <= SAME_LEVEL_EPS;

        boolean enraged = (combat != null) && combat.getHealth() <= maxHp * enrageThresholdRatio;
        if (enraged && !enragedOnce) {
            enragedOnce = true;
            enter(State.ENRAGED);
        }

        switch (state) {
            case WANDER:
                if (enraged) {
                    enter(State.ENRAGED);
                } else if (dx <= attackRange && sameLevel && cd <= 0f) {
                    enter(State.ATTACK);
                } else if (dx <= aggroRange && sameLevel) {
                    enter(State.CHASE);
                }
                break;

            case CHASE:
                if (enraged) {
                    enter(State.ENRAGED);
                } else if (dx <= attackRange && sameLevel && cd <= 0f) {
                    enter(State.ATTACK);
                } else if (dx > aggroRange * 1.2f || !sameLevel) {
                    // 超出牵引范围或不同层 → 退出追击
                    enter(State.WANDER);
                }
                break;

            case ENRAGED:
                if (dx <= attackRange && sameLevel && cd <= 0f) {
                    enter(State.ATTACK);
                } else if (dx <= aggroRange && sameLevel) {
                    enter(State.CHASE);
                } else {
                    enter(State.WANDER);
                }
                break;

            case ATTACK:
                entity.getEvents().trigger("boss:attackStart");
                entity.getEvents().trigger("boss:doAttack");
                cd = enragedOnce ? attackCooldown * furyCooldownMul : attackCooldown;

                if (enraged) {
                    enter(State.ENRAGED);
                } else if (dx <= aggroRange && sameLevel) {
                    enter(State.CHASE);
                } else {
                    enter(State.WANDER);
                }
                break;

            case DEAD:
                break;
        }
    }

    private void enter(State next) {
        if (state == next) return;

        // 关键：离开 CHASE 的瞬间通知移动器“刹车 + 清目标”
        if (state == State.CHASE && next != State.CHASE) {
            entity.getEvents().trigger("chaseStop");
        }

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
                entity.getEvents().trigger("chaseStart"); // 狂暴期间保持横向追
                break;
            case ATTACK:
                break;
            case DEAD:
                entity.getEvents().trigger("chaseStop");  // 死亡也停
                entity.getEvents().trigger("boss:death");
                break;
        }
    }

    // 如需保留旧的“上方判断”工具函数也可以，但当前逻辑用 sameLevel 替代
    @SuppressWarnings("unused")
    private boolean targetClearlyAbove() {
        if (target == null) return false;
        float myY = entity.getPosition().y;
        float ty  = target.getPosition().y;
        return (ty - myY) > SAME_LEVEL_EPS;
    }
}





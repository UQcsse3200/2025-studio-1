package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * 巡逻（上方固定Y左右来回）+ 冲撞玩家 + 回到巡逻位 的技能组件
 * 动画由 Boss2AnimationController 通过事件驱动。
 */
public class BossChargeSkillComponent extends Component {
    private enum State { PATROL, PREP, CHARGING, RETURN, COOLDOWN }
    private PhysicsComponent phys;
    private final Entity target;
    private final float triggerRange;
    private final float dwellTime;
    private final float prepareTime;
    private final float chargeSpeed;
    private final float chargeDuration;
    private final float cooldown;

    // 巡逻参数：左右边界与固定Y、速度
    private final float patrolLeftX;
    private final float patrolRightX;
    private final float patrolY;
    private final float patrolSpeed;

    // 运行时变量
    private final Vector2 lockedPos = new Vector2(); // 冲撞目标点（锁定玩家中心）
    private final Vector2 vel = new Vector2();       // 当前移动速度
    private float dwellCounter = 0f;
    private float timer = 0f;
    private int patrolDir = 1;                       // 1→右，-1→左
    private float anchorX;                           // 离开巡逻线时的X（回位用）
    private State state = State.PATROL;

    private GameTime time;
    private AITaskComponent ai;

    public BossChargeSkillComponent(Entity target,
                                    float triggerRange,
                                    float dwellTime,
                                    float prepareTime,
                                    float chargeSpeed,
                                    float chargeDuration,
                                    float cooldown,
                                    float patrolLeftX,
                                    float patrolRightX,
                                    float patrolY,
                                    float patrolSpeed) {
        this.target = target;
        this.triggerRange = triggerRange;
        this.dwellTime = dwellTime;
        this.prepareTime = prepareTime;
        this.chargeSpeed = chargeSpeed;
        this.chargeDuration = chargeDuration;
        this.cooldown = cooldown;
        this.patrolLeftX = Math.min(patrolLeftX, patrolRightX);
        this.patrolRightX = Math.max(patrolLeftX, patrolRightX);
        this.patrolY = patrolY;
        this.patrolSpeed = Math.max(0.01f, patrolSpeed);
    }

    @Override
    public void create() {
        time = ServiceLocator.getTimeSource();
        ai   = entity.getComponent(AITaskComponent.class);
        phys = entity.getComponent(PhysicsComponent.class);
        // 关闭重力，避免“往下掉”
        if (phys != null && phys.getBody() != null) {
            phys.getBody().setGravityScale(0f);
            phys.getBody().setFixedRotation(true);
            phys.getBody().setLinearVelocity(0f, 0f);
        }
        // 出生时就把Y拉到巡逻线（避免别的组件改了Y）
        Vector2 p = entity.getPosition();
        entity.setPosition(p.x, patrolY);

        // 告诉动画控制器：进入巡逻
        triggerAnim("boss2:patrol");
    }

    @Override
    public void update() {
        float dt = time.getDeltaTime();

        switch (state) {
            case PATROL: {
                // 固定在巡逻Y，仅做水平移动
                Vector2 p = entity.getPosition();
                p.y = patrolY;
                p.x += patrolDir * patrolSpeed * dt;

                // 到边界就反向
                if (p.x >= patrolRightX) { p.x = patrolRightX; patrolDir = -1; }
                if (p.x <= patrolLeftX)  { p.x = patrolLeftX;  patrolDir =  1; }
                entity.setPosition(p);

                // 触发检测
                float dist = entity.getCenterPosition().dst(target.getCenterPosition());
                if (dist <= triggerRange) {
                    dwellCounter += dt;
                    if (dwellCounter >= dwellTime) {
                        // 记录离开时的巡逻X，用于回位
                        anchorX = entity.getPosition().x;
                        lockedPos.set(target.getCenterPosition());
                        pauseAI(true);
                        timer = prepareTime;

                        // 进入准备（动画交给控制器）
                        triggerAnim("boss2:prep");
                        state = State.PREP;
                    }
                } else {
                    dwellCounter = 0f;
                }
                break;
            }

            case PREP: {
                timer -= dt;
                if (timer <= 0f) {
                    // 计算冲撞方向
                    Vector2 from = entity.getCenterPosition();
                    Vector2 dir = new Vector2(lockedPos).sub(from);
                    if (!dir.isZero()) dir.nor(); else dir.set(1, 0);
                    vel.set(dir.scl(chargeSpeed));
                    timer = chargeDuration;

                    // 开始冲撞
                    triggerAnim("boss2:charge");
                    state = State.CHARGING;
                }
                break;
            }

            case CHARGING: {
                Vector2 pos = entity.getPosition();
                pos.add(vel.x * dt, vel.y * dt);
                entity.setPosition(pos);

                timer -= dt;
                if (timer <= 0f) {
                    vel.setZero();
                    // 冲撞后先回到巡逻线的离开位置
                    triggerAnim("boss2:return");
                    state = State.RETURN;
                }
                break;
            }

            case RETURN: {
                // 目标点： (anchorX, patrolY)
                Vector2 pos = entity.getPosition();
                Vector2 goal = new Vector2(anchorX, patrolY);
                Vector2 dir = goal.cpy().sub(pos);
                float len = dir.len();
                if (len < 0.05f) { // 误差阈值
                    entity.setPosition(anchorX, patrolY);
                    timer = cooldown;

                    // 回到巡逻线，进入冷却
                    triggerAnim("boss2:cooldown");
                    state = State.COOLDOWN;
                } else {
                    dir.scl(chargeSpeed / Math.max(0.0001f, len)); // 用冲撞速度快速回位
                    pos.add(dir.x * dt, dir.y * dt);
                    entity.setPosition(pos);
                }
                break;
            }

            case COOLDOWN: {
                timer -= dt;
                if (timer <= 0f) {
                    pauseAI(false);
                    dwellCounter = 0f;

                    // 冷却结束，继续巡逻
                    triggerAnim("boss2:patrol");
                    state = State.PATROL;
                }
                break;
            }
        }
    }

    private void pauseAI(boolean pause) {
        if (ai != null) {
            try { ai.setEnabled(!pause); } catch (Throwable ignored) {}
        }
    }

    /** 触发交由 Boss2AnimationController 处理的动画事件 */
    private void triggerAnim(String evt) {
        entity.getEvents().trigger(evt);
    }
}



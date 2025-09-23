package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;


public class BossChargeSkillComponent extends Component {
    private enum State {PATROL, PREP, CHARGING, RETURN, COOLDOWN}

    private PhysicsComponent phys;
    private final Entity target;
    private final float triggerRange;
    private final float dwellTime;
    private final float prepareTime;
    private final float chargeSpeed;
    private final float chargeDuration;
    private final float cooldown;

    private final float patrolLeftX;
    private final float patrolRightX;
    private final float patrolY;
    private final float patrolSpeed;


    private final Vector2 lockedPos = new Vector2();
    private final Vector2 vel = new Vector2();
    private float dwellCounter = 0f;
    private float timer = 0f;
    private int patrolDir = 1;
    private float anchorX;
    private State state = State.PATROL;
    private boolean attack = true;
    private boolean crash = false;
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
        ai = entity.getComponent(AITaskComponent.class);
        phys = entity.getComponent(PhysicsComponent.class);
        if (phys != null && phys.getBody() != null) {
            phys.getBody().setGravityScale(0f);
            phys.getBody().setFixedRotation(true);
            phys.getBody().setLinearVelocity(0f, 0f);
        }
        Vector2 p = entity.getPosition();
        entity.setPosition(p.x, patrolY);
        triggerAnim("boss2:patrol");
    }

    @Override
    public void update() {
        if (!attack) return;
        float dt = time.getDeltaTime();

        switch (state) {
            case PATROL: {
                Vector2 p = entity.getPosition();
                p.y = patrolY;
                p.x += patrolDir * patrolSpeed * dt;
                if (p.x >= patrolRightX) {
                    p.x = patrolRightX;
                    patrolDir = -1;
                }
                if (p.x <= patrolLeftX) {
                    p.x = patrolLeftX;
                    patrolDir = 1;
                }
                entity.setPosition(p);
                if (!crash) {
                    dwellCounter = 0f;
                    break;
                }
                float dist = entity.getCenterPosition().dst(target.getCenterPosition());
                if (dist <= triggerRange) {
                    dwellCounter += dt;
                    if (dwellCounter >= dwellTime) {
                        anchorX = entity.getPosition().x;
                        lockedPos.set(target.getCenterPosition());
                        pauseAI(true);
                        timer = prepareTime;
                        triggerAnim("boss2:prep");
                        state = State.PREP;
                    }
                } else {
                    dwellCounter = 0f;
                }
                break;
            }

            case PREP: {
                if (!crash) {
                    pauseAI(false);
                    dwellCounter = 0f;
                    triggerAnim("boss2:patrol");
                    state = State.PATROL;
                    break;
                }
                timer -= dt;
                if (timer <= 0f) {
                    Vector2 from = entity.getCenterPosition();
                    Vector2 dir = new Vector2(lockedPos).sub(from);
                    if (!dir.isZero()) dir.nor();
                    else dir.set(1, 0);
                    vel.set(dir.scl(chargeSpeed));
                    timer = chargeDuration;
                    triggerAnim("boss2:charge");
                    state = State.CHARGING;
                }
                break;
            }

            case CHARGING: {
                if (!crash) {
                    vel.setZero();
                    triggerAnim("boss2:return");
                    state = State.RETURN;
                    break;
                }
                Vector2 pos = entity.getPosition();
                pos.add(vel.x * dt, vel.y * dt);
                entity.setPosition(pos);

                timer -= dt;
                if (timer <= 0f) {
                    vel.setZero();
                    triggerAnim("boss2:return");
                    state = State.RETURN;
                }
                break;
            }

            case RETURN: {
                Vector2 pos = entity.getPosition();
                Vector2 goal = new Vector2(anchorX, patrolY);
                Vector2 dir = goal.cpy().sub(pos);
                float len = dir.len();
                if (len < 0.05f) {
                    entity.setPosition(anchorX, patrolY);
                    timer = cooldown;
                    triggerAnim("boss2:cooldown");
                    state = State.COOLDOWN;
                } else {
                    dir.scl(chargeSpeed / Math.max(0.0001f, len));
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
                    triggerAnim("boss2:patrol");
                    state = State.PATROL;
                }
                break;
            }
        }
    }

    private void pauseAI(boolean pause) {
        if (ai != null) {
            try {
                ai.setEnabled(!pause);
            } catch (Throwable ignored) {
            }
        }
    }

    private void triggerAnim(String evt) {
        entity.getEvents().trigger(evt);
    }

    public void setAttack(boolean attack) {
        this.attack = attack;
    }

    public void setCrash(boolean crash) {
        this.crash = crash;
    }
}



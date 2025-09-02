package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class BossChargeSkillComponent extends Component {
    private enum State { IDLE, PREP, CHARGING, COOLDOWN }

    private final Entity target;
    private final float triggerRange;
    private final float dwellTime;
    private final float prepareTime;
    private final float chargeSpeed;
    private final float chargeDuration;
    private final float cooldown;

    private final Vector2 lockedPos = new Vector2();
    private final Vector2 chargeVel = new Vector2();
    private float dwellCounter = 0f;
    private float timer = 0f;
    private State state = State.IDLE;

    private GameTime time;
    private AITaskComponent ai;
    private AnimationRenderComponent anim;

    public BossChargeSkillComponent(Entity target,
                                    float triggerRange,
                                    float dwellTime,
                                    float prepareTime,
                                    float chargeSpeed,
                                    float chargeDuration,
                                    float cooldown) {
        this.target = target;
        this.triggerRange = triggerRange;
        this.dwellTime = dwellTime;
        this.prepareTime = prepareTime;
        this.chargeSpeed = chargeSpeed;
        this.chargeDuration = chargeDuration;
        this.cooldown = cooldown;
    }

    @Override
    public void create() {
        time = ServiceLocator.getTimeSource();
        ai   = entity.getComponent(AITaskComponent.class);
        anim = entity.getComponent(AnimationRenderComponent.class);
    }

    @Override
    public void update() {
        float dt = time.getDeltaTime();

        switch (state) {
            case IDLE: {
                float dist = entity.getCenterPosition().dst(target.getCenterPosition());
                if (dist <= triggerRange) {
                    dwellCounter += dt;
                    if (dwellCounter >= dwellTime) {
                        lockedPos.set(target.getCenterPosition());
                        pauseAI(true);
                        playAnim("angry_float");
                        timer = prepareTime;
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
                    Vector2 from = entity.getCenterPosition();
                    Vector2 dir = new Vector2(lockedPos).sub(from);
                    if (!dir.isZero()) dir.nor(); else dir.set(1, 0);
                    chargeVel.set(dir.scl(chargeSpeed));
                    timer = chargeDuration;
                    state = State.CHARGING;
                }
                break;
            }

            case CHARGING: {
                Vector2 pos = entity.getPosition();
                pos.add(chargeVel.x * dt, chargeVel.y * dt);
                entity.setPosition(pos);

                timer -= dt;
                if (timer <= 0f) {
                    chargeVel.setZero();
                    playAnim("float");
                    timer = cooldown;
                    state = State.COOLDOWN;
                }
                break;
            }

            case COOLDOWN: {
                timer -= dt;
                if (timer <= 0f) {
                    pauseAI(false);
                    dwellCounter = 0f;
                    state = State.IDLE;
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

    private void playAnim(String name) {
        if (anim != null && anim.hasAnimation(name)) {
            anim.startAnimation(name);
        }
    }
}



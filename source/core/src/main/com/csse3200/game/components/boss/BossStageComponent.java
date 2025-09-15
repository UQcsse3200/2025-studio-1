package com.csse3200.game.components.boss;
import com.csse3200.game.rendering.*;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.components.enemy.*;
import com.csse3200.game.components.boss.MissueAttackComponent;

public class BossStageComponent extends Component {
    private int currentStage = 1;
    private final Entity boss;
    private final float stage2ThresholdPercent = 0.5f; // 50%

    public BossStageComponent(Entity boss) {
        this.boss = boss;
    }
    @Override
    public void update() {
        CombatStatsComponent stats = boss.getComponent(CombatStatsComponent.class);
        int currentHp = stats.getHealth();
        int maxHp = stats.getMaxHealth();
        if (currentStage == 1 && currentHp * 2 <= maxHp) {
            enterStage2();
        }
    }

    private void enterStage2() {
        currentStage = 2;
        FireballAttackComponent fireball = entity.getComponent(FireballAttackComponent.class);
        fireball.setAttack(false);
        BlackholeComponent balckhole =  entity.getComponent(BlackholeComponent.class);
        balckhole.setAttack(false);
        MissueAttackComponent missle = entity.getComponent(MissueAttackComponent.class);
        missle.setAttack(true);
    }
}
package com.csse3200.game.components.boss;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.BlackholeComponent;
import com.csse3200.game.components.enemy.BossChargeSkillComponent;
import com.csse3200.game.components.enemy.FireballAttackComponent;
import com.csse3200.game.entities.Entity;

public class BossStageComponent extends Component {
    private int currentStage = 1;
    private final Entity boss;
    private final float stage2 = 0.5f;
    private final float stage3 = 0.3f;// 50%

    public BossStageComponent(Entity boss) {
        this.boss = boss;
    }

    @Override
    public void update() {
        CombatStatsComponent stats = boss.getComponent(CombatStatsComponent.class);
        int currentHp = stats.getHealth();
        int maxHp = stats.getMaxHealth();
        if (currentStage == 1 && currentHp <= maxHp * stage2) {
            enterStage2();
        }
        if (currentStage == 2 && currentHp <= maxHp * stage3) {
            enterStage3();
        }
    }

    private void enterStage2() {
        currentStage = 2;
        BlackholeComponent balckhole = entity.getComponent(BlackholeComponent.class);
        balckhole.setAttack(false);
        BossChargeSkillComponent move = entity.getComponent(BossChargeSkillComponent.class);
        move.setCrash(true);
    }

    private void enterStage3() {
        currentStage = 3;
        FireballAttackComponent fireball = entity.getComponent(FireballAttackComponent.class);
        fireball.setAttack(false);
        BlackholeComponent balckhole = entity.getComponent(BlackholeComponent.class);
        balckhole.setAttack(false);
        MissueAttackComponent missle = entity.getComponent(MissueAttackComponent.class);
        missle.setAttack(true);
        BossChargeSkillComponent move = entity.getComponent(BossChargeSkillComponent.class);
        move.setAttack(false);
    }
}
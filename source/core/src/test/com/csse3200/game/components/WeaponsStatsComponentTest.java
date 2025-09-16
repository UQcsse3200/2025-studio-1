package com.csse3200.game.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeaponsStatsComponentTest {

    @Nested
    @DisplayName("Objective: Base attack behaviour")
    class BaseAttackTests {
        @Test
        void usesAttackersBaseAttack() {
            WeaponsStatsComponent atk = new WeaponsStatsComponent(50);
            assertEquals(50, atk.getBaseAttack());
        }

        @Test
        void negativeInitialBaseAttack_defaultsToZero() {
            WeaponsStatsComponent atk = new WeaponsStatsComponent(-20);
            assertEquals(0, atk.getBaseAttack());
        }

        @Test
        void setBaseAttack_rejectsNegativeValues() {
            WeaponsStatsComponent atk = new WeaponsStatsComponent(10);
            atk.setBaseAttack(-5);
            assertEquals(10, atk.getBaseAttack(), "Negative values should be ignored");
        }
    }

    @Nested
    @DisplayName("Objective: Cooldown behaviour")
    class CooldownTests {
        @Test
        void shouldSetAndGetCooldown() {
            WeaponsStatsComponent combat = new WeaponsStatsComponent(0);
            assertEquals(0.2f, combat.getCoolDown());

            combat.setCoolDown(100);
            assertEquals(100, combat.getCoolDown());

            combat.setCoolDown(-100);
            assertEquals(0.2f, combat.getCoolDown(), "Negative cooldown " +
                    "should clamp to 0.2f");
        }
    }

    @Nested
    @DisplayName("Objective: Disable damage flag")
    class DisableDamageTests {
        @Test
        void disableDamageFlag_canBeToggled() {
            WeaponsStatsComponent combat = new WeaponsStatsComponent(20);
            combat.setDisableDamage(true);

            // we can’t directly call private canAttack(), but we can indirectly test
            combat.setBaseAttack(40);
            assertEquals(40, combat.getBaseAttack(), "Setting base attack still works");
            // behaviour of disabling is enforced in consuming systems, so just ensure toggle works
            combat.setDisableDamage(false); // no crash
        }
    }

    @Nested
    @DisplayName("Objective: Upgrade Feature")
    class upgradeTests {

        @Test
        void baseUpgradeTest() {
            WeaponsStatsComponent combat = new WeaponsStatsComponent(20);
            int originalAttack = combat.getBaseAttack();
            combat.upgrade();
            //Check the damage has doubled and the upgrade stage is now 2
            assertEquals(2 * originalAttack, combat.getBaseAttack());
            assertEquals(2, combat.getUpgradeStage());
        }

        @Test
        void maxUpgradeTest() {
            WeaponsStatsComponent combat = new WeaponsStatsComponent(20);

            for (int i = 0; i < combat.getMaxUpgradeStage(); i++) {
                combat.upgrade();
            }

            assertEquals(combat.getMaxUpgradeStage(), combat.getUpgradeStage());
            combat.upgrade();
            assertEquals(combat.getMaxUpgradeStage(), combat.getUpgradeStage());

        }
    }


}

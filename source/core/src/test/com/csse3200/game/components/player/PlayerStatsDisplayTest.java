package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.enemy.EnemyDeathRewardComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class PlayerStatsDisplayTest {
    
    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = PlayerStatsDisplay.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            fail("Failed to set field '" + fieldName + "': " + e);
        }
    }

    private static void invokeKillOne(PlayerStatsDisplay d) {
        try {
            Method m = PlayerStatsDisplay.class.getDeclaredMethod("killOneEnemy");
            m.setAccessible(true);
            m.invoke(d);
        } catch (Exception e) {
            fail("killOneEnemy invocation failed: " + e);
        }
    }

    private static ProgressBar pb(int min, int max, int step) {
        Drawable dummy = mock(Drawable.class);
        ProgressBarStyle style = new ProgressBarStyle();
        style.background = dummy;
        style.knob = dummy;
        style.knobBefore = dummy;
        return new ProgressBar(min, max, step, false, style);
    }

    private static Label makeLabel() {
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = new BitmapFont();
        return new Label("", ls);
    }

    private static Object getField(Object target, String fieldName) {
        try {
            Field f = PlayerStatsDisplay.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            fail("Failed to read field '" + fieldName + "': " + e);
            return null;
        }
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void updateHealthUI_clampsWithinBarRange() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();
        ProgressBar bar = pb(0, 100, 1);
        d.setHealthBar(bar);

        d.updatePlayerHealthUI(50);
        assertEquals(50f, bar.getValue(), 0.01);

        d.updatePlayerHealthUI(-10); // clamp to 0
        assertEquals(0f, bar.getValue(), 0.01);

        d.updatePlayerHealthUI(200); // clamp to 100
        assertEquals(100f, bar.getValue(), 0.01);
    }

    @Test
    void updateStaminaUI_updatesPercentage() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();
        ProgressBar stamina = pb(0, 100, 1);
        setField(d, "staminaBar", stamina);

        d.updatePlayerStaminaUI(25, 50); // 50%
        assertEquals(50f, stamina.getValue(), 0.01);
    }

    @Test
    void updateStaminaUI_ignoresWhenMaxNonPositive() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();
        ProgressBar stamina = pb(0, 100, 1);
        stamina.setValue(33f);
        setField(d, "staminaBar", stamina);

        d.updatePlayerStaminaUI(10, 0); // should early-return
        assertEquals(33f, stamina.getValue(), 0.01);
    }

    @Test
    void updateProcessorUI_formatsText() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();
        Label lbl = makeLabel();
        d.setProcessorLabel(lbl);

        d.updatePlayerProcessorUI(5);
        assertEquals("Processor: 5", lbl.getText().toString());

        d.updatePlayerProcessorUI(123);
        assertEquals("Processor: 123", lbl.getText().toString());
    }

    @Test
    void updateAmmoUI_singleWhenNoEquipped() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();

        // mocks
        AmmoStatsComponent ammo = mock(AmmoStatsComponent.class);
        when(ammo.getAmmo()).thenReturn(30);
        InventoryComponent inv = mock(InventoryComponent.class);
        when(inv.getCurrItem()).thenReturn(null);

        // wire into instance
        setField(d, "ammoStats", ammo);
        setField(d, "inventory", inv);
        setField(d, "ammoLabel", makeLabel());

        d.updateAmmoUI();
        assertEquals("Ammo: 30", ((Label) getField(d, "ammoLabel")).getText().toString());
    }

    /* ---------- ammo after switch (uses inventory.get(index)) ---------- */

    @Test
    void updateAmmoUI_dualWhenRangedWithMag() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();

        // reserves
        AmmoStatsComponent ammo = mock(AmmoStatsComponent.class);
        when(ammo.getAmmo()).thenReturn(50);

        // equipped weapon entity with components
        Entity weapon = new Entity();
        ItemComponent info = mock(ItemComponent.class);
        when(info.getType()).thenReturn(ItemTypes.RANGED);
        weapon.addComponent(info);

        MagazineComponent mag = mock(MagazineComponent.class);
        when(mag.getCurrentAmmo()).thenReturn(7);
        weapon.addComponent(mag);

        InventoryComponent inv = mock(InventoryComponent.class);
        when(inv.getCurrItem()).thenReturn(weapon);

        setField(d, "ammoStats", ammo);
        setField(d, "inventory", inv);
        Label label = makeLabel();
        setField(d, "ammoLabel", label);

        d.updateAmmoUI();
        assertEquals("Ammo: 50/7", label.getText().toString());
    }

    @Test
    void updateAmmoUIAfterSwitch_usesIndexAndNonRangedFallsBackSingle() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();

        // reserves
        AmmoStatsComponent ammo = mock(AmmoStatsComponent.class);
        when(ammo.getAmmo()).thenReturn(12);

        // slot entity that is NOT ranged
        Entity slot = new Entity();
        ItemComponent info = mock(ItemComponent.class);
        when(info.getType()).thenReturn(ItemTypes.MELEE); // not ranged
        slot.addComponent(info);

        InventoryComponent inv = mock(InventoryComponent.class);
        when(inv.get(2)).thenReturn(slot);

        setField(d, "ammoStats", ammo);
        setField(d, "inventory", inv);
        Label label = makeLabel();
        setField(d, "ammoLabel", label);

        d.updateAmmoUIAfterSwitch(2);
        assertEquals("Ammo: 12", label.getText().toString());
    }

    /* ---------- killOneEnemy (private) ---------- */

    @Test
    void updateAmmoUI_noLabelEarlyReturn() {
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();
        // Do not set ammoLabel -> should early-return without NPE
        d.updateAmmoUI();
        d.updateAmmoUIAfterSwitch(0);
    }

    @Test
    void killOneEnemy_returnsWhenNoEntityService() {
        ServiceLocator.clear();
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();
        // attach to a player entity (so isPlayer check can run if needed)
        Entity player = new Entity().addComponent(d);

        // No EntityService registered -> should just return without exception
        invokeKillOne(d);
    }

    @Test
    void killOneEnemy_killsExactlyOneValidEnemy() {
        ServiceLocator.clear();

        // Build player with display (so code won't target the player)
        PlayerStatsDisplay d = new TestPlayerStatsDisplay();
        Entity player = new Entity().addComponent(d);

        // Enemy with positive health and a reward component
        Entity enemy = new Entity();
        CombatStatsComponent enemyStats = mock(CombatStatsComponent.class);
        when(enemyStats.getHealth()).thenReturn(10);
        enemy.addComponent(enemyStats);
        enemy.addComponent(mock(EnemyDeathRewardComponent.class)); // presence is enough

        // Another invalid entity (e.g., already dead) to ensure it skips
        Entity deadEnemy = new Entity();
        CombatStatsComponent deadStats = mock(CombatStatsComponent.class);
        when(deadStats.getHealth()).thenReturn(0);
        deadEnemy.addComponent(deadStats);
        deadEnemy.addComponent(mock(EnemyDeathRewardComponent.class));

        // EntityService returns all three
        Array<Entity> entities = new Array<>();
        entities.add(player);
        entities.add(enemy);
        entities.add(deadEnemy);

        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerEntityService(es);

        // Invoke private method
        invokeKillOne(d);

        // Verify ONLY the valid enemy was "killed"
        verify(enemyStats, times(1)).setHealth(0);
        verify(deadStats, never()).setHealth(anyInt());
    }

    @Test
    void testUpdateHealthUI_minimalOriginal() {
        Drawable dummyDrawable = mock(Drawable.class);

        ProgressBarStyle style = new ProgressBarStyle();
        style.background = dummyDrawable;
        style.knob = dummyDrawable;
        ProgressBar bar = new ProgressBar(0, 100, 1, false, style);

        PlayerStatsDisplay display = new TestPlayerStatsDisplay();
        display.setHealthBar(bar);

        display.updatePlayerHealthUI(50);
        assertEquals(50, bar.getValue(), 0.01);

        display.updatePlayerHealthUI(90);
        assertEquals(90, bar.getValue(), 0.01);
    }

    @Test
    void testUpdateProcessorUI_minimalOriginal() {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = new BitmapFont();
        Label label = new Label("", style);

        PlayerStatsDisplay display = new TestPlayerStatsDisplay();
        display.setProcessorLabel(label);

        display.updatePlayerProcessorUI(5);
        assertEquals("Processor: 5", label.getText().toString());

        display.updatePlayerProcessorUI(123);
        assertEquals("Processor: 123", label.getText().toString());
    }

    static class TestPlayerStatsDisplay extends PlayerStatsDisplay {
        @Override
        public void create() {
            // skip BaseScreenDisplay.create() and stage/skin setup in tests
        }
    }
}

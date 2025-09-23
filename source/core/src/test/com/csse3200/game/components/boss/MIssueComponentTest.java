package com.csse3200.game.components.boss;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class MIssueComponentTest {

    static class FixedTime extends GameTime {
        private final float dt;
        private long now = 0L;

        FixedTime(float dt) {
            this.dt = dt;
        }

        @Override
        public float getDeltaTime() {
            return dt;
        }

        @Override
        public long getTime() {
            now += (long) (dt * 1000f);
            return now;
        }
    }

    @Before
    public void setUp() {
        ServiceLocator.registerTimeSource(new FixedTime(0.1f));
        ServiceLocator.registerEntityService(new EntityService());
    }

    @After
    public void tearDown() {
        try {
            ServiceLocator.clear();
        } catch (Throwable ignored) {
        }
    }

    private static float getTimer(MissueAttackComponent c) throws Exception {
        Field f = MissueAttackComponent.class.getDeclaredField("timer");
        f.setAccessible(true);
        return f.getFloat(c);
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<Object> getActives(MissueAttackComponent c) throws Exception {
        Field f = MissueAttackComponent.class.getDeclaredField("actives");
        f.setAccessible(true);
        return (ArrayList<Object>) f.get(c);
    }

    @Test
    public void disabled_timerNotAdvance_noSpawn() throws Exception {
        Entity boss = new Entity();
        MissueAttackComponent comp = new MissueAttackComponent();
        boss.addComponent(comp);
        boss.create();
        comp.setAttack(false);
        comp.update();
        assertEquals(0f, getTimer(comp), 1e-6);
        assertEquals(0, getActives(comp).size());
    }
}
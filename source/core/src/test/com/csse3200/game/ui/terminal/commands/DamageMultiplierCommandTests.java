package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DamageMultiplierCommandTests {

    private static ArrayList<String> args(String... a) { return new ArrayList<>(List.of(a)); }
    private static boolean run(DamageMultiplierCommand cmd, String arg) { return cmd.action(args(arg)); }

    @Test
    void acceptsValidMultiplier_integer() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        EntityService es = mock(EntityService.class);
        Entity e1 = mock(Entity.class);
        Entity e2 = mock(Entity.class);
        WeaponsStatsComponent w1 = mock(WeaponsStatsComponent.class);
        WeaponsStatsComponent w2 = mock(WeaponsStatsComponent.class);
        when(e1.getComponent(WeaponsStatsComponent.class)).thenReturn(w1);
        when(e2.getComponent(WeaponsStatsComponent.class)).thenReturn(w2);

        Array<Entity> entities = new Array<>();
        entities.addAll(e1, e2);
        when(es.getEntities()).thenReturn(entities);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            assertTrue(run(cmd, "2"));
            verify(w1).setDamageMultiplier(2f);
            verify(w2).setDamageMultiplier(2f);
        }
    }

    @Test
    void acceptsValidMultiplier_decimalAndWhitespace() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        EntityService es = mock(EntityService.class);
        Entity e = mock(Entity.class);
        WeaponsStatsComponent w = mock(WeaponsStatsComponent.class);
        when(e.getComponent(WeaponsStatsComponent.class)).thenReturn(w);

        Array<Entity> entities = new Array<>();
        entities.add(e);
        when(es.getEntities()).thenReturn(entities);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            assertTrue(run(cmd, "  1.5 "));
            verify(w).setDamageMultiplier(1.5f);
        }
    }

    @Test
    void skipsEntitiesWithoutWeaponStats() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        EntityService es = mock(EntityService.class);
        Entity noWeapon = mock(Entity.class);
        when(noWeapon.getComponent(WeaponsStatsComponent.class)).thenReturn(null); // not a weapon entity

        Entity yesWeapon = mock(Entity.class);
        WeaponsStatsComponent w = mock(WeaponsStatsComponent.class);
        when(yesWeapon.getComponent(WeaponsStatsComponent.class)).thenReturn(w);

        Array<Entity> entities = new Array<>();
        entities.addAll(noWeapon, yesWeapon);
        when(es.getEntities()).thenReturn(entities);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            assertTrue(run(cmd, "3"));
            // only the entity with a component should be updated
            verify(w).setDamageMultiplier(3f);
            verify(noWeapon, only()).getComponent(WeaponsStatsComponent.class);
        }
    }

    @Test
    void rejectsInvalidMultiplier_missingOrNonNumeric() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        // even if services exist, an invalid arg should cause early return false
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(mock(EntityService.class));
            assertFalse(run(cmd, ""), "missing arg should be rejected");
            assertFalse(run(cmd, "notANumber"), "non-numeric should be rejected");
        }
    }

    @Test
    void rejectsInvalidMultiplier_negativeOrNonFinite() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(mock(EntityService.class));
            assertFalse(run(cmd, "-1"), "negative should be rejected");
            assertFalse(run(cmd, "NaN"), "NaN should be rejected");
            assertFalse(run(cmd, "Infinity"), "Infinity should be rejected");
            assertFalse(run(cmd, "-Infinity"), "negative infinity should be rejected");
        }
    }

    @Test
    void handlesNoEntityServiceGracefully() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(null);
            // behavior choice: command can return false (can’t apply) or true (parsed but no-op)
            // Pick the one your implementation uses. Here we assume false:
            assertFalse(run(cmd, "2"));
        }
    }

    @Test
    void handlesEmptyEntityList() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(new Array<>());

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            // Parsed successfully; nothing to update. Typically return true.
            assertTrue(run(cmd, "2"));
        }
    }

    // Optional: if your command validates arg count == 1
    @Test
    void rejectsExtraArgs_whenCommandRequiresSingleArg() {
        DamageMultiplierCommand cmd = new DamageMultiplierCommand();

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(mock(EntityService.class));
            assertFalse(cmd.action(new ArrayList<>(List.of("2", "extra"))));
        }
    }
}
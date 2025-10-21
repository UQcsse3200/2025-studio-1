package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HoverBobComponent.
 */
class HoverBobComponentTest {

    private static float getPrivateFloat(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.getFloat(obj);
        } catch (Exception e) {
            throw new AssertionError("Reflection failed for " + field, e);
        }
    }

    @Test
    void defaultCtor_hasDocumentedDefaults() {
        HoverBobComponent bob = new HoverBobComponent();
        // Check private defaults via reflection: amplitude=0.1f, speed=2.2f, t=0f
        assertEquals(0.1f, getPrivateFloat(bob, "amplitude"), 1e-6);
        assertEquals(2.2f, getPrivateFloat(bob, "speed"), 1e-6);
        assertEquals(0f, getPrivateFloat(bob, "t"), 1e-6);
    }

    @Test
    void customCtor_setsAmplitudeAndSpeed() {
        HoverBobComponent bob = new HoverBobComponent(0.75f, 9.42f);
        assertEquals(0.75f, getPrivateFloat(bob, "amplitude"), 1e-6);
        assertEquals(9.42f, getPrivateFloat(bob, "speed"), 1e-6);
        assertEquals(0f, getPrivateFloat(bob, "t"), 1e-6);
    }

    @Test
    void create_anchorsToInitialY_andDoesNotMoveEntity() {
        Entity e = new Entity();
        e.setPosition(3f, 7f);

        HoverBobComponent bob = new HoverBobComponent();
        e.addComponent(bob);

        // Calling create() should NOT move the entity
        bob.create();
        assertEquals(3f, e.getPosition().x, 1e-6);
        assertEquals(7f, e.getPosition().y, 1e-6);

        // anchorY captured as current Y
        assertEquals(7f, getPrivateFloat(bob, "anchorY"), 1e-6);
    }

    @Test
    void create_isIdempotent_repeatedCallsDontMoveOrDriftAnchor() {
        Entity e = new Entity();
        e.setPosition(10f, 2.5f);

        HoverBobComponent bob = new HoverBobComponent(0.2f, 5f);
        e.addComponent(bob);

        bob.create();
        float anchor1 = getPrivateFloat(bob, "anchorY");
        assertEquals(2.5f, anchor1, 1e-6);
        assertEquals(10f, e.getPosition().x, 1e-6);
        assertEquals(2.5f, e.getPosition().y, 1e-6);

        // Call create() again — should be a no-op for position and anchor
        bob.create();
        float anchor2 = getPrivateFloat(bob, "anchorY");
        assertEquals(anchor1, anchor2, 1e-6);
        assertEquals(10f, e.getPosition().x, 1e-6);
        assertEquals(2.5f, e.getPosition().y, 1e-6);
    }

    @Test
    void create_afterPositionChange_anchorsToNewY() {
        Entity e = new Entity();
        e.setPosition(0f, 0f);

        HoverBobComponent bob = new HoverBobComponent(0.4f, 3f);
        e.addComponent(bob);

        // Move the entity BEFORE create(); anchor should follow the latest Y
        e.setPosition(4f, 9f);
        bob.create();

        assertEquals(4f, e.getPosition().x, 1e-6);
        assertEquals(9f, e.getPosition().y, 1e-6);
        assertEquals(9f, getPrivateFloat(bob, "anchorY"), 1e-6);
    }

    @Test
    void twoEntities_haveIndependentAnchors() {
        Entity e1 = new Entity();
        e1.setPosition(1f, 2f);
        HoverBobComponent b1 = new HoverBobComponent(0.1f, 2.2f);
        e1.addComponent(b1);
        b1.create();

        Entity e2 = new Entity();
        e2.setPosition(5f, 20f);
        HoverBobComponent b2 = new HoverBobComponent(0.6f, 7f);
        e2.addComponent(b2);
        b2.create();

        assertEquals(2f, getPrivateFloat(b1, "anchorY"), 1e-6);
        assertEquals(20f, getPrivateFloat(b2, "anchorY"), 1e-6);

        // No cross-talk: moving one before create() on the other already tested above;
        // here just double-check they’re distinct objects/fields.
        assertNotEquals(b1, b2);
    }

    @Test
    void xCoordinateNeverTouchedByCreate() {
        Entity e = new Entity();
        e.setPosition(-3.2f, 4.4f);

        HoverBobComponent bob = new HoverBobComponent(0.99f, 0.01f);
        e.addComponent(bob);

        // multiple create() calls mustn’t alter X
        bob.create();
        assertEquals(-3.2f, e.getPosition().x, 1e-6);

        // Move entity’s X and call create() again: still shouldn’t change X
        e.setPosition(123.45f, e.getPosition().y);
        bob.create();
        assertEquals(123.45f, e.getPosition().x, 1e-6);
    }
}

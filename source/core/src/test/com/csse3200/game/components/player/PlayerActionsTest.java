package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class PlayerActionsTest {

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        // Minimal services PlayerActions expects during create()/update()
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerTimeSource(new GameTime()); // provides deltaTime
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void shouldApplyImpulseOnWalk() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));
        when(body.getMass()).thenReturn(2f);
        Vector2 worldCenter = new Vector2(1f, 1f);
        when(body.getWorldCenter()).thenReturn(worldCenter);

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        Input mockInput = mock(Input.class);
        Gdx.input = mockInput;
        actions.walk(new Vector2(1f, 0f));
        when(mockInput.isKeyPressed(Input.Keys.D)).thenReturn(true);
        actions.update();

        Vector2 expectedImpulse = new Vector2(6f, 0f); // (3 - 0) * mass(2)
        verify(body).applyLinearImpulse(approx(expectedImpulse), eq(worldCenter), eq(true));
    }

    @Test
    void shouldApplyImpulseOnStopWalking() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getLinearVelocity()).thenReturn(new Vector2(5f, 0f));
        when(body.getMass()).thenReturn(2f);
        Vector2 worldCenter = new Vector2(2f, 3f);
        when(body.getWorldCenter()).thenReturn(worldCenter);

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        actions.stopWalking();

        Vector2 expectedImpulse = new Vector2(-10f, 0f); // (0 - 5) * 2
        verify(body).applyLinearImpulse(approx(expectedImpulse), eq(worldCenter), eq(true));
    }

    @Test
    void shouldApplyImpulseOnJump() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        Vector2 worldCenter = new Vector2(0f, 0f);
        when(body.getWorldCenter()).thenReturn(worldCenter);

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity().addComponent(actions).addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        actions.infStamina();
        actions.infJumps();

        // reflect static JUMP_VELOCITY field
        Field jvField = PlayerActions.class.getDeclaredField("JUMP_VELOCITY");
        jvField.setAccessible(true);
        Vector2 jv = (Vector2) jvField.get(null);

        actions.jump();

        Vector2 expectedJump = new Vector2(0f, jv.y);
        verify(body).applyLinearImpulse(approx(expectedJump), eq(worldCenter), eq(true));
    }

    @Test
    void shouldPlayAttackSound() {
        ItemComponent mockItem = mock(ItemComponent.class);
        when(mockItem.getTexture()).thenReturn("images/mud.png");
        ResourceService resourceService = mock(ResourceService.class);
        Sound sound = mock(Sound.class);
        when(resourceService.getAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(sound);
        ServiceLocator.registerResourceService(resourceService);

        PlayerActions actions = new PlayerActions();
        actions.setTimeSinceLastAttack(1.5f);
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent())
                .addComponent(new InventoryComponent(50))
                .addComponent(new com.csse3200.game.components.CombatStatsComponent(100)); // hp, atk

        Entity weapon = new Entity();
        weapon.addComponent(mockItem);
        weapon.addComponent(new WeaponsStatsComponent(20));
        player.getComponent(InventoryComponent.class).addItem(weapon);
        player.getComponent(InventoryComponent.class).setSelectSlot(0);
        player.create();

        actions.attack();

        verify(sound).play();
    }

    @Test
    void shouldAllowDoubleJumpWhileFalling_andBlockThird() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        Vector2 worldCenter = new Vector2(0f, 0f);
        when(body.getWorldCenter()).thenReturn(worldCenter);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f)); // grounded first

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        actions.infStamina();
        actions.jump(); // ground jump

        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, -1f)); // airborne
        actions.jump(); // double jump

        // third jump blocked
        actions.jump();

        // reflect static JUMP_VELOCITY field
        Field jvField = PlayerActions.class.getDeclaredField("JUMP_VELOCITY");
        jvField.setAccessible(true);
        Vector2 jv = (Vector2) jvField.get(null);
        Vector2 expected = new Vector2(0f, jv.y);
        verify(body, times(2)).applyLinearImpulse(approx(expected), eq(worldCenter), eq(true));
    }

    @Test
    void shouldResetJumpsOnLandingTransition() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getWorldCenter()).thenReturn(new Vector2(0f, 0f));
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, -1f)); // falling

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        // consume all jumps
        Field jumpsLeftF = PlayerActions.class.getDeclaredField("jumpsLeft");
        jumpsLeftF.setAccessible(true);
        jumpsLeftF.setInt(actions, 0);

        // land
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));
        actions.update();

        Field maxJumpsF = PlayerActions.class.getDeclaredField("MAX_JUMPS");
        maxJumpsF.setAccessible(true);
        int maxJumps = maxJumpsF.getInt(actions);
        assertEquals(maxJumps, jumpsLeftF.getInt(actions));
    }

    @Test
    void groundJumpCooldownShouldNotBlockImmediateAirJump() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        Vector2 worldCenter = new Vector2(0f, 0f);
        when(body.getWorldCenter()).thenReturn(worldCenter);

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        actions.infStamina();

        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));   // ground
        actions.jump();

        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, -1f)); // air, still within cooldown
        actions.jump(); // allowed as double-jump

        // reflect static JUMP_VELOCITY field
        Field jvField = PlayerActions.class.getDeclaredField("JUMP_VELOCITY");
        jvField.setAccessible(true);
        Vector2 jv = (Vector2) jvField.get(null);
        Vector2 expected = new Vector2(0f, jv.y);
        verify(body, times(2)).applyLinearImpulse(approx(expected), eq(worldCenter), eq(true));
    }

    @Test
    void shouldApplySprintImpulseOnWalk() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));
        when(body.getMass()).thenReturn(2f);
        Vector2 worldCenter = new Vector2(1f, 1f);
        when(body.getWorldCenter()).thenReturn(worldCenter);

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        // force sprinting
        Field sprintingField = PlayerActions.class.getDeclaredField("sprinting");
        sprintingField.setAccessible(true);
        sprintingField.set(actions, true);

        // read constant so we don't hardcode
        Field sprintSpeedField = PlayerActions.class.getDeclaredField("SPRINT_SPEED");
        sprintSpeedField.setAccessible(true);
        Vector2 sprintSpeed = (Vector2) sprintSpeedField.get(null);

        actions.walk(new Vector2(1f, 0f));
        actions.update();

        Vector2 expectedImpulse = new Vector2(sprintSpeed.x * 2f, 0f);
        verify(body).applyLinearImpulse(approx(expectedImpulse), eq(worldCenter), eq(true));
    }

    @Test
    void shouldUseNormalSpeedAfterSprintStop() throws Exception {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));
        when(body.getMass()).thenReturn(2f);
        Vector2 worldCenter = new Vector2(1f, 1f);
        when(body.getWorldCenter()).thenReturn(worldCenter);

        PlayerActions actions = new PlayerActions();
        Entity player = new Entity()
                .addComponent(actions)
                .addComponent(new StaminaComponent());
        player.create();

        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        Field sprintingField = PlayerActions.class.getDeclaredField("sprinting");
        sprintingField.setAccessible(true);
        sprintingField.set(actions, true);

        Field sprintSpeedField = PlayerActions.class.getDeclaredField("SPRINT_SPEED");
        sprintSpeedField.setAccessible(true);
        Vector2 sprintSpeed = (Vector2) sprintSpeedField.get(null);

        Field maxSpeedField = PlayerActions.class.getDeclaredField("MAX_SPEED");
        maxSpeedField.setAccessible(true);
        Vector2 maxSpeed = (Vector2) maxSpeedField.get(null);

        actions.walk(new Vector2(1f, 0f));

        // while sprinting
        actions.update();

        // stop sprinting
        sprintingField.set(actions, false);
        actions.update();

        InOrder inOrder = inOrder(body);
        Vector2 expectedSprintImpulse = new Vector2(sprintSpeed.x * 2f, 0f);
        Vector2 expectedNormalImpulse = new Vector2(maxSpeed.x * 2f, 0f);

        inOrder.verify(body).applyLinearImpulse(approx(expectedSprintImpulse), eq(worldCenter), eq(true));
        inOrder.verify(body).applyLinearImpulse(approx(expectedNormalImpulse), eq(worldCenter), eq(true));
    }

    private static Vector2 approx(Vector2 expected) {
        return org.mockito.ArgumentMatchers.argThat(v ->
                v != null &&
                        Math.abs(v.x - expected.x) <= (float) 0.001 &&
                        Math.abs(v.y - expected.y) <= (float) 0.001
        );
    }

    @Nested
    @DisplayName("Testing inventory and equipped actions")
    class InventoryActionsTests {
        InventoryComponent inventory;
        PlayerActions actions;
        Entity player;

        @BeforeEach
        void setup() {
            inventory = new InventoryComponent(5);
            actions = new PlayerActions();
            player = new Entity().addComponent(actions).addComponent(inventory);
            player.create();
        }

        @Test
        /**
         * should select slot, set as equipped slot, and set as current item if slot is not empty
         */
        void testingEquipSlot() {
            Entity item = new Entity();
            item.addComponent(new ItemComponent());

            inventory.addItem(item);
            inventory.setEquippedSlot(2);

            assertEquals(1, inventory.getEquippedSlot());
        }

        /**
         * should not return any slot if the inventory is empty
         */
        @Test
        void testingEmptySlot() {
            Entity item = new Entity();
            item.addComponent(new ItemComponent());

            inventory.addItem(item);
            inventory.setEquippedSlot(0);

            assertEquals(-1, inventory.getEquippedSlot(), "Inventory is empty");
        }


        /**
         * should not select any slot and return a message for invalid slot index
         */
        @Test
        void testingInvalidEquipSlot() {
            Entity item = new Entity();
            item.addComponent(new ItemComponent());

            inventory.addItem(item);
            inventory.setEquippedSlot(7);

            assertFalse(inventory.getEquippedSlot() < 5 && inventory.getEquippedSlot() <= 0,
                    "Invalid equipped Slot");
        }
    }
}

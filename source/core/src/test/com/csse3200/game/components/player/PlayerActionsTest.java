package com.csse3200.game.components.player;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerActionsTest {

  /**
   * Helper function to create and register PhysicsService for access when testing.
   */
  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  /**
   * Allows reading into the private field of an object for checking internal
   * values iff the value is a float
   *
   * @param target the object to attempt to read
   * @param fieldName the private field of the object
   * @param fallback a fallback value if the field is not found or is not a float
   * @return the float stored in the private field, or the fallback.
   */
  private static float reflectFloat(Object target, String fieldName, float fallback) {
    try {
      Field f = target.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      return f.getFloat(target);
    } catch (Exception e) {
      return fallback;
    }
  }

  /**
   * Checks whether the Vector2 provided is almost within a provided epsilon.
   *
   * @param expected the Vector2 that is expected
   * @param eps the allowed epsilon value.
   * @return the Vector2 if it is within the epsilon.
   */
  private static Vector2 approx(Vector2 expected, float eps) {
    return argThat(v ->
            v != null &&
                    Math.abs(v.x - expected.x) <= eps &&
                    Math.abs(v.y - expected.y) <= eps
    );
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
    Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
    physField.setAccessible(true);
    physField.set(actions, physicsComponent);

    // read walkSpeed if present
    float walkSpeed = reflectFloat(actions, "walkSpeed", 3f);

    Vector2 direction = new Vector2(1f, 0f);
    actions.walk(direction);
    actions.update();

    // Impulse = (targetVx - currentVx) * mass
    Vector2 expectedImpulse = new Vector2(walkSpeed * 2f, 0f);
    verify(body).applyLinearImpulse(approx(expectedImpulse, 1e-3f), eq(worldCenter), eq(true));
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
    Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
    physField.setAccessible(true);
    physField.set(actions, physicsComponent);

    actions.stopWalking();

    Vector2 expectedImpulse = new Vector2(-10f, 0f);
    verify(body).applyLinearImpulse(approx(expectedImpulse, 1e-3f), eq(worldCenter), eq(true));
  }

  @Test
  void shouldApplyImpulseOnJump() throws Exception {
    PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
    Body body = mock(Body.class);
    when(physicsComponent.getBody()).thenReturn(body);
    Vector2 worldCenter = new Vector2(0f, 0f);
    when(body.getWorldCenter()).thenReturn(worldCenter);

    PlayerActions actions = new PlayerActions();
    Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
    physField.setAccessible(true);
    physField.set(actions, physicsComponent);

    // read jumpImpulse if present
    float jumpImpulse = reflectFloat(actions, "jumpImpulse", 120f);

    actions.jump();

    Vector2 expectedJump = new Vector2(0f, jumpImpulse);
    verify(body).applyLinearImpulse(approx(expectedJump, 1e-3f), eq(worldCenter), eq(true));
  }

  @Test
  void shouldPlayAttackSound() {
    ResourceService resourceService = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resourceService.getAsset("sounds/Impact4.ogg", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resourceService);

    PlayerActions actions = new PlayerActions();
    actions.attack();

    verify(sound).play();
  }
}
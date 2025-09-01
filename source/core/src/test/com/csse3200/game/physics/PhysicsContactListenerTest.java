package com.csse3200.game.physics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener2;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@SuppressWarnings("unchecked")
@ExtendWith(GameExtension.class)
class PhysicsContactListenerTest {

  @BeforeEach
  void beforeEach() {
    // Set up services and time
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(0.02f);
    ServiceLocator.registerTimeSource(gameTime);
    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  @Test
  void shouldTriggerCollisionStart() {
    // Set up colliding entities
    Entity entity1 = createPhysicsEntity();
    Entity entity2 = createPhysicsEntity();
    entity1.setPosition(0f, 0f);
    entity2.setPosition(0f, 0f);

    // Register collision callbacks (Entity-based now)
    EventListener2<Entity, Entity> callback1 = (EventListener2<Entity, Entity>) mock(EventListener2.class);
    EventListener2<Entity, Entity> callback2 = (EventListener2<Entity, Entity>) mock(EventListener2.class);
    entity1.getEvents().addListener("collisionStart", callback1);
    entity2.getEvents().addListener("collisionStart", callback2);

    // Trigger collisions
    ServiceLocator.getPhysicsService().getPhysics().update();

    // Verify callbacks invoked correctly with Entities
    verify(callback1).handle(entity1, entity2);
    verify(callback2).handle(entity2, entity1);
  }

  @Test
  void shouldTriggerCollisionEnd() {
    // Set up colliding entities
    Entity entity1 = createPhysicsEntity();
    Entity entity2 = createPhysicsEntity();
    entity1.setPosition(0f, 0f);
    entity2.setPosition(0f, 0f);

    // Register end collision callbacks (Entity-based now)
    EventListener2<Entity, Entity> endCallback1 = (EventListener2<Entity, Entity>) mock(EventListener2.class);
    EventListener2<Entity, Entity> endCallback2 = (EventListener2<Entity, Entity>) mock(EventListener2.class);
    entity1.getEvents().addListener("collisionEnd", endCallback1);
    entity2.getEvents().addListener("collisionEnd", endCallback2);

    // First update: still colliding, so no end events
    ServiceLocator.getPhysicsService().getPhysics().update();
    verifyNoInteractions(endCallback1);
    verifyNoInteractions(endCallback2);

    // Move entities apart to end collision
    entity1.setPosition(10f, 10f);
    entity2.setPosition(-10f, -10f);
    ServiceLocator.getPhysicsService().getPhysics().update();

    // Verify end collision callbacks invoked with Entities
    verify(endCallback1).handle(entity1, entity2);
    verify(endCallback2).handle(entity2, entity1);
  }

  Entity createPhysicsEntity() {
    Entity entity =
            new Entity().addComponent(new PhysicsComponent()).addComponent(new ColliderComponent());
    entity.create();

    // Ensure BodyUserData is set so PhysicsContactListener can find the Entity
    PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
    BodyUserData userData = new BodyUserData();
    userData.entity = entity;
    physics.getBody().setUserData(userData);

    return entity;
  }

  Fixture createFixture(PhysicsEngine engine) {
    Body body = engine.createBody(new BodyDef());
    return body.createFixture(new FixtureDef());
  }
}
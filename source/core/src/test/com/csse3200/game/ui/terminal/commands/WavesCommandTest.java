package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WavesCommand to improve code coverage (branches: no game area, no entity service,
 * no suitable player entity, and successful wave start).
 */
class WavesCommandTest {
  private WavesCommand command;

  @BeforeEach
  void setUp() {
    ServiceLocator.clear();
    command = new WavesCommand();
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  /**
   * Stub GameArea overriding startWaves to avoid libGDX dependency and capture invocation.
   */
  private static class StubGameArea extends GameArea {
    Entity playerPassed;
    int calls;
    public StubGameArea() { super(null, null); }
    @Override public void create() { /* not needed */ }
    @Override public void startWaves(Entity player) { this.playerPassed = player; calls++; }
    @Override public Entity getPlayer() { return null; }
  }

  /**
   * Stub EntityService allowing us to inject entities without triggering create() lifecycle.
   */
  private static class StubEntityService extends EntityService {
    private final Array<Entity> stubEntities = new Array<>();
    void add(Entity e) { stubEntities.add(e); }
    @Override public Array<Entity> getEntities() { return new Array<>(stubEntities); }
  }

  @Test
  void returnsFalseWhenNoGameAreaRegistered() {
    // No game area, entity service irrelevant
    ServiceLocator.registerEntityService(new StubEntityService());
    assertFalse(command.action(new ArrayListWrapper()));
  }

  @Test
  void returnsFalseWhenNoEntityServiceRegistered() {
    ServiceLocator.registerGameArea(new StubGameArea());
    assertFalse(command.action(new ArrayListWrapper()));
  }

  @Test
  void returnsFalseWhenNoPlayerFound() {
    ServiceLocator.registerGameArea(new StubGameArea());
    StubEntityService es = new StubEntityService();
    // Entity with only CombatStats (missing Stamina => not a player)
    Entity nonPlayer = new Entity().addComponent(new CombatStatsComponent(10));
    es.add(nonPlayer);
    ServiceLocator.registerEntityService(es);
    assertFalse(command.action(new ArrayListWrapper()));
  }

  @Test
  void returnsFalseWhenOnlyStaminaWithoutCombatStats() {
    ServiceLocator.registerGameArea(new StubGameArea());
    StubEntityService es = new StubEntityService();
    Entity staminaOnly = new Entity().addComponent(new StaminaComponent());
    es.add(staminaOnly);
    ServiceLocator.registerEntityService(es);
    assertFalse(command.action(new ArrayListWrapper()));
  }

  @Test
  void startsWavesWhenPlayerFound() {
    StubGameArea area = new StubGameArea();
    ServiceLocator.registerGameArea(area);
    StubEntityService es = new StubEntityService();
    Entity player = new Entity()
            .addComponent(new CombatStatsComponent(20))
            .addComponent(new StaminaComponent());
    es.add(player);
    ServiceLocator.registerEntityService(es);

    boolean result = command.action(new ArrayListWrapper());

    assertTrue(result, "Expected command to return true when player present");
    assertEquals(1, area.calls, "startWaves should be invoked exactly once");
    assertSame(player, area.playerPassed, "Player entity passed to startWaves should match");
  }

  /** Simple mutable empty ArrayList substitute for args */
  private static class ArrayListWrapper extends java.util.ArrayList<String> { }
}


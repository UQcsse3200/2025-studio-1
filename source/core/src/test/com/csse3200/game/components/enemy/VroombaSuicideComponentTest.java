package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coverage tests for VroombaSuicideComponent verifying arming, fuse timing, damage application,
 * self-destruction, no damage when target outside damage radius, single detonation, and fallback
 * death event when Vroomba lacks its own CombatStatsComponent.
 */
@ExtendWith(GameExtension.class)
class VroombaSuicideComponentTest {
  private static class MutableTime extends GameTime { private float delta; void setDelta(float d){ delta=d; } @Override public float getDeltaTime(){ return delta; }}
  private MutableTime time;

  @BeforeEach
  void setup() {
    ServiceLocator.clear();
    time = new MutableTime();
    ServiceLocator.registerTimeSource(time);
  }

  private void step(Entity e) { e.earlyUpdate(); e.update(); }

  @Test
  void armsAndDetonatesDamagingPlayer() {
    Entity player = new Entity().addComponent(new CombatStatsComponent(50));
    player.setPosition(0,0);
    player.create();
    int damage = 15;
    float fuse = 0.5f;
    Entity vroomba = new Entity()
        .addComponent(new CombatStatsComponent(20))
        .addComponent(new VroombaSuicideComponent(player, 2f, 1.5f, damage, fuse));
    vroomba.setPosition(0.5f,0); // within trigger & damage radius
    vroomba.create();

    // First update: should arm, no damage yet
    time.setDelta(0.1f);
    step(vroomba);
    assertEquals(50, player.getComponent(CombatStatsComponent.class).getHealth(), "No damage before fuse completes");

    // Advance just less than fuse
    time.setDelta(0.3f);
    step(vroomba);
    assertEquals(50, player.getComponent(CombatStatsComponent.class).getHealth(), "Still no damage mid-fuse");

    // Complete fuse causing detonate
    time.setDelta(0.2f); // total 0.6 >= fuse
    step(vroomba);
    assertEquals(35, player.getComponent(CombatStatsComponent.class).getHealth(), "Player should take damage on detonation");
    assertTrue(vroomba.getComponent(CombatStatsComponent.class).isDead(), "Vroomba should self-destruct");

    // Further updates shouldn't re-apply damage
    time.setDelta(1f);
    step(vroomba);
    assertEquals(35, player.getComponent(CombatStatsComponent.class).getHealth(), "Damage should not repeat after explosion");
  }

  @Test
  void detonationOutsideDamageRadiusDealsNoDamage() {
    Entity player = new Entity().addComponent(new CombatStatsComponent(40));
    player.setPosition(0,0); player.create();
    Entity vroomba = new Entity()
        .addComponent(new CombatStatsComponent(10))
        .addComponent(new VroombaSuicideComponent(player, 2f, 0.5f, 12, 0.2f));
    vroomba.setPosition(1.0f, 0); // within trigger radius (2) but outside damage radius (0.5)
    vroomba.create();

    time.setDelta(0.05f); step(vroomba); // arm
    time.setDelta(0.2f); step(vroomba); // detonate
    assertEquals(40, player.getComponent(CombatStatsComponent.class).getHealth(), "Player should not be damaged when outside damage radius");
  }

  @Test
  void fallbackDeathEventWhenNoSelfCombatStats() {
    Entity player = new Entity().addComponent(new CombatStatsComponent(30));
    player.setPosition(0,0); player.create();
    Entity vroomba = new Entity()
        .addComponent(new VroombaSuicideComponent(player, 2f, 1f, 5, 0.1f)); // no self CombatStatsComponent
    final boolean[] deathEvent = {false};
    vroomba.getEvents().addListener("death", () -> deathEvent[0] = true);
    vroomba.setPosition(0.2f,0); // inside damage radius
    vroomba.create();

    time.setDelta(0.05f); step(vroomba); // arm
    time.setDelta(0.1f); step(vroomba); // detonate
    assertTrue(deathEvent[0], "Death event should fire without self CombatStats");
  }

  @Test
  void updateGracefulWhenTargetMissing() {
    Entity vroomba = new Entity()
        .addComponent(new CombatStatsComponent(10))
        .addComponent(new VroombaSuicideComponent(null, 2f, 1f, 5, 0.1f));
    vroomba.setPosition(0,0);
    vroomba.create();
    time.setDelta(1f);
    assertDoesNotThrow(() -> step(vroomba));
  }
}


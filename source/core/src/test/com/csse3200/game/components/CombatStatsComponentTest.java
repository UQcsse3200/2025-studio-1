package com.csse3200.game.components;

import com.csse3200.game.components.enemy.BlackholeAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(GameExtension.class)
class BlackholeAttackComponentTest {
  @BeforeEach
  void setup() {
    ServiceLocator.clear();
    ServiceLocator.registerTimeSource(new GameTime());
  }

  @Test
  void shouldPullPlayerToHoleCenterOnce() {
    Entity player = new Entity();
    Entity blackhole = new Entity();
    player.setPosition(1f, 0f);
    blackhole.setPosition(0f, 0f);

    var attack = new BlackholeAttackComponent(player, 5f, 1f);
    blackhole.addComponent(attack);
    blackhole.create();

    attack.update();

    // 组件是用 hole 的“中心”给 player 调用 setPosition，
    // 所以对比：玩家的“左下角位置” == 黑洞的“中心”
    assertEquals(blackhole.getCenterPosition(), player.getPosition(),
            "pass");
  }
}

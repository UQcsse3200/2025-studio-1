package com.csse3200.game.components.player;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {
  @Test
  void shouldSetGetProcessor() {
    InventoryComponent inventory = new InventoryComponent(100);
    assertEquals(100, inventory.getProcessor());
    inventory.setProcessor(150);
    assertEquals(150, inventory.getProcessor());

    inventory.setProcessor(-50);
    assertEquals(0, inventory.getProcessor());
  }

  @Test
  void shouldCheckHasProcessors() {
    InventoryComponent inventory = new InventoryComponent(150);
    assertTrue(inventory.hasProcessors(100));
    assertFalse(inventory.hasProcessors(200));
  }

  @Test
  void shouldAddProcessor() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addProcessor(-500);
    assertEquals(0, inventory.getProcessor());

    inventory.addProcessor(100);
    inventory.addProcessor(-20);
    assertEquals(80, inventory.getProcessor());
  }
  @Test
  void shouldRemoveProcessors() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addProcessor(-500);
    assertEquals(0, inventory.getProcessor());
  }
}

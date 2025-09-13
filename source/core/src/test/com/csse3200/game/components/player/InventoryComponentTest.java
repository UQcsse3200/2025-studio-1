package com.csse3200.game.components.player;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {
  private int processor = 100;

  @Nested
  @DisplayName("Testing: addItem, setItem, get, getInventory, remove, isFull, isEmpty")
  class inventoryMethodsTest {
    private int processor = 100;
    private int MAX_INVENTORY = 5;
    private String texture = "images/mud.png";
    ArrayList<Entity> testInven;
    InventoryComponent inventory;

    @BeforeEach
    void setup() {
      testInven = new ArrayList<Entity>();
      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
        testInven.add(null);
      }
      inventory = new InventoryComponent(this.processor);
      Entity owner = new Entity();
      owner.addComponent(inventory);
      Gdx.files = mock(Files.class);
      when(Gdx.files.internal(anyString())).thenReturn(mock(FileHandle.class));

      // Physics needed by ItemFactory -> PhysicsComponent
      PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
      Body physicsBody = mock(Body.class);
      when(physicsEngine.createBody(org.mockito.ArgumentMatchers.any())).thenReturn(physicsBody);
      ServiceLocator.registerPhysicsService(new PhysicsService(physicsEngine));

      // Mock ResourceService so any texture fetch succeeds
      ResourceService resourceService = mock(ResourceService.class);
      ServiceLocator.registerResourceService(resourceService);
      Texture texture = mock(Texture.class);
      when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
      owner.create();
    }

    @Test
    void testGetInventoryEmpty() {
      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
        assertNull(inventory.get(idx));
        assertNull(inventory.getTex(idx));
      }
      ArrayList<String> test = new ArrayList<String>();
      for (int idx = 0; idx < MAX_INVENTORY; idx++)
        test.add(idx, null);

      assertFalse(inventory.isFull(), "Starting inventory isn't full");
      assertTrue(inventory.isEmpty(), "Starting inventory is empty");
      assertEquals(testInven, inventory.getInventory(), "Starting inventory is empty");
      assertEquals(test, inventory.getTextures(), "Starting inventory textures should be empty");
      assertEquals(0, inventory.getSize());
    }

    @Test
    void testGetInventorySingleItem() {
      Entity item = ItemFactory.createItem(texture);
      inventory.addItem(item);
      testInven.set(0, item);

      assertSame(testInven.get(0), inventory.getInventory().get(0),
              "Inventory of 1 item holds 1 item");
      for (int idx = 1; idx < MAX_INVENTORY; idx++) {
        assertNull(inventory.getInventory().get(idx),
                String.format("Slot %d is null (\'empty\')", idx));
      }
      assertFalse(inventory.isEmpty());
      assertFalse(inventory.isFull());
      assertEquals(MAX_INVENTORY, inventory.getInventory().size(), "inventory size doesn't increase");
      assertEquals(1, inventory.getSize());
    }

    @Test
    void testGetInventoryFullInventory() {
      Entity[] testSet = new Entity[MAX_INVENTORY];
      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
        testSet[idx] = ItemFactory.createItem(texture);
        inventory.addItem(testSet[idx]);
        testInven.set(idx, testSet[idx]);
      }

      // check full inventory
      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
          assertSame(testInven.get(idx), inventory.getInventory().get(idx),
                  "Returns full inventory");
      }
      assertTrue(inventory.isFull());
      assertFalse(inventory.isEmpty());
      assertEquals(MAX_INVENTORY, inventory.getSize());

      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
          assertNotNull(inventory.get(idx));
          assertNotNull(inventory.getTex(idx));
      }
    }

    @Test
    void shouldGet() {
      Entity[] testSet = new Entity[MAX_INVENTORY];
      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
        assertNull(inventory.get(idx), String.format("pos: %d in inventory is Empty", idx));
        assertNull(inventory.getTex(idx), String.format("pos: %d in inventory is Empty", idx));
      }

      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
        testSet[idx] = ItemFactory.createItem(texture);
        inventory.addItem(testSet[idx]);
        assertSame(testSet[idx], inventory.get(idx),
                String.format("inventory should have an \'item\' with an id of: %d", idx));
      }

      assertNull(inventory.get(6), "Invalid index returns null");
      assertNull(inventory.get(5), "Invalid index returns null");
      assertNull(inventory.get(-1), "Invalid index returns null");
      assertNull(inventory.getTex(6), "Invalid index returns null");
      assertNull(inventory.getTex(5), "Invalid index returns null");
      assertNull(inventory.getTex(-1), "Invalid index returns null");
      assertNotNull(inventory.get(4), "Valid index returns an Entity");
      assertNotNull(inventory.get(0), "Valid index returns an Entity");
      assertNotNull(inventory.getTex(4), "Valid index returns a texture path");
      assertNotNull(inventory.getTex(0), "Valid index returns a texture path");
    }

    @Test
    void shouldRemoveItem() {
      for (int idx = 0; idx < MAX_INVENTORY; idx++) {
        inventory.addItem(ItemFactory.createItem(texture));
        testInven.set(idx, ItemFactory.createItem(texture));
      }

      assertTrue(inventory.remove(0), "Removing works");
      assertTrue(inventory.remove(4), "Removing works");
      assertFalse(inventory.remove(-1), "Removing doesn't work on an invalid index");
      assertFalse(inventory.remove(5), "Removing doesn't work on an invalid index");

      assertNull(inventory.get(0), "Removed item is replaced with null");
      assertEquals(3, inventory.getSize(), "Inventory should contain 2 Entities");

      assertTrue(inventory.remove(1), "Removing works");
      assertTrue(inventory.remove(2), "Removing works");
      assertTrue(inventory.remove(3), "Removing works");

      assertEquals(0, inventory.getSize(), "Inventory should be empty");
    }

    @Test
    void shouldSetItem() {
      Entity item1 = ItemFactory.createItem(texture);
      assertTrue(inventory.setItem(0, item1), "Successfully set first item");
      assertEquals(1, inventory.getSize(), "Size should be 1 after adding first item");
      assertEquals(item1, inventory.get(0), "First item should be in slot 0");

      Entity item2 = ItemFactory.createItem(texture);
      assertTrue(inventory.setItem(3, item2), "Successfully set second item");
      assertEquals(2, inventory.getSize(), "Size should be 2 after adding second item");
      assertEquals(item2, inventory.get(3), "Second item in slot 3");

      Entity item3 = ItemFactory.createItem(texture);
      Entity item4 = ItemFactory.createItem(texture);
      Entity item5 = ItemFactory.createItem(texture);
      assertTrue(inventory.setItem(1, item3), "Should set item3");
      assertTrue(inventory.setItem(2, item4), "Should set item4");
      assertTrue(inventory.setItem(4, item5), "Should set item5");

      assertEquals(MAX_INVENTORY, inventory.getSize(), "Should have 5 items");
      assertTrue(inventory.isFull(), "Inventory should be full");

      assertEquals(item1, inventory.get(0), "Item1 in slot 0");
      assertEquals(item2, inventory.get(3), "Item2 in slot 3");
      assertEquals(item3, inventory.get(1), "Item3 in slot 1");
      assertEquals(item4, inventory.get(2), "Item4 in slot 2");
      assertEquals(item5, inventory.get(4), "Item5 in slot 4");

      assertEquals(texture, inventory.getTex(0), "texture in slot 0");
      assertEquals(texture, inventory.getTex(3), "texture in slot 3");
      assertEquals(texture, inventory.getTex(1), "texture in slot 1");
      assertEquals(texture, inventory.getTex(2), "texture in slot 2");
      assertEquals(texture, inventory.getTex(4), "texture in slot 4");
    }

    @Test
    void shouldAddItem() {
      Entity item1 = ItemFactory.createItem(texture);
      assertTrue(inventory.addItem(item1), "Should successfully add first item");
      assertEquals(1, inventory.getSize(), "Size should be 1 after adding first item");
      assertEquals(item1, inventory.get(0), "First item should be in slot 0");
      assertEquals(texture, inventory.getTex(0), "First item texture should be in slot 0");

      Entity item2 = ItemFactory.createItem(texture);
      assertTrue(inventory.addItem(item2), "Should successfully add second item");
      assertEquals(2, inventory.getSize(), "Size should be 2 after adding second item");
      assertEquals(texture, inventory.getTex(1), "Second item texture should be in slot 1");

      Entity item3 = ItemFactory.createItem(texture);
      Entity item4 = ItemFactory.createItem(texture);
      Entity item5 = ItemFactory.createItem(texture);
      assertTrue(inventory.addItem(item3), "Should add third item");
      assertTrue(inventory.addItem(item4), "Should add fourth item");
      assertTrue(inventory.addItem(item5), "Should add fifth item");

      assertEquals(MAX_INVENTORY, inventory.getSize(), "Should have 5 items");
      assertTrue(inventory.isFull(), "Inventory should be full");
    }

    @Test
    void shouldSetGetCurrItem() {
      //Test for a weapon
      Entity thing = WeaponsFactory.createWeapon(Weapons.PISTOL);
      inventory.setCurrItem(thing);

      assertInstanceOf(WeaponsStatsComponent.class, inventory.getCurrItemStats());
      assertEquals(thing, inventory.getCurrItem());

      //Test for a nothing entity
      thing = new Entity();
      inventory.setCurrItem(thing);
      assertEquals(thing, inventory.getCurrItem());
      assertNull(inventory.getCurrItemStats());
    }
  }

  @Test
  void shouldSetGetProcessor() {
    InventoryComponent inventory = new InventoryComponent(100);
    assertEquals(100, inventory.getProcessor());
    inventory.setProcessor(150);
    assertEquals(150, inventory.getProcessor());

    inventory.setProcessor(150);
    assertEquals(150, inventory.getProcessor());

    inventory.setProcessor(-50);
    assertEquals(0, inventory.getProcessor());
  }

  @Test
  void shouldCheckHasProcessor() {
    InventoryComponent inventory = new InventoryComponent(150);
    assertTrue(inventory.hasProcessor(100));
    assertFalse(inventory.hasProcessor(200));
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
}

package com.csse3200.game.components.shop;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CatalogEntry: basic construction & accessors")
class CatalogEntryTest {

    private Entity mkItemEntity(String name, String texture) {
        Entity e = new Entity();
        ItemComponent item = new ItemComponent();
        item.setName(name);
        item.setTexture(texture);
        e.addComponent(item);
        e.create();
        return e;
    }

    @Nested
    @DisplayName("Objective: Construction & validation")
    class ConstructionValidation {

        @Test
        @DisplayName("Constructs successfully with valid args")
        void constructsWithValidArgs() {
            Entity item = mkItemEntity("Sword", "sword.png");
            CatalogEntry entry = new CatalogEntry(item, 50, true, 10, 1);

            assertSame(item, entry.item());
            assertEquals(50, entry.price());
            assertTrue(entry.enabled());
            assertEquals(10, entry.maxStack());
            assertEquals(1, entry.bundleQuantity());
        }

        @Test
        @DisplayName("Null item -> throws IllegalArgumentException")
        void nullItemThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CatalogEntry(null, 10, true, 5, 1));
        }

        @Test
        @DisplayName("Non-positive price -> throws IllegalArgumentException")
        void nonPositivePriceThrows() {
            Entity item = mkItemEntity("Apple", "apple.png");
            assertThrows(IllegalArgumentException.class,
                    () -> new CatalogEntry(item, 0, true, 5, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> new CatalogEntry(item, -1, true, 5, 1));
        }

        @Test
        @DisplayName("bundleQuantity <= 0 -> throws IllegalArgumentException")
        void nonPositiveBundleThrows() {
            Entity item = mkItemEntity("Potion", "potion.png");
            assertThrows(IllegalArgumentException.class,
                    () -> new CatalogEntry(item, 10, true, 5, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> new CatalogEntry(item, 10, true, 5, -3));
        }

        @Test
        @DisplayName("maxStack < 1 -> throws IllegalArgumentException")
        void invalidMaxStackThrows() {
            Entity item = mkItemEntity("Key", "key.png");
            assertThrows(IllegalArgumentException.class,
                    () -> new CatalogEntry(item, 10, true, 0, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> new CatalogEntry(item, 10, true, -5, 1));
        }
    }

    @Nested
    @DisplayName("Objective: Accessors")
    class Accessors {

        @Test
        @DisplayName("getItem() returns the same entity")
        void getItemReturnsEntity() {
            Entity item = mkItemEntity("Shield", "shield.png");
            CatalogEntry entry = new CatalogEntry(item, 30, true, 5, 1);
            assertSame(item, entry.getItem());
        }

        @Test
        @DisplayName("getItemName() reads ItemComponent name")
        void getItemNameReadsFromComponent() {
            Entity item = mkItemEntity("Bow", "bow.png");
            CatalogEntry entry = new CatalogEntry(item, 40, true, 3, 1);
            assertEquals("Bow", entry.getItemName());
        }

        @Test
        @DisplayName("Record accessors reflect constructor values")
        void recordAccessorsMatch() {
            Entity item = mkItemEntity("Gem", "gem.png");
            CatalogEntry entry = new CatalogEntry(item, 99, false, 7, 2);
            assertEquals(99, entry.price());
            assertFalse(entry.enabled());
            assertEquals(7, entry.maxStack());
            assertEquals(2, entry.bundleQuantity());
        }
    }
}
package com.csse3200.game.entities.factories;

import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.configs.weapons.DaggerConfig;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class WeaponsFactoryTest {

    @Test
    void shouldSetItemName() {
        WeaponConfig config = new DaggerConfig();
        ItemComponent item = new ItemComponent();

        WeaponsFactory.setItemNameFromConfig(config, item);
        assertEquals(config.getName(), item.getName());
    }

    @Test
    @Description("should not when config name is empty")
    void shouldNotSetItemNameEmpty() {
        WeaponConfig config = new DaggerConfig();
        config.setName("");
        ItemComponent item = new ItemComponent();
        WeaponsFactory.setItemNameFromConfig(config, item);

        assertNull(item.getName());
    }
}

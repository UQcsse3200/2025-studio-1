package com.csse3200.game.entities.configs.armour;

import com.csse3200.game.entities.configs.ItemTypes;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class ArmourConfigsTest {
    @Test
    void chestplateConfigTest() {
        ArmourConfig chestplateConfig = new ChestplateConfig();
        assertEquals(ItemTypes.CHESTPLATE_ARMOUR, chestplateConfig.armourType);
        assertEquals(15, chestplateConfig.protection);
        assertNotNull(chestplateConfig.texturePath);
        assertNotNull(chestplateConfig.rightOffset);
        assertNotNull(chestplateConfig.leftOffset);
        assertNotNull(chestplateConfig.heightScale);
    }

    @Test
    void hoodConfigTest() {
        ArmourConfig hoodConfig = new HoodConfig();
        assertEquals(ItemTypes.HOOD_ARMOUR, hoodConfig.armourType);
        assertEquals(5, hoodConfig.protection);
        assertNotNull(hoodConfig.texturePath);
        assertNotNull(hoodConfig.rightOffset);
        assertNotNull(hoodConfig.leftOffset);
        assertNotNull(hoodConfig.heightScale);
    }
}

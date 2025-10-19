package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.entities.configs.characters.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import org.junit.After;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.*;

public class PlayerFactoryTests {

    MockedStatic<FileLoader> mocked;

    @BeforeEach
    void setup() {
        mocked = mockStatic(FileLoader.class);

    }

    @Test
    void safeLoadWhenNoConfig() {
        mocked.when(() -> FileLoader.readClass(PlayerConfig.class, "configs/player.json"))
                .thenReturn(null);

        PlayerConfig cfg = PlayerFactory.getStats();
        assertEquals(0, cfg.gold);
        assertEquals(100, cfg.health);
        assertEquals(10, cfg.baseAttack);
    }

    @Test
    void safeLoadPlayerConfigWithFile() {
        PlayerConfig mockConfig = new PlayerConfig();
        mockConfig.gold = 50;
        mockConfig.health = 80;
        mockConfig.baseAttack = 15;

        mocked.when(() -> FileLoader.readClass(PlayerConfig.class, "configs/player.json"))
                .thenReturn(mockConfig);

        PlayerConfig cfg = PlayerFactory.getStats();
        assertEquals(50, cfg.gold);
        assertEquals(80, cfg.health);
        assertEquals(15, cfg.baseAttack);
    }



    @AfterEach
    void after() throws NoSuchFieldException, IllegalAccessException {
        mocked.close();
        java.lang.reflect.Field field = PlayerFactory.class.getDeclaredField("stats");
        field.setAccessible(true);
        field.set(null, null);
    }
}

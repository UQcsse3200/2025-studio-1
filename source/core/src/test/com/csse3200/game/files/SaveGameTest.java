package com.csse3200.game.files;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.configuration.IMockitoConfiguration;


import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(GameExtension.class)
public class SaveGameTest {

    private final String fileName = "saveFileValid.json";
    private final SaveGame saveGame = spy(new SaveGame());
    private SaveGame.GameState testState = new SaveGame.GameState();
    private SaveGame.itemInInven inventoryTest = new SaveGame.itemInInven();
    private SaveGame.information playerInfoTest = new SaveGame.information();

    @BeforeAll
    public static void testSaveGame() {
        Entity weaponTest = new Entity();
    }


}

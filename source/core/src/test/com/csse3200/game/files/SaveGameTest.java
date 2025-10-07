package com.csse3200.game.files;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.extension.ExtendWith;


import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

//built on in sprint 4 ran out of time
@ExtendWith(GameExtension.class)
public class SaveGameTest {

    private final String fileName = "saveFileValid.json";
    private final SaveGame saveGame = spy(new SaveGame());
    private SaveGame.GameState testState = new SaveGame.GameState();

//    public SaveGame.GameState getTestState() {
//        testState.getInventory();
//    }

//    @Test
//    public void testSaveGame() {
//        SaveGame.GameState gameStateTest = FileLoader.readPlayer(SaveGame.GameState.class, fileName, FileLoader.Location.LOCAL);
//
//    }
}

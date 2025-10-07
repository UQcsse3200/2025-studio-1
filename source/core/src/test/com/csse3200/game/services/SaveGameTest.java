package com.csse3200.game.services;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.SaveGame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;



import static org.junit.jupiter.api.Assertions.*;
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

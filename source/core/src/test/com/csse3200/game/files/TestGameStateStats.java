package com.csse3200.game.files;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

//Sprint 4 willupon this
@ExtendWith(GameExtension.class)
public class TestGameStateStats {

    public String difficultyLoad = "";


    public static class PlayerStatTest {
        public Vector2 possLoad = new Vector2(1, 1);
        public String avatarLoad = "testAvatar";
        public int stamLoad = 1;
        public int healthLoad = 1;
        public int processorLoad = 1;
        public int ammoLoad = 1;
    }

    public static class onlyArgsInventory {
        public String typeLoad = "";
        public String itemTextureLoad = "";
        public int stamLoad = 1;
        public int upgradeLoad = 1;
        public int itemCountLoad = 1;
        public int weaponAmmoLoad = 1;
    }

}

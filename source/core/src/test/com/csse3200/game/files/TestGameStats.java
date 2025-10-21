package com.csse3200.game.files;

import com.badlogic.gdx.math.Vector2;

//Sprint 4 willupon this
public class TestGameStats {

    public String difficultyLoad = "";


    public static class PlayerStatTest {
        public Vector2 possLoad = new Vector2(1, 1);
        public String avatarLoad = "testAvatarTex";
        public float stamLoad = 100;
        public int healthLoad = 1;
        public int maxHealth = 10;
        public int processorLoad = 1;
        public int ammoLoad = 1;
        public int keyCardLvl = 1;
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

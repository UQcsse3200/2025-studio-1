package com.csse3200.game.files;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.extension.ExtendWith;

//Sprint 4 willupon this
@ExtendWith(GameExtension.class)
public class TestGameStateStats {

    public class playerStatTest {
        public Vector2 Posstat = new Vector2(1, 1);
        public String avatar = "testAvatar";
        public String name;
    }
}

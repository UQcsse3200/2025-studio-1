// src/test/java/com/csse3200/game/components/friendlynpc/CompanionControlPanel_NullArgsTest.java
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 极简测试：只验证 attach(...) 的空参早返回分支。
 * 不创建任何 UI/Skin/Texture，保证测试稳过。
 */
public class CompanionControlPanelTest {

    @Test
    void attach_returnsNull_whenStageIsNull() {
        Entity comp = mock(Entity.class);

        // 静态 mock 只是为了确保不会不小心调用 ServiceLocator（可选）
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            assertNull(CompanionControlPanel.attach(null, comp));
            assertNull(CompanionControlPanel.attach(null, comp, 0.44f));
            sl.verifyNoInteractions(); // attach 提前返回，不应访问 ServiceLocator
        }
    }

    @Test
    void attach_returnsNull_whenCompIsNull() {
        Stage stage = mock(Stage.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            assertNull(CompanionControlPanel.attach(stage, null));
            assertNull(CompanionControlPanel.attach(stage, null, 200f));
            sl.verifyNoInteractions();
        }
    }
}

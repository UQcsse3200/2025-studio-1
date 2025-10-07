// src/test/java/com/csse3200/game/components/friendlynpc/RemoteOpenComponentTest.java
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.csse3200.game.rendering.RenderService;


import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteOpenComponentTest {



    private RemoteOpenComponent comp;
    private Entity owner;

    @BeforeEach
    void setup() {
        // mock Gdx.input（LibGDX 静态字段）
        Gdx.input = mock(Input.class);

        comp = new RemoteOpenComponent();
        owner = mock(Entity.class);
        comp.setEntity(owner);
    }

    /** 通过反射读取私有字段 panel */
    private static Actor getPanel(RemoteOpenComponent c) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("panel");
        f.setAccessible(true);
        return (Actor) f.get(c);
    }

    /** 通过反射置换私有字段 panel */
    private static void setPanel(RemoteOpenComponent c, Actor panel) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("panel");
        f.setAccessible(true);
        f.set(c, panel);
    }

    @Test
    void update_noKeyPressed_doesNothing() throws Exception {
        // 未按键
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(false);

        // 静态 mock ServiceLocator，确保不会访问
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            comp.update();

            // 不应访问 ServiceLocator 和 CompanionControlPanel
            sl.verifyNoInteractions();
            ccp.verifyNoInteractions();

            assertNull(getPanel(comp));
        }
    }

    @Test
    void update_firstPress_createsPanel_whenStageAvailable() throws Exception {
        // 模拟按键（默认 C 键）
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        // 准备 RenderService 和 Stage
        RenderService renderService = mock(RenderService.class);
        Stage stage = mock(Stage.class);
        when(renderService.getStage()).thenReturn(stage);

        // 准备 attach 的返回 Actor
        Table panel = mock(Table.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);
            ccp.when(() -> CompanionControlPanel.attach(stage, owner)).thenReturn(panel);

            comp.update();

            // 验证 attach 被调用
            ccp.verify(() -> CompanionControlPanel.attach(stage, owner), times(1));

            // 面板已保存（panel 字段类型是 Actor，Table 继承 Actor，可直接断言）
            assertSame(panel, getPanel(comp));
        }
    }

    @Test
    void update_secondPress_removesPanel_whenAlreadyOnStage() throws Exception {
        // 先构造一个“已存在的 panel 且在舞台上”的状态
        Actor panel = mock(Actor.class);
        Stage stage = mock(Stage.class);
        when(panel.getStage()).thenReturn(stage);
        setPanel(comp, panel);

        // 按键触发关闭
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        // RenderService 存在，但不会调用 attach（走移除分支）
        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(stage);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);

            comp.update();

            // 不会调用 attach
            ccp.verifyNoInteractions();
            // 会调用 remove
            verify(panel, times(1)).remove();
            // 状态被清空
            assertNull(getPanel(comp));
        }
    }

    @Test
    void update_stageNull_doesNothing() throws Exception {
        // 按键
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        // RenderService 返回 null stage
        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(null);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);

            comp.update();

            // 不会 attach
            ccp.verifyNoInteractions();
            assertNull(getPanel(comp));
        }
    }

    @Test
    void dispose_removesPanel_ifExists() throws Exception {
        Actor panel = mock(Actor.class);
        setPanel(comp, panel);

        comp.dispose();

        verify(panel, times(1)).remove();
        assertNull(getPanel(comp));
    }
}


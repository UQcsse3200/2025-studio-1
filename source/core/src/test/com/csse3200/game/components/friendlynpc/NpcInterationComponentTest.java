// src/test/java/com/csse3200/game/components/friendlynpc/NpcInterationComponentTest.java
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class NpcInterationComponentTest {

    private Entity npc;                 // 掛载组件的NPC
    private Entity player;              // 玩家
    private DialogueDisplay ui;         // UI组件
    private NpcDialogueDataComponent data; // 对话数据
    private EventHandler events;        // 事件

    @BeforeEach
    void setUp() {
        // 直接把 Gdx.input 设为 mock（LibGDX 的静态字段，可直接赋值）
        Gdx.input = mock(Input.class);

        npc = mock(Entity.class);
        player = mock(Entity.class);
        ui = mock(DialogueDisplay.class);
        data = mock(NpcDialogueDataComponent.class);
        events = mock(EventHandler.class);

        when(npc.getComponent(DialogueDisplay.class)).thenReturn(ui);
        when(npc.getComponent(NpcDialogueDataComponent.class)).thenReturn(data);
        when(npc.getEvents()).thenReturn(events);

        // 默认位置：npc(0,0), player(0,0) —— 在需要的测试里再覆盖
        when(npc.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(0f, 0f));
    }

    @Test
    void outOfRange_onlyHide_andReturn() {
        // 距离设为 2，大于 triggerDist=1
        when(npc.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(2f, 0f));

        NpcInterationComponent c = new NpcInterationComponent(player, 1f);
        c.setEntity(npc);

        // 无按键
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(false);

        c.update();

        // 只会调用 hide()，不触发事件、不展示
        verify(ui, times(1)).hide();
        verify(ui, never()).bindData(any());
        verify(ui, never()).showFirst();
        verify(events, never()).trigger(anyString());
    }

    @Test
    void pressEscape_hideAndTriggerEnd() {
        // 在范围内（否则会提前 return）
        when(npc.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(0.5f, 0f));

        NpcInterationComponent c = new NpcInterationComponent(player, 1f);
        c.setEntity(npc);

        // ESC 被按下
        when(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)).thenReturn(true);

        c.update();

        verify(ui, times(1)).hide();
        verify(events, times(1)).trigger("npcDialogueEnd");

        // 不会去处理交互键
        verify(ui, never()).bindData(any());
        verify(ui, never()).showFirst();
        verify(events, never()).trigger("npcDialogueStart");
    }

    @Test
    void firstPressInteract_bindShow_setConsumed_andTriggerStart_onlyOnce() {
        // 在范围内
        when(npc.getPosition()).thenReturn(new Vector2(0f, 0f));
        when(player.getPosition()).thenReturn(new Vector2(0.5f, 0f));

        // 使用自定义交互键（例如 E），也可用默认构造
        int key = Input.Keys.E;
        NpcInterationComponent c = new NpcInterationComponent(player, 1f, key);
        c.setEntity(npc);

        // 第一次按交互键
        when(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)).thenReturn(false);
        when(Gdx.input.isKeyJustPressed(key)).thenReturn(true);

        c.update();

        // 首次会绑定数据、显示首句并触发 start
        verify(ui, times(1)).bindData(data);
        verify(ui, times(1)).showFirst();
        verify(events, times(1)).trigger("npcDialogueStart");

        // 第二帧：再次按交互键也不会重复触发（consumedEver = true）
        reset(ui, events);
        when(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)).thenReturn(false);
        when(Gdx.input.isKeyJustPressed(key)).thenReturn(true);

        c.update();

        verify(ui, never()).bindData(any());
        verify(ui, never()).showFirst();
        verify(events, never()).trigger(anyString());
    }
}

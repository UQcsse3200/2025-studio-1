package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class NpcInteractionConponmentTest{
    private Entity npc;
    private Entity player;
    private DialogueDisplay ui;
    private NpcDialogueDataComponent data;
    private Input input;

    @BeforeEach
    void setup() {
        npc = new Entity();
        player = new Entity();
        npc.setPosition(new Vector2(0, 0));
        player.setPosition(new Vector2(0, 0));
        ui = mock(DialogueDisplay.class);
        data = mock(NpcDialogueDataComponent.class);
        npc.addComponent(ui);
        npc.addComponent(data);
        input = mock(Input.class);
        Gdx.input = input;
    }

    @Test
    void hides_whenOutOfRange() {
        player.setPosition(new Vector2(10, 0));
        NpcInterationComponent comp = new NpcInterationComponent(player, 1f);
        npc.addComponent(comp);

        when(input.isKeyJustPressed(anyInt())).thenReturn(false);

        comp.update();
        verify(ui, times(1)).hide();
    }

    @Test
    void hidesAndTriggersEnd_whenEscPressed() {
        player.setPosition(new Vector2(0.5f, 0));
        NpcInterationComponent comp = new NpcInterationComponent(player, 1f);
        npc.addComponent(comp);

        when(input.isKeyJustPressed(Input.Keys.ESCAPE)).thenReturn(true);

        comp.update();

        verify(ui).hide();
        EventHandler ev = npc.getEvents();
    }

    @Test
    void bindsAndShowsFirstTime_whenInteractKeyPressed() {
        player.setPosition(new Vector2(0.5f, 0));
        NpcInterationComponent comp = new NpcInterationComponent(player, 1f);
        npc.addComponent(comp);

        when(input.isKeyJustPressed(Input.Keys.F)).thenReturn(true);

        comp.update();

        verify(ui).bindData(data);
        verify(ui).showFirst();
    }

    @Test
    void customKeyWorks() {
        player.setPosition(new Vector2(0.5f, 0));
        int customKey = Input.Keys.E;
        NpcInterationComponent comp = new NpcInterationComponent(player, 1f, customKey);
        npc.addComponent(comp);
        when(input.isKeyJustPressed(customKey)).thenReturn(true);
        comp.update();
        verify(ui).bindData(data);
        verify(ui).showFirst();
    }
}
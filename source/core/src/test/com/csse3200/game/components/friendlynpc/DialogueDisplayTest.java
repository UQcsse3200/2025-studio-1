package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DialogueDisplayTest {

    private DialogueDisplay dialogueDisplay;

    @BeforeEach
    void setUp() {
        dialogueDisplay = new DialogueDisplay();
    }

    @Test
    void testCreateDoesNotThrow() {
        // Test that create method doesn't throw exceptions
        // Even if services are not available
        assertDoesNotThrow(() -> dialogueDisplay.create());
    }

    @Test
    void testDisposeDoesNotThrow() {
        // Test dispose without creating first
        assertDoesNotThrow(() -> dialogueDisplay.dispose());
    }

    @Test
    void testCreateThenDispose() {
        // Test the full lifecycle
        assertDoesNotThrow(() -> {
            dialogueDisplay.create();
            dialogueDisplay.dispose();
        });
    }

    @Test
    void testMultipleDisposeCallsSafe() {
        // Test that multiple dispose calls are safe
        assertDoesNotThrow(() -> {
            dialogueDisplay.create();
            dialogueDisplay.dispose();
            dialogueDisplay.dispose(); // Second dispose should be safe
        });
    }

    @Test
    void testBasicComponentFunctionality() {
        // Test basic component methods inherited from Component
        assertNotNull(dialogueDisplay);

        // Test that the component can be created without exceptions
        assertDoesNotThrow(() -> dialogueDisplay.create());

        // Test early disposal (if this method exists in Component)
        // Remove this line if earlyDispose doesn't exist
        // assertDoesNotThrow(() -> dialogueDisplay.earlyDispose());
    }

    @Test
    void testServiceHandling() {
        // Test that the component handles service unavailability gracefully
        try (MockedStatic<ServiceLocator> mockedServiceLocator = mockStatic(ServiceLocator.class)) {
            // Mock ServiceLocator to throw exceptions
            mockedServiceLocator.when(ServiceLocator::getRenderService)
                    .thenThrow(new RuntimeException("Service not available"));

            // Should not crash when services are unavailable
            assertDoesNotThrow(() -> dialogueDisplay.create());
        }
    }

    @Test
    void testNullServiceHandling() {
        // Test that the component handles null services gracefully
        try (MockedStatic<ServiceLocator> mockedServiceLocator = mockStatic(ServiceLocator.class)) {
            // Mock ServiceLocator to return null
            mockedServiceLocator.when(ServiceLocator::getRenderService).thenReturn(null);

            // Should not crash when services return null
            assertDoesNotThrow(() -> dialogueDisplay.create());
        }
    }

    @Test
    void testShowFirstAndNextOrHide() {
        // Prepare data
        String[] lines = {"hello", "world"};
        NpcDialogueDataComponent data = new NpcDialogueDataComponent("Mia", "", lines);

        // Do not throw exception
        assertDoesNotThrow(() -> dialogueDisplay.showFirst(data));

        // Call nextOrHide twice to simulate sentence-by-sentence advancement
        assertDoesNotThrow(dialogueDisplay::nextOrHide);
        assertDoesNotThrow(dialogueDisplay::nextOrHide);
    }

    @Test
    void testEmptyDataHandledGracefully() {
        NpcDialogueDataComponent empty = new NpcDialogueDataComponent("", "", new String[0]);
        assertDoesNotThrow(() -> dialogueDisplay.showFirst(empty));
    }

    @Test
    void testSetTextShowHideSafe() {
        assertDoesNotThrow(() -> dialogueDisplay.setText("some text"));
        assertDoesNotThrow(dialogueDisplay::show);
        assertDoesNotThrow(dialogueDisplay::hide);
        assertDoesNotThrow(() -> dialogueDisplay.setText(null));
    }

    @Test
    void testBindDataStoresValuesCorrectly() {
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent("Alice", "portraits/alice.png", new String[]{"Hi", "Bye"});
        assertDoesNotThrow(() -> dialogueDisplay.bindData(data));

        // Use reflection to read private fields to verify cache values
        try {
            var fSpeaker = DialogueDisplay.class.getDeclaredField("speaker");
            fSpeaker.setAccessible(true);
            var fLines = DialogueDisplay.class.getDeclaredField("lines");
            fLines.setAccessible(true);

            String speaker = (String) fSpeaker.get(dialogueDisplay);
            String[] lines = (String[]) fLines.get(dialogueDisplay);

            assertEquals("Alice", speaker);
            assertArrayEquals(new String[]{"Hi", "Bye"}, lines);
        } catch (Exception e) {
            fail("Reflection access failed: " + e.getMessage());
        }
    }

    @Test
    void testBindDataHandlesNullGracefully() {
        assertDoesNotThrow(() -> dialogueDisplay.bindData(null));
        assertDoesNotThrow(() -> dialogueDisplay.bindData(
                new NpcDialogueDataComponent(null, null, null)));
    }

    @Test
    void testEnableClickToNextDoesNotThrow() {
        assertDoesNotThrow(() -> dialogueDisplay.enableClickToNext());
    }

    @Test
    void testNextOrHideWithoutLinesDoesNotThrow() {
        assertDoesNotThrow(() -> dialogueDisplay.nextOrHide());
    }

    @Test
    void testSetTextHandlesVariousInputs() {
        assertDoesNotThrow(() -> dialogueDisplay.setText("Line 1"));
        assertDoesNotThrow(() -> dialogueDisplay.setText(""));
        assertDoesNotThrow(() -> dialogueDisplay.setText(null));
    }

    @Test
    void testShowFirstHandlesNoDataGracefully() {
        assertDoesNotThrow(() -> dialogueDisplay.showFirst());
    }

    @Test
    void testShowFirstDataCallsBindDataInternally() {
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent("Bob", "", new String[]{"one", "two"});
        assertDoesNotThrow(() -> dialogueDisplay.showFirst(data));
    }

    @Test
    void testMultipleShowAndHideSafe() {
        assertDoesNotThrow(() -> {
            dialogueDisplay.show();
            dialogueDisplay.hide();
            dialogueDisplay.show();
            dialogueDisplay.hide();
        });
    }

    @Test
    void createEntityAndBindData() throws Exception {
        // Arrange: inject a fake entity so that create() enters the entity/data branch
        DialogueDisplay ui = new DialogueDisplay();
        Entity entity = mock(Entity.class);
        NpcDialogueDataComponent data =
                new NpcDialogueDataComponent("NPC", "", new String[]{"Hello", "World"});
        when(entity.getComponent(NpcDialogueDataComponent.class)).thenReturn(data);

        ui.setEntity(entity);

        // Act
        assertDoesNotThrow(ui::create);

        // Assert: bindData() must have updated the internal cache
        Field fSpeaker = DialogueDisplay.class.getDeclaredField("speaker");
        fSpeaker.setAccessible(true);
        Field fLines = DialogueDisplay.class.getDeclaredField("lines");
        fLines.setAccessible(true);

        assertEquals("NPC", fSpeaker.get(ui));
        assertArrayEquals(new String[]{"Hello", "World"}, (String[]) fLines.get(ui));
    }

    @Test
    void enableClickToNextInteractsWithTable() throws Exception {
        // Arrange: inject a mock Table so enableClickToNext() performs UI wiring
        DialogueDisplay ui = new DialogueDisplay();
        Table table = mock(Table.class);

        Field fTable = DialogueDisplay.class.getDeclaredField("dialogueTable");
        fTable.setAccessible(true);
        fTable.set(ui, table);

        // Act
        ui.enableClickToNext();

        // Assert: verify touch + listener wiring on the table
        verify(table).setTouchable(Touchable.enabled);
        verify(table).clearListeners();
        verify(table).addListener(any());
    }

    @Test
    void updateClickOnlyOnce() throws Exception {
        // Arrange: spy component, and inject a mock Table to avoid NPE
        DialogueDisplay spyUi = spy(new DialogueDisplay());
        Table table = mock(Table.class);

        Field fTable = DialogueDisplay.class.getDeclaredField("dialogueTable");
        fTable.setAccessible(true);
        fTable.set(spyUi, table);

        // Act: call update() twice
        spyUi.update();
        spyUi.update();

        // Assert: enableClickToNext() should be called only once
        verify(spyUi, times(1)).enableClickToNext();
    }

    @Test
    void nextOrHideStepsAndThenTriggersEnd() throws Exception {
        // Arrange: prepare two lines and visible table
        DialogueDisplay ui = spy(new DialogueDisplay());

        Field fLines = DialogueDisplay.class.getDeclaredField("lines");
        fLines.setAccessible(true);
        fLines.set(ui, new String[]{"L1", "L2"});

        Field fIndex = DialogueDisplay.class.getDeclaredField("index");
        fIndex.setAccessible(true);
        fIndex.set(ui, 0);

        Table table = mock(Table.class);
        when(table.isVisible()).thenReturn(true);

        Field fTable = DialogueDisplay.class.getDeclaredField("dialogueTable");
        fTable.setAccessible(true);
        fTable.set(ui, table);

        // Mock entity + events for end trigger
        Entity entity = mock(Entity.class);
        EventHandler events = mock(EventHandler.class);
        when(entity.getEvents()).thenReturn(events);
        ui.setEntity(entity);

        // Act: step once (0 -> 1)
        ui.nextOrHide();
        assertEquals(1, fIndex.get(ui));
        verify(ui, atLeastOnce()).setText(any());

        // Act: step again (end -> hide + trigger)
        ui.nextOrHide();

        // Assert: end event fired and table hidden
        verify(events, times(1)).trigger("npcDialogueEnd");
        verify(table, atLeastOnce()).setVisible(false);
    }

    @Test
    void disposeInternalTextures() throws Exception {
        // Arrange: push a mock texture into disposableTextures to hit the loop body
        DialogueDisplay ui = new DialogueDisplay();
        Texture t = mock(Texture.class);

        Field fList = DialogueDisplay.class.getDeclaredField("disposableTextures");
        fList.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<Texture> list = (java.util.List<Texture>) fList.get(ui);
        list.add(t);

        // Avoid NPE on remove()
        Field fTable = DialogueDisplay.class.getDeclaredField("dialogueTable");
        fTable.setAccessible(true);
        fTable.set(ui, null);
        Field fLabel = DialogueDisplay.class.getDeclaredField("dialogueLabel");
        fLabel.setAccessible(true);
        fLabel.set(ui, null);

        // Act
        ui.dispose();

        // Assert
        verify(t, times(1)).dispose();
    }

    @Test
    void setLineFormatsWithSpeakerPrefix() throws Exception {
        // Arrange: spy to observe setText(), then inject speaker + lines
        DialogueDisplay ui = spy(new DialogueDisplay());

        Field fLines = DialogueDisplay.class.getDeclaredField("lines");
        fLines.setAccessible(true);
        fLines.set(ui, new String[]{"Hi"});

        Field fSpeaker = DialogueDisplay.class.getDeclaredField("speaker");
        fSpeaker.setAccessible(true);
        fSpeaker.set(ui, "Mia");

        // Act: invoke private setLine(0) via reflection
        Method mSetLine = DialogueDisplay.class.getDeclaredMethod("setLine", int.class);
        mSetLine.setAccessible(true);
        mSetLine.invoke(ui, 0);

        // Assert: text should be "Mia: Hi"
        verify(ui).setText(eq("Mia: Hi"));
    }

    @Test
    void showFirstSetsIndexAndShowsTable() throws Exception {
        DialogueDisplay ui = spy(new DialogueDisplay());
        // inject lines
        var fLines = DialogueDisplay.class.getDeclaredField("lines");
        fLines.setAccessible(true);
        fLines.set(ui, new String[]{"A", "B"});
        // inject table
        Table table = mock(Table.class);
        var fTable = DialogueDisplay.class.getDeclaredField("dialogueTable");
        fTable.setAccessible(true);
        fTable.set(ui, table);
        // act
        ui.showFirst();
        // assert
        var fIndex = DialogueDisplay.class.getDeclaredField("index");
        fIndex.setAccessible(true);
        assertEquals(0, fIndex.get(ui));
        verify(table).setVisible(true);
    }

}
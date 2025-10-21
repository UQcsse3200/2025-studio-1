package com.csse3200.game.components.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ButtonSoundService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class PauseMenuDisplayTest {

    private Stage stage;
    private GdxGame mockGame;
    private ButtonSoundService mockSound;
    private RenderService renderService;

    private final ArgumentCaptor<Actor> addActorCaptor = ArgumentCaptor.forClass(Actor.class);

    @BeforeEach
    void setup() {
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.getResourceService().loadAll();

        stage = mock(Stage.class);
        renderService = new RenderService();
        renderService.setStage(stage);
        ServiceLocator.registerRenderService(renderService);
        ServiceLocator.registerInputService(new InputService());

        mockGame = mock(GdxGame.class);
        mockSound = mock(ButtonSoundService.class);
        ServiceLocator.registerButtonSoundService(mockSound);

        PauseMenuDisplay.resetEscConsumed();
    }

    private List<Actor> collectAddedActors() {
        verify(stage, atLeastOnce()).addActor(addActorCaptor.capture());
        return new ArrayList<>(addActorCaptor.getAllValues());
    }

    private static Table findRoot(List<Actor> added) {
        for (Actor a : added) if (a instanceof Table t) return t;
        return null;
    }

    private static TextButton findButton(Table root, String label) {
        for (Actor a : root.getChildren()) {
            if (a instanceof TextButton tb && label.equals(tb.getText().toString())) return tb;
            if (a instanceof Table t) {
                TextButton nested = findButton(t, label);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    @Test
    void build_addsDimmer_andButtons_exist() {
        PauseMenuDisplay display = new PauseMenuDisplay(mockGame);
        Entity ui = new Entity(); ui.addComponent(display); display.create();

        List<Actor> added = collectAddedActors();
        assertTrue(added.stream().anyMatch(a -> a instanceof Image));

        Table root = findRoot(added);
        assertNotNull(root);
        assertNotNull(findButton(root, "Resume"));
        assertNotNull(findButton(root, "Restart"));
        assertNotNull(findButton(root, "Main Menu"));
        assertNotNull(findButton(root, "Save"));
    }

    @Test
    void resumeButton_triggersResume_and_playsClick() {
        PauseMenuDisplay display = new PauseMenuDisplay(mockGame);
        Entity ui = new Entity();
        ui.addComponent(display);
        display.create();

        Table root = findRoot(collectAddedActors());
        assertNotNull(root);

        AtomicInteger resumeCount = new AtomicInteger();
        display.getEntity().getEvents().addListener("resume", resumeCount::incrementAndGet);

        TextButton resume = findButton(root, "Resume");
        assertNotNull(resume);
        resume.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());

        verify(mockSound, atLeastOnce()).playClick();
        assertEquals(1, resumeCount.get());
    }

    @Test
    void restartButton_switchesToMainGame_and_playsClick() {
        PauseMenuDisplay display = new PauseMenuDisplay(mockGame);
        Entity ui = new Entity();
        ui.addComponent(display);
        display.create();

        Table root = findRoot(collectAddedActors());
        TextButton restart = findButton(root, "Restart");
        assertNotNull(restart);
        restart.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());

        verify(mockSound, atLeastOnce()).playClick();
        verify(mockGame).setScreen(GdxGame.ScreenType.MAIN_GAME);
    }

    @Test
    void mainMenuButton_switchesToMainMenu_and_playsClick() {
        PauseMenuDisplay display = new PauseMenuDisplay(mockGame);
        Entity ui = new Entity();
        ui.addComponent(display);
        display.create();

        Table root = findRoot(collectAddedActors());
        TextButton mainMenu = findButton(root, "Main Menu");
        assertNotNull(mainMenu);
        mainMenu.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());

        verify(mockSound, atLeastOnce()).playClick();
        verify(mockGame).setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    @Test
    void saveButton_triggersSave_andPlaysClick() {
        PauseMenuDisplay display = new PauseMenuDisplay(mockGame);
        Entity ui = new Entity(); ui.addComponent(display); display.create();

        Table root = findRoot(collectAddedActors());
        TextButton save = findButton(root, "Save");

        AtomicInteger saveCount = new AtomicInteger();
        display.getEntity().getEvents().addListener("save", saveCount::incrementAndGet);

        save.fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());

        verify(mockSound, atLeastOnce()).playClick();
        assertEquals(1, saveCount.get());
    }

    @Test
    void zIndex_isHigh() {
        PauseMenuDisplay display = new PauseMenuDisplay(mockGame);
        assertEquals(100f, display.getZIndex(), 0.0001f);
    }

    @Test
    void dispose_runsWithoutException() {
        PauseMenuDisplay display = new PauseMenuDisplay(mockGame);
        Entity ui = new Entity();
        ui.addComponent(display);
        display.create();
        assertDoesNotThrow(display::dispose);
    }
}

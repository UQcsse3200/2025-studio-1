package com.csse3200.game.components.settingsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.ui.UIComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@ExtendWith(GameExtension.class)
class ControlDisplayTest {

    private static final String BG_PATH = "images/menu_background.png";

    @BeforeAll
    static void bootFiles() {

        assertTrue(
                Gdx.files.internal(BG_PATH).exists(),
                "Missing test asset. Put it at src/test/resources/" + BG_PATH
        );


        assertDoesNotThrow(() -> {
            Class.forName(UIComponent.class.getName());
        }, "UIComponent static initialiser failed (skin assets missing?). " +
                "Place your skin files in src/test/resources at the paths UIComponent loads.");
    }

    private static void callBuildUI(ControlDisplay disp, Table root) throws Exception {
        Method m = ControlDisplay.class.getDeclaredMethod("buildUI", Table.class);
        m.setAccessible(true);
        m.invoke(disp, root);
    }

    @Test
    void buildUI_adds_background_title_controls_and_back_and_backFires() throws Exception {
        // Arrange
        GdxGame game = mock(GdxGame.class, withSettings());
        AtomicInteger backCalls = new AtomicInteger(0);
        ControlDisplay ui = new ControlDisplay(game, backCalls::incrementAndGet);
        Table root = new Table();

        callBuildUI(ui, root);

        assertTrue(root.getChildren().size >= 3);
        Actor first = root.getChildren().first();
        assertInstanceOf(Image.class, first, "first child should be background Image");
        TextureRegionDrawable dr = (TextureRegionDrawable) ((Image) first).getDrawable();
        assertNotNull(dr.getRegion().getTexture(), "background texture should exist");

        // Title label
        Label title = null;
        for (Actor a : root.getChildren()) {
            if (a instanceof Label l && "Controls".contentEquals(l.getText())) {
                title = l;
                break;
            }
        }
        assertNotNull(title, "title label present");
        assertEquals(1.5f, title.getFontScaleX(), 1e-6);
        assertEquals(Align.center, title.getLabelAlign());

        Table controls = null;
        for (Actor a : root.getChildren())
            if (a instanceof Table t) {
                controls = t;
                break;
            }
        assertNotNull(controls, "controls table present");
        assertEquals(22, controls.getChildren().size, "11 rows × (label+button)");

        // Buttons disabled, keys as expected
        Set<String> expectedKeys = Set.of("A", "D", "S", "Space", "Spacex2", "I", "E", "R", "Tab","LMB","Q");
        int btns = 0;
        for (Actor a : controls.getChildren()) {
            if (a instanceof TextButton tb) {
                btns++;
                assertTrue(tb.isDisabled(), "key button should be disabled");
                assertTrue(expectedKeys.contains(tb.getText().toString()));
            }
        }
        assertEquals(11, btns);

        // Back button fires callback
        TextButton back = null;
        for (Actor a : root.getChildren()) {
            if (a instanceof TextButton tb && "Back".contentEquals(tb.getText())) {
                back = tb;
                break;
            }
        }
        assertNotNull(back, "Back button present");
        back.fire(new ChangeListener.ChangeEvent());
        assertEquals(1, backCalls.get(), "onBack should be invoked exactly once");
    }

    @Test
    void getZIndex_is_100() {
        GdxGame game = mock(GdxGame.class, withSettings());
        ControlDisplay ui = new ControlDisplay(game, null);
        assertEquals(100f, ui.getZIndex(), 1e-6);
    }
}
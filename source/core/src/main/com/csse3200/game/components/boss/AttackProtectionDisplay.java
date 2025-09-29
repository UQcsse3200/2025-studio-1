package com.csse3200.game.components.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * AttackProtectionDisplay (always gray)
 * - listens to "updateHealth"
 * - each health drop: bar -= 1/6
 * - after 6 hits: wait 2s then restore to full
 * - positioned directly under BossStatusDisplay with the same width
 * visual only
 */
public class AttackProtectionDisplay extends Component {
    // ui (match BossStatusDisplay length = 800f)
    private static final float width = 800f; // same as BossStatusDisplay.BAR_WIDTH
    private static final float height = 16f;
    private static final float gap = 6f;   // vertical space below the boss bar
    private static final Color bg = Color.DARK_GRAY;
    private static final Color fill = Color.GRAY;

    // behavior
    private static final float step = 1f / 6f;
    private static final float delay = 2f;

    private final List<Texture> textures = new ArrayList<>();
    private Table table;
    private ProgressBar bar;

    private int lasthealth = Integer.MIN_VALUE;
    private int hits = 0;
    private float timer = 0f;

    @Override
    public void create() {
        super.create();

        CombatStatsComponent combat = entity.getComponent(CombatStatsComponent.class);
        if (combat != null) lasthealth = combat.getHealth();

        entity.getEvents().addListener("updateHealth", this::onhealth);

        Stage stage = ServiceLocator.getRenderService().getStage();
        if (stage == null) {
            return;
        }

        table = new Table();
        table.setSize(width, height);

        // BossStatusDisplay uses:
        // x = (stageWidth - 800) / 2
        // y = stageHeight - 40 - 270
        float bossbar_y = stage.getHeight() - 40f - 270f;
        float x = (stage.getWidth() - width) / 2f;
        float y = bossbar_y - gap - height; // place directly below

        table.setPosition(x, y);

        ProgressBar.ProgressBarStyle style = createstyle();
        bar = new ProgressBar(0f, 1f, 0.01f, false, style);
        bar.setValue(1f);
        bar.setAnimateDuration(0f);

        table.add(bar).width(width).height(height).pad(5);
        stage.addActor(table);
    }

    private void onhealth(int cur) {
        if (lasthealth == Integer.MIN_VALUE) {
            lasthealth = cur;
            return;
        }

        if (cur < lasthealth) {
            if (timer <= 0f) {
                float next = (bar != null ? bar.getValue() : 1f) - step;
                if (next < 0f) next = 0f;
                setvalue(next);

                hits++;
                if (hits >= 6) {
                    timer = delay;
                }
            }
        }

        lasthealth = cur;
    }

    @Override
    public void update() {
        if (timer > 0f) {
            timer -= ServiceLocator.getTimeSource().getDeltaTime();
            if (timer <= 0f) {
                setvalue(1f);
                hits = 0;
            }
        }
    }

    private ProgressBar.ProgressBarStyle createstyle() {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();

        style.background = makedrawable(bg);
        style.background.setMinHeight(height);

        style.knobBefore = makedrawable(fill);
        style.knobBefore.setMinHeight(height);

        style.knob = null;
        return style;
    }

    private void setvalue(float v) {
        if (bar != null) bar.setValue(v);
    }

    private Drawable makedrawable(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        textures.add(tex);
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bar != null) bar.remove();
        if (table != null) table.remove();
        for (Texture tex : textures) {
            if (tex != null) tex.dispose();
        }
        textures.clear();
    }
}

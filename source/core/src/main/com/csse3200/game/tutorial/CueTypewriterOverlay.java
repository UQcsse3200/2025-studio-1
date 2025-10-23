package com.csse3200.game.tutorial;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.StringBuilder;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

import java.util.List;

/**
 * Centre-screen tutorial overlay that walks the player through a sequence of cues:
 * show prompt &rarr; validate key sequence &rarr; show feedback &rarr; advance.
 *
 * <p><strong>UI upgrades:</strong></p>
 * <ul>
 *   <li>Rounded panel with soft border &amp; drop shadow</li>
 *   <li>Subtle fade/scale-in on show; shake on error</li>
 *   <li>Softer palette (normal/green/red)</li>
 *   <li>Single-line text with ellipsis (no wrapping)</li>
 *   <li>Blinking typewriter caret while typing</li>
 * </ul>
 */
public final class CueTypewriterOverlay extends Component {
    // ────────────────────────── Tunables ──────────────────────────
    private static final float TYPE_INTERVAL = 0.03f;   // seconds per char
    private static final float CARET_INTERVAL = 0.5f;   // seconds toggle caret
    private static final float FEEDBACK_HOLD = 1.2f;    // seconds to hold after feedback
    private static final float DIM_ALPHA = 0.40f;       // background dim amount
    private static final float PANEL_ALPHA = 0.90f;     // panel opacity
    private static final float PAD = 24f;               // panel padding
    private static final float PANEL_CORNER = 16f;      // px corner radius
    private static final float PANEL_BORDER = 2f;       // px border
    private static final float SHADOW_OFFSET = 6f;      // px
    private static final float SHADOW_ALPHA = 0.35f;    // shadow opacity

    private static final String AI_PREFIX = "ClankerAI: ";
    private static final char CARET_CHAR = '█'; // full block
    private final List<Cue> cues;
    // ────────────────────────── Runtime state ──────────────────────────
    private final StringBuilder typed = new StringBuilder(); // efficient typed text buffer
    private final IntSet unlockedKeys = new IntSet();        // keys that stay allowed after being learned
    private final IntSet allowedForGameplay = new IntSet();  // dynamic allow-list: unlocked ∪ current cue
    private Stage stage;
    private Table root;                 // full-screen dim
    private Container<Label> panel;     // centre card
    private Container<Label> shadow;    // drop shadow
    private Label label;
    private InputListener listener;
    // typewriter state
    private String fullText = "";
    private int index = 0;
    private float t = 0f;
    private boolean typing = false;
    private float postHold = 0f;
    // blinking caret state
    private float caretT = 0f;
    private boolean caretOn = false;
    // sequence state
    private int current = 0;
    private int seqPos = 0;           // progress within current cue's sequence
    private boolean showingPrompt = true;

    // panel backgrounds
    private Texture dimTex;
    private Texture panelTexNormal;
    private Texture panelTexGreen;
    private Texture panelTexRed;
    private Texture shadowTex;

    // completion callback
    private Runnable onComplete;

    public CueTypewriterOverlay(List<Cue> cues) {
        this.cues = cues;
    }

    // ────────────────────────── Small texture helpers ──────────────────────────
    private static Texture solid(Color c) {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Rounded card with border.
     */
    private static Texture roundedCard(int w, int h, Color fill, float alpha, float radius, float border, Color borderCol) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        Color f = new Color(fill);
        f.a = alpha;

        // Fill
        pm.setColor(f);
        fillRoundRect(pm, 0, 0, w, h, radius);

        // Border (draw slightly inset so it's crisp)
        if (border > 0f) {
            pm.setColor(new Color(borderCol.r, borderCol.g, borderCol.b, Math.min(1f, borderCol.a)));
            strokeRoundRect(pm, (int) (border / 2f), (int) (border / 2f), w - (int) border, h - (int) border, radius);
        }

        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Soft shadow (rounded, blurred-ish via alpha falloff).
     */
    private static Texture softShadow(int w, int h, float radius, float alpha) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        // central darker
        pm.setColor(new Color(0, 0, 0, alpha));
        fillRoundRect(pm, 0, 0, w, h, radius);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // Rounded rect helpers using circles + rects
    private static void fillRoundRect(Pixmap pm, int x, int y, int w, int h, float r) {
        int ri = Math.round(r);

        // central rect
        pm.fillRectangle(x + ri, y, w - 2 * ri, h);
        pm.fillRectangle(x, y + ri, w, h - 2 * ri);

        // corners
        pm.fillCircle(x + ri, y + ri, ri);
        pm.fillCircle(x + w - ri - 1, y + ri, ri);
        pm.fillCircle(x + ri, y + h - ri - 1, ri);
        pm.fillCircle(x + w - ri - 1, y + h - ri - 1, ri);
    }

    private static void strokeRoundRect(Pixmap pm, int x, int y, int w, int h, float r) {
        int ri = Math.round(r);
        // outline via thin filled rectangles (simple & crisp)
        // top/bottom
        pm.fillRectangle(x + ri, y, w - 2 * ri, 1);
        pm.fillRectangle(x + ri, y + h - 1, w - 2 * ri, 1);
        // left/right
        pm.fillRectangle(x, y + ri, 1, h - 2 * ri);
        pm.fillRectangle(x + w - 1, y + ri, 1, h - 2 * ri);
        // corner arcs (just dots around corners for a light border)
        pm.drawPixel(x + ri, y);
        pm.drawPixel(x, y + ri);
        pm.drawPixel(x + w - ri - 1, y);
        pm.drawPixel(x + w - 1, y + ri);
        pm.drawPixel(x + ri, y + h - 1);
        pm.drawPixel(x, y + h - ri - 1);
        pm.drawPixel(x + w - ri - 1, y + h - 1);
        pm.drawPixel(x + w - 1, y + h - ri - 1);
    }

    // ────────────────────────── Lifecycle ──────────────────────────

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        dimTex = solid(new Color(0, 0, 0, DIM_ALPHA));
        // Softer palette
        Color normal = new Color(0.08f, 0.08f, 0.10f, 1f);
        Color green = new Color(0.06f, 0.38f, 0.20f, 1f);
        Color red = new Color(0.40f, 0.10f, 0.10f, 1f);
        Color border = new Color(1f, 1f, 1f, 0.08f);

        // Generate card & shadow textures at a comfortable size; Scene2D will scale them.
        int baseW = 1024, baseH = 256;
        panelTexNormal = roundedCard(baseW, baseH, normal, PANEL_ALPHA, PANEL_CORNER, PANEL_BORDER, border);
        panelTexGreen = roundedCard(baseW, baseH, green, PANEL_ALPHA, PANEL_CORNER, PANEL_BORDER, border);
        panelTexRed = roundedCard(baseW, baseH, red, PANEL_ALPHA, PANEL_CORNER, PANEL_BORDER, border);
        shadowTex = softShadow(baseW, baseH, PANEL_CORNER + 6f, SHADOW_ALPHA);

        root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(new TextureRegion(dimTex)));
        root.setVisible(false);

        // Label style
        label = new Label("", new Label.LabelStyle(new BitmapFont(), Color.valueOf("F1F1F1")));
        label.setWrap(false);
        label.setEllipsis(true);
        label.setAlignment(Align.center);
        label.setFontScale(1.8f);

        // Card + shadow
        panel = new Container<>(label);
        panel.background(new TextureRegionDrawable(new TextureRegion(panelTexNormal)));
        panel.pad(PAD);

        shadow = new Container<>(new Label("", label.getStyle())); // dummy actor for sizing
        shadow.setBackground(new TextureRegionDrawable(new TextureRegion(shadowTex)));
        shadow.pad(PAD);

        // Responsive width; keep it nice on ultrawide + small screens
        float w = Math.min(960f, stage.getWidth() - 120f);
        // Shadow sits underneath with a slight offset for depth
        root.row();
        root.add(panel).width(w).center();

        stage.addActor(root);

        listener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (!root.isVisible() || cues == null || cues.isEmpty()) return false;
                if (current < 0 || current >= cues.size()) return false;

                Cue c = cues.get(current);
                if (showingPrompt) {
                    if (keycode == c.seq[seqPos]) {
                        // correct step
                        seqPos++;
                        if (seqPos >= c.seq.length) {
                            // sequence done → feedback
                            showFeedback(c);
                        } else {
                            // progress line (no keyName)
                            panel.clearActions();
                            setPanelBg(panelTexNormal);
                            writeInstant(AI_PREFIX + c.prompt + " (" + seqPos + "/" + c.seq.length + ")");
                            refreshAllowListForCue(c);
                        }
                    } else {
                        // wrong → shake + red, reset to start
                        wrongKeyFeedback();
                        seqPos = 0;
                        refreshAllowListForCue(c);
                    }
                }
                return false;
            }
        };
        stage.addListener(listener);

        if (cues != null && !cues.isEmpty()) {
            current = 0;
            showPrompt(cues.get(current));
        }
    }

    @Override
    public void update() {
        if (!root.isVisible()) return;

        float dt = ServiceLocator.getTimeSource().getDeltaTime();

        // Typewriter
        if (typing) {
            t += dt;
            while (t >= TYPE_INTERVAL && index < fullText.length()) {
                t -= TYPE_INTERVAL;
                typed.append(fullText.charAt(index++));
                applyCaretAndSetText();
            }
            if (index >= fullText.length()) {
                typing = false;
                caretOn = false; // hide caret after completing a frame, but keep blink below
                if (!showingPrompt) postHold = FEEDBACK_HOLD;
            }
        } else if (postHold > 0f) {
            postHold -= dt;
            if (postHold <= 0f) advance();
        }

        // Blinking caret while typing
        if (typing) {
            caretT += dt;
            if (caretT >= CARET_INTERVAL) {
                caretT = 0f;
                caretOn = !caretOn;
                applyCaretAndSetText();
            }
        }
    }

    @Override
    public void dispose() {
        if (stage != null && listener != null) stage.removeListener(listener);
        if (root != null) root.remove();
        if (dimTex != null) dimTex.dispose();
        if (panelTexNormal != null) panelTexNormal.dispose();
        if (panelTexGreen != null) panelTexGreen.dispose();
        if (panelTexRed != null) panelTexRed.dispose();
        if (shadowTex != null) shadowTex.dispose();
    }

    // ────────────────────────── Public API ──────────────────────────

    /**
     * Allow only ESC, F1, and the current allow-list (unlocked ∪ current cue) to reach gameplay.
     */
    public boolean allowGameplayKey(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.F1) return true;
        return allowedForGameplay.contains(keycode);
    }

    /**
     * Optional: called when the last cue completes.
     */
    public void setOnComplete(Runnable r) {
        this.onComplete = r;
    }

    /**
     * Optionally “replay” a key press (e.g., if SPACE also started the tutorial).
     */
    public void injectKey(int keycode) {
        if (cues == null || cues.isEmpty()) return;
        if (current < 0 || current >= cues.size()) return;
        if (!root.isVisible()) root.setVisible(true);

        Cue c = cues.get(current);
        if (showingPrompt) {
            if (keycode == c.seq[seqPos]) {
                seqPos++;
                if (seqPos >= c.seq.length) {
                    showFeedback(c);
                } else {
                    panel.clearActions();
                    setPanelBg(panelTexNormal);
                    writeInstant(AI_PREFIX + c.prompt +
                            " (" + seqPos + "/" + c.seq.length + ")");
                    refreshAllowListForCue(c);
                }
            } else {
                wrongKeyFeedback();
                seqPos = 0;
                refreshAllowListForCue(c);
            }
        }
    }

    // ────────────────────────── Internals ──────────────────────────

    private void showPrompt(Cue cue) {
        showingPrompt = true;
        seqPos = 0;
        panel.clearActions();
        setPanelBg(panelTexNormal);
        refreshAllowListForCue(cue);

        String base = AI_PREFIX + cue.prompt +
                (cue.seq.length > 1 ? " (0/" + cue.seq.length + ")" : "");
        startType(base, true);

        // Nice entrance
        panel.setTransform(true);
        panel.setScale(0.96f);
        panel.getColor().a = 0f;
        shadow.setScale(0.96f);
        shadow.getColor().a = 0f;

        panel.addAction(Actions.parallel(
                Actions.fadeIn(0.20f),
                Actions.scaleTo(1f, 1f, 0.18f)
        ));
        shadow.addAction(Actions.parallel(
                Actions.fadeIn(0.20f),
                Actions.scaleTo(1f, 1f, 0.18f)
        ));
    }

    private void showFeedback(Cue cue) {
        showingPrompt = false;
        panel.clearActions();
        setPanelBg(panelTexGreen);

        // Persist newly learned keys so they stay allowed after this cue
        for (int code : cue.seq) unlockedKeys.add(code);
        // During feedback, keep ALL unlocked keys allowed (movement continues)
        allowedForGameplay.clear();
        for (IntSet.IntSetIterator it = unlockedKeys.iterator(); it.hasNext; ) {
            allowedForGameplay.add(it.next());
        }

        startType(AI_PREFIX + cue.feedback, false);
    }

    private void wrongKeyFeedback() {
        setPanelBg(panelTexRed);
        panel.clearActions();

        Cue cur = cues.get(current);
        writeInstant(AI_PREFIX + "Oops! wrong key for " + cur.keyName +
                (cur.seq.length > 1 ? " (0/" + cur.seq.length + ")" : "") + ".");

        typing = false;
        postHold = 0f;

        float d = 10f;
        float dur = 0.05f;
        panel.addAction(Actions.sequence(
                Actions.moveBy(d, 0, dur),
                Actions.moveBy(-2 * d, 0, dur),
                Actions.moveBy(2 * d, 0, dur),
                Actions.moveBy(-d, 0, dur),
                Actions.run(() -> {
                    if (showingPrompt) {
                        setPanelBg(panelTexNormal);
                        writeInstant(AI_PREFIX + cur.prompt +
                                (cur.seq.length > 1 ? " (0/" + cur.seq.length + ")" : ""));
                    }
                })
        ));
    }

    private void advance() {
        current++;
        if (current >= cues.size()) {
            // Soft fade out
            panel.addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.fadeOut(0.18f),
                            Actions.scaleTo(0.98f, 0.98f, 0.18f)
                    ),
                    Actions.run(() -> {
                        root.setVisible(false);
                        if (onComplete != null) onComplete.run();
                    })
            ));
            shadow.addAction(Actions.fadeOut(0.18f));
            return;
        }
        seqPos = 0;
        showPrompt(cues.get(current));
    }

    private void startType(String text, boolean ensureVisible) {
        fullText = (text != null) ? text : "";
        index = 0;
        t = 0f;
        typing = true;
        postHold = 0f;
        caretT = 0f;
        caretOn = false;
        typed.setLength(0);
        applyCaretAndSetText();
        if (ensureVisible && !root.isVisible()) root.setVisible(true);
    }

    private void writeInstant(String text) {
        typing = false;
        fullText = text != null ? text : "";
        typed.setLength(0);
        typed.append(fullText);
        caretOn = false;
        label.setText(typed);
    }

    private void applyCaretAndSetText() {
        // Render typed text + optional blinking caret (only while typing)
        if (typing) {
            label.setText(typed.toString() + (caretOn ? CARET_CHAR : ""));
        } else {
            label.setText(typed);
        }
    }

    private void setPanelBg(Texture tex) {
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(tex)));
    }

    /**
     * Refresh allowedForGameplay = unlockedKeys ∪ current cue keys.
     */
    private void refreshAllowListForCue(Cue cue) {
        allowedForGameplay.clear();
        for (IntSet.IntSetIterator it = unlockedKeys.iterator(); it.hasNext; ) {
            allowedForGameplay.add(it.next());
        }
        for (int code : cue.seq) allowedForGameplay.add(code);
    }

    // ────────────────────────── Data ──────────────────────────
    public static final class Cue {
        /**
         * Expected key sequence (e.g., {SPACE, SPACE} for double-press).
         */
        public final int[] seq;
        /**
         * For reference/logging (not shown in the prompt).
         */
        public final String keyName;
        /**
         * Prompt text.
         */
        public final String prompt;
        /**
         * Feedback text.
         */
        public final String feedback;

        public Cue(int[] seq, String keyName, String prompt, String feedback) {
            this.seq = seq;
            this.keyName = keyName;
            this.prompt = prompt;
            this.feedback = feedback;
        }
    }
}

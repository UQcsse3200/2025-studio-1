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
 * show prompt → validate key sequence → show feedback → advance.
 * <p>
 * Behaviour:
 * - Correct key(s): panel tints GREEN and types feedback, then advances to next cue.
 * - Wrong key: panel shakes RED briefly and stays on the same prompt (progress resets).
 * - Only one cue is active at a time.
 * - Dynamic input allow-list: ESC, F1, and (unlocked keys ∪ current cue keys).
 */
public final class CueTypewriterOverlay extends Component {
    // ────────────────────────── Tunables ──────────────────────────
    private static final float TYPE_INTERVAL = 0.03f;  // seconds per char
    private static final float FEEDBACK_HOLD = 1.2f;   // seconds to hold after feedback
    private static final float DIM_ALPHA = 0.35f;  // background dim amount
    private static final float PANEL_ALPHA = 0.85f;  // panel opacity
    private static final float PAD = 22f;    // panel padding
    private static final String AI_PREFIX = "ClankerAI: ";
    private final List<Cue> cues;
    // ────────────────────────── Runtime state ──────────────────────────
    private final StringBuilder typed = new StringBuilder(); // efficient typed text buffer
    private final IntSet unlockedKeys = new IntSet();        // keys that stay allowed after being learned
    private final IntSet allowedForGameplay = new IntSet();  // dynamic allow-list: unlocked ∪ current cue
    private Stage stage;
    private Table root;               // full-screen dim
    private Container<Label> panel;   // centred panel
    private Label label;
    private InputListener listener;
    // typewriter state
    private String fullText = "";
    private int index = 0;
    private float t = 0f;
    private boolean typing = false;
    private float postHold = 0f;
    // sequence state
    private int current = 0;
    private int seqPos = 0;           // progress within current cue's sequence
    private boolean showingPrompt = true;
    // panel backgrounds
    private Texture dimTex;
    private Texture panelTexNormal;
    private Texture panelTexGreen;
    private Texture panelTexRed;
    // completion callback
    private Runnable onComplete;

    public CueTypewriterOverlay(List<Cue> cues) {
        this.cues = cues;
    }

    private static Texture solid(Color c) {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // ────────────────────────── Lifecycle ──────────────────────────

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        dimTex = solid(new Color(0, 0, 0, DIM_ALPHA));
        panelTexNormal = solid(new Color(0, 0, 0, PANEL_ALPHA));
        panelTexGreen = solid(new Color(0f, 0.5f, 0f, PANEL_ALPHA));
        panelTexRed = solid(new Color(0.5f, 0f, 0f, PANEL_ALPHA));

        root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(new TextureRegion(dimTex)));
        root.setVisible(false);

        label = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        label.setWrap(false);
        label.setAlignment(Align.center);
        label.setFontScale(1.8f);

        panel = new Container<>(label);
        panel.background(new TextureRegionDrawable(new TextureRegion(panelTexNormal)));
        panel.pad(PAD);

        float w = Math.min(900f, stage.getWidth() - 64f);
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
                            label.setText(AI_PREFIX + c.prompt + " (" + seqPos + "/" + c.seq.length + ")");
                            // while progressing this cue, keep allowed set = unlocked ∪ current cue
                            refreshAllowListForCue(c);
                        }
                    } else {
                        // wrong → shake + red, reset to start
                        wrongKeyFeedback();
                        seqPos = 0;
                        // restore allow-list for this cue step 0
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
        if (typing) {
            t += dt;
            while (t >= TYPE_INTERVAL && index < fullText.length()) {
                t -= TYPE_INTERVAL;
                typed.append(fullText.charAt(index++));
                label.setText(typed);
            }
            if (index >= fullText.length()) {
                typing = false;
                if (!showingPrompt) postHold = FEEDBACK_HOLD;
            }
        } else if (postHold > 0f) {
            postHold -= dt;
            if (postHold <= 0f) advance();
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
                    label.setText(AI_PREFIX + c.prompt +
                            (c.seq.length > 1 ? " (" + seqPos + "/" + c.seq.length + ")" : ""));
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

        // allow-list: unlocked keys from prior cues + current cue keys
        refreshAllowListForCue(cue);

        String base = AI_PREFIX + cue.prompt +
                (cue.seq.length > 1 ? " (0/" + cue.seq.length + ")" : "");
        startType(base, true);
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
        label.setText(AI_PREFIX + "ew! why did your press " + cur.keyName +
                (cur.seq.length > 1 ? " (0/" + cur.seq.length + ")" : "") + "?");

        typing = false;
        postHold = 0f;

        float d = 8f;
        float dur = 0.05f;
        panel.addAction(Actions.sequence(
                Actions.moveBy(d, 0, dur),
                Actions.moveBy(-2 * d, 0, dur),
                Actions.moveBy(2 * d, 0, dur),
                Actions.moveBy(-d, 0, dur),
                Actions.run(() -> {
                    if (showingPrompt) {
                        setPanelBg(panelTexNormal);
                        label.setText(AI_PREFIX + cur.prompt +
                                (cur.seq.length > 1 ? " (0/" + cur.seq.length + ")" : ""));
                    }
                })
        ));
    }

    private void advance() {
        current++;
        if (current >= cues.size()) {
            root.setVisible(false);
            if (onComplete != null) onComplete.run();
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
        typed.setLength(0);
        label.setText(typed);
        if (ensureVisible && !root.isVisible()) root.setVisible(true);
    }

    private void setPanelBg(Texture tex) {
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(tex)));
    }

    /**
     * Refresh allowedForGameplay = unlockedKeys ∪ current cue keys.
     */
    private void refreshAllowListForCue(Cue cue) {
        allowedForGameplay.clear();
        // keep everything learned so far
        for (IntSet.IntSetIterator it = unlockedKeys.iterator(); it.hasNext; ) {
            allowedForGameplay.add(it.next());
        }
        // plus current cue keys
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

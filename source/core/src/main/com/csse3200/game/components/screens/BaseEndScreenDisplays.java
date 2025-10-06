package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.GdxGame;
import com.csse3200.game.ui.effects.TextEffects;

import java.util.Locale;
import java.util.function.Supplier;

public class BaseEndScreenDisplays extends BaseScreenDisplay {
    public static final String DEFEATED_STR = "DEFEATED";
    private final String titleText;
    private final Color titleColor;
    private final String primaryText;
    private final Runnable primaryAction;
    private final String secondaryText;
    private final Runnable secondaryAction;
    private final Supplier<String> subtitleSupplier;

    private final TextEffects subtitleFx = new TextEffects();
    private final TextEffects titleFxPhase = new TextEffects();
    private final TextEffects titleFxLoop = new TextEffects();
    private Timer.Task titleFollowupTask;

    private Label roundLabelRef;
    private Label timeLabelRef;
    private Runnable leaderboardAction;

    protected BaseEndScreenDisplays(
            GdxGame game,
            String titleText,
            Color titleColor,
            String primaryText,
            Runnable primaryAction,
            Runnable secondaryAction
    ) {
        this(game, titleText, titleColor, primaryText, primaryAction, secondaryAction, null);
    }

    protected BaseEndScreenDisplays(
            GdxGame game,
            String titleText,
            Color titleColor,
            String primaryText,
            Runnable primaryAction,
            Runnable secondaryAction,
            Supplier<String> subtitleSupplier
    ) {
        super(game);
        this.titleText = titleText;
        this.titleColor = titleColor;
        this.primaryText = primaryText;
        this.primaryAction = primaryAction;
        this.secondaryText = "Main Menu";
        this.secondaryAction = (secondaryAction != null) ? secondaryAction : this::backMainMenu;
        this.subtitleSupplier = subtitleSupplier;
    }

    public static BaseEndScreenDisplays victory(GdxGame game) {
        return new BaseEndScreenDisplays(
                game, "Victory", new Color(0f, 1f, 0f, 1f), "Continue",
                () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME),
                null,
                () -> TextEffects.readRandomLine("text/victoryprompt.txt", "You did it!")
        );
    }

    public static BaseEndScreenDisplays defeated(GdxGame game) {
        return new BaseEndScreenDisplays(
                game, DEFEATED_STR, new Color(1f, 0f, 0f, 1f), "Try Again",
                () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME),
                null,
                () -> TextEffects.readRandomLine("text/deathprompt.txt", "Death is only the beginning.")
        );
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String toMMSS(long totalSeconds) {
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", m, s);
    }

    // Ensure there is no parent/label tint multiplying our markup colours.
    private static void neutralizeTint(Actor label, Actor wrapper, Table root) {
        if (root != null) root.setColor(Color.WHITE);
        if (wrapper != null) wrapper.setColor(Color.WHITE);
        if (label != null) ((Label) label).setColor(Color.WHITE);
    }

    private static void at(float delay, Runnable r) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                r.run();
            }
        }, delay);
    }

    private static void neutralizeTintDeep(Label lbl, Actor... ancestors) {
        // Wipe actor tint
        lbl.setColor(1f, 1f, 1f, 1f);

        // Copy style so we don't globally change the Skin
        Label.LabelStyle s = lbl.getStyle();
        if (s != null) {
            Label.LabelStyle copy = new Label.LabelStyle(s);
            if (copy.fontColor == null) copy.fontColor = new Color(Color.WHITE);
            else copy.fontColor.set(Color.WHITE);
            if (copy.fontColor != null) copy.fontColor.set(Color.WHITE);
            lbl.setStyle(copy);
            if (copy.font != null && copy.font.getData() != null) {
                copy.font.getData().markupEnabled = true;
            }
        }

        // Parent tint multiplies too; make sure they're white
        if (ancestors != null) {
            for (Actor a : ancestors) {
                if (a != null) a.setColor(1f, 1f, 1f, 1f);
            }
        }
    }

    /**
     * Allow chaining a Leaderboard button
     */
    public BaseEndScreenDisplays withLeaderboard(Runnable leaderboardAction) {
        this.leaderboardAction = leaderboardAction;
        return this;
    }

    @Override
    protected void buildUI(Table root) {
        // Title via addTitle(...), then wrap so we can transform the wrapper.
        Label titleLbl = addTitle(root, titleText, titleFontScale(), titleColor, 0f);
        TextEffects.enableMarkup(titleLbl);
        titleLbl.remove();

        Container<Label> titleWrap = new Container<>(titleLbl);
        titleWrap.setTransform(true);
        titleWrap.setOrigin(Align.center);
        titleWrap.fill();

        // Add wrapped title back
        root.add(titleWrap)
                .colspan(2)
                .center()
                .width(Value.percentWidth(0.9f, root))
                .padBottom(blockPad());
        root.row();

        // Neutralise any tint from the start
        neutralizeTint(titleLbl, titleWrap, root);

        // === Title FX ===
        if (DEFEATED_STR.equalsIgnoreCase(titleText)) {
            titleWrap.setScale(0.85f);
            titleLbl.setColor(new Color(0.9f, 0.2f, 0.2f, 1f));
            titleWrap.addAction(Actions.sequence(
                    Actions.scaleTo(1.18f, 1.18f, 0.16f, Interpolation.swingOut),
                    Actions.scaleTo(1.00f, 1.00f, 0.08f, Interpolation.sine)
            ));

            titleFxPhase.crazyOrType(
                    titleLbl,
                    "{CRAZY style=blast origin=middle spread=4 fps=95 cycles=2 jitter=6 " +
                            "rainbow=true rhz=1.3 rshift=28 flash=3 overshoot=1 edgeboost=0.6 " +
                            "flashhexa=ffffff flashhexb=ffe066}DEFEATED{/CRAZY}",
                    28f
            );

            final float glitchDelay = 0.22f;
            final float glitchDur = 0.55f;
            titleFollowupTask = Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    titleFxPhase.glitchReveal(titleLbl, DEFEATED_STR, glitchDur);
                }
            }, glitchDelay);

            final float totalShake = 0.9f;
            final float step = 0.045f;
            final float amp = 7f;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    neutralizeTint(titleLbl, titleWrap, root);
                    titleFxLoop.strobe(titleLbl, "ff0000", "000000", 7.5f, totalShake);

                    int steps = Math.max(1, (int) (totalShake / step));
                    Action seq = Actions.sequence();
                    for (int i = 0; i < steps; i++) {
                        float dx = (new java.security.SecureRandom().nextFloat() * 2f - 1f) * amp;
                        float dy = (new java.security.SecureRandom().nextFloat() * 2f - 1f) * amp;
                        float rot = (new java.security.SecureRandom().nextFloat() * 2f - 1f) * 1.2f;
                        seq = Actions.sequence(seq,
                                Actions.parallel(
                                        Actions.moveBy(dx, dy, step, Interpolation.sine),
                                        Actions.rotateBy(rot, step, Interpolation.sine)
                                )
                        );
                    }
                    seq = Actions.sequence(seq,
                            Actions.parallel(
                                    Actions.rotateTo(0f, 0.06f, Interpolation.sine),
                                    Actions.scaleTo(1.00f, 1.00f, 0.06f, Interpolation.sine)
                            )
                    );
                    titleWrap.clearActions();
                    titleWrap.addAction(seq);

                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            neutralizeTint(titleLbl, titleWrap, root);
                            titleFxLoop.pulseBetween(titleLbl, "7a0000", "b03a3a", 0.18f);
                        }
                    }, totalShake);
                }
            }, glitchDelay + glitchDur + 0.12f);

        } else if ("Victory".equalsIgnoreCase(titleText)) {
            titleWrap.setScale(0.70f);

            // Start from a fully neutral color pipeline
            neutralizeTintDeep(titleLbl, titleWrap, root); // see helper below

            titleWrap.addAction(Actions.sequence(
                    Actions.scaleTo(1.38f, 1.38f, 0.18f, Interpolation.swingOut),
                    Actions.scaleTo(1.08f, 1.08f, 0.10f, Interpolation.sine)
            ));

            // Phase A: blast intro (can use rainbow, jitter, etc.)
            titleFxPhase.crazyOrType(
                    titleLbl,
                    "{CRAZY style=blast origin=middle spread=3 fps=130 cycles=2 jitter=3 " +
                            "rainbow=true rhz=1.4 rshift=26 flash=4 overshoot=2 edgeboost=0.8 " +
                            "flashhexa=ffffff flashhexb=fff0a6}VICTORY{/CRAZY}",
                    30f
            );

            // Single timeline instead of nesting; we re-neutralize before each FX
            final float phaseAEnd = 1.10f;
            float t = phaseAEnd;

            Runnable resetForFx = () -> {
                neutralizeTintDeep(titleLbl, titleWrap, root);
                titleLbl.setText("VICTORY");
                TextEffects.enableMarkup(titleLbl); // ensures font markup is on
            };

            at(t, () -> {
                titleFxPhase.cancel();   // stop intro anim cleanly
                resetForFx.run();
                // GREEN ↔ WHITE thunderclap
                titleFxLoop.strobeDirect(titleLbl, Color.valueOf("00ff00"), Color.WHITE, 12f, 0.28f);
            });

            t += 0.06f;
            at(t, () -> {
                resetForFx.run();
                titleFxLoop.sparkle(titleLbl, 0.65f, 14f, 1.30f);
            });

            t += 0.70f;
            at(t, () -> {
                resetForFx.run();
                titleFxLoop.pulseBetween(titleLbl, "ffd54f", "fff6cc", 1.1f);
            });

            t += 0.85f;
            at(t, () -> {
                resetForFx.run();
                titleFxLoop.sweepRainbow(titleLbl, 0.70f, 22f, 0.26f);

                // Gentle idle motion
                titleWrap.clearActions();
                titleWrap.addAction(Actions.forever(Actions.sequence(
                        Actions.parallel(
                                Actions.scaleTo(1.12f, 1.12f, 0.40f, Interpolation.sine),
                                Actions.rotateTo(2.0f, 0.40f, Interpolation.sine)
                        ),
                        Actions.parallel(
                                Actions.scaleTo(1.04f, 1.04f, 0.40f, Interpolation.sine),
                                Actions.rotateTo(-1.5f, 0.40f, Interpolation.sine)
                        ),
                        Actions.parallel(
                                Actions.scaleTo(1.08f, 1.08f, 0.32f, Interpolation.sine),
                                Actions.rotateTo(0.0f, 0.32f, Interpolation.sine)
                        )
                )));
            });
        }

        // Subtitle
        if (subtitleSupplier != null) {
            String sub = safeTrim(subtitleSupplier.get());
            if (!sub.isEmpty()) {
                Label subLbl = addBody(root, "", infoFontScale() * 0.9f, blockPad() * 0.5f);
                subLbl.setColor(Color.LIGHT_GRAY);
                TextEffects.enableMarkup(subLbl);
                subtitleFx.crazyOrType(subLbl, sub, 28f);
            }
        }

        // Round + Time
        roundLabelRef = addBody(root, "Round: 1", infoFontScale(), blockPad());
        timeLabelRef = addBody(root, "Time: 00:00", infoFontScale(), blockPad());

        // Buttons
        Table row = new Table();
        TextButton primary = button(primaryText, buttonLabelScale(), primaryAction);   // Continue
        TextButton secondary = button(secondaryText, buttonLabelScale(), secondaryAction); // Main Menu
        row.add(primary).left().padRight(buttonsGap());
        row.add(secondary).left();

        root.add(row)
                .colspan(2)
                .center()
                .padTop(blockPad())
                .row();

        pinLeaderboardTopRight(root);
    }

    private void pinLeaderboardTopRight(Table root) {
        if (leaderboardAction == null) return;

        TextButton leaderboardTop = button("Leaderboard", buttonLabelScale(), leaderboardAction);

        // Overlay anchored to screen via fillParent
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().right();
        overlay.add(leaderboardTop).padTop(16f).padRight(16f);

        // Add as an actor so it doesn't participate in root's grid layout
        root.addActor(overlay);
        overlay.toFront();
    }

    public void setRound(int round) {
        if (roundLabelRef != null) roundLabelRef.setText("Round: " + Math.max(1, round));
    }

    public void setElapsedSeconds(long seconds) {
        if (timeLabelRef != null) timeLabelRef.setText("Time: " + toMMSS(Math.max(0, seconds)));
    }

    public void setElapsedText(String mmss) {
        if (timeLabelRef != null) timeLabelRef.setText("Time: " + mmss);
    }

    @Override
    public void dispose() {
        super.dispose();
        subtitleFx.cancel();
        titleFxPhase.cancel();
        titleFxLoop.cancel();
        if (titleFollowupTask != null) {
            titleFollowupTask.cancel();
            titleFollowupTask = null;
        }
    }

    protected float titleFontScale() {
        return 3.0f;
    }

    protected float infoFontScale() {
        return 3.0f;
    }

    protected float buttonLabelScale() {
        return 2.0f;
    }

    protected float buttonsGap() {
        return 30f;
    }

    protected float blockPad() {
        return 50f;
    }
}

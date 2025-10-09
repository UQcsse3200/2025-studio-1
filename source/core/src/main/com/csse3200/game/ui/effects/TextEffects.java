package com.csse3200.game.ui.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TextEffects
 * ----------
 * Reusable label text effects and helpers:
 * - Typewriter (basic + punctuation-aware smart pauses)
 * - CRAZY block animation: {CRAZY [key=value ...]}text{/CRAZY}
 * Supported opts: fps, jitter, cycles, from=rand|A, rainbow, rhz, rshift,
 * style=normal|explode|blast, origin=left|middle|right,
 * spread=[int], flash=[frames], overshoot=[hops],
 * edgeboost=0..1, flashhexa, flashhexb
 * - Rainbow per-char effects (sweep), full-label pulse, strobe, sparkle
 * - Random-line reader from internal files
 * - Word-by-word reveal (no lookbehind)
 * - Blinking caret, backspace/erase, glitch/decoder, marquee, flash-highlight
 * <p>
 * Owns a single Timer.Task so only one effect runs per instance at a time.
 * IMPORTANT: All colorizing effects rebuild from plain text to avoid nested markup.
 */
public class TextEffects {
    public static final String FFE_066 = "ffe066";
    public static final String FFFFFF = "ffffff";
    private static final Logger log = LoggerFactory.getLogger(TextEffects.class);
    // Punctuation wheel for CRAZY
    private static final char[] PUNCT_RING = (
            ".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`"
    ).toCharArray();
    private static final Random RNG = new SecureRandom();
    private static final String GLITCH_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#%$*+-_=<>/?";
    // Cached sources (set via refreshBases / crazyOrType)
    private String baseMarked = "";
    private String basePlain = "";
    // ---------- Public helpers ----------
    // Single running task per instance
    private Timer.Task task;

    /**
     * Read a random non-empty, non-comment line from an internal file; return fallback on error/empty.
     */
    public static String readRandomLine(String internalPath, String fallback) {
        try {
            FileHandle fh = Gdx.files.internal(internalPath);
            if (!fh.exists()) return fallback;
            String raw = fh.readString(StandardCharsets.UTF_8.name());
            String[] lines = raw.split("\r?\n");
            List<String> pool = new ArrayList<>(lines.length);
            for (String line : lines) {
                String t = line.trim();
                if (!t.isEmpty() && !t.startsWith("#")) pool.add(t);
            }
            if (pool.isEmpty()) return fallback;
            int idx = RNG.nextInt(pool.size());
            return pool.get(idx);
        } catch (Exception e) {
            return fallback;
        }
    }

    // ---------- Internal base management ----------

    /**
     * Ensure the font backing this label parses LibGDX color markup.
     */
    public static void enableMarkup(Label label) {
        Label.LabelStyle style = label.getStyle();
        if (style != null && style.font != null && style.font.getData() != null) {
            style.font.getData().markupEnabled = true;
        }
    }

    /**
     * Strip LibGDX color markup; preserve literal '[' via "[[" escaping.
     */
    private static String stripMarkup(String s) {
        if (s == null || s.isEmpty()) return "";
        final String ESC = "\u0001";         // sentinel to preserve "[["
        s = s.replace("[[", ESC);
        s = s.replaceAll("\\[#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})]", ""); // open tags
        s = s.replace("[]", "");                                        // close tags
        return s.replace(ESC, "[");
    }

    private static List<Piece> parseCrazyPieces(String s) {
        final int n = s.length();
        int idx = 0;
        List<Piece> out = new ArrayList<>();
        while (idx < n) {
            idx = processCrazyChunk(s, idx, out);
        }
        return out;
    }

    private static int processCrazyChunk(String s, int idx, List<Piece> out) {
        final String OPEN_PREFIX = "{CRAZY";
        final String CLOSE = "{/CRAZY}";
        final int n = s.length();

        int a = s.indexOf(OPEN_PREFIX, idx);
        if (a < 0) {                    // no more opens: rest is plain
            if (idx < n) out.add(Piece.plain(s.substring(idx)));
            return n;
        }

        if (a > idx) {                  // emit leading plain
            out.add(Piece.plain(s.substring(idx, a)));
        }

        int tagEnd = s.indexOf('}', a + OPEN_PREFIX.length());
        if (tagEnd < 0) {               // malformed open (no '}'): rest plain
            out.add(Piece.plain(s.substring(a)));
            return n;
        }

        String tagInside = s.substring(a + OPEN_PREFIX.length(), tagEnd).trim();
        int b = s.indexOf(CLOSE, tagEnd + 1);
        if (b < 0) {                    // missing close: rest plain
            out.add(Piece.plain(s.substring(a)));
            return n;
        }

        String inner = s.substring(tagEnd + 1, b);
        CrazyOpts opts = parseOpts(tagInside);
        out.add(Piece.crazy(inner, opts));
        return b + CLOSE.length();      // continue after close
    }


    private static CrazyOpts parseOpts(String optStr) {
        CrazyOpts o = new CrazyOpts();
        if (optStr == null || optStr.isEmpty()) return o;
        String[] parts = optStr.split("\\s+");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            if (kv.length != 2) continue;
            String k = kv[0].toLowerCase(Locale.ROOT).trim();
            String v = kv[1].toLowerCase(Locale.ROOT).trim();
            try {
                switch (k) {
                    case "fps" -> o.fps = Math.clamp(Integer.parseInt(v), 1, 240);
                    case "jitter" -> o.jitter = Math.clamp(Integer.parseInt(v), 0, 60);
                    case "cycles" -> o.cycles = Math.clamp(Integer.parseInt(v), 0, 10);
                    case "from" -> o.from = ("rand".equals(v) ? CrazyOpts.From.RAND : CrazyOpts.From.A);
                    case "rainbow" -> o.rainbow = ("true".equals(v) || "1".equals(v));
                    case "rhz" -> o.rhz = Math.clamp(Float.parseFloat(v), 0.01f, 5f);
                    case "rshift" -> o.rshift = Math.clamp(Float.parseFloat(v), 0f, 360f);
                    case "style" -> o.style = switch (v) {
                        case "explode" -> CrazyOpts.Style.EXPLODE;
                        case "blast" -> CrazyOpts.Style.BLAST;
                        default -> CrazyOpts.Style.NORMAL;
                    };
                    case "origin" -> o.origin = switch (v) {
                        case "left" -> CrazyOpts.Origin.LEFT;
                        case "right" -> CrazyOpts.Origin.RIGHT;
                        default -> CrazyOpts.Origin.MIDDLE;
                    };
                    case "spread" -> o.spread = Math.max(0, Integer.parseInt(v));
                    case "flash" -> o.flashFrames = Math.max(0, Integer.parseInt(v));
                    case "overshoot" -> o.overshoot = Math.max(0, Integer.parseInt(v));
                    case "edgeboost" -> o.edgeBoost = Math.clamp(Float.parseFloat(v), 0f, 1f);
                    case "flashhexa" -> o.flashHexA = v.replace("#", "");
                    case "flashhexb" -> o.flashHexB = v.replace("#", "");
                    default -> { /* ignore unknown keys to keep robustness */ }
                }
            } catch (NumberFormatException e) {
                log.warn("Bad opt value for key '{}' = '{}'", k, v, e);
            }
        }
        return o;
    }

    private static String joinPiecesStatic(List<Piece> pieces) {
        StringBuilder sb = new StringBuilder();
        for (Piece p : pieces) sb.append(p.text);
        return sb.toString();
    }

    private static boolean isUpper(char c) {
        return (c >= 'A' && c <= 'Z');
    }

    private static boolean isLetter(char c) {
        return isUpper(c) || (c >= 'a' && c <= 'z');
    }

    private static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private static boolean isPunctGlobal(char c) {
        if (c == ' ') return false;
        for (char p : PUNCT_RING) if (p == c) return true;
        return false;
    }

    // ---------- Color helpers ----------

    private static int hsvToRgb(float hDeg, float s, float v) {
        float h = (hDeg % 360f + 360f) % 360f / 60f;
        int i = (int) Math.floor(h);
        float f = h - i;
        float p = v * (1f - s);
        float q = v * (1f - s * f);
        float t = v * (1f - s * (1f - f));
        float r;
        float g;
        float b;
        switch (i) {
            case 0 -> {
                r = v;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = v;
                b = p;
            }
            case 2 -> {
                r = p;
                g = v;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = v;
            }
            case 4 -> {
                r = t;
                g = p;
                b = v;
            }
            default -> {
                r = v;
                g = p;
                b = q;
            }
        }
        int rA = (int) (r * 255.0f + 0.5f);
        int gA = (int) (g * 255.0f + 0.5f);
        int bA = (int) (b * 255.0f + 0.5f);
        return (rA << 16) | (gA << 8) | bA;
    }

    private static String toHex6(int rgb) {
        String s = Integer.toHexString(rgb).toLowerCase(Locale.ROOT);
        int pad = 6 - s.length();
        if (pad <= 0) return s.substring(s.length() - 6);
        return "0".repeat(pad) + s;
    }

    // ---------- Core effects (plain-text driven) ----------

    /**
     * Builds a BlockAnim for one CRAZY piece, with EXPLODE/BLAST timing.
     */
    private static BlockAnim buildBlock(String text, CrazyOpts opts) {
        final char[] target = (text == null ? "" : text).toCharArray();
        final int n = target.length;
        final char[] curr = new char[n];
        final int[] delays = new int[n];
        final int[] remaining = new int[n];

        final boolean isBlast = (opts.style == CrazyOpts.Style.BLAST);
        final boolean isExplode = (opts.style == CrazyOpts.Style.EXPLODE);

        // BLAST arrays (null when not used)
        final int[] flashLeft = isBlast ? new int[n] : null;
        final int[] overshootLeft = isBlast ? new int[n] : null;
        final int[] postLockHold = isBlast ? new int[n] : null;

        final int originIdx = originIndex(n, opts.origin);
        final float invEdgeSpan = inverseEdgeSpan(n, originIdx);
        final boolean useDistDelay = isExplode || isBlast;

        for (int i = 0; i < n; i++) {
            final char t = target[i];

            // 1) Delay (jitter + optional distance spread)
            delays[i] = baseDelay(i, originIdx, useDistDelay, opts.jitter, opts.spread);

            // 2) Base cycles + optional BLAST edge boost
            int extraCycles = opts.cycles + edgeBoostCycles(isBlast, n, i, originIdx, invEdgeSpan, t, opts.edgeBoost);

            // 3) Seed current glyph + remaining steps
            initSeedState(curr, remaining, i, t, extraCycles, opts);

            // 4) BLAST extras per-char
            initBlastAt(isBlast, flashLeft, overshootLeft, postLockHold, i, opts.flashFrames, opts.overshoot);
        }

        BlockAnim.Visual vis = new BlockAnim.Visual(
                opts.rainbow,
                opts.rhz,
                opts.rshift,
                sanitizeHex(opts.flashHexA, FFFFFF),
                sanitizeHex(opts.flashHexB, FFE_066)
        );

        BlockAnim.BlastExtras blast = isBlast
                ? new BlockAnim.BlastExtras(flashLeft, overshootLeft, postLockHold)
                : null;

        return new BlockAnim(
                Math.max(1, opts.fps),
                delays,
                remaining,
                target,
                curr,
                vis,
                blast
        );
    }

    private static int originIndex(int n, CrazyOpts.Origin origin) {
        return switch (origin) {
            case LEFT -> 0;
            case RIGHT -> Math.max(0, n - 1);
            case MIDDLE -> Math.max(0, (n - 1) / 2);
        };
    }

    private static float inverseEdgeSpan(int n, int originIdx) {
        int edgeSpan = Math.max(originIdx, (n - 1) - originIdx);
        return edgeSpan > 0 ? 1f / edgeSpan : 0f;
    }

    private static int baseDelay(int i, int originIdx, boolean distanceDelay, int jitter, int spread) {
        int d = RNG.nextInt(0, Math.max(1, jitter)); // 0..jitter-1
        if (distanceDelay) {
            int dist = Math.abs(i - originIdx);
            d += dist * Math.max(0, spread);
        }
        return d;
    }

    private static int glyphSpan(char t) {
        if (isLetter(t)) return 26;
        if (isDigit(t)) return 10;
        return PUNCT_RING.length;
    }

    private static int edgeBoostCycles(boolean isBlast, int n, int i, int originIdx,
                                       float invEdgeSpan, char t, float edgeBoost) {
        if (!isBlast || n <= 1 || invEdgeSpan == 0f || edgeBoost <= 0f) return 0;
        float distNorm = Math.abs(i - originIdx) * invEdgeSpan;
        return Math.round(distNorm * edgeBoost * glyphSpan(t));
    }

    private static void initSeedState(char[] curr, int[] remaining, int idx, char t, int extraCycles, CrazyOpts opts) {
        if (isLetter(t)) {
            curr[idx] = BlockAnim.seedLetter(t, opts);
            remaining[idx] = extraCycles;
            return;
        }
        if (isDigit(t)) {
            curr[idx] = BlockAnim.seedDigit(opts);
            remaining[idx] = extraCycles;
            return;
        }
        if (isPunctGlobal(t)) {
            curr[idx] = BlockAnim.seedPunct(opts);
            remaining[idx] = extraCycles;
            return;
        }
        curr[idx] = t;         // snap for spaces/others
        remaining[idx] = 0;
    }

    private static void initBlastAt(boolean isBlast, int[] flashLeft, int[] overshootLeft, int[] postLockHold,
                                    int i, int flashFrames, int overshoot) {
        if (!isBlast) return;
        flashLeft[i] = Math.max(0, flashFrames);
        overshootLeft[i] = Math.max(0, overshoot * 2); // off/back
        postLockHold[i] = 0;                          // assigned when flash ends
    }


    private static String sanitizeHex(String in, String def) {
        if (in == null) return def;
        String s = in.trim();
        if (s.startsWith("#")) s = s.substring(1);
        s = s.toLowerCase(Locale.ROOT);
        if (s.matches("^[0-9a-f]{3}$")) {               // expand 3-digit -> 6-digit
            s = "" + s.charAt(0) + s.charAt(0)
                    + s.charAt(1) + s.charAt(1)
                    + s.charAt(2) + s.charAt(2);
        }
        if (!s.matches("^[0-9a-f]{6}$")) return def;    // fallback if invalid
        return s;
    }

    public static void ensureOwnStyle(Label label) {
        Label.LabelStyle s = label.getStyle();
        if (s == null) return;
        // Defensive clone so we don't mutate a shared style from the Skin
        label.setStyle(new Label.LabelStyle(s));
    }

    /**
     * Frames between A↔B color toggles for a given frequency. If hz <= 0, never toggles.
     */
    private static int framesPerToggle(float hz, int fps) {
        if (hz <= 0f) return Integer.MAX_VALUE;
        // Two toggles per full cycle (A->B and B->A), so divide by (hz*2)
        return Math.max(1, Math.round(fps / (hz * 2f)));
    }

    private static List<BlockAnim> buildBlocks(List<Piece> pieces) {
        List<BlockAnim> blocks = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.kind == Piece.Kind.CRAZY) {
                blocks.add(buildBlock(p.text, p.opts));
            }
        }
        return blocks;
    }

    private static int maxFps(List<BlockAnim> blocks) {
        int fps = 1;
        for (BlockAnim b : blocks) {
            if (b.fps > fps) fps = b.fps;
        }
        return fps;
    }

    private static boolean advanceAll(List<BlockAnim> blocks, int finalFps) {
        boolean allDone = true;
        for (BlockAnim b : blocks) {
            b.subframe++;
            int skip = Math.max(1, Math.round((float) finalFps / b.fps));
            boolean shouldStep = (b.subframe % skip == 0) && !b.done;
            if (shouldStep) {
                b.stepFrame();
            }
            if (!b.done) {
                allDone = false;
            }
        }
        return allDone;
    }

    private static String renderPieces(List<Piece> pieces, List<BlockAnim> blocks) {
        StringBuilder sb = new StringBuilder();
        int bi = 0;
        for (Piece p : pieces) {
            if (p.kind == Piece.Kind.PLAIN) {
                sb.append(p.text);
            } else {
                sb.append(blocks.get(bi++).currentString());
            }
        }
        return sb.toString();
    }

    private static void initGlitchState(char[] target, char[] curr, boolean[] locked) {
        for (int i = 0; i < target.length; i++) {
            char t = target[i];
            boolean isSpace = (t == ' ');
            locked[i] = isSpace;
            curr[i] = isSpace ? ' ' : GLITCH_CHARS.charAt(RNG.nextInt(GLITCH_CHARS.length()));
        }
    }

    private static int lockCountForFrame(int frame, int totalFrames, int len) {
        // proportionally increase locks; clamp to [0, len]
        int count = Math.round((frame / (float) totalFrames) * len);
        return Math.clamp(count, 0, len);
    }

    private static void lockProgress(boolean[] locked, char[] target, char[] curr, int lockCount) {
        // lock from the start-up to lockCount indices that aren’t spaces and not yet locked
        for (int i = 0; i < lockCount; i++) {
            if (!locked[i] && target[i] != ' ') {
                locked[i] = true;
                curr[i] = target[i];
            }
        }
    }


    // ---------- CRAZY orchestration ----------

    private static void scrambleUnlocked(boolean[] locked, char[] target, char[] curr) {
        // refresh random glyphs for any still-unlocked, non-space characters
        for (int i = 0; i < target.length; i++) {
            if (!locked[i] && target[i] != ' ') {
                curr[i] = GLITCH_CHARS.charAt(RNG.nextInt(GLITCH_CHARS.length()));
            }
        }
    }

    private static void appendSparkleChar(StringBuilder sb, char ch, boolean phaseA, float density) {
        if (ch == ' ') {
            sb.append(' ');
            return;
        }
        boolean twinkle = RNG.nextFloat() < density;
        if (twinkle) {
            sb.append(phaseA ? "[#ffe066]" : "[#ffffff]");
            appendEscaped(sb, ch);
            sb.append("[]");
        } else {
            appendEscaped(sb, ch);
        }
    }

    // ---------- Title/label color effects (driven from plain text) ----------

    private static void appendEscaped(StringBuilder sb, char ch) {
        if (ch == '[') sb.append("[[");
        else sb.append(ch);
    }

    /**
     * Cancel any running effect for this instance.
     */
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Refresh the cached sources (marked + plain).
     */
    private void refreshBases(Label label, String incomingOrNull) {
        baseMarked = (incomingOrNull != null) ? incomingOrNull : label.getText().toString();
        basePlain = stripMarkup(baseMarked);
    }

    /**
     * Basic typewriter from plain string.
     */
    public void typewriter(Label label, String fullText, float charsPerSecond) {
        TypingInit init = prepareTyping(label, fullText, charsPerSecond);
        if (init == null) return;

        final String text = init.text();
        final StringBuilder buf = init.buf();
        final int[] i = init.idx();

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (i[0] >= text.length()) {
                    label.setText(buf.toString());
                    cancel();
                    return;
                }
                buf.append(text.charAt(i[0]++));
                label.setText(buf.toString());
            }
        }, 0f, init.interval());
    }

    /**
     * Punctuation-aware typewriter.
     */
    public void typewriterSmart(Label label, String fullText, float charsPerSecond,
                                float shortPause, float longPause, Runnable onTick) {
        TypingInit init = prepareTyping(label, fullText, charsPerSecond);
        if (init == null) return;

        final String text = init.text();
        final StringBuilder buf = init.buf();
        final int[] i = init.idx();
        final float baseInterval = init.interval();

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (i[0] >= text.length()) {
                    label.setText(buf.toString());
                    cancel();
                    return;
                }
                char ch = text.charAt(i[0]++);
                buf.append(ch);
                label.setText(buf.toString());
                if (onTick != null) onTick.run();

                float extra = switch (ch) {
                    case ',', ';', ':' -> shortPause;
                    case '.', '!', '?' -> longPause;
                    default -> 0f;
                };
                if (i[0] < text.length()) {
                    char nxt = text.charAt(i[0]);
                    if ((ch == '.' && nxt == '.') || (ch == '!' && nxt == '!') || (ch == '?' && nxt == '?')) {
                        extra *= 0.6f;
                    }
                }
                this.cancel();
                task = Timer.schedule(this, Math.max(0f, baseInterval + extra));
            }
        }, 0f);
    }

    /**
     * Common init for typing effects that start from an empty label.
     * Cancels any running task, normalises text, clears label, validates cps,
     * then returns the pieces you need to schedule the animation.
     * Returns null if there is nothing to animate.
     */
    private TypingInit prepareTyping(Label label, String fullText, float charsPerSecond) {
        cancel();
        final String text = (fullText == null) ? "" : fullText;
        label.setText("");
        if (text.isEmpty() || charsPerSecond <= 0f) return null;

        final float interval = 1f / charsPerSecond;
        return new TypingInit(text, interval, new StringBuilder(), new int[]{0});
    }

    /**
     * Word-by-word reveal at given WPS (no regex lookbehind).
     */
    public void wordReveal(Label label, String fullText, float wordsPerSecond) {
        cancel();
        final String text = fullText == null ? "" : fullText;
        if (text.isEmpty() || wordsPerSecond <= 0f) {
            label.setText(text);
            return;
        }

        Pattern tok = Pattern.compile("(\\s+|\\S+)");
        Matcher m = tok.matcher(text);
        List<String> tokens = new ArrayList<>();
        while (m.find()) tokens.add(m.group());

        final StringBuilder buf = new StringBuilder();
        final int[] i = {0};
        final float interval = 1f / wordsPerSecond;

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (i[0] >= tokens.size()) {
                    cancel();
                    return;
                }
                buf.append(tokens.get(i[0]++));
                label.setText(buf.toString());
            }
        }, 0f, interval);
    }

    /**
     * Blink a caret next to the current content.
     */
    public void blinkCaret(Label label, float intervalSeconds, String caret) {
        cancel();
        final String base = label.getText().toString();
        final String c = (caret == null || caret.isEmpty()) ? "▌" : caret;
        final boolean[] on = {true};
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                on[0] = !on[0];
                label.setText(on[0] ? (base + c) : base);
            }
        }, 0f, Math.max(0.05f, intervalSeconds));
    }

    /**
     * Erase down to target length.
     */
    public void backspaceTo(Label label, int targetLength, float charsPerSecond) {
        cancel();
        final StringBuilder buf = new StringBuilder(label.getText().toString());
        final int tgt = Math.clamp(buf.length(), 0, targetLength);
        if (charsPerSecond <= 0f || buf.length() <= tgt) return;

        final float interval = 1f / charsPerSecond;
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (buf.length() <= tgt) {
                    cancel();
                    return;
                }
                buf.setLength(buf.length() - 1);
                label.setText(buf.toString());
            }
        }, 0f, interval);
    }

    /**
     * Glitch/decoder reveal from random glyphs to final text.
     */
    public void glitchReveal(Label label, String finalText, float durationSec) {
        cancel();
        enableMarkup(label);

        final String tgt = (finalText == null) ? "" : finalText;
        if (durationSec <= 0f || tgt.isEmpty()) {
            label.setText(tgt);
            return;
        }

        final char[] target = tgt.toCharArray();
        final char[] curr = new char[target.length];
        final boolean[] locked = new boolean[target.length];

        initGlitchState(target, curr, locked);

        final int fps = 60;
        final int totalFrames = Math.max(1, Math.round(durationSec * fps));
        final int[] frame = {0};

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (frame[0] >= totalFrames) {
                    label.setText(tgt);
                    cancel();
                    return;
                }

                final int lockCount = lockCountForFrame(frame[0], totalFrames, target.length);
                lockProgress(locked, target, curr, lockCount);
                scrambleUnlocked(locked, target, curr);

                label.setText(new String(curr));
                frame[0]++;
            }
        }, 0f, 1f / fps);
    }

    /**
     * Full-label rainbow pulse (no per-char phase).
     */
    public void pulseRainbow(Label label, float hz, float startHueDeg) {
        cancel();
        enableMarkup(label);
        refreshBases(label, null);
        final String base = basePlain;
        final int fps = 60;
        final float[] phase = {startHueDeg};
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                phase[0] = (phase[0] + (360f * hz / fps)) % 360f;
                String hex = toHex6(hsvToRgb(phase[0], 1f, 1f));
                label.setText("[#" + hex + "]" + base + "[]");
            }
        }, 0f, 1f / fps);
    }

    /**
     * Marquee/ticker.
     */
    public void marquee(Label label, String text, int windowChars, float charsPerSecond) {
        cancel();
        final String t = (text == null) ? "" : text;
        final int win = Math.max(1, windowChars);
        final String pad = " ".repeat(win);
        final String tape = pad + t + "   ";
        final int total = tape.length();
        final int[] offset = {0};

        final float interval = 1f / Math.max(1f, charsPerSecond);
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                int start = offset[0] % total;
                String view;
                if (start + win <= total) view = tape.substring(start, start + win);
                else {
                    int a = total - start;
                    view = tape.substring(start) + tape.substring(0, win - a);
                }
                label.setText(view);
                offset[0]++;
            }
        }, 0f, interval);
    }

    /**
     * Blink-highlight first occurrence of substring using markup, then restore.
     */
    public void flashHighlight(Label label, String substring, int flashes, float hz, String colorHex) {
        cancel();
        final String text = label.getText().toString();
        if (flashes <= 0) return;
        if (substring == null || substring.isEmpty()) return;   // <— ensure non-null below
        if (text.isEmpty()) return;

        enableMarkup(label);

        final int idx = text.indexOf(substring);
        if (idx < 0) return;

        final int subLen = substring.length();
        final String pre = text.substring(0, idx);
        final String mid = text.substring(idx, idx + subLen);
        final String post = text.substring(idx + substring.length());
        final String safeHex = (colorHex == null || colorHex.isEmpty()) ? "ffff00" : colorHex.toLowerCase(Locale.ROOT);

        final int totalTicks = flashes * 2;
        final int[] tick = {0};
        final float interval = 1f / Math.max(0.1f, hz);

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                boolean on = (tick[0] % 2 == 0);
                String m = on ? ("[#" + safeHex + "]" + mid + "[]") : mid;
                label.setText(pre + m + post);
                tick[0]++;
                if (tick[0] >= totalTicks) {
                    label.setText(text);
                    cancel();
                }
            }
        }, 0f, interval);
    }

    // ---------- CRAZY internal models + EXPLODE/BLAST ----------

    /**
     * Parse CRAZY blocks on the incoming text; if none, typewriter the plain text.
     * CRAZY pieces animate; PLAIN pieces are left as-is (including any markup you authored).
     */
    public void crazyOrType(Label label, String text, float typewriterCps) {
        cancel();
        enableMarkup(label);

        refreshBases(label, text);                 // sets baseMarked + basePlain
        List<Piece> pieces = parseCrazyPieces(baseMarked);

        boolean hasCrazy = false;
        for (Piece p : pieces) {
            if (p.kind == Piece.Kind.CRAZY) {
                hasCrazy = true;
                break;
            }
        }

        if (hasCrazy) {
            startCrazyRevealMulti(label, pieces);
        } else {
            // typewriter should NOT reveal raw markup; use plain
            typewriter(label, basePlain, typewriterCps);
        }
    }

    /**
     * Animate all CRAZY blocks; static pieces remain unchanged (including their markup).
     */
    private void startCrazyRevealMulti(Label label, List<Piece> pieces) {
        cancel();

        final List<BlockAnim> blocks = buildBlocks(pieces);
        if (blocks.isEmpty()) {
            label.setText(joinPiecesStatic(pieces));
            return;
        }

        final int finalFps = maxFps(blocks);
        final float interval = 1f / finalFps;

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                boolean allDone = advanceAll(blocks, finalFps);
                label.setText(renderPieces(pieces, blocks));
                if (allDone) cancel();
            }
        }, 0f, interval);
    }

    /**
     * Rapid color strobe A==B for duration; restores plain text after.
     */
    public void strobe(Label label, String hexA, String hexB, float hz, float durationSec) {
        cancel();
        enableMarkup(label);
        if (basePlain == null || basePlain.isEmpty()) refreshBases(label, null);
        final String text = basePlain;

        final String a = sanitizeHex(hexA, "00ff00");  // default green if bad
        final String b = sanitizeHex(hexB, FFFFFF);  // default white if bad

        final float interval = 1f / Math.max(0.1f, hz) / 2f;
        final int totalTicks = Math.max(1, Math.round(durationSec / interval));
        final int[] tick = {0};

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                boolean onA = (tick[0] % 2 == 0);
                label.setText("[#" + (onA ? a : b) + "]" + text + "[]");
                tick[0]++;
                if (tick[0] >= totalTicks) {
                    label.setText(text);
                    cancel();
                }
            }
        }, 0f, interval);
    }

    /**
     * Smoothly pulse between two colors indefinitely; wraps plain text.
     */
    public void pulseBetween(Label label, String hexA, String hexB, float hz) {
        cancel();
        enableMarkup(label);
        if (basePlain == null || basePlain.isEmpty()) refreshBases(label, null);
        final String base = basePlain;

        final String A = sanitizeHex(hexA, FFFFFF);
        final String B = sanitizeHex(hexB, FFFFFF);
        final int Ar = Integer.parseInt(A.substring(0, 2), 16);
        final int Ag = Integer.parseInt(A.substring(2, 4), 16);
        final int Ab = Integer.parseInt(A.substring(4, 6), 16);
        final int Br = Integer.parseInt(B.substring(0, 2), 16);
        final int Bg = Integer.parseInt(B.substring(2, 4), 16);
        final int Bb = Integer.parseInt(B.substring(4, 6), 16);

        final int fps = 60;
        final int[] t = {0};

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                double phase = (2 * Math.PI) * (hz * (t[0] / (double) fps));
                double w = (1 - Math.cos(phase)) * 0.5;
                int r = (int) Math.round(Ar + (Br - Ar) * w);
                int g = (int) Math.round(Ag + (Bg - Ag) * w);
                int b = (int) Math.round(Ab + (Bb - Ab) * w);
                String hex = String.format(Locale.ROOT, "%02x%02x%02x", r, g, b);
                label.setText("[#" + hex + "]" + base + "[]");
                t[0]++;
            }
        }, 0f, 1f / fps);
    }

    /**
     * Short sparkle burst using plain text as source; restores after.
     */
    public void sparkle(Label label, float density, float hz, float durationSec) {
        cancel();
        enableMarkup(label);
        if (basePlain == null || basePlain.isEmpty()) refreshBases(label, null);

        final char[] chars = basePlain.toCharArray();
        final int n = chars.length;
        if (n == 0 || durationSec <= 0f || density <= 0f) return;

        final float dens = Math.clamp(density, 0f, 1f);
        final int fps = 60;
        final int totalFrames = Math.max(1, Math.round(durationSec * fps));
        final int toggleFrames = framesPerToggle(hz, fps);
        final boolean staticPhaseA = (toggleFrames == Integer.MAX_VALUE);
        final int[] frame = {0};

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                final boolean phaseA = staticPhaseA || (((frame[0] / Math.max(1, toggleFrames)) & 1) == 0);

                StringBuilder sb = new StringBuilder(n * 12);
                for (char ch : chars) {
                    appendSparkleChar(sb, ch, phaseA, dens);
                }
                label.setText(sb.toString());

                frame[0]++;
                if (frame[0] >= totalFrames) {
                    label.setText(basePlain);
                    cancel();
                }
            }
        }, 0f, 1f / fps);
    }

    /**
     * Per-character traveling rainbow sweep (indefinite), built from plain text.
     */
    public void sweepRainbow(Label label, float bandHz, float perCharShiftDeg, float travelHz) {
        cancel();
        enableMarkup(label);
        if (basePlain == null || basePlain.isEmpty()) refreshBases(label, null);

        final char[] chars = basePlain.toCharArray();
        final int n = chars.length;
        if (n == 0) return;

        final int fps = 60;
        final int[] t = {0};

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                float bandPhase = (t[0] * (bandHz / fps)) * 360f;
                float travel = (t[0] * (travelHz / fps)) * 360f;

                StringBuilder sb = new StringBuilder(n * 12);
                for (int i = 0; i < n; i++) {
                    char ch = chars[i];
                    float hue = (bandPhase + i * perCharShiftDeg + travel) % 360f;
                    String hex = toHex6(hsvToRgb(hue, 1f, 1f));
                    sb.append("[#").append(hex).append("]");
                    sb.append(ch == '[' ? "[[" : ch).append("[]");
                }
                label.setText(sb.toString());
                t[0]++;
            }
        }, 0f, 1f / fps);
    }

    /**
     * Strobe by toggling the LabelStyle's fontColor (bypasses markup entirely).
     */
    public void strobeDirect(Label label, Color a, Color b, float hz, float durationSec) {
        cancel();
        ensureOwnStyle(label);

        final Label.LabelStyle style = label.getStyle();
        if (style == null) return;
        // Make sure label tint doesn't multiply our chosen color
        label.setColor(Color.WHITE);

        final float interval = 1f / Math.max(0.1f, hz) / 2f; // A,B,A,B...
        final int totalTicks = Math.max(1, Math.round(durationSec / interval));
        final int[] tick = {0};

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                style.fontColor = (tick[0] % 2 == 0) ? a : b;
                label.invalidateHierarchy(); // force redraw with new color
                tick[0]++;
                if (tick[0] >= totalTicks) {
                    // leave it on 'b' (usually white) at the end
                    style.fontColor = b;
                    label.invalidateHierarchy();
                    cancel();
                }
            }
        }, 0f, interval);
    }

    private record TypingInit(String text, float interval, StringBuilder buf, int[] idx) {

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;

            // record pattern: destructure 'o' into its components
            if (!(o instanceof TypingInit(String t, float itv, StringBuilder b, int[] idx2))) {
                return false;
            }

            return Float.compare(this.interval, itv) == 0
                    && java.util.Objects.equals(this.text, t)
                    && this.buf == b
                    && java.util.Arrays.equals(this.idx, idx2);
        }


        @Override
        public int hashCode() {
            // Use identity for buf to match equals
            int h = java.util.Objects.hash(this.text, this.interval, System.identityHashCode(this.buf));
            h = 31 * h + java.util.Arrays.hashCode(this.idx);
            return h;
        }

        @Override
        public String toString() {
            // Show buf identity, and idx contents
            return "TypingInit[text=%s, interval=%s, buf@%s, idx=%s]".formatted(
                    this.text,
                    this.interval,
                    java.lang.Integer.toHexString(System.identityHashCode(this.buf)),
                    java.util.Arrays.toString(this.idx)
            );
        }
    }


    private record Piece(Kind kind, String text, CrazyOpts opts) {

        static Piece plain(String s) {
            return new Piece(Kind.PLAIN, s, null);
        }

        static Piece crazy(String s, CrazyOpts o) {
            return new Piece(Kind.CRAZY, s, o);
        }

        enum Kind {PLAIN, CRAZY}
    }

    private static class CrazyOpts {
        int fps = 60;
        int jitter = 8;
        int cycles = 0;
        From from = From.A;
        boolean rainbow = false;
        float rhz = 0.6f;
        float rshift = 18f;

        // explode/blast
        Style style = Style.NORMAL;
        Origin origin = Origin.MIDDLE;
        int spread = 2;         // delay added per char distance

        // BLAST extras
        int flashFrames = 3;    // white/yellow flash frames before lock
        int overshoot = 1;      // quick hop past target then back (2 ticks = off/back)
        float edgeBoost = 0.6f; // add extra cycles at edges (0..1)
        String flashHexA = FFFFFF;
        String flashHexB = FFE_066;

        enum From {A, RAND}

        enum Style {NORMAL, EXPLODE, BLAST}

        enum Origin {LEFT, MIDDLE, RIGHT}
    }

    private static class BlockAnim {
        final int fps;
        final int[] delays;
        final int[] remaining;
        final char[] target;
        final char[] curr;

        // visual
        final boolean rainbow;
        final float rhz;
        final float rshift;
        final String flashHexA;
        final String flashHexB;

        // BLAST extras (nullable if not used)
        final int[] flashLeft;
        final int[] overshootLeft;
        final int[] postLockHold;

        boolean done = false;
        int subframe = 0;
        int frame = 0;

        // ***** exactly 7 parameters *****
        BlockAnim(int fps,
                  int[] delays,
                  int[] remaining,
                  char[] target,
                  char[] curr,
                  Visual visual,
                  BlastExtras blast) {

            // basic validation (no imports needed)
            if (delays == null || remaining == null || target == null || curr == null) {
                throw new IllegalArgumentException("Null array passed to BlockAnim");
            }
            int n = target.length;
            if (curr.length != n || delays.length != n || remaining.length != n) {
                throw new IllegalArgumentException("Array length mismatch in BlockAnim");
            }

            this.fps = Math.max(1, fps);
            this.delays = delays;
            this.remaining = remaining;
            this.target = target;
            this.curr = curr;

            Visual v = (visual == null) ? Visual.defaults() : visual;
            this.rainbow = v.rainbow;
            this.rhz = v.rhz;
            this.rshift = v.rshift;
            this.flashHexA = v.flashHexA;
            this.flashHexB = v.flashHexB;

            if (blast != null) {
                // allow any of these to be null; we treat null as "feature off"
                this.flashLeft = blast.flashLeft;
                this.overshootLeft = blast.overshootLeft;
                this.postLockHold = blast.postLockHold;
                // optional: verify lengths if non-null
                if ((flashLeft != null && flashLeft.length != n) ||
                        (overshootLeft != null && overshootLeft.length != n) ||
                        (postLockHold != null && postLockHold.length != n)) {
                    throw new IllegalArgumentException("BlastExtras array length mismatch");
                }
            } else {
                this.flashLeft = null;
                this.overshootLeft = null;
                this.postLockHold = null;
            }
        }

        private static char seedLetter(char t, CrazyOpts opts) {
            if (opts.from == CrazyOpts.From.RAND) {
                return isUpper(t) ? (char) ('A' + RNG.nextInt(26)) : (char) ('a' + RNG.nextInt(26));
            }
            return isUpper(t) ? 'A' : 'a';
        }

        private static char seedDigit(CrazyOpts opts) {
            return (opts.from == CrazyOpts.From.RAND) ? (char) ('0' + RNG.nextInt(10)) : '0';
        }

        private static char seedPunct(CrazyOpts opts) {
            int start = (opts.from == CrazyOpts.From.RAND) ? RNG.nextInt(PUNCT_RING.length) : 0;
            return PUNCT_RING[start];
        }

        private static void appendEscaped(StringBuilder sb, char ch) {
            if (ch == '[') sb.append("[[");
            else sb.append(ch);
        }

        private static boolean hasPending(int[] arr, int i) {
            return arr != null && arr[i] > 0;
        }

        void stepFrame() {
            boolean allFinished = true;
            for (int i = 0; i < target.length; i++) {
                if (processCharAt(i)) { // true => more work pending for this char
                    allFinished = false;
                }
            }
            done = allFinished;
            frame++;
        }

        private boolean processCharAt(int i) {
            final char t = target[i];

            if (hasPending(delays, i)) {
                delays[i]--;
                return true;
            }

            if (hasPending(remaining, i)) {
                spinAdvance(i, t);
                remaining[i]--;
                return true;
            }

            if (handleOvershoot(i, t)) return true;
            if (handleFlash(i, t)) return true;

            if (hasPending(postLockHold, i)) {
                postLockHold[i]--;
                curr[i] = t;
                return false;
            }

            curr[i] = t; // settled
            return false;
        }

        private boolean handleOvershoot(int i, char t) {
            if (!hasPending(overshootLeft, i)) return false;
            if ((overshootLeft[i] & 1) == 0) {
                spinAdvance(i, t);       // hop off target
            } else {
                curr[i] = t;             // snap back
            }
            overshootLeft[i]--;
            return true;
        }

        private boolean handleFlash(int i, char t) {
            if (!hasPending(flashLeft, i)) return false;
            curr[i] = t;
            flashLeft[i]--;
            if (flashLeft[i] == 0 && postLockHold != null) {
                postLockHold[i] = 1;     // ensure one visible frame after flash
            }
            return true;
        }

        private void spinAdvance(int i, char t) {
            if (isLetter(t)) {
                spinLetter(i, t);
            } else if (isDigit(t)) {
                spinDigit(i, t);
            } else {
                spinPunct(i, t);
            }
        }

        // Only spin if t is a letter; otherwise do nothing.
        private void spinLetter(int i, char t) {
            if (!isLetter(t)) return;

            final boolean upper = isUpper(t);
            final char base = upper ? 'A' : 'a';
            final int span = 26;

            char c = curr[i];
            c = upper ? Character.toUpperCase(c) : Character.toLowerCase(c);
            if (c < base || c > (char) (base + span - 1)) c = base;

            // advance by 1 using floorMod; no need for the pos<0 fixup branch
            int pos = Math.floorMod((c - base + 1), span);
            curr[i] = (char) (base + pos);
        }

        // Only spin if t is a digit; otherwise do nothing.
        private void spinDigit(int i, char t) {
            if (!isDigit(t)) return;                 // <-- guard for tests

            int pos = (curr[i] >= '0' && curr[i] <= '9') ? (curr[i] - '0') : 0;
            pos = (pos + 1) % 10;
            curr[i] = (char) ('0' + pos);
        }

        // Only spin if t is punctuation in our ring; otherwise do nothing.
        private void spinPunct(int i, char t) {
            if (!isPunctGlobal(t)) return;           // <-- guard for tests

            int idx = 0;
            char c = curr[i];
            for (int k = 0; k < PUNCT_RING.length; k++) {
                if (PUNCT_RING[k] == c) {
                    idx = k;
                    break;
                }
            }
            curr[i] = PUNCT_RING[(idx + 1) % PUNCT_RING.length];
        }

        String currentString() {
            final boolean hasAnyFlash = (flashLeft != null) && !allZero(flashLeft);

            // Fast path: no rainbow and no flash anywhere
            if (!rainbow && !hasAnyFlash) {
                return new String(curr);
            }

            final StringBuilder sb = new StringBuilder(curr.length * 14);
            final float phase = (frame * (rhz / Math.max(1f, fps))) * 360f;

            for (int i = 0; i < curr.length; i++) {
                appendRenderedChar(sb, i, phase, rainbow);
            }
            return sb.toString();
        }

        private void appendRenderedChar(StringBuilder sb, int i, float phase, boolean rainbowActive) {
            final char ch = curr[i];

            // Flash takes priority over rainbow
            if (isFlashing(i)) {
                appendFlash(sb, ch, flashLeft[i]);
                return;
            }

            if (!rainbowActive) {
                appendEscaped(sb, ch);
                return;
            }

            final float hue = (phase + i * rshift) % 360f;
            final String hex = toHex6(hsvToRgb(hue, 1f, 1f));
            sb.append("[#").append(hex).append("]");
            appendEscaped(sb, ch);
            sb.append("[]");
        }

        private boolean isFlashing(int i) {
            return flashLeft != null && flashLeft[i] > 0;
        }

        private void appendFlash(StringBuilder sb, char ch, int flashCounter) {
            final boolean useA = (flashCounter & 1) == 0;
            sb.append("[#").append(useA ? flashHexA : flashHexB).append("]");
            appendEscaped(sb, ch);
            sb.append("[]");
        }

        private boolean allZero(int[] arr) {
            if (arr == null) return true;
            for (int v : arr) if (v != 0) return false;
            return true;
        }

        private static final class Visual {
            final boolean rainbow;
            final float rhz;
            final float rshift;
            final String flashHexA;
            final String flashHexB;

            Visual(boolean rainbow, float rhz, float rshift, String flashHexA, String flashHexB) {
                this.rainbow = rainbow;
                this.rhz = rhz;
                this.rshift = rshift;
                this.flashHexA = (flashHexA == null || flashHexA.isEmpty()) ? FFFFFF : flashHexA;
                this.flashHexB = (flashHexB == null || flashHexB.isEmpty()) ? FFE_066 : flashHexB;
            }

            static Visual defaults() {
                return new Visual(false, 0.6f, 18f, FFFFFF, FFE_066);
            }
        }

        private static final class BlastExtras {
            final int[] flashLeft;
            final int[] overshootLeft;
            final int[] postLockHold;

            BlastExtras(int[] flashLeft, int[] overshootLeft, int[] postLockHold) {
                this.flashLeft = flashLeft;
                this.overshootLeft = overshootLeft;
                this.postLockHold = postLockHold;
            }
        }
    }
}

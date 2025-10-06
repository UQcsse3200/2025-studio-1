package com.csse3200.game.ui.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Timer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
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
 * spread=<int>, flash=<frames>, overshoot=<hops>,
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
    // Punctuation wheel for CRAZY
    private static final char[] PUNCT_RING = (
            ".,;:!?-_/\\|@#$%^&*()[]{}<>'\"+=~`"
    ).toCharArray();

    // Cached sources (set via refreshBases / crazyOrType)
    private String baseMarked = "";
    private String basePlain = "";

    // Single running task per instance
    private Timer.Task task;

    // ---------- Public helpers ----------

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
            int idx = ThreadLocalRandom.current().nextInt(pool.size());
            return pool.get(idx);
        } catch (Exception e) {
            return fallback;
        }
    }

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
        s = s.replaceAll("\\[#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})\\]", ""); // open tags
        s = s.replace("[]", "");                                        // close tags
        return s.replace(ESC, "[");
    }

    // ---------- Internal base management ----------

    private static List<Piece> parseCrazyPieces(String s) {
        final String OPEN = "{CRAZY";
        final String CLOSE = "{/CRAZY}";
        int idx = 0;
        List<Piece> out = new ArrayList<>();
        while (idx < s.length()) {
            int a = s.indexOf(OPEN, idx);
            if (a < 0) {
                out.add(Piece.plain(s.substring(idx)));
                break;
            }
            if (a > idx) out.add(Piece.plain(s.substring(idx, a)));

            int tagEnd = s.indexOf('}', a + OPEN.length());
            if (tagEnd < 0) {
                out.add(Piece.plain(s.substring(a)));
                break;
            }
            String tagInside = s.substring(a + OPEN.length(), tagEnd).trim();

            int b = s.indexOf(CLOSE, tagEnd + 1);
            if (b < 0) {
                out.add(Piece.plain(s.substring(a)));
                break;
            }

            String inner = s.substring(tagEnd + 1, b);
            CrazyOpts opts = parseOpts(tagInside);
            out.add(Piece.crazy(inner, opts));
            idx = b + CLOSE.length();
        }
        return out;
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
                    case "fps" -> o.fps = clampInt(Integer.parseInt(v), 1, 240);
                    case "jitter" -> o.jitter = clampInt(Integer.parseInt(v), 0, 60);
                    case "cycles" -> o.cycles = clampInt(Integer.parseInt(v), 0, 10);
                    case "from" -> o.from = ("rand".equals(v) ? CrazyOpts.From.RAND : CrazyOpts.From.A);
                    case "rainbow" -> o.rainbow = ("true".equals(v) || "1".equals(v));
                    case "rhz" -> o.rhz = clampFloat(Float.parseFloat(v), 0.01f, 5f);
                    case "rshift" -> o.rshift = clampFloat(Float.parseFloat(v), 0f, 360f);

                    case "style" -> o.style = switch (v) {
                        case "explode" -> CrazyOpts.Style.EXPLODE;
                        case "blast" -> CrazyOpts.Style.BLAST;
                        default -> CrazyOpts.Style.NORMAL;
                    };
                    case "origin" -> {
                        if ("left".equals(v)) o.origin = CrazyOpts.Origin.LEFT;
                        else if ("right".equals(v)) o.origin = CrazyOpts.Origin.RIGHT;
                        else o.origin = CrazyOpts.Origin.MIDDLE;
                    }
                    case "spread" -> o.spread = Math.max(0, Integer.parseInt(v));
                    case "flash" -> o.flashFrames = Math.max(0, Integer.parseInt(v));
                    case "overshoot" -> o.overshoot = Math.max(0, Integer.parseInt(v));
                    case "edgeboost" -> o.edgeBoost = clampFloat(Float.parseFloat(v), 0f, 1f);
                    case "flashhexa" -> o.flashHexA = v.replace("#", "");
                    case "flashhexb" -> o.flashHexB = v.replace("#", "");
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return o;
    }

    // ---------- CRAZY parsing ----------

    private static int clampInt(int x, int lo, int hi) {
        return Math.max(lo, Math.min(hi, x));
    }

    private static float clampFloat(float x, float lo, float hi) {
        return Math.max(lo, Math.min(hi, x));
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

    private static int hsvToRgb(float hDeg, float s, float v) {
        float h = (hDeg % 360f + 360f) % 360f / 60f;
        int i = (int) Math.floor(h);
        float f = h - i;
        float p = v * (1f - s);
        float q = v * (1f - s * f);
        float t = v * (1f - s * (1f - f));
        float r, g, b;
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
        int R = (int) (r * 255.0f + 0.5f);
        int G = (int) (g * 255.0f + 0.5f);
        int B = (int) (b * 255.0f + 0.5f);
        return (R << 16) | (G << 8) | B;
    }

    private static String toHex6(int rgb) {
        String s = Integer.toHexString(rgb).toLowerCase(Locale.ROOT);
        int pad = 6 - s.length();
        if (pad <= 0) return s.substring(s.length() - 6);
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < pad; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }

    // ---------- Color helpers ----------

    /**
     * Builds a BlockAnim for one CRAZY piece, with EXPLODE/BLAST timing.
     */
    private static BlockAnim buildBlock(String text, CrazyOpts opts) {
        char[] target = (text == null ? "" : text).toCharArray();
        char[] curr = new char[target.length];
        int[] delays = new int[target.length];
        int[] remaining = new int[target.length];

        // BLAST arrays (null when not used)
        int[] flashLeft = null;
        int[] overshootLeft = null;
        int[] postLockHold = null;

        ThreadLocalRandom r = ThreadLocalRandom.current();
        int n = target.length;

        // origin index
        int originIdx = switch (opts.origin) {
            case LEFT -> 0;
            case RIGHT -> Math.max(0, n - 1);
            default -> Math.max(0, (n - 1) / 2);
        };

        boolean isBlast = (opts.style == CrazyOpts.Style.BLAST);
        boolean isExplode = (opts.style == CrazyOpts.Style.EXPLODE);

        if (isBlast) {
            flashLeft = new int[n];
            overshootLeft = new int[n];
            postLockHold = new int[n];
        }

        for (int i = 0; i < n; i++) {
            char t = target[i];

            // base jitter
            int d = r.nextInt(0, Math.max(1, opts.jitter)); // 0..jitter-1

            // explode/blast distance delay
            if (isExplode || isBlast) {
                int dist = Math.abs(i - originIdx);
                d += dist * Math.max(0, opts.spread);
            }
            delays[i] = d;

            // base cycles
            int extraCycles = opts.cycles;

            // edge boost for BLAST: chars far from origin spin longer
            if (isBlast && n > 1) {
                float distNorm = Math.abs(i - originIdx) / (float) Math.max(1, Math.max(originIdx, n - 1 - originIdx));
                int baseSpan = isLetter(t) ? 26 : isDigit(t) ? 10 : PUNCT_RING.length;
                extraCycles += Math.round(distNorm * opts.edgeBoost * baseSpan);
            }

            remaining[i] = 0;

            if (isLetter(t)) {
                curr[i] = (opts.from == CrazyOpts.From.RAND)
                        ? (isUpper(t) ? (char) ('A' + r.nextInt(26)) : (char) ('a' + r.nextInt(26)))
                        : (isUpper(t) ? 'A' : 'a');
                remaining[i] = extraCycles;
            } else if (isDigit(t)) {
                curr[i] = (opts.from == CrazyOpts.From.RAND) ? (char) ('0' + r.nextInt(10)) : '0';
                remaining[i] = extraCycles;
            } else if (isPunctGlobal(t)) {
                int start = (opts.from == CrazyOpts.From.RAND) ? r.nextInt(PUNCT_RING.length) : 0;
                curr[i] = PUNCT_RING[start];
                remaining[i] = extraCycles;
            } else {
                curr[i] = t; // snap
                delays[i] = 0;
                remaining[i] = 0;
            }

            if (isBlast) {
                // brief flash as it locks
                flashLeft[i] = Math.max(0, opts.flashFrames);
                // tiny overshoot hop after the spins finish (2 ticks per hop: off/back)
                overshootLeft[i] = Math.max(0, opts.overshoot * 2);
                // hold gets assigned in stepFrame when flash ends
                postLockHold[i] = 0;
            }
        }

        return new BlockAnim(
                Math.max(1, opts.fps),
                delays, remaining, target, curr,
                opts.rainbow, opts.rhz, opts.rshift,
                flashLeft, overshootLeft, postLockHold,
                (opts.flashHexA == null ? "ffffff" : opts.flashHexA),
                (opts.flashHexB == null ? "ffe066" : opts.flashHexB)
        );
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

    // ---------- Core effects (plain-text driven) ----------

    public static void ensureOwnStyle(Label label) {
        Label.LabelStyle s = label.getStyle();
        if (s == null) return;
        // Defensive clone so we don't mutate a shared style from the Skin
        label.setStyle(new Label.LabelStyle(s));
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
        cancel();
        final String text = fullText == null ? "" : fullText;
        label.setText("");
        if (text.isEmpty() || charsPerSecond <= 0f) return;

        final float interval = 1f / charsPerSecond;
        final StringBuilder buf = new StringBuilder();
        final int[] i = {0};

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
        }, 0f, interval);
    }

    /**
     * Punctuation-aware typewriter.
     */
    public void typewriterSmart(Label label, String fullText, float charsPerSecond,
                                float shortPause, float longPause, Runnable onTick) {
        cancel();
        final String text = fullText == null ? "" : fullText;
        label.setText("");
        if (text.isEmpty() || charsPerSecond <= 0f) return;

        final float baseInterval = 1f / charsPerSecond;
        final StringBuilder buf = new StringBuilder();
        final int[] i = {0};

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
        final int tgt = Math.max(0, Math.min(targetLength, buf.length()));
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
        final String tgt = finalText == null ? "" : finalText;
        if (durationSec <= 0f || tgt.isEmpty()) {
            label.setText(tgt);
            return;
        }

        final char[] target = tgt.toCharArray();
        final char[] curr = new char[target.length];
        final boolean[] locked = new boolean[target.length];
        final ThreadLocalRandom r = ThreadLocalRandom.current();
        final String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#%$*+-_=<>/?";

        for (int i = 0; i < target.length; i++) {
            char t = target[i];
            curr[i] = (t == ' ' ? ' ' : charset.charAt(r.nextInt(charset.length())));
            locked[i] = (t == ' ');
        }

        final int fps = 60;
        final int totalFrames = Math.max(1, Math.round(durationSec * fps));
        final int[] frame = {0};
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                float progress = (float) frame[0] / totalFrames;
                int lockCount = Math.round(progress * target.length);

                for (int k = 0; k < lockCount; k++) {
                    if (!locked[k] && target[k] != ' ') {
                        locked[k] = true;
                        curr[k] = target[k];
                    }
                }
                for (int i = 0; i < target.length; i++) {
                    if (!locked[i] && target[i] != ' ') {
                        curr[i] = charset.charAt(r.nextInt(charset.length()));
                    }
                }

                label.setText(new String(curr));
                frame[0]++;
                if (frame[0] > totalFrames) {
                    label.setText(tgt);
                    cancel();
                }
            }
        }, 0f, 1f / fps);
    }

    // ---------- CRAZY orchestration ----------

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

    // ---------- Title/label color effects (driven from plain text) ----------

    /**
     * Blink-highlight first occurrence of substring using markup, then restore.
     */
    public void flashHighlight(Label label, String substring, int flashes, float hz, String colorHex) {
        cancel();
        final String text = label.getText().toString();
        final int idx = (substring == null || substring.isEmpty()) ? -1 : text.indexOf(substring);
        if (idx < 0 || flashes <= 0) return;

        enableMarkup(label);
        final String pre = text.substring(0, idx);
        final String mid = text.substring(idx, idx + substring.length());
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

        // Build per-piece animation states
        List<BlockAnim> blocks = new ArrayList<>();
        for (Piece p : pieces) if (p.kind == Piece.Kind.CRAZY) blocks.add(buildBlock(p.text, p.opts));

        if (blocks.isEmpty()) {
            label.setText(joinPiecesStatic(pieces));
            return;
        }

        int fps = 1;
        for (BlockAnim b : blocks) fps = Math.max(fps, b.fps);
        final int finalFps = fps;
        final float interval = 1f / (float) fps;

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                boolean allDone = true;

                for (BlockAnim b : blocks) {
                    b.subframe++;
                    int skip = Math.max(1, Math.round((float) finalFps / b.fps));
                    if (b.subframe % skip != 0) {
                        if (!b.done) allDone = false;
                        continue;
                    }
                    if (!b.done) {
                        b.stepFrame();
                        if (!b.done) allDone = false;
                    }
                }

                // Rebuild full string
                StringBuilder sb = new StringBuilder();
                int bi = 0;
                for (Piece p : pieces) {
                    if (p.kind == Piece.Kind.PLAIN) sb.append(p.text);
                    else sb.append(blocks.get(bi++).currentString());
                }
                label.setText(sb.toString());

                if (allDone) cancel();
            }
        }, 0f, interval);
    }

    /**
     * Rapid color strobe A<->B for duration; restores plain text after.
     */
    public void strobe(Label label, String hexA, String hexB, float hz, float durationSec) {
        cancel();
        enableMarkup(label);
        if (basePlain == null || basePlain.isEmpty()) refreshBases(label, null);
        final String text = basePlain;

        final String a = sanitizeHex(hexA, "00ff00");  // default green if bad
        final String b = sanitizeHex(hexB, "ffffff");  // default white if bad

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


    // ---------- CRAZY internal models + EXPLODE/BLAST ----------

    /**
     * Smoothly pulse between two colors indefinitely; wraps plain text.
     */
    public void pulseBetween(Label label, String hexA, String hexB, float hz) {
        cancel();
        enableMarkup(label);
        if (basePlain == null || basePlain.isEmpty()) refreshBases(label, null);
        final String base = basePlain;

        final String A = sanitizeHex(hexA, "ffffff");
        final String B = sanitizeHex(hexB, "ffffff");
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
        if (n == 0) return;

        final int fps = Math.max(10, Math.round(hz * 2f * 10)); // enough frame rate for twinkle
        final int totalFrames = Math.max(1, Math.round(durationSec * fps));
        final int[] frame = {0};
        final java.util.Random rng = new java.util.Random();

        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder(n * 12);
                for (int i = 0; i < n; i++) {
                    char ch = chars[i];
                    boolean twinkle = rng.nextFloat() < density && ch != ' ';
                    if (twinkle) {
                        boolean phaseA = ((frame[0] / Math.max(1, (fps / (int) Math.max(1, hz)))) % 2) == 0;
                        sb.append(phaseA ? "[#ffe066]" : "[#ffffff]");
                        sb.append(ch == '[' ? "[[" : ch).append("[]");
                    } else {
                        sb.append(ch == '[' ? "[[" : ch);
                    }
                }
                label.setText(sb.toString());
                frame[0]++;
                if (frame[0] > totalFrames) {
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

        task = com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
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

    private static class Piece {
        final Kind kind;
        final String text;
        final CrazyOpts opts;

        private Piece(Kind kind, String text, CrazyOpts opts) {
            this.kind = kind;
            this.text = text;
            this.opts = opts;
        }

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
        String flashHexA = "ffffff";
        String flashHexB = "ffe066";

        enum From {A, RAND}

        enum Style {NORMAL, EXPLODE, BLAST}

        enum Origin {LEFT, MIDDLE, RIGHT}
    }

    private static class BlockAnim {
        final int fps;
        final int[] delays;     // frames to wait before char starts
        final int[] remaining;  // spin steps before settle (letters/digits/punct)
        final char[] target;
        final char[] curr;

        // visual
        final boolean rainbow;
        final float rhz;
        final float rshift;

        // BLAST extras (nullable if not used)
        final int[] flashLeft;      // per-char pre-lock flash frames
        final int[] overshootLeft;  // per-char quick hop frames
        final int[] postLockHold;   // tiny hold so flash can render once
        final String flashHexA;
        final String flashHexB;

        boolean done = false;
        int subframe = 0;
        int frame = 0;

        BlockAnim(int fps, int[] delays, int[] remaining, char[] target, char[] curr,
                  boolean rainbow, float rhz, float rshift,
                  int[] flashLeft, int[] overshootLeft, int[] postLockHold,
                  String flashHexA, String flashHexB) {
            this.fps = fps;
            this.delays = delays;
            this.remaining = remaining;
            this.target = target;
            this.curr = curr;
            this.rainbow = rainbow;
            this.rhz = rhz;
            this.rshift = rshift;
            this.flashLeft = flashLeft;
            this.overshootLeft = overshootLeft;
            this.postLockHold = postLockHold;
            this.flashHexA = flashHexA;
            this.flashHexB = flashHexB;
        }

        void stepFrame() {
            boolean all = true;
            for (int i = 0; i < target.length; i++) {
                char t = target[i];

                if (delays[i] > 0) {
                    delays[i]--;
                    all = false;
                    continue;
                }

                // If we still have spin steps, advance ring
                if (remaining[i] > 0) {
                    if (isLetter(t)) spinLetter(i, t);
                    else if (isDigit(t)) spinDigit(i, t);
                    else if (isPunctGlobal(t)) spinPunct(i, t);
                    remaining[i]--;
                    all = false;
                    continue;
                }

                // Overshoot (hop one step past target then back)
                if (overshootLeft != null && overshootLeft[i] > 0) {
                    if (overshootLeft[i] % 2 == 0) {
                        // hop off target
                        if (isLetter(t)) spinLetter(i, t);
                        else if (isDigit(t)) spinDigit(i, t);
                        else if (isPunctGlobal(t)) spinPunct(i, t);
                    } else {
                        // snap back to target
                        curr[i] = t;
                    }
                    overshootLeft[i]--;
                    all = false;
                    continue;
                }

                // Flash frames before final settle (rendered in currentString)
                if (flashLeft != null && flashLeft[i] > 0) {
                    curr[i] = t; // ensure we show the real glyph during flash
                    flashLeft[i]--;
                    all = false;
                    // we keep postLockHold so at least one frame shows final color after flash
                    if (flashLeft[i] == 0 && postLockHold != null) postLockHold[i] = 1;
                    continue;
                }

                // Hold one frame at final glyph (prevents skipping if multiple chars finish same frame)
                if (postLockHold != null && postLockHold[i] > 0) {
                    postLockHold[i]--;
                    // fall through to mark finished this loop
                } else {
                    curr[i] = t; // ensure target
                }
            }
            done = all;
            frame++;
        }

        private void spinLetter(int i, char t) {
            boolean upper = isUpper(t);
            char base = upper ? 'A' : 'a';
            int span = 26;
            if (curr[i] == 0) curr[i] = base;
            int off = (curr[i] - base + 1) % span;
            curr[i] = (char) (base + off);
        }

        private void spinDigit(int i, char t) {
            char base = '0';
            int span = 10;
            if (curr[i] == 0) curr[i] = base;
            int off = (curr[i] - base + 1) % span;
            curr[i] = (char) (base + off);
        }

        private void spinPunct(int i, char t) {
            if (curr[i] == 0) curr[i] = PUNCT_RING[0];
            int idx = 0;
            for (int k = 0; k < PUNCT_RING.length; k++)
                if (PUNCT_RING[k] == curr[i]) {
                    idx = k;
                    break;
                }
            curr[i] = PUNCT_RING[(idx + 1) % PUNCT_RING.length];
        }

        String currentString() {
            // If rainbow-off and no flash needed, fast path
            if (!rainbow && (flashLeft == null || allZero(flashLeft))) return new String(curr);

            StringBuilder sb = new StringBuilder(curr.length * 14);
            float phase = (frame * (rhz / Math.max(1f, fps))) * 360f;

            for (int i = 0; i < curr.length; i++) {
                char ch = curr[i];

                // Flash coloring if active
                if (flashLeft != null && flashLeft[i] > 0) {
                    boolean a = (flashLeft[i] % 2 == 0);
                    sb.append("[#").append(a ? flashHexA : flashHexB).append("]");
                    sb.append(ch == '[' ? "[[" : ch).append("[]");
                    continue;
                }

                if (!rainbow) {
                    sb.append(ch == '[' ? "[[" : ch);
                    continue;
                }

                float hue = (phase + i * rshift) % 360f;
                String hex = toHex6(hsvToRgb(hue, 1f, 1f));
                sb.append("[#").append(hex).append("]");
                sb.append(ch == '[' ? "[[" : ch).append("[]");
            }
            return sb.toString();
        }

        private boolean allZero(int[] arr) {
            if (arr == null) return true;
            for (int v : arr) if (v != 0) return false;
            return true;
        }
    }
}

package com.csse3200.game.components.screens;

/**
 * Immutable description of an animated tutorial clip composed of sequential PNG frames.
 * <p>
 * A {@code TutorialClip} specifies where frame images are located, how they are named,
 * how many frames there are, and how they should be played (rate and looping).
 * Rendering logic (e.g., {@code AnimatedClipImage}) uses this metadata to load and
 * animate the frames.
 *
 * <pre>
 * Example:
 *   folder  = "tutorial/movement"
 *   pattern = "move_%03d.png"   // results in move_000.png, move_001.png, ...
 *   frameCount = 60
 *   fps = 30f
 *   loop = true
 * </pre>
 *
 * @param folder     Folder containing the PNG frames, relative to the assets root.
 * @param pattern    {@link String#format(String, Object...)} pattern for frame filenames, typically
 *                   including a numeric placeholder (e.g., {@code "%03d"}).
 *                   <p>Example: {@code "move_%03d.png"} produces {@code move_000.png}, {@code move_001.png}, …</p>
 * @param frameCount Total number of frames available for this clip (must be ≥ 1).
 * @param fps        Playback rate in frames per second (must be &gt; 0).
 * @param loop       Whether playback should loop when it reaches the final frame.
 */
public record TutorialClip(String folder, String pattern, int frameCount, float fps, boolean loop) {
    /**
     * Creates a clip description for building an animation from numbered PNG frames.
     *
     * @param folder     the folder containing all frames (relative to assets root), not {@code null}
     * @param pattern    filename pattern for frames, suitable for {@link String#format}; should include an integer placeholder
     * @param frameCount total frame count (must be ≥ 1)
     * @param fps        playback frames per second (must be &gt; 0)
     * @param loop       whether playback should loop
     */
    public TutorialClip {
    }
}

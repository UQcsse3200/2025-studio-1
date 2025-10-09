package com.csse3200.game.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Finds level files by glob (e.g., "levels/*.json"), loads them,
 * bulk-loads textures into AssetManager, and places entities via a pluggable hook.
 */
public final class LevelPatternLoader {
    private static final Logger log = LoggerFactory.getLogger(LevelPatternLoader.class);
    private final AssetManager assets;
    private final FileLoader.Location location;
    private final EntityPlacer placer;
    private final boolean synchronousTextureLoad;

    public LevelPatternLoader(AssetManager assets,
                              FileLoader.Location location,
                              EntityPlacer placer,
                              boolean synchronousTextureLoad) {
        this.assets = Objects.requireNonNull(assets, "assets");
        this.location = Objects.requireNonNull(location, "location");
        this.placer = Objects.requireNonNull(placer, "placer");
        this.synchronousTextureLoad = synchronousTextureLoad;
    }

    private static FileHandle getFileHandle(String filename, FileLoader.Location location) {
        return switch (location) {
            case CLASSPATH -> Gdx.files.classpath(filename);
            case INTERNAL -> Gdx.files.internal(filename);
            case LOCAL -> Gdx.files.local(filename);
            case EXTERNAL -> Gdx.files.external(filename);
            case ABSOLUTE -> Gdx.files.absolute(filename);
        };
    }

    private static String join(String dir, String name) {
        return dir.endsWith("/") ? dir + name : dir + "/" + name;
    }

    /* ----------------------------- helpers ----------------------------- */

    /**
     * Minimal glob→regex: supports '*' and '?' (no brace expansion).
     */
    public static Pattern compileGlob(String glob) {
        final StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*':
                    sb.append("[^/]*");
                    break;
                case '?':
                    sb.append('.');
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                case '{':
                case '}':
                case '[':
                case ']':
                case '\\':
                    sb.append('\\').append(c);
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append('$');
        return Pattern.compile(sb.toString());
    }

    /**
     * Load all files in {@code directory} whose names match {@code glob} (e.g. "*.json").
     * Returns the LevelData list (in load order). Textures are queued once (deduped)
     * and optionally loaded synchronously.
     */
    public List<LevelIO.LevelData> loadAll(String directory, String glob) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(glob, "glob");

        final FileHandle dir = getFileHandle(directory, location);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            log.atError().setMessage("Directory {} ({}) not found or not a directory")
                    .addArgument(directory).addArgument(location).log();
            return List.of();
        }

        final Pattern namePattern = compileGlob(glob);
        final List<FileHandle> matches = Arrays.stream(dir.list())
                .filter(f -> f.exists() && !f.isDirectory())
                .filter(f -> namePattern.matcher(f.name()).matches())
                .sorted(Comparator.comparing(FileHandle::name))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            log.atWarn().setMessage("No files matched '{}' in {} ({})")
                    .addArgument(glob).addArgument(directory).addArgument(location).log();
            return List.of();
        }

        final List<LevelIO.LevelData> loaded = new ArrayList<>();
        final Set<String> texturePaths = new LinkedHashSet<>();

        // Parse all level files first
        for (FileHandle f : matches) {
            final String path = join(directory, f.name());
            LevelIO.load(path, location).ifPresent(data -> {
                loaded.add(data);
                texturePaths.addAll(data.textures().values());
            });
        }

        // Enqueue textures once (deduped)
        for (String path : texturePaths) {
            if (!assets.isLoaded(path)) {
                assets.load(path, com.badlogic.gdx.graphics.Texture.class);
            }
        }
        if (synchronousTextureLoad) {
            assets.finishLoading();
        }

        // Place entities
        for (LevelIO.LevelData data : loaded) {
            for (var spec : data.entities()) {
                try {
                    placer.place(spec);
                } catch (Exception ex) {
                    log.atError().setCause(ex)
                            .setMessage("Entity placement failed for {}").addArgument(spec).log();
                }
            }
        }

        return loaded;
    }

    /**
     * Hook you implement to actually spawn/place the entity in-game.
     */
    @FunctionalInterface
    public interface EntityPlacer {
        void place(FileLoader.MapEntitySpec spec);
    }
}

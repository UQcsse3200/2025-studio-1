package com.csse3200.game.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Utility for reading and writing Java objects to/from JSON using LibGDX.
 *
 * <ul>
 *   <li>No {@code null} returns (uses {@link Optional})</li>
 *   <li>Explicit, scoped Json instances per operation</li>
 *   <li>Input validation and clearer logging</li>
 * </ul>
 *
 * <p>Note: LibGDX {@link Json} is not documented as thread-safe; we create
 * fresh instances to avoid shared mutable state.</p>
 */
public final class FileLoader {
    private static final Logger log = LoggerFactory.getLogger(FileLoader.class);

    private FileLoader() {
        // utility class
    }

    /* --------------------------------- Public API --------------------------------- */

    /**
     * Read an instance of {@code type} from the given file and location.
     * JSON properties override class defaults.
     *
     * @param type     target class
     * @param filename file path appropriate for the given {@code location}
     * @param location storage backend
     */
    public static <T> Optional<T> read(Class<T> type, String filename, Location location) {
        Objects.requireNonNull(type, "type");
        final var fhOpt = resolveExisting(filename, location, "read", type.getSimpleName());
        return fhOpt.flatMap(fh -> safeFromJson(type, fh, json -> {
        }));
    }

    /**
     * Specialised reader for {@code SaveGame.GameState} that also configures
     * the element type for the {@code loadedInventory} collection.
     */
    public static Optional<SaveGame.GameState> readGameState(String filename, Location location) {
        final var fhOpt = resolveExisting(filename, location, "readGameState", "GameState");
        return fhOpt.flatMap(fh -> safeFromJson(
                SaveGame.GameState.class,
                fh,
                json -> json.setElementType(SaveGame.GameState.class, "loadedInventory", SaveGame.itemRetrieve.class)
        ));
    }

    /**
     * Write an object as JSON to the given file and location (pretty-printed).
     *
     * @return {@code true} on success, {@code false} on failure
     */
    public static boolean write(Object object, String filename, Location location) {
        return write(object, filename, location, /*pretty*/ true);
    }

    /**
     * Write an object as JSON to the given file and location.
     *
     * @param object   object to serialise
     * @param filename destination path appropriate for {@code location}
     * @param location storage backend
     * @param pretty   whether to pretty-print
     */
    public static boolean write(Object object, String filename, Location location, boolean pretty) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(filename, "filename");
        Objects.requireNonNull(location, "location");

        log.debug("Writing {} to {} ({})", object.getClass().getSimpleName(), filename, location);
        final FileHandle file = getFileHandle(filename, location);
        if (file == null) {
            log.error("Unable to obtain FileHandle for {} ({})", filename, location);
            return false;
        }

        try {
            final Json json = newDefaultJson();
            final String payload = pretty ? json.prettyPrint(object) : json.toJson(object);
            file.writeString(payload, /*append*/ false);
            return true;
        } catch (Exception e) {
            log.error("Failed to write {} to {}: {}", object.getClass().getSimpleName(), file.path(), e.toString(), e);
            return false;
        }
    }

    /**
     * Reads texture asset mappings from a JSON file.
     *
     * <p>It looks for any top-level object whose name ends with {@code "Textures"} and
     * treats its key/value pairs as {@code id -> relativeOrAbsolutePath} entries.</p>
     *
     * <p>Only top-level objects with names ending in "Textures" are parsed. If duplicate ids appear across
     * groups, the first occurrence wins and a warning is logged.</p>
     */
    public static Optional<Map<String, String>> readTextureMap(String filename, Location location) {
        final var fhOpt = resolveExisting(filename, location, "readTextureMap", "TextureMap");
        if (fhOpt.isEmpty()) return Optional.empty();

        try {
            final JsonValue root = tryParseRootObject(fhOpt.get());
            if (root == null) return Optional.empty();

            final Map<String, String> out = new LinkedHashMap<>();
            for (JsonValue group = root.child; group != null; group = group.next) {
                processTextureGroup(group, fhOpt.get(), out);
            }

            if (out.isEmpty()) {
                log.warn("No '*Textures' sections or entries found in {}", fhOpt.get().path());
                return Optional.empty();
            }
            return Optional.of(out);
        } catch (Exception e) {
            log.error("Failed parsing texture map {}: {}", fhOpt.get().path(), e.toString(), e);
            return Optional.empty();
        }
    }

    /* ------------------------------- Safer replacements ------------------------------- */
    // The previously deprecated methods (readClass, readPlayer, writeClass, etc.)
    // have been removed. Use the Optional/boolean APIs above.
    // If you still need "player" reads, use readGameState(...).
    // If you need INTERNAL/EXTERNAL defaults, pass the desired Location explicitly.

    /* --------------------------------- Internals --------------------------------- */

    private static <T> Optional<T> safeFromJson(Class<T> type, FileHandle file, Consumer<Json> config) {
        try {
            final Json json = newDefaultJson();
            config.accept(json);
            final T obj = json.fromJson(type, file);
            if (obj == null) {
                log.error("Deserialisation returned null for {} from {}", type.getSimpleName(), file.path());
                return Optional.empty();
            }
            if (log.isDebugEnabled()) {
                log.debug("Loaded {} from {}.", type.getSimpleName(), file.path());
            }
            return Optional.of(obj);
        } catch (Exception e) {
            log.error("Failed to read {} from {}: {}", type.getSimpleName(), file.path(), e.toString(), e);
            return Optional.empty();
        }
    }

    private static Optional<FileHandle> resolveExisting(String filename, Location location, String op, String kind) {
        Objects.requireNonNull(filename, "filename");
        Objects.requireNonNull(location, "location");

        log.debug("{} {} from {} ({})", capitalize(op), kind, filename, location);
        final FileHandle file = getFileHandle(filename, location);
        if (file == null || !file.exists()) {
            log.warn("{} file not found: {} ({})", kind, filename, location);
            return Optional.empty();
        }
        return Optional.of(file);
    }

    private static JsonValue tryParseRootObject(FileHandle file) {
        final var reader = new JsonReader();
        final JsonValue root = reader.parse(file);
        if (root == null || !root.isObject()) {
            log.error("Root is not a JSON object: {}", file.path());
            return null;
        }
        return root;
    }

    private static void processTextureGroup(JsonValue group, FileHandle file, Map<String, String> out) {
        final String groupName = group.name();
        if (groupName == null || !group.isObject() || !groupName.endsWith("Textures")) return;

        for (JsonValue entry = group.child; entry != null; entry = entry.next) {
            handleTextureEntry(groupName, entry, file, out);
        }
    }

    private static void handleTextureEntry(String groupName, JsonValue entry, FileHandle file, Map<String, String> out) {
        final String id = entry.name();
        if (id == null) return;

        if (!entry.isString()) {
            log.warn("Skipping non-string texture path '{}.{}' in {}", groupName, id, file.path());
            return;
        }

        final String path = entry.asString();
        if (path == null || path.isBlank()) {
            log.warn("Skipping blank texture path for id '{}' in group '{}' ({})", id, groupName, file.path());
            return;
        }

        if (out.containsKey(id)) {
            log.warn("Duplicate texture id '{}' encountered in group '{}'; keeping first value '{}', ignoring '{}'",
                    id, groupName, out.get(id), path);
            return;
        }
        out.put(id, path);
    }

    /**
     * Fresh, consistently configured Json instance.
     */
    private static Json newDefaultJson() {
        final Json json = new Json(JsonWriter.OutputType.json);
        json.setTypeName(null); // omit @class type metadata for cleaner JSON
        return json;
    }

    /**
     * Maps our {@link Location} to a LibGDX {@link FileHandle}.
     */
    private static FileHandle getFileHandle(String filename, Location location) {
        Objects.requireNonNull(filename, "filename");
        Objects.requireNonNull(location, "location");
        return switch (location) {
            case CLASSPATH -> Gdx.files.classpath(filename);
            case INTERNAL -> Gdx.files.internal(filename);
            case LOCAL -> Gdx.files.local(filename);
            case EXTERNAL -> Gdx.files.external(filename);
            case ABSOLUTE -> Gdx.files.absolute(filename);
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Storage backends that map to LibGDX file APIs.
     */
    public enum Location {
        CLASSPATH, INTERNAL, LOCAL, EXTERNAL, ABSOLUTE
    }
}

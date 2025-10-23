package com.csse3200.game.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Utility for reading and writing Java objects to/from JSON using LibGDX.
 *
 * <p><strong>Design goals</strong></p>
 * <ul>
 *   <li>No {@code null} returns (uses {@link Optional}).</li>
 *   <li>Fresh, scoped {@link Json} instances for each operation.</li>
 *   <li>Input validation and clear, structured logging via SLF4J 2.x fluent API.</li>
 * </ul>
 *
 * <p><strong>Thread-safety:</strong> LibGDX {@link Json} is not documented as thread-safe.
 * Each method creates and uses a fresh {@code Json} instance to avoid shared mutable state.</p>
 *
 * <p><strong>LibGDX environment:</strong> Methods that touch the filesystem rely on
 * {@link Gdx#files}. Ensure an appropriate LibGDX application (desktop/headless) is initialised
 * before invoking these methods in production or tests.</p>
 *
 * <p><strong>Error handling:</strong> I/O and parse failures are logged and represented as
 * {@code Optional.empty()} where applicable. The API avoids throwing checked exceptions.</p>
 *
 * <p><strong>Example</strong></p>
 * <pre>{@code
 * var player = FileLoader.read(PlayerConfig.class, "config/player.json", FileLoader.Location.INTERNAL)
 *                        .orElseGet(PlayerConfig::new);
 *
 * FileLoader.write(player, "config/player.json", FileLoader.Location.LOCAL, true);
 * }</pre>
 */
public final class FileLoader {
    private static final Logger log = LoggerFactory.getLogger(FileLoader.class);
    private static final String ARG_TYPE = "type";
    private static final String ARG_FILENAME = "filename";
    private static final String ARG_LOCATION = "location";

    private FileLoader() {
        // utility class
    }

    /* --------------------------------- Public API --------------------------------- */

    /**
     * Deserialises an instance of {@code type} from a JSON file at {@code filename} within the given {@code location}.
     * <p>JSON properties in the file override class defaults.</p>
     *
     * @param <T>      the target type
     * @param type     class token for the target type (non-null)
     * @param filename file path appropriate for the given {@code location} (non-null)
     * @param location storage backend that determines how {@code filename} is resolved (non-null)
     * @return an {@link Optional} containing the deserialised object if the file exists and parses successfully;
     * otherwise {@link Optional#empty()}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T> Optional<T> read(Class<T> type, String filename, Location location) {
        Objects.requireNonNull(type, ARG_TYPE);
        final var fhOpt = resolveExisting(filename, location, "read", type.getSimpleName());
        return fhOpt.flatMap(fh -> safeFromJson(type, fh, json -> {
        }));
    }

    /**
     * Specialised reader for {@link SaveGame.GameState} that also configures the element type for the
     * {@code loadedInventory} collection during deserialisation.
     *
     * <p>Specifically, it sets {@code loadedInventory}'s element type to {@link SaveGame.itemInInven} to assist
     * LibGDX JSON in constructing the collection correctly.</p>
     *
     * @param filename file path appropriate for the given {@code location} (non-null)
     * @param location storage backend that determines how {@code filename} is resolved (non-null)
     * @return an {@link Optional} containing the deserialised {@link SaveGame.GameState}; empty if the file
     * does not exist or cannot be parsed
     * @throws NullPointerException if {@code filename} or {@code location} is {@code null}
     */
    public static Optional<SaveGame.GameState> readGameState(String filename, Location location) {
        final var fhOpt = resolveExisting(filename, location, "readGameState", "GameState");
        return fhOpt.flatMap(fh -> safeFromJson(
                SaveGame.GameState.class,
                fh,
                json -> json.setElementType(SaveGame.GameState.class, "loadedInventory", SaveGame.itemInInven.class)
        ));
    }

    /**
     * Serialises {@code object} as JSON and writes it to {@code filename} at {@code location}.
     *
     * <p>If {@code pretty} is {@code true}, the output is pretty-printed; otherwise it is compact.</p>
     *
     * <p>Failures are logged; no exception is thrown. The target directory must already exist if required
     * by the platform-specific {@link FileHandle} implementation.</p>
     *
     * @param object   object to serialise (non-null)
     * @param filename destination path appropriate for {@code location} (non-null)
     * @param location storage backend (non-null)
     * @param pretty   whether to pretty-print the JSON
     * @throws NullPointerException if any required argument is {@code null}
     */
    public static void write(Object object, String filename, Location location, boolean pretty) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(filename, ARG_FILENAME);
        Objects.requireNonNull(location, ARG_LOCATION);

        log.atDebug()
                .setMessage("Writing {} to {} ({})")
                .addArgument(object.getClass().getSimpleName())
                .addArgument(filename)
                .addArgument(location)
                .log();

        final var file = getFileHandle(filename, location);
        if (file == null) {
            log.atError()
                    .setMessage("Unable to obtain FileHandle for {} ({})")
                    .addArgument(filename)
                    .addArgument(location)
                    .log();
            return;
        }

        try {
            final var json = newDefaultJson();
            final var payload = pretty ? json.prettyPrint(object) : json.toJson(object);
            file.writeString(payload, /*append*/ false);
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .setMessage("Failed to write {} to {}")
                    .addArgument(() -> object.getClass().getSimpleName())
                    .addArgument(file::path)
                    .log();
        }
    }

    /**
     * Reads texture asset mappings from a JSON file.
     *
     * <p>Supports two shapes:</p>
     * <ul>
     *   <li>Nested container (preferred):
     *     <pre>{@code
     *     { "textures": { "<group>Textures": { id: "path", ... }, ... } }
     *     }</pre>
     *   </li>
     *   <li>Legacy root-level groups:
     *     <pre>{@code
     *     { "<group>Textures": { id: "path", ... }, ... }
     *     }</pre>
     *   </li>
     * </ul>
     *
     * <p>The JSON member name must end with {@code "Textures"} to be treated as a group.
     * If duplicate ids appear across groups, the first occurrence wins; a warning is logged for duplicates.
     * Non-string or blank values are skipped with a warning.</p>
     *
     * @param filename path to the JSON file appropriate for {@code location} (non-null)
     * @param location storage backend (non-null)
     * @return an {@link Optional} containing a {@link LinkedHashMap} (in insertion order) of {@code id -> path}
     * if at least one valid entry is found; otherwise {@link Optional#empty()}
     * @throws NullPointerException if {@code filename} or {@code location} is {@code null}
     */
    public static Optional<Map<String, String>> readTextureMap(String filename, Location location) {
        final var fhOpt = resolveExisting(filename, location, "readTextureMap", "TextureMap");
        if (fhOpt.isEmpty()) return Optional.empty();

        try {
            final var rootOpt = tryParseRootObject(fhOpt.get());
            if (rootOpt.isEmpty()) return Optional.empty();

            final var out = new LinkedHashMap<String, String>();

            // 1) Preferred: nested "textures" container
            final var texturesContainer = rootOpt.get().get("textures");
            if (texturesContainer != null && texturesContainer.isObject()) {
                for (var group = texturesContainer.child; group != null; group = group.next) {
                    processTextureGroup(group, fhOpt.get(), out);
                }
            }

            // 2) Legacy: also scan root-level groups (first-wins semantics prevent overwriting)
            for (var group = rootOpt.get().child; group != null; group = group.next) {
                processTextureGroup(group, fhOpt.get(), out);
            }

            if (out.isEmpty()) {
                log.atWarn()
                        .setMessage("No '*Textures' sections or entries found in {} (checked 'textures' and root)")
                        .addArgument(() -> fhOpt.map(FileHandle::path).orElse("(unknown)"))
                        .log();
                return Optional.empty();
            }
            return Optional.of(out);
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .setMessage("Failed parsing texture map {}")
                    .addArgument(fhOpt.get()::path)
                    .log();
            return Optional.empty();
        }
    }

    /**
     * Reads level entities from a JSON file that has a top-level "entities" array.
     *
     * <p>Each entity object should be:</p>
     * <pre>{@code
     * {
     *   "name": "<id>",
     *   "type": "<category>",
     *   "location": { "x": <int>, "y": <int> }
     * }
     * }</pre>
     *
     * @param filename path appropriate for the given {@code location}
     * @param location storage backend
     * @return list of parsed {@link MapEntitySpec}, or empty if none valid
     */
    public static Optional<List<MapEntitySpec>> readMapEntities(String filename, Location location) {
        final var fhOpt = resolveExisting(filename, location, "readMapEntities", "Entities");
        if (fhOpt.isEmpty()) return Optional.empty();

        final var file = fhOpt.get();
        try {
            final var rootOpt = tryParseRootObject(file);
            if (rootOpt.isEmpty()) return Optional.empty();

            final var entitiesNode = getEntitiesArray(rootOpt.get(), file);
            if (entitiesNode == null) return Optional.empty();

            final var out = collectValidEntities(entitiesNode, file);
            if (out.isEmpty()) {
                log.atWarn()
                        .setMessage("No valid entities found in {}")
                        .addArgument(file::path)
                        .log();
                return Optional.empty();
            }
            return Optional.of(out);
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .setMessage("Failed reading entities from {}")
                    .addArgument(file::path)
                    .log();
            return Optional.empty();
        }
    }

// --- helpers below ---

    /**
     * Returns the top-level "entities" array or logs and returns null if absent/wrong type.
     */
    private static JsonValue getEntitiesArray(JsonValue root, FileHandle file) {
        final var entitiesNode = root.get("entities");
        if (entitiesNode == null || !entitiesNode.isArray()) {
            log.atWarn()
                    .setMessage("Top-level 'entities' array not found or not an array in {}")
                    .addArgument(file::path)
                    .log();
            return null;
        }
        return entitiesNode;
    }

    /**
     * Iterates the array, validates each item, and collects valid specs.
     */
    private static List<MapEntitySpec> collectValidEntities(JsonValue entitiesNode, FileHandle file) {
        final var out = new ArrayList<MapEntitySpec>();
        for (var item = entitiesNode.child; item != null; item = item.next) {
            tryBuildSpec(item, file).ifPresent(out::add);
        }
        return out;
    }

    /**
     * Attempts to build a MapEntitySpec from a single JSON object, with logging on failures.
     */
    private static Optional<MapEntitySpec> tryBuildSpec(JsonValue item, FileHandle file) {
        if (!item.isObject()) {
            log.atWarn()
                    .setMessage("Skipping non-object entity in {}")
                    .addArgument(file::path)
                    .log();
            return Optional.empty();
        }

        final var nameVal = item.get("name");
        final var typeVal = item.get(ARG_TYPE);
        final var locVal = item.get(ARG_LOCATION);

        if (isMissingEntityMembers(nameVal, typeVal, locVal)) {
            log.atWarn()
                    .setMessage("Skipping entity missing name/type/location in {}: {}")
                    .addArgument(file::path)
                    .addArgument(item::toString)
                    .log();
            return Optional.empty();
        }

        final var name = nameVal.asString();
        final var type = typeVal.asString();
        final var x = locVal.getInt("x", Integer.MIN_VALUE);
        final var y = locVal.getInt("y", Integer.MIN_VALUE);

        if (isInvalidEntityValues(name, type, x, y)) {
            log.atWarn()
                    .setMessage("Skipping invalid entity values in {}: {}")
                    .addArgument(file::path)
                    .addArgument(item::toString)
                    .log();
            return Optional.empty();
        }

        return Optional.of(new MapEntitySpec(name, type, new GridPoint2(x, y)));
    }

    private static boolean isMissingEntityMembers(JsonValue nameVal, JsonValue typeVal, JsonValue locVal) {
        return nameVal == null || typeVal == null || locVal == null || !locVal.isObject();
    }

    private static boolean isInvalidEntityValues(String name, String type, int x, int y) {
        return name == null || name.isBlank()
                || type == null || type.isBlank()
                || x == Integer.MIN_VALUE || y == Integer.MIN_VALUE;
    }


    /**
     * Helper to safely parse JSON into type {@code T} using a supplied configuration hook.
     *
     * @param <T>    target type
     * @param type   class token for the target type
     * @param file   source file
     * @param config callback to configure the fresh {@link Json} instance prior to reading
     * @return {@code Optional.of(value)} if deserialisation succeeds and returns non-null; otherwise empty
     */
    private static <T> Optional<T> safeFromJson(Class<T> type, FileHandle file, Consumer<Json> config) {
        try {
            final var json = newDefaultJson();
            config.accept(json);
            final var obj = json.fromJson(type, file);
            if (obj == null) {
                log.atError()
                        .setMessage("Deserialisation returned null for {} from {}")
                        .addArgument(type::getSimpleName)
                        .addArgument(file::path)
                        .log();
                return Optional.empty();
            }
            log.atDebug()
                    .setMessage("Deserialized {} from {}")
                    .addArgument(type::getSimpleName)
                    .addArgument(file::path)
                    .log();
            return Optional.of(obj);
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .setMessage("Failed to read {} from {}")
                    .addArgument(type::getSimpleName)
                    .addArgument(file::path)
                    .log();
            return Optional.empty();
        }
    }

    /* --------------------------------- Internals --------------------------------- */

    /**
     * Resolves a {@link FileHandle} for {@code filename} and {@code location}, verifying existence.
     * Logs a warning and returns {@link Optional#empty()} if the file is missing.
     *
     * @param filename path relative to the given {@code location}
     * @param location logical storage backend
     * @param op       operation name (for logging only)
     * @param kind     human-readable kind (for logging only)
     * @return optional existing {@link FileHandle}
     */
    private static Optional<FileHandle> resolveExisting(String filename, Location location, String op, String kind) {
        Objects.requireNonNull(filename, ARG_FILENAME);
        Objects.requireNonNull(location, ARG_LOCATION);

        log.atDebug()
                .setMessage("{} {} from {} ({})")
                .addArgument(capitalize(op))
                .addArgument(kind)
                .addArgument(location)
                .addArgument(filename)
                .log();

        final var file = getFileHandle(filename, location);
        if (file == null || !file.exists()) {
            log.atWarn()
                    .setMessage("{} file not found: {} ({})")
                    .addArgument(kind)
                    .addArgument(filename)
                    .addArgument(location)
                    .log();
            return Optional.empty();
        }
        return Optional.of(file);
    }

    /**
     * Parses the root JSON value and verifies it is an object.
     *
     * @param file file to parse
     * @return {@code Optional.of(root)} if parsing succeeds and the root is an object; otherwise empty
     */
    private static Optional<JsonValue> tryParseRootObject(FileHandle file) {
        try {
            final var reader = new JsonReader();
            final var root = reader.parse(file);
            if (root == null || !root.isObject()) {
                log.atError()
                        .setMessage("Root is not a JSON object: {}")
                        .addArgument(file::path)
                        .log();
                return Optional.empty();
            }
            return Optional.of(root);
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .setMessage("Error parsing JSON {}")
                    .addArgument(file::path)
                    .log();
            return Optional.empty();
        }
    }

    /**
     * Processes a single {@code *Textures} object (either under "textures" or at root),
     * adding valid entries into {@code out}.
     *
     * @param group JSON member
     * @param file  file being parsed (for logging context)
     * @param out   output map to populate
     */
    private static void processTextureGroup(JsonValue group, FileHandle file, Map<String, String> out) {
        final var groupName = group.name();
        if (groupName == null || !group.isObject() || !groupName.endsWith("Textures")) return;

        for (var entry = group.child; entry != null; entry = entry.next) {
            handleTextureEntry(groupName, entry, file, out);
        }
    }

    /**
     * Validates and records a single texture mapping entry.
     *
     * @param groupName name of the {@code *Textures} group
     * @param entry     JSON entry mapping {@code id -> path}
     * @param file      file being parsed (for logging context)
     * @param out       output map to populate
     */
    public static void handleTextureEntry(String groupName, JsonValue entry, FileHandle file, Map<String, String> out) {
        final var id = entry.name();
        if (id == null) return;

        if (!entry.isString()) {
            log.atWarn()
                    .setMessage("Skipping non-string texture path (textureId={}, group={}, file={})")
                    .addArgument(id)
                    .addArgument(groupName)
                    .addArgument(file::path)
                    .log();
            return;
        }

        final var path = entry.asString();
        if (path == null || path.isBlank()) {
            log.atWarn()
                    .setMessage("Skipping blank texture path (textureId={}, group={}, file={})")
                    .addArgument(id)
                    .addArgument(groupName)
                    .addArgument(file::path)
                    .log();
            return;
        }

        if (out.containsKey(id)) {
            log.atWarn()
                    .setMessage("Duplicate texture id; keeping first (textureId={}, group={}, kept={}, ignored={})")
                    .addArgument(id)
                    .addArgument(groupName)
                    .addArgument(() -> out.get(id))
                    .addArgument(path)
                    .log();
            return;
        }
        out.put(id, path);
    }

    /**
     * Creates a fresh {@link Json} instance with consistent defaults for this utility.
     * <ul>
     *   <li>{@link JsonWriter.OutputType#json} for stable output.</li>
     *   <li>{@code typeName} disabled to omit {@code @class} metadata.</li>
     * </ul>
     *
     * @return configured {@link Json} instance
     */
    private static Json newDefaultJson() {
        final var json = new Json(JsonWriter.OutputType.json);
        json.setTypeName(null); // omit @class type metadata for cleaner JSON
        return json;
    }

    static FileHandle getFileHandleUnsafe(String filename, String locationName) {
        Objects.requireNonNull(filename, ARG_FILENAME);
        Objects.requireNonNull(locationName, ARG_LOCATION);

        return switch (locationName) {
            case "CLASSPATH" -> Gdx.files.classpath(filename);
            case "INTERNAL" -> Gdx.files.internal(filename);
            case "LOCAL" -> Gdx.files.local(filename);
            case "EXTERNAL" -> Gdx.files.external(filename);
            case "ABSOLUTE" -> Gdx.files.absolute(filename);
            default -> {
                log.atError()
                        .setMessage("Unknown FileLoader.Location: {} (filename={})")
                        .addArgument(locationName)
                        .addArgument(filename)
                        .log();
                yield null;
            }
        };
    }

    /**
     * Maps a logical {@link Location} to a LibGDX {@link FileHandle} using {@link Gdx#files}.
     *
     * @param filename path to resolve
     * @param location storage backend
     * @return a {@link FileHandle} for the given location
     * @throws NullPointerException if {@code filename} or {@code location} is {@code null}
     */
    public static FileHandle getFileHandle(String filename, Location location) {
        Objects.requireNonNull(location, ARG_LOCATION);
        return getFileHandleUnsafe(filename, location.name());
    }


    /**
     * Capitalises the first character of the given string.
     *
     * @param s input string
     * @return capitalised string; returns {@code null} if {@code s} is {@code null}
     */
    private static String capitalize(String s) {
        return switch (s) {
            case null -> null;
            case String t when t.isEmpty() -> t;
            default -> Character.toUpperCase(s.charAt(0)) + s.substring(1);
        };
    }

    /**
     * Storage backends that map to LibGDX file APIs via {@link Gdx#files}.
     *
     * <ul>
     *   <li>{@link #CLASSPATH} → {@link Gdx#files classpath}</li>
     *   <li>{@link #INTERNAL} → {@link Gdx#files internal}</li>
     *   <li>{@link #LOCAL} → {@link Gdx#files local}</li>
     *   <li>{@link #EXTERNAL} → {@link Gdx#files external}</li>
     *   <li>{@link #ABSOLUTE} → {@link Gdx#files absolute}</li>
     * </ul>
     */
    public enum Location {
        /**
         * {@code Gdx.files.classpath(filename)}
         */
        CLASSPATH,
        /**
         * {@code Gdx.files.internal(filename)}
         */
        INTERNAL,
        /**
         * {@code Gdx.files.local(filename)}
         */
        LOCAL,
        /**
         * {@code Gdx.files.external(filename)}
         */
        EXTERNAL,
        /**
         * {@code Gdx.files.absolute(filename)}
         */
        ABSOLUTE
    }

    /**
     * Minimal entity spec for level placement: name, type, and grid location.
     */
    public record MapEntitySpec(String name, String type, GridPoint2 location) {
    }
}

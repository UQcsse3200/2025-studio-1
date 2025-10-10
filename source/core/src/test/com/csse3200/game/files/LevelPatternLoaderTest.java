package com.csse3200.game.files;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LevelPatternLoaderTest {
    private String rootDir;
    private Method join;

    static Stream<Object[]> cases() {
        return Stream.of(
                // no trailing slash in dir
                new Object[]{"a/b", "c", "a/b/c"},
                // trailing slash in dir
                new Object[]{"a/b/", "c", "a/b/c"},
                // root dir
                new Object[]{"/", "file", "/file"},
                // empty dir
                new Object[]{"", "file", "/file"},
                // empty name
                new Object[]{"a/b", "", "a/b/"},
                new Object[]{"a/b/", "", "a/b/"},
                // both empty -> produces single slash
                new Object[]{"", "", "/"},
                // name starts with slash -> current behavior yields double slash
                new Object[]{"a/b", "/file", "a/b//file"},
                new Object[]{"a/b/", "/file", "a/b//file"}
        );
    }

    @Test
    void dirNull_triggersEarlyReturn() {
        // Arrange
        Files original = Gdx.files;
        Files nullFiles = new Files() {
            @Override
            public FileHandle getFileHandle(String p, FileType t) {
                return null;
            }

            @Override
            public FileHandle classpath(String p) {
                return null;
            }

            @Override
            public FileHandle internal(String p) {
                return null;
            }

            @Override
            public FileHandle external(String p) {
                return null;
            }

            @Override
            public FileHandle absolute(String p) {
                return null;
            }

            @Override
            public FileHandle local(String p) {
                return null;
            }

            @Override
            public String getExternalStoragePath() {
                return "";
            }

            @Override
            public boolean isExternalStorageAvailable() {
                return false;
            }

            @Override
            public String getLocalStoragePath() {
                return "";
            }

            @Override
            public boolean isLocalStorageAvailable() {
                return true;
            }
        };

        var assets = mock(AssetManager.class);
        var placer = mock(LevelPatternLoader.EntityPlacer.class);
        var loader = new LevelPatternLoader(assets, FileLoader.Location.LOCAL, placer, false);

        try {
            Gdx.files = nullFiles; // make FileLoader.getFileHandle(...) yield null
            // Act
            var out = loader.loadAll("any/dir", "*.json");
            // Assert
            assertTrue(out.isEmpty(), "dir == null should early-return empty");
            verifyNoInteractions(assets, placer);
        } finally {
            Gdx.files = original; // restore
        }
    }

    @BeforeAll
    void createRoot() {
        rootDir = "unit-tests/level-pattern/" + UUID.randomUUID();
        Gdx.files.local(rootDir).mkdirs();
    }

    // ---------------- compileGlob ----------------

    @AfterAll
    void cleanup() {
        FileHandle root = Gdx.files.local(rootDir);
        if (root.exists()) root.deleteDirectory();
    }


    // ---------------- loadAll: missing dir ----------------

    private String p(String rel) {
        return rootDir + "/" + rel;
    }

    // ---------------- loadAll: no matches ----------------

    @ParameterizedTest(name = "glob={0} tested={1} => {2}")
    @CsvSource({
            // '*' matches any non-slash sequence
            "'*.json','level1.json',true",
            "'*.json','a.b.json',true",
            "'*.json','folder/lev.json',false",  // '*' doesn't cross '/'

            // '?' matches exactly one char
            "'file?.dat','file1.dat',true",
            "'file?.dat','file10.dat',false",    // TWO chars → no match

            // dots and metachars are escaped
            "'a.b','a.b',true",
            "'a.b','aXb',false",

            // sanity negatives
            "'*.json','readme.txt',false",
            "'file?.dat','file.dat',false"       // zero chars where one is required
    })
    void compileGlob_behaviour(String glob, String tested, boolean expected) {
        var pat = LevelPatternLoader.compileGlob(glob);
        assertEquals(expected, pat.matcher(tested).matches(),
                () -> "Glob=" + glob + " tested=" + tested);
    }

    // ---------------- loadAll: happy path ----------------

    @Test
    void loadAll_missingDirectory_returnsEmpty() {
        AssetManager assets = mock(AssetManager.class);
        LevelPatternLoader.EntityPlacer placer = mock(LevelPatternLoader.EntityPlacer.class);

        LevelPatternLoader loader = new LevelPatternLoader(
                assets, FileLoader.Location.LOCAL, placer, /*sync*/ true);

        String missing = p("does-not-exist");
        var out = loader.loadAll(missing, "*.json");
        assertTrue(out.isEmpty());
        verifyNoInteractions(assets, placer);
    }

    @Test
    void loadAll_noMatches_returnsEmpty_andWarns() {
        // create an empty directory (or files that don't match)
        String dir = p("nomatch");
        Gdx.files.local(dir).mkdirs();
        // create a file that won't match *.json
        Gdx.files.local(dir + "/readme.txt").writeString("hi", false);

        AssetManager assets = mock(AssetManager.class);
        LevelPatternLoader.EntityPlacer placer = mock(LevelPatternLoader.EntityPlacer.class);
        LevelPatternLoader loader = new LevelPatternLoader(
                assets, FileLoader.Location.LOCAL, placer, /*sync*/ false);

        var out = loader.loadAll(dir, "*.json");
        assertTrue(out.isEmpty());
        verifyNoInteractions(assets, placer);
    }

    @Test
    void loadAll_matches_areSorted_texturesDeduped_assetLoadAndPlacement_called() {
        String dir = p("levels");
        Gdx.files.local(dir).mkdirs();

        // two files out of order to ensure sorting
        Gdx.files.local(dir + "/b.json").writeString("{}", false);
        Gdx.files.local(dir + "/a.json").writeString("{}", false);

        // Mocks
        AssetManager assets = mock(AssetManager.class);
        LevelPatternLoader.EntityPlacer placer = mock(LevelPatternLoader.EntityPlacer.class);

        // Mock LevelData instances (with different textures + overlap) and entities
        var dataA = mock(LevelIO.LevelData.class);
        var dataB = mock(LevelIO.LevelData.class);

        // textures: ensure de-dup occurs across files
        Map<String, String> texA = new LinkedHashMap<>();
        texA.put("button", "ui/button.png");
        texA.put("grass", "world/grass.png");  // overlap
        Map<String, String> texB = new LinkedHashMap<>();
        texB.put("grass", "world/grass.png");  // duplicate
        texB.put("label", "ui/label.png");

        when(dataA.textures()).thenReturn(texA);
        when(dataB.textures()).thenReturn(texB);

        var e1 = new FileLoader.MapEntitySpec("p1", "player", new com.badlogic.gdx.math.GridPoint2(1, 1));
        var e2 = new FileLoader.MapEntitySpec("t1", "tree", new com.badlogic.gdx.math.GridPoint2(2, 3));
        var e3 = new FileLoader.MapEntitySpec("npc", "ally", new com.badlogic.gdx.math.GridPoint2(0, 0));
        when(dataA.entities()).thenReturn(List.of(e1, e2));
        when(dataB.entities()).thenReturn(List.of(e3));

        // Static mock LevelIO.load to return Optionals per file path
        try (MockedStatic<LevelIO> mocked = mockStatic(LevelIO.class)) {
            // NOTE: LevelPatternLoader joins path as directory + "/" + f.name()
            mocked.when(() -> LevelIO.load(dir + "/a.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(dataA));
            mocked.when(() -> LevelIO.load(dir + "/b.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(dataB));

            LevelPatternLoader loader = new LevelPatternLoader(assets, FileLoader.Location.LOCAL, placer, true);
            var loaded = loader.loadAll(dir, "*.json");

            // Order: a.json then b.json (sorted by file name)
            assertEquals(List.of(dataA, dataB), loaded);

            // Asset loads: unique texture paths only
            verify(assets, times(1)).load("ui/button.png", com.badlogic.gdx.graphics.Texture.class);
            verify(assets, times(1)).load("world/grass.png", com.badlogic.gdx.graphics.Texture.class);
            verify(assets, times(1)).load("ui/label.png", com.badlogic.gdx.graphics.Texture.class);

            // Because synchronousTextureLoad=true
            verify(assets, times(1)).finishLoading();

            // Placement for each entity, and continues even if a placement throws
            doThrow(new RuntimeException("boom")).when(placer).place(e2);

            // Re-run to exercise placement catch path
            loader.loadAll(dir, "*.json");
            verify(placer, atLeastOnce()).place(e1);
            verify(placer, atLeastOnce()).place(e2);
            verify(placer, atLeastOnce()).place(e3);
        }
    }

    @BeforeAll
    void reflect() throws Exception {
        join = LevelPatternLoader.class.getDeclaredMethod("join", String.class, String.class);
        join.setAccessible(true);
    }

    @ParameterizedTest(name = "{index}: join(\"{0}\", \"{1}\") => \"{2}\"")
    @MethodSource("cases")
    void join_behaviour(String dir, String name, String expected) throws Exception {
        String actual = (String) join.invoke(null, dir, name);
        assertEquals(expected, actual);
    }

    @Test
    void loadAll_nullDirectory_throwsNpe() {
        var loader = new LevelPatternLoader(new AssetManager(), FileLoader.Location.LOCAL, spec -> {
        }, false);
        assertThrows(NullPointerException.class, () -> loader.loadAll(null, "*.json"));
    }

    @Test
    void loadAll_nullGlob_throwsNpe() {
        var loader = new LevelPatternLoader(new AssetManager(), FileLoader.Location.LOCAL, spec -> {
        }, false);
        assertThrows(NullPointerException.class, () -> loader.loadAll("someDir", null));
    }

    /* ------------- dir missing / not directory -> empty ------------- */

    @Test
    void loadAll_missingDir_returnsEmpty() {
        var loader = new LevelPatternLoader(new AssetManager(), FileLoader.Location.LOCAL, spec -> {
        }, true);
        var out = loader.loadAll(p("does-not-exist"), "*.json");
        assertTrue(out.isEmpty());
    }

    @Test
    void loadAll_pathIsFile_notDirectory_returnsEmpty() {
        String file = p("not-a-dir");
        Gdx.files.local(file).writeString("hi", false); // create a file, not a directory
        var loader = new LevelPatternLoader(new AssetManager(), FileLoader.Location.LOCAL, spec -> {
        }, true);
        var out = loader.loadAll(file, "*.json");
        assertTrue(out.isEmpty());
    }

    /* ------------------------ no matches branch ------------------------ */

    @Test
    void loadAll_noMatches_warnsAndReturnsEmpty() {
        String dir = p("no-matches");
        Gdx.files.local(dir).mkdirs();
        // Create files that won't match *.json
        Gdx.files.local(dir + "/readme.txt").writeString("text", false);
        Gdx.files.local(dir + "/script.js").writeString("console.log()", false);

        var loader = new LevelPatternLoader(new AssetManager(), FileLoader.Location.LOCAL, spec -> {
        }, false);
        var out = loader.loadAll(dir, "*.json");
        assertTrue(out.isEmpty());
    }

    /* ------------------------- happy + branches ------------------------- */

    @Test
    void loadAll_filtersSorts_parses_dedupTextures_loads_finishAndPlaces_withCatch() {
        String dir = p("levels");
        Gdx.files.local(dir).mkdirs();

        // Create files out of order plus a subdirectory (to ensure it's filtered out)
        Gdx.files.local(dir + "/b.json").writeString("{}", false);
        Gdx.files.local(dir + "/a.json").writeString("{}", false);
        Gdx.files.local(dir + "/sub").mkdirs(); // ensure subdir is skipped

        // Mocks
        AssetManager assets = mock(AssetManager.class);
        LevelPatternLoader.EntityPlacer placer = mock(LevelPatternLoader.EntityPlacer.class);

        // Prepare LevelData mocks
        LevelIO.LevelData dataA = mock(LevelIO.LevelData.class);
        LevelIO.LevelData dataB = mock(LevelIO.LevelData.class);

        // textures: dedupe + isLoaded branch
        Map<String, String> texA = new LinkedHashMap<>();
        texA.put("button", "ui/button.png");          // already loaded → skip
        texA.put("grass", "world/grass.png");        // new → load

        Map<String, String> texB = new LinkedHashMap<>();
        texB.put("grass", "world/grass.png");        // duplicate across files → no extra load
        texB.put("label", "ui/label.png");           // new → load

        when(dataA.textures()).thenReturn(texA);
        when(dataB.textures()).thenReturn(texB);

        // entities for placement; one will throw to hit catch-and-continue
        var e1 = new FileLoader.MapEntitySpec("p1", "player", new GridPoint2(1, 1));
        var e2 = new FileLoader.MapEntitySpec("t1", "tree", new GridPoint2(2, 3));
        var e3 = new FileLoader.MapEntitySpec("npc", "ally", new GridPoint2(0, 0));
        when(dataA.entities()).thenReturn(List.of(e1, e2));
        when(dataB.entities()).thenReturn(List.of(e3));

        // assets.isLoaded branch: make "ui/button.png" appear preloaded
        when(assets.isLoaded("ui/button.png")).thenReturn(true);
        when(assets.isLoaded("world/grass.png")).thenReturn(false);
        when(assets.isLoaded("ui/label.png")).thenReturn(false);

        try (MockedStatic<LevelIO> mocked = mockStatic(LevelIO.class)) {
            // join(directory, f.name()) produces dir + "/" + name
            mocked.when(() -> LevelIO.load(dir + "/a.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(dataA));
            mocked.when(() -> LevelIO.load(dir + "/b.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(dataB));

            // sync textures = true → calls finishLoading()
            LevelPatternLoader loaderSync = new LevelPatternLoader(assets, FileLoader.Location.LOCAL, placer, true);
            var loaded = loaderSync.loadAll(dir, "*.json");

            // Sorted by filename: a.json then b.json
            assertEquals(List.of(dataA, dataB), loaded);

            // Asset loads: only for not-already-loaded & unique paths
            verify(assets, times(0)).load("ui/button.png", com.badlogic.gdx.graphics.Texture.class);
            verify(assets, times(1)).load("world/grass.png", com.badlogic.gdx.graphics.Texture.class);
            verify(assets, times(1)).load("ui/label.png", com.badlogic.gdx.graphics.Texture.class);

            // finishLoading called because synchronousTextureLoad==true
            verify(assets, times(1)).finishLoading();

            // Placement: e1 then e2 (throw once), but continues to e3
            doThrow(new RuntimeException("boom")).when(placer).place(e2);

            // run again to execute placement branch with the throw
            loaderSync.loadAll(dir, "*.json");
            verify(placer, atLeastOnce()).place(e1);
            verify(placer, atLeastOnce()).place(e2);
            verify(placer, atLeastOnce()).place(e3);
        }
    }

    @Test
    void loadAll_asyncTextureLoad_doesNotCallFinishLoading() {
        String dir = p("levels-async");
        Gdx.files.local(dir).mkdirs();
        Gdx.files.local(dir + "/only.json").writeString("{}", false);

        AssetManager assets = mock(AssetManager.class);
        LevelPatternLoader.EntityPlacer placer = mock(LevelPatternLoader.EntityPlacer.class);

        LevelIO.LevelData data = mock(LevelIO.LevelData.class);
        when(data.textures()).thenReturn(Map.of("button", "ui/button.png"));
        when(data.entities()).thenReturn(List.of());

        try (MockedStatic<LevelIO> mocked = mockStatic(LevelIO.class)) {
            mocked.when(() -> LevelIO.load(dir + "/only.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(data));

            LevelPatternLoader loader = new LevelPatternLoader(assets, FileLoader.Location.LOCAL, placer, false);
            loader.loadAll(dir, "*.json");

            verify(assets, never()).finishLoading(); // async path
        }
    }

    /* -------------------- tiny sanity for compileGlob -------------------- */

    @Test
    void compileGlob_starDoesNotCrossSlash_andQuestionIsOneChar() {
        var star = LevelPatternLoader.compileGlob("*.json");
        assertTrue(star.matcher("level.json").matches());
        assertFalse(star.matcher("a/b.json").matches()); // '*' = [^/]*

        var q = LevelPatternLoader.compileGlob("file?.dat");
        assertTrue(q.matcher("file1.dat").matches());
        assertFalse(q.matcher("file10.dat").matches());
    }

    @Test
    void onlyRegularFilesPassPredicate_andThenGlobFilters() {
        // Dir contains: one matching file, one non-matching file, one subdirectory
        var dir = rootDir + "/d";
        Gdx.files.local(dir).mkdirs();
        Gdx.files.local(dir + "/ok.json").writeString("{}", false);  // should pass predicate + glob
        Gdx.files.local(dir + "/note.txt").writeString("..", false); // pass predicate, fail glob
        Gdx.files.local(dir + "/sub").mkdirs();                      // fail predicate (!isDirectory())

        var assets = mock(AssetManager.class);
        var placer = mock(LevelPatternLoader.EntityPlacer.class);
        var loader = new LevelPatternLoader(assets, FileLoader.Location.LOCAL, placer, false);

        // Mock LevelIO.load only for OK.json; ensure it returns a LevelData with no textures/entities
        var data = mock(LevelIO.LevelData.class);
        when(data.textures()).thenReturn(Collections.emptyMap());
        when(data.entities()).thenReturn(Collections.emptyList());

        try (MockedStatic<LevelIO> st = mockStatic(LevelIO.class)) {
            st.when(() -> LevelIO.load(dir + "/ok.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(data));

            var out = loader.loadAll(dir, "*.json");

            // Only the matching regular file should be parsed/loaded
            assertEquals(List.of(data), out);
            verifyNoInteractions(assets, placer); // no textures, no entities ⇒ no calls
        }
    }

    @Test
    void filter_existsAndNotDirectory_hits_true_and_false() {
        // Arrange: one matching file, one subdirectory (to force predicate false)
        String dir = rootDir + "/d";
        Gdx.files.local(dir).mkdirs();
        Gdx.files.local(dir + "/a.json").writeString("{}", false); // exists == true, isDirectory == false → predicate true
        Gdx.files.local(dir + "/sub").mkdirs();                    // exists == true, isDirectory == true  → predicate false

        // Minimal LevelData (no textures/entities needed for this coverage)
        var data = mock(LevelIO.LevelData.class);
        when(data.textures()).thenReturn(Collections.emptyMap());
        when(data.entities()).thenReturn(Collections.emptyList());

        try (MockedStatic<LevelIO> st = mockStatic(LevelIO.class)) {
            // LevelPatternLoader.join(directory, f.name()) builds dir + "/" + name
            st.when(() -> LevelIO.load(dir + "/a.json", FileLoader.Location.LOCAL))
                    .thenReturn(Optional.of(data));

            var assets = mock(AssetManager.class);
            var placer = mock(LevelPatternLoader.EntityPlacer.class);
            var loader = new LevelPatternLoader(assets, FileLoader.Location.LOCAL, placer, false);

            // Act: glob matches only the file; the stream still evaluates predicate for both entries
            var out = loader.loadAll(dir, "*.json");

            // Assert: only the file passed the predicate + glob, subdirectory was filtered out
            assertEquals(List.of(data), out);
            verifyNoInteractions(assets, placer); // no textures/entities
        }
    }

    @Test
    void predicate_existsAndNotDirectory_allBranchesCovered() {
        final String directory = "virt"; // arbitrary logical dir used by join()
        final var location = FileLoader.Location.LOCAL;

        // Mocks: directory handle and its children
        FileHandle dir = mock(FileHandle.class);
        when(dir.exists()).thenReturn(true);
        when(dir.isDirectory()).thenReturn(true);

        FileHandle ghost = mock(FileHandle.class); // exists == false → LHS false
        when(ghost.exists()).thenReturn(false);
        when(ghost.isDirectory()).thenReturn(false);
        when(ghost.name()).thenReturn("ghost.json");

        FileHandle sub = mock(FileHandle.class); // exists true, isDirectory true → RHS false
        when(sub.exists()).thenReturn(true);
        when(sub.isDirectory()).thenReturn(true);
        when(sub.name()).thenReturn("sub");

        FileHandle ok = mock(FileHandle.class); // exists true, isDirectory false → passes
        when(ok.exists()).thenReturn(true);
        when(ok.isDirectory()).thenReturn(false);
        when(ok.name()).thenReturn("ok.json");

        when(dir.list()).thenReturn(new FileHandle[]{ghost, sub, ok});

        // Minimal LevelData (no textures/entities required for this assertion)
        LevelIO.LevelData data = mock(LevelIO.LevelData.class);
        when(data.textures()).thenReturn(Collections.emptyMap());
        when(data.entities()).thenReturn(Collections.emptyList());

        try (MockedStatic<FileLoader> sf = mockStatic(FileLoader.class);
             MockedStatic<LevelIO> sl = mockStatic(LevelIO.class)) {

            // FileLoader.getFileHandle(...) should return our mocked dir
            sf.when(() -> FileLoader.getFileHandle(directory, location)).thenReturn(dir);

            // LevelIO.load called only for the file that survives the predicate + glob
            sl.when(() -> LevelIO.load(directory + "/ok.json", location))
                    .thenReturn(java.util.Optional.of(data));

            AssetManager assets = mock(AssetManager.class);
            LevelPatternLoader.EntityPlacer placer = mock(LevelPatternLoader.EntityPlacer.class);

            LevelPatternLoader loader = new LevelPatternLoader(assets, location, placer, false);
            var out = loader.loadAll(directory, "*.json");

            // Only ok.json should pass the predicate and the glob
            assertEquals(List.of(data), out);

            // No textures/entities → no further interactions needed
            verifyNoInteractions(assets, placer);
        }
    }
}

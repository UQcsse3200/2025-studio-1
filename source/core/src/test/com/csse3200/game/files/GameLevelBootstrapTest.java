package com.csse3200.game.files;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.csse3200.game.files.GameLevelBootstrap.defaultPlacer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(GameExtension.class)
class GameLevelBootstrapTest {
    private Method parseVariant;
    private Method spawnEnemy;

    private static List<LevelIO.LevelData> getLevelData() {
        Map<String, String> tex1 = new LinkedHashMap<>();
        tex1.put("intro", "audio/intro.mp3");     // audio
        tex1.put("amb", "audio/amb.ogg");       // audio
        tex1.put("btn", "ui/button.png");       // texture

        Map<String, String> tex2 = new LinkedHashMap<>();
        tex2.put("click", "audio/click.wav");     // audio
        tex2.put("grass", "world/grass.png");     // texture

        LevelIO.LevelData ld1 = new LevelIO.LevelData(tex1, List.of());
        LevelIO.LevelData ld2 = new LevelIO.LevelData(tex2, List.of());
        return List.of(ld1, ld2);
    }
    /* --------------------- loadDirectoryAndPlace --------------------- */

    static Stream<Object[]> cases_playerPresent() {
        return Stream.of(
                new Object[]{"ghostgpt", "ggpt"},
                new Object[]{"ghostgptred", "ggptRed"},
                new Object[]{"ghostgptblue", "ggptBlue"},

                new Object[]{"deepspin", "deep"},
                new Object[]{"deepspinred", "deepRed"},
                new Object[]{"deepspinblue", "deepBlue"},

                new Object[]{"vroomba", "vroomba"},
                new Object[]{"vroombared", "vroombaRed"},
                new Object[]{"vroombablue", "vroombaBlue"},

                new Object[]{"grokdroid", "grok"},
                new Object[]{"grokdroidred", "grokRed"},
                new Object[]{"grokdroidblue", "grokBlue"},

                new Object[]{"turret", "turret"},

                new Object[]{"", "ggpt"},         // no subtype → GhostGPT
                new Object[]{"unknown", "ggpt"}          // default → GhostGPT
        );
    }

    /**
     * Only need one variant to prove the "player == null → create at (10,10)" branch.
     */
    static Stream<String> cases_playerMissing() {
        return Stream.of("ghostgpt", ""); // both route to createGhostGPT
    }

    /**
     * Helper to stub the specific static factory matched by the key.
     */
    private static void stubFactory(MockedStatic<NPCFactory> nf,
                                    String key,
                                    Entity player,
                                    GameArea area,
                                    Entity made) {
        switch (key) {
            case "ggpt" -> nf.when(() -> NPCFactory.createGhostGPT(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);
            case "ggptRed" -> nf.when(() -> NPCFactory.createGhostGPTRed(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);
            case "ggptBlue" -> nf.when(() -> NPCFactory.createGhostGPTBlue(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);

            case "deep" -> nf.when(() -> NPCFactory.createDeepspin(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);
            case "deepRed" -> nf.when(() -> NPCFactory.createDeepspinRed(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);
            case "deepBlue" -> nf.when(() -> NPCFactory.createDeepspinBlue(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);

            case "vroomba" -> nf.when(() -> NPCFactory.createVroomba(eq(player), anyFloat()))
                    .thenReturn(made);
            case "vroombaRed" -> nf.when(() -> NPCFactory.createVroombaRed(eq(player), anyFloat()))
                    .thenReturn(made);
            case "vroombaBlue" -> nf.when(() -> NPCFactory.createVroombaBlue(eq(player), anyFloat()))
                    .thenReturn(made);

            case "grok" -> nf.when(() -> NPCFactory.createGrokDroid(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);
            case "grokRed" -> nf.when(() -> NPCFactory.createGrokDroidRed(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);
            case "grokBlue" -> nf.when(() -> NPCFactory.createGrokDroidBlue(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);

            case "turret" -> nf.when(() -> NPCFactory.createTurret(eq(player), eq(area), anyFloat()))
                    .thenReturn(made);

            default -> throw new IllegalArgumentException("Unknown key: " + key);
        }
    }

    private static FileLoader.MapEntitySpec spec(String name, String type, int x, int y) {
        return new FileLoader.MapEntitySpec(name, type, new GridPoint2(x, y));
    }

    @BeforeEach
    void reflect() throws Exception {
        parseVariant = GameLevelBootstrap.class.getDeclaredMethod("parseVariant", String.class);
        parseVariant.setAccessible(true);
        spawnEnemy = GameLevelBootstrap.class.getDeclaredMethod(
                "spawnEnemy", com.csse3200.game.areas.GameArea.class, String.class,
                com.badlogic.gdx.math.GridPoint2.class, String.class);
        spawnEnemy.setAccessible(true);
    }

    @Test
    void isAudio_returnsFalse_whenPathIsNull() throws Exception {
        Method m = GameLevelBootstrap.class.getDeclaredMethod("isAudio", String.class);
        m.setAccessible(true);

        // null branch
        boolean result = (boolean) m.invoke(null, (Object) null);
        assertFalse(result, "isAudio(null) must return false");
    }

    @ParameterizedTest(name = "{index}: \"{0}\" -> \"{1}\"")
    @CsvSource({
            // TRUE branch: i>=0 && i+1<len
            "enemy:ghostgpt, ghostgpt",
            ":start, start",
            "enemy:deep:blue, deep:blue",

            // FALSE branch cases:
            // - no colon (i<0)
            "enemy, ",
            // - colon at end (i+1 == len)
            "enemy:, ",
            // - empty string (i<0)
            "'', "
    })
    void parseVariant_coversAllBranches(String in, String expected) throws Exception {
        String out = (String) parseVariant.invoke(null, in);
        // expected empty -> CsvSource passes empty as "", which is fine (we want "")
        assertEquals(expected == null ? "" : expected, out);
    }

    @Test
    void loadDirectoryAndPlace_noAudio_noFinishLoading() {
        AssetManager assets = mock(AssetManager.class);
        RegistryEntityPlacer placer = new RegistryEntityPlacer((n, t, g) -> {
        });

        // One level with only textures (no audio)
        Map<String, String> tex = new LinkedHashMap<>();
        tex.put("button", "ui/button.png");
        tex.put("grass", "world/grass.png");
        LevelIO.LevelData ld = new LevelIO.LevelData(tex, List.of());
        List<LevelIO.LevelData> levels = List.of(ld);

        try (MockedConstruction<LevelPatternLoader> ignored =
                     mockConstruction(LevelPatternLoader.class, (mock, ctx) ->
                             when(mock.loadAll("dir", "*.json")).thenReturn(levels))) {

            // Texture loaded or not shouldn’t matter here for finishLoading
            when(assets.isLoaded("ui/button.png", Texture.class)).thenReturn(true);
            when(assets.isLoaded("world/grass.png", Texture.class)).thenReturn(false);

            var out = GameLevelBootstrap.loadDirectoryAndPlace(
                    assets, FileLoader.Location.LOCAL, "dir", "*.json", placer, /*syncTextureLoad*/ true);

            assertEquals(levels, out);
            // No audio → finishLoading must not be called by GameLevelBootstrap
            verify(assets, never()).finishLoading();
            // No Music/Sound loads
            verify(assets, never()).load(anyString(), eq(Music.class));
            verify(assets, never()).load(anyString(), eq(Sound.class));
        }
    }

    @Test
    void loadDirectoryAndPlace_withAudio_loadsMusicAndSound_andFinishLoadingWhenSync() {
        AssetManager assets = mock(AssetManager.class);
        RegistryEntityPlacer placer = new RegistryEntityPlacer((n, t, g) -> {
        });

        // Mixed textures & audio
        List<LevelIO.LevelData> levels = getLevelData();

        try (MockedConstruction<LevelPatternLoader> ignored =
                     mockConstruction(LevelPatternLoader.class, (mock, ctx) ->
                             when(mock.loadAll("dir", "*.json")).thenReturn(levels))) {

            // Already-loaded checks for audio (ensure skip-paths are honored)
            when(assets.isLoaded("audio/intro.mp3", Music.class)).thenReturn(true);
            when(assets.isLoaded("audio/intro.mp3", Sound.class)).thenReturn(false);
            when(assets.isLoaded("audio/amb.ogg", Music.class)).thenReturn(false);
            when(assets.isLoaded("audio/amb.ogg", Sound.class)).thenReturn(false);
            when(assets.isLoaded("audio/click.wav", Music.class)).thenReturn(false);
            when(assets.isLoaded("audio/click.wav", Sound.class)).thenReturn(true);

            // textures may or may not be already queued; bootstrap only logs about them
            when(assets.isLoaded("ui/button.png", Texture.class)).thenReturn(false);
            when(assets.isLoaded("world/grass.png", Texture.class)).thenReturn(true);

            // sync=true -> finishLoading must be called when audio present
            GameLevelBootstrap.loadDirectoryAndPlace(
                    assets, FileLoader.Location.LOCAL, "dir", "*.json", placer, /*syncTextureLoad*/ true);

            // For each audio path: load Music if not loaded, and Sound if not loaded
            verify(assets, never()).load("audio/intro.mp3", Music.class); // already loaded
            verify(assets, times(1)).load("audio/intro.mp3", Sound.class);        // not loaded as Sound

            verify(assets, times(1)).load("audio/amb.ogg", Music.class);
            verify(assets, times(1)).load("audio/amb.ogg", Sound.class);

            verify(assets, times(1)).load("audio/click.wav", Music.class);
            verify(assets, never()).load("audio/click.wav", Sound.class); // already loaded as Sound

            // finishLoading executed because audio present and sync=true
            verify(assets, times(1)).finishLoading();
        }
    }

    @Test
    void loadDirectoryAndPlace_withAudio_noFinishWhenAsync() {
        AssetManager assets = mock(AssetManager.class);
        RegistryEntityPlacer placer = new RegistryEntityPlacer((n, t, g) -> {
        });

        Map<String, String> tex = new LinkedHashMap<>();
        tex.put("intro", "audio/intro.mp3");
        LevelIO.LevelData ld = new LevelIO.LevelData(tex, List.of());

        try (MockedConstruction<LevelPatternLoader> ignored =
                     mockConstruction(LevelPatternLoader.class, (mock, ctx) ->
                             when(mock.loadAll("dir", "*.json")).thenReturn(List.of(ld)))) {

            when(assets.isLoaded("audio/intro.mp3", Music.class)).thenReturn(false);
            when(assets.isLoaded("audio/intro.mp3", Sound.class)).thenReturn(false);

            GameLevelBootstrap.loadDirectoryAndPlace(
                    assets, FileLoader.Location.LOCAL, "dir", "*.json", placer, /*syncTextureLoad*/ false);

            // loaded as both, but no finishLoading
            verify(assets, times(1)).load("audio/intro.mp3", Music.class);
            verify(assets, times(1)).load("audio/intro.mp3", Sound.class);
            verify(assets, never()).finishLoading();
        }
    }

    @Test
    void defaultPlacer_spawnsPlayerIfMissing_beforeEnemyCreation_andUnknownSubtypeFallsBack() {
        GameArea area = mock(GameArea.class);
        Entity createdPlayer = mock(Entity.class);
        when(area.getBaseDifficultyScale()).thenReturn(1.0f);
        when(area.spawnOrRepositionPlayer(any(GridPoint2.class))).thenReturn(createdPlayer);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<NPCFactory> nf = mockStatic(NPCFactory.class)) {

            // No player present initially
            sl.when(ServiceLocator::getPlayer).thenReturn(null);

            Entity ghost = mock(Entity.class);
            nf.when(() -> NPCFactory.createGhostGPT(createdPlayer, area, 1.0f)).thenReturn(ghost);

            RegistryEntityPlacer placer = defaultPlacer(area);

            GridPoint2 g = new GridPoint2(7, 7);
            // “enemy:Unknown” → unknown subtype branch -> warn + GhostGPT
            placer.place(new FileLoader.MapEntitySpec("x", "enemy:unknownThing", g));

            // Player must have been spawned first at (10,10) per bootstrap
            verify(area, times(1)).spawnOrRepositionPlayer(new GridPoint2(10, 10));
            // And then enemy spawned
            verify(area, times(1)).spawnEntityAt(ghost, g, true, true);
        }
    }

    @ParameterizedTest(name = "player present: variant=\"{0}\"")
    @MethodSource("cases_playerPresent")
    void spawnEnemy_playerPresent_callsExpectedFactory_andSpawns(String variantLower, String key) {
        GameArea area = mock(GameArea.class);
        Entity player = mock(Entity.class);
        Entity made = mock(Entity.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<NPCFactory> nf = mockStatic(NPCFactory.class)) {

            // Player already present
            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            // Map each key to a stubbed static factory returning "made"
            stubFactory(nf, key, player, area, made);

            GridPoint2 grid = new GridPoint2(4, 5);

            // Exercise
            assertDoesNotThrow(() -> spawnEnemy.invoke(null, area, variantLower, grid, "enemyName"));

            // Verify the entity from the factory is spawned centered on the tile
            verify(area, times(1)).spawnEntityAt(made, grid, true, true);
            // No default player spawn in this branch
            verify(area, never()).spawnOrRepositionPlayer(new GridPoint2(10, 10));
        }
    }

    @ParameterizedTest(name = "player missing: variant=\"{0}\"")
    @MethodSource("cases_playerMissing")
    void spawnEnemy_playerMissing_spawnsPlayerThenEnemy(String variantLower) {
        GameArea area = mock(GameArea.class);
        Entity createdPlayer = mock(Entity.class);
        Entity made = mock(Entity.class);

        // When default-created, the code uses (10,10)
        when(area.spawnOrRepositionPlayer(new GridPoint2(10, 10))).thenReturn(createdPlayer);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<NPCFactory> nf = mockStatic(NPCFactory.class)) {

            // No player present initially
            sl.when(ServiceLocator::getPlayer).thenReturn(null);

            // Stub only GhostGPT (covers "" and default path too)
            nf.when(() -> NPCFactory.createGhostGPT(any(Entity.class), eq(area), anyFloat()))
                    .thenReturn(made);

            GridPoint2 grid = new GridPoint2(1, 2);

            // Exercise
            assertDoesNotThrow(() -> spawnEnemy.invoke(null, area, variantLower, grid, "eX"));

            // Ensure player created first at (10,10), then enemy spawned
            verify(area, times(1)).spawnOrRepositionPlayer(new GridPoint2(10, 10));
            verify(area, times(1)).spawnEntityAt(made, grid, true, true);
        }
    }

    /* ------------ Fallback branches (trim/lower) ------------ */

    @Test
    void fallback_player_with_spaces_and_mixed_case_calls_area() {
        GameArea area = mock(GameArea.class);
        var placer = GameLevelBootstrap.defaultPlacer(area);

        // "  PlAyEr  " won't match registerCi because RegistryEntityPlacer compares raw key first
        GridPoint2 g = new GridPoint2(2, 3);
        placer.place(spec("p", "  PlAyEr  ", g.x, g.y));

        verify(area, times(1)).spawnOrRepositionPlayer(g);
    }

    @Test
    void fallback_door_with_spaces_spawns_door_entity_at_grid() {
        GameArea area = mock(GameArea.class);
        var doorEntity = mock(Entity.class);

        try (MockedStatic<ObstacleFactory> of = mockStatic(ObstacleFactory.class)) {
            of.when(ObstacleFactory::createDoor).thenReturn(doorEntity);

            var placer = GameLevelBootstrap.defaultPlacer(area);
            GridPoint2 g = new GridPoint2(9, 9);
            placer.place(spec("d", " DOOR ", g.x, g.y));

            verify(area, times(1)).spawnEntityAt(doorEntity, g, false, false);
        }
    }

    @Test
    void fallback_enemy_with_variant_trims_and_spawns_via_spawnEnemy() {
        GameArea area = mock(GameArea.class);
        Entity player = mock(Entity.class);
        Entity made = mock(Entity.class);
        when(area.getBaseDifficultyScale()).thenReturn(1.25f);

        // Provide a player so defaultPlacer doesn't create one
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<NPCFactory> nf = mockStatic(NPCFactory.class)) {

            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            // "enemy:VROOMBaBlue  " -> variantLower "vroombablue"
            nf.when(() -> NPCFactory.createVroombaBlue(eq(player), anyFloat())).thenReturn(made);

            var placer = GameLevelBootstrap.defaultPlacer(area);
            GridPoint2 g = new GridPoint2(5, 6);

            placer.place(spec("vb", "enemy:VROOMBaBlue  ", g.x, g.y));

            verify(area, times(1)).spawnEntityAt(made, g, true, true);
        }
    }

    /* ------------ Registered fast handlers ------------ */

    @Test
    void registered_handler_player_case_insensitive() {
        GameArea area = mock(GameArea.class);
        var placer = GameLevelBootstrap.defaultPlacer(area);

        GridPoint2 g = new GridPoint2(1, 1);
        placer.place(spec("hero", "PLAYER", g.x, g.y));

        verify(area, times(1)).spawnOrRepositionPlayer(g);
    }

    @Test
    void registered_handler_enemy_defaults_to_ghostgpt() {
        GameArea area = mock(GameArea.class);
        Entity player = mock(Entity.class);
        Entity made = mock(Entity.class);
        when(area.getBaseDifficultyScale()).thenReturn(1.0f);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<NPCFactory> nf = mockStatic(NPCFactory.class)) {

            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            // registered "enemy" handler calls spawnEnemy(...,"ghostgpt",...)
            nf.when(() -> NPCFactory.createGhostGPT(eq(player), eq(area), anyFloat())).thenReturn(made);

            var placer = GameLevelBootstrap.defaultPlacer(area);
            GridPoint2 g = new GridPoint2(7, 8);

            placer.place(spec("e1", "enemy", g.x, g.y));

            verify(area, times(1)).spawnEntityAt(made, g, true, true);
        }
    }

    /* ------------ Unknown & null types ------------ */

    @Test
    void unknown_type_only_logs_no_spawns() {
        GameArea area = mock(GameArea.class);
        var placer = GameLevelBootstrap.defaultPlacer(area);

        GridPoint2 g = new GridPoint2(0, 0);
        placer.place(spec("x", "foobar", g.x, g.y));

        // neither player nor entity spawn should be called
        verify(area, never()).spawnOrRepositionPlayer(any());
        verify(area, never()).spawnEntityAt(any(), any(), anyBoolean(), anyBoolean());
    }

    @Test
    void null_type_routes_to_fallback_and_no_spawns() {
        GameArea area = mock(GameArea.class);
        var placer = GameLevelBootstrap.defaultPlacer(area);

        GridPoint2 g = new GridPoint2(2, 2);
        // MapEntitySpec with null type
        placer.place(new FileLoader.MapEntitySpec("n", null, g));

        // Fallback early-returns when type==null → no calls into area
        verify(area, never()).spawnOrRepositionPlayer(any());
        verify(area, never()).spawnEntityAt(any(), any(), anyBoolean(), anyBoolean());
    }

    @Test
    void registerCi_door_spawnsDoorAtGrid_caseInsensitive() {
        // Arrange
        GameArea area = mock(GameArea.class);
        Entity doorEntity = mock(Entity.class);

        try (MockedStatic<ObstacleFactory> of = mockStatic(ObstacleFactory.class)) {
            of.when(ObstacleFactory::createDoor).thenReturn(doorEntity);

            var placer = GameLevelBootstrap.defaultPlacer(area);

            // Act: exact “door”
            GridPoint2 g1 = new GridPoint2(3, 4);
            placer.place(spec("d1", "door", g1.x, g1.y));

            // Act: mixed-case “DoOr” proves case-insensitive registration
            GridPoint2 g2 = new GridPoint2(7, 8);
            placer.place(spec("d2", "DoOr", g2.x, g2.y));

            // Assert: both calls spawned the factory-made door with (solidSnap=false, center=false)
            verify(area, times(1)).spawnEntityAt(doorEntity, g1, false, false);
            verify(area, times(1)).spawnEntityAt(doorEntity, g2, false, false);

            // And the factory was invoked for each placement
            of.verify(ObstacleFactory::createDoor, times(2));
        }
    }
}

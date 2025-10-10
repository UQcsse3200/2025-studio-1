package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.FileLoader.Location;
import com.csse3200.game.files.FileLoader.MapEntitySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GameExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileLoaderReadMapEntitiesTest {
    private String rootDir;

    static Stream<Arguments> invalidDocs() {
        return Stream.of(
                Arguments.of("nonNumericCoordinate_triggersCatch",
                        """
                                {"entities":[{"name":"boom","type":"npc","location":{"x":"NaN","y":1}}]}
                                """),
                Arguments.of("missingOrNonObjectMembers_allInvalid",
                        """
                                {"entities":[
                                  {"type":"npc","location":{"x":1,"y":1}},
                                  {"name":"noType","location":{"x":2,"y":2}},
                                  {"name":"noLoc","type":"npc"},
                                  {"name":"badLoc","type":"npc","location":5}
                                ]}
                                """),
                Arguments.of("onlyInvalidValues_mix",
                        """
                                {"entities":[
                                  5,
                                  {"name":"bad1","location":{"x":1,"y":2}},
                                  {"name":"","type":"npc","location":{"x":9,"y":9}},
                                  {"name":"bad2","type":"npc","location":{"y":2}},
                                  {"name":"bad3","type":"","location":{"x":1,"y":1}},
                                  {"name":"bad4","type":"npc","location":{"x":-1}}
                                ]}
                                """)
        );
    }

    static Stream<Arguments> invalidValueCases() {
        int min = Integer.MIN_VALUE;
        return Stream.of(
                Arguments.of("blank name",
                        """
                                {"name":"  ","type":"npc","location":{"x":1,"y":1}}
                                """
                ),
                Arguments.of("blank type",
                        """
                                {"name":"ok","type":"  ","location":{"x":1,"y":1}}
                                """
                ),
                Arguments.of("missing x",
                        """
                                {"name":"ok","type":"npc","location":{"y":5}}
                                """
                ),
                Arguments.of("missing y",
                        """
                                {"name":"ok","type":"npc","location":{"x":5}}
                                """
                ),
                Arguments.of("explicit x == MIN",
                        """
                                {"name":"ok","type":"npc","location":{"x":%d,"y":1}}
                                """.formatted(min)
                ),
                Arguments.of("explicit y == MIN",
                        """
                                {"name":"ok","type":"npc","location":{"x":1,"y":%d}}
                                """.formatted(min)
                )
        );
    }

    static Stream<Arguments> invalidStructureDocs() {
        return Stream.of(
                Arguments.of(
                        "malformed json",
                        "{"
                ),
                Arguments.of(
                        "missing entities array",
                        "{\"x\":[]}"
                ),
                Arguments.of(
                        "entities key not array",
                        "{\"entities\":{}}"
                )
        );
    }

    private String p(String rel) {
        return rootDir + "/" + UUID.randomUUID() + (rel.startsWith("/") ? rel : "/" + rel);
    }

    @BeforeAll
    void setupRoot() {
        rootDir = "unit-tests/read-map-" + UUID.randomUUID();
        Gdx.files.local(rootDir).mkdirs();
    }

    @AfterAll
    void cleanup() {
        var root = Gdx.files.local(rootDir);
        if (root.exists()) root.deleteDirectory();
    }

    @Test
    void missingFile_returnsEmptyOptional() {
        assertTrue(FileLoader.readMapEntities(p("nope.json"), Location.LOCAL).isEmpty());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidStructureDocs")
    void invalidStructure_returnsEmptyOptional(String label, String json) {
        String path = p(label.replaceAll("\\s+", "_") + ".json");
        Gdx.files.local(path).parent().mkdirs();
        Gdx.files.local(path).writeString(json, false);

        assertTrue(FileLoader.readMapEntities(path, Location.LOCAL).isEmpty(), "case: " + label);
    }

    @Test
    void returnsOnlyValidEntities_inInputOrder() {
        String path = p("mixed.json");
        String json = """
                { "entities":[
                  {"name":"p1","type":"player","location":{"x":3,"y":4}},
                  123,
                  {"name":"t1","type":"tree","location":{"x":0,"y":0}},
                  {"name":"bad1","location":{"x":1,"y":2}},
                  {"name":"","type":"npc","location":{"x":9,"y":9}},
                  {"name":"bad2","type":"npc","location":{"y":2}},
                  {"name":"npc","type":"ally","location":{"x":7,"y":8}}
                ]}""";
        Gdx.files.local(path).writeString(json, false);

        var out = FileLoader.readMapEntities(path, Location.LOCAL).orElseThrow();
        assertEquals(3, out.size());
        assertEquals(new MapEntitySpec("p1", "player", new GridPoint2(3, 4)), out.get(0));
        assertEquals(new MapEntitySpec("t1", "tree", new GridPoint2(0, 0)), out.get(1));
        assertEquals(new MapEntitySpec("npc", "ally", new GridPoint2(7, 8)), out.get(2));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidDocs")
    void invalidDocuments_returnEmpty(String label, String doc) {
        String path = p("case.json");
        Gdx.files.local(path).writeString(doc, false);
        assertTrue(FileLoader.readMapEntities(path, Location.LOCAL).isEmpty(), "case=" + label);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidValueCases")
    void onlyEntity_isRejected_andBranchIsHit(String label, String one) {
        String path = p("val.json");
        String doc = "{ \"entities\": [" + one + "] }";
        Gdx.files.local(path).writeString(doc, false);
        assertTrue(FileLoader.readMapEntities(path, Location.LOCAL).isEmpty(), label);
    }

    @Test
    void locationMustHaveBothXandY_ints() {
        String path = p("loc.json");
        String json = """
                {"entities":[
                  {"name":"a","type":"t","location":{"x":0}},
                  {"name":"b","type":"t","location":{"y":1}},
                  {"name":"c","type":"t","location":{"x":2,"y":2}}
                ]}
                """;
        Gdx.files.local(path).writeString(json, false);
        var out = FileLoader.readMapEntities(path, Location.LOCAL).orElseThrow();
        assertEquals(1, out.size());
        assertEquals(new MapEntitySpec("c", "t", new GridPoint2(2, 2)), out.getFirst());
    }
}

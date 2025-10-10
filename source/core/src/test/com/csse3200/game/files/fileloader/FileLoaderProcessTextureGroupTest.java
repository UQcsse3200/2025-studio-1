package com.csse3200.game.files.fileloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GameExtension.class)
class FileLoaderProcessTextureGroupTest {

    private Method processTextureGroup;

    private static void setName(JsonValue v, String name) {
        try {
            Field f = JsonValue.class.getDeclaredField("name");
            f.setAccessible(true);
            f.set(v, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setChild(JsonValue parent, JsonValue child) {
        try {
            Field f = JsonValue.class.getDeclaredField("child");
            f.setAccessible(true);
            f.set(parent, child);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setNext(JsonValue node, JsonValue next) {
        try {
            Field f = JsonValue.class.getDeclaredField("next");
            f.setAccessible(true);
            f.set(node, next);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void reflect() throws Exception {
        processTextureGroup = FileLoader.class.getDeclaredMethod(
                "processTextureGroup", JsonValue.class, FileHandle.class, Map.class);
        processTextureGroup.setAccessible(true);
    }

    private FileHandle tempFile() {
        String base = "unit-tests/ptg/" + UUID.randomUUID();
        Gdx.files.local(base).mkdirs();
        String path = base + "/dummy.json";
        Gdx.files.local(path).writeString("{}", false);
        return Gdx.files.local(path);
    }

    @Test
    void returnsEarly_whenGroupNameIsNull() throws Exception {
        // object group but name == null triggers the first disjunct
        JsonValue group = new JsonValue(JsonValue.ValueType.object);
        setName(group, null);

        Map<String, String> out = new LinkedHashMap<>();
        processTextureGroup.invoke(null, group, tempFile(), out);

        assertTrue(out.isEmpty(), "null group name must early-return");
    }

    @Test
    void returnsEarly_whenGroupIsNotObject() throws Exception {
        // non-object group with correct suffix still returns early via !isObject()
        JsonValue group = new JsonValue("ignored");
        setName(group, "uiTextures");

        Map<String, String> out = new LinkedHashMap<>();
        processTextureGroup.invoke(null, group, tempFile(), out);

        assertTrue(out.isEmpty(), "non-object group must early-return");
    }

    @Test
    void returnsEarly_whenNameDoesNotEndWithTextures() throws Exception {
        // object group but wrong suffix → third disjunct
        JsonValue group = new JsonValue(JsonValue.ValueType.object);
        setName(group, "uiThings");

        Map<String, String> out = new LinkedHashMap<>();
        processTextureGroup.invoke(null, group, tempFile(), out);

        assertTrue(out.isEmpty(), "name without 'Textures' suffix must early-return");
    }

    @Test
    void processesEntries_whenObjectNameEndsWithTextures() throws Exception {
        // object group with proper suffix and two string entries → loop runs
        JsonValue group = new JsonValue(JsonValue.ValueType.object);
        setName(group, "uiTextures");

        JsonValue e1 = new JsonValue("ui/button.png");
        setName(e1, "button");
        JsonValue e2 = new JsonValue("ui/label.png");
        setName(e2, "label");
        setChild(group, e1);
        setNext(e1, e2);

        Map<String, String> out = new LinkedHashMap<>();
        processTextureGroup.invoke(null, group, tempFile(), out);

        assertEquals(2, out.size());
        assertEquals("ui/button.png", out.get("button"));
        assertEquals("ui/label.png", out.get("label"));
    }
}

package com.csse3200.game.entities;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AvatarRegistryTest {

    private static final String CONFIG_PATH = "configs/avatars.json";

    private Files filesMock;
    private FileHandle avatarsHandleMock;

    // Minimal JSON the registry expects (array of avatar objects)
    private static final String AVATARS_JSON = "[\n" +
            "  {\"id\":\"scout\",\"displayName\":\"Scout\",\"texturePath\":\"images/a.png\",\"baseHealth\":80,\"baseDamage\":10,\"moveSpeed\":5.0,\"atlas\":\"images/a.atlas\"},\n" +
            "  {\"id\":\"soldier\",\"displayName\":\"Soldier\",\"texturePath\":\"images/b.png\",\"baseHealth\":120,\"baseDamage\":15,\"moveSpeed\":3.8,\"atlas\":\"images/b.atlas\"}\n" +
            "]";

    @Before
    public void setUp() {
        filesMock = Mockito.mock(Files.class);
        avatarsHandleMock = Mockito.mock(FileHandle.class);

        when(avatarsHandleMock.readString()).thenReturn(AVATARS_JSON);

        when(avatarsHandleMock.reader()).thenReturn(new StringReader(AVATARS_JSON));
        when(avatarsHandleMock.reader(anyString())).thenReturn(new StringReader(AVATARS_JSON));

        when(filesMock.internal(CONFIG_PATH)).thenReturn(avatarsHandleMock);

        // Attach the mock to Gdx
        Gdx.files = filesMock;

        // Clear any previously selected avatar
        AvatarRegistry.set(null);
    }

    @After
    public void tearDown() {
        // Clean up static state so tests don't bleed into each other
        AvatarRegistry.set(null);
    }

    @Test
    public void getAll_shouldParseJsonIntoAvatars() {
        List<Avatar> avatars = AvatarRegistry.getAll();

        assertNotNull("getAll() should not return null", avatars);
        assertEquals("Expected 2 avatars parsed", 2, avatars.size());

        Avatar a0 = avatars.get(0);
        assertEquals("scout", a0.id());
        assertEquals("Scout", a0.displayName());
        assertEquals("images/a.png", a0.texturePath());
        assertEquals(80, a0.baseHealth());
        assertEquals(10, a0.baseDamage());
        assertEquals(5.0f, a0.moveSpeed(), 0.0001f);
        assertEquals("images/a.atlas", a0.atlas());

        Avatar a1 = avatars.get(1);
        assertEquals("soldier", a1.id());
        assertEquals("Soldier", a1.displayName());
        assertEquals("images/b.png", a1.texturePath());
        assertEquals(120, a1.baseHealth());
        assertEquals(15, a1.baseDamage());
        assertEquals(3.8f, a1.moveSpeed(), 0.0001f);
        assertEquals("images/b.atlas", a1.atlas());

        // Verify we hit the expected path exactly once
        verify(filesMock, times(1)).internal(CONFIG_PATH);
        verify(avatarsHandleMock, atLeastOnce()).reader(anyString());
    }

    @Test
    public void setAndGet_shouldReturnSameAvatar() {
        Avatar pick = new Avatar("eng", "Engineer", "images/eng.png", 100, 8, 4.2f, "images/eng.atlas");
        AvatarRegistry.set(pick);

        Avatar got = AvatarRegistry.get();
        assertSame("get() should return the avatar set()", pick, got);
    }
}

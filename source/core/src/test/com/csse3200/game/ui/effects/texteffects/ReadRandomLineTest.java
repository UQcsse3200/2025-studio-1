package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReadRandomLineTest extends TextEffectsTestBase {

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of("ok.txt", "# c\n\nhello\n", "fb", "hello"),
                Arguments.of("comment_only.txt", "#a\n#b\n", "fb", "fb"),
                Arguments.of("missing.txt", null, "x", "x"),
                Arguments.of("boom.txt", ":throw", "fallback", "fallback")
        );
    }

    static Stream<Arguments> fallbackCases() {
        return Stream.of(
                // 1) missing file
                Arguments.of("missing.txt", ":missing", "FALLBACK"),
                // 2) only comments/blank -> pool empty
                Arguments.of("comments.txt", "# a\n\n# b", "FALLBACK"),
                // 3) exception thrown
                Arguments.of("boom.txt", ":throw", "FALLBACK")
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void readRandomLine_matrix(String path, String content, String fallback, String expected) {
        Files original = Gdx.files;
        try {
            if (":throw".equals(content)) {
                Files files = mock(Files.class, CALLS_REAL_METHODS);
                FileHandle fh = mock(FileHandle.class);
                when(files.internal(path)).thenReturn(fh);
                when(fh.readString()).thenThrow(new RuntimeException("kaboom"));
                when(fh.readString(any())).thenThrow(new RuntimeException("kaboom"));
                when(fh.read()).thenThrow(new RuntimeException("kaboom"));
                when(fh.reader()).thenThrow(new RuntimeException("kaboom"));
                Gdx.files = files;
            } else {
                Gdx.files = memFiles;
                if (content != null) memFiles.put(path, content);
                else memFiles.remove(path);
            }
            assertEquals(expected, TextEffects.readRandomLine(path, fallback));
        } finally {
            Gdx.files = original;
        }
    }

    @ParameterizedTest
    @MethodSource("fallbackCases")
    void readRandomLine_invokes_fallback(String path, String contentOrMode, String expected) {
        var original = Gdx.files;
        try {
            if (":missing".equals(contentOrMode)) {
                // make internal(path) say exists() == false
                Files files = mock(Files.class, CALLS_REAL_METHODS);
                FileHandle fh = mock(FileHandle.class);
                when(files.internal(path)).thenReturn(fh);
                when(fh.exists()).thenReturn(false);
                Gdx.files = files;
            } else if (":throw".equals(contentOrMode)) {
                // force an exception during read -> catch -> fallback
                Files files = mock(Files.class, CALLS_REAL_METHODS);
                FileHandle fh = mock(FileHandle.class);
                when(files.internal(path)).thenReturn(fh);
                when(fh.exists()).thenReturn(true);
                when(fh.readString(any())).thenThrow(new RuntimeException("boom"));
                Gdx.files = files;
            } else {
                // content is provided: only comments/blank -> pool empty -> fallback
                // using your MemFiles stub if you have it; else a simple mock:
                Files files = mock(Files.class, CALLS_REAL_METHODS);
                FileHandle fh = mock(FileHandle.class);
                when(files.internal(path)).thenReturn(fh);
                when(fh.exists()).thenReturn(true);
                when(fh.readString(any())).thenReturn(contentOrMode);
                Gdx.files = files;
            }

            assertEquals(expected, TextEffects.readRandomLine(path, expected));
        } finally {
            Gdx.files = original;
        }
    }

}

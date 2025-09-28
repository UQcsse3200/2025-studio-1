package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import java.util.Objects;

/**
 * The "Dialogue Data" component attached to the Friendly NPC.
 * Only data for display is stored: name, avatar path, and line list.
 * The trigger reads from here and feeds the dialog UI.
 */
public class NpcDialogueDataComponent extends Component {
    private final String name;          // NPC name
    private final String portraitPath;  // Image resource path
    private final String[] lines;       // words

    /**
     * @param name  NPC name
     * @param portraitPath Image resource path
     * @param lines words
     */
    public NpcDialogueDataComponent(String name, String portraitPath, String[] lines) {
        this.name = Objects.requireNonNullElse(name, "");
        this.portraitPath = portraitPath == null ? "" : portraitPath;
        this.lines = lines == null ? new String[0] : lines.clone();
    }

    public String getName() {
        return name;
    }

    public String getPortraitPath() {
        return portraitPath;
    }

    public String[] getLines() {
        return lines.clone();
    }

    public boolean isEmpty() {
        return lines.length == 0;
    }
}


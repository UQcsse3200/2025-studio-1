// src/test/java/com/csse3200/game/ui/terminal/testing/FakeCommand.java
package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.ui.terminal.commands.Command;
import java.util.ArrayList;
import java.util.List;

public class FakeCommand implements Command {
    public final List<List<String>> invocations = new ArrayList<>();
    private final boolean result;

    public FakeCommand() { this(true); }
    public FakeCommand(boolean result) { this.result = result; }

    @Override
    public boolean action(ArrayList<String> args) {
        invocations.add(new ArrayList<>(args));
        return result;
    }
}

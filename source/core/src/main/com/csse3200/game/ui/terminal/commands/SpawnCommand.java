package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;

public class SpawnCommand implements Command {
    @Override
    public boolean action(ArrayList<String> args) {
        GameArea ga = ServiceLocator.getGameArea();
        ForestGameArea area = (ForestGameArea) ga;
        area.spawnDeepspin(2, 1f);
        return true;
    }
}

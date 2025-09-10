package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Starts enemy waves in the current ForestGameArea.
 * Usage: waves
 */
public class WavesCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(WavesCommand.class);

  @Override
  public boolean action(ArrayList<String> args) {
    GameArea ga = ServiceLocator.getGameArea();
    if (!(ga instanceof ForestGameArea)) {
      logger.warn("WavesCommand: Current GameArea is not a ForestGameArea; cannot start waves");
      return false;
    }
    ForestGameArea area = (ForestGameArea) ga;
    area.startWaves();
    logger.info("WavesCommand: Enemy waves started");
    return true;
  }
}


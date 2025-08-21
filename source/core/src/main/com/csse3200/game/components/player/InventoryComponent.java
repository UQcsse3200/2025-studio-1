package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component intended to be used by the player to track their inventory.
 * Currently only stores the gold amount but can be extended for more advanced functionality such as storing items.
 * Can also be used as a more generic component for other entities.
 */
public class InventoryComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);
  private int processor;
  private int processor_total;

  public InventoryComponent(int processor) {
    setProcessor(processor);
  }

  /**
   * Returns the player's processor's.
   *
   * @return entity's current processor count
   */
  public int getProcessor() {
    return this.processor;
  }

  /**
   *  Returns the total players processor's throughout game
   * @return players total processors gained
   */
  public int getProcessor_total() {
    return processor_total;
  }

  /**
   * Returns if the player has a certain amount of gold.
   * @param processor required amount of gold
   * @return player has greater than or equal to the required amount of gold
   */
  public Boolean hasProcessors(int processor) {
    return this.processor >= processor;
  }

  /**
   * Sets the player's gold. Gold has a minimum bound of 0.
   *
   * @param processor processor
   */
  public void setProcessor(int processor) {
    this.processor = Math.max(processor, 0);
    logger.debug("Setting gold to {}", this.processor);
  }

  /**
   * Adds to the player's gold. The amount added can be negative.
   * @param processor processor to add
   */
  public void addProcessor(int processor) {
    setProcessor(this.processor + processor);
    processor_total += processor;
  }

}

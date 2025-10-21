package com.csse3200.game.components.minigames;

import com.csse3200.game.components.player.InventoryComponent;

/**
 * Handles all logic related to placing and resolving bets in a mini-game.
 * <p>
 * This class is independent of any UI components and manages the player's
 * current bet, balance, and winnings based on game results. It interacts
 * with the {@link InventoryComponent} to update the player's available funds.
 * </p>
 */
public class BettingLogic {

    private final InventoryComponent inventory;
    private final int multiplier;
    private int balance;
    private int bet;

    /**
     * Constructs a new {@code BettingLogic} instance.
     *
     * @param multiplier the payout multiplier applied when the player wins
     * @param inventory  the player's {@link InventoryComponent}, used to
     *                   get and update their balance
     */
    public BettingLogic(int multiplier, InventoryComponent inventory) {
        this.multiplier = multiplier;
        this.inventory = inventory;
        this.balance = inventory.getProcessor();
        this.bet = 0;
    }

    /**
     * Returns the player's current balance.
     *
     * @return the available balance
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Returns the current bet amount.
     *
     * @return the active bet amount
     */
    public int getBet() {
        return bet;
    }

    /**
     * Places a bet of the given amount. Deducts the bet from the player's
     * balance in their {@link InventoryComponent}.
     *
     * @param amount the amount to bet
     * @throws IllegalArgumentException if the bet is zero, negative, or exceeds the available balance
     */
    public void placeBet(int amount) {
        balance = inventory.getProcessor();
        if (amount <= 0) throw new IllegalArgumentException("Bet must be positive");
        if (amount > balance) throw new IllegalArgumentException("Not enough balance");

        bet = amount;
        inventory.addProcessor(-bet);
        balance = inventory.getProcessor();
    }

    /**
     * Adjusts the current bet by the specified delta value.
     * <p>
     * Ensures that the bet never becomes negative.
     * </p>
     *
     * @param delta the amount to increase or decrease the bet by
     */
    public void adjustBet(int delta) {
        bet = Math.max(0, bet + delta);
    }

    /**
     * Calculates the total winnings if the player wins,
     * based on the bet and multiplier.
     *
     * @return the total winnings amount
     */
    public int calculateWinnings() {
        return bet * multiplier;
    }

    /**
     * Handles the win condition by awarding winnings to the player's inventory
     * and updating the balance accordingly.
     */
    public void onWin() {
        int winnings = calculateWinnings();
        inventory.addProcessor(winnings);
        balance = inventory.getProcessor();
    }

    /**
     * Handles the tie condition by refunding the player's bet.
     */
    public void onTie() {
        inventory.addProcessor(bet);
        balance = inventory.getProcessor();
    }

    /**
     * Handles the lose condition.
     * <p>
     * The bet has already been deducted when placed, so no additional
     * balance updates are needed.
     * </p>
     */
    public void onLose() {
        // Already subtracted bet, nothing more to do
    }

    /**
     * Doubles the current bet by subtracting the bet amount again
     * from the player's inventory.
     *
     * @throws IllegalArgumentException if the player does not have enough balance
     */
    public void doubleBet() {
        balance = inventory.getProcessor();
        if (bet > balance) throw new IllegalArgumentException("Not enough balance");
        inventory.addProcessor(-bet);
        balance = inventory.getProcessor();
    }

    /**
     * Checks if the player has enough balance to double their current bet.
     *
     * @return true if doubling is allowed, false otherwise
     */
    public boolean canDouble() {
        balance = inventory.getProcessor();
        return bet < balance;
    }

    /**
     * Splits the current bet into a new bet of equal size.
     * <p>
     * In this simplified logic, it simply places another bet of the same amount.
     * </p>
     */
    public void split() {
        placeBet(bet);
    }

    /**
     * Handles the double-win condition, awarding double winnings to the player's inventory.
     */
    public void doubleWin() {
        int winnings = calculateWinnings();
        inventory.addProcessor(winnings * 2);
        balance = inventory.getProcessor();
    }

    /**
     * Handles the double-tie condition, refunding double the bet amount to the player.
     */
    public void doubleTie() {
        inventory.addProcessor(bet * 2);
        balance = inventory.getProcessor();
    }
}

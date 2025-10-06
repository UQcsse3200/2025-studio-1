package com.csse3200.game.components.minigames;

import com.csse3200.game.components.player.InventoryComponent;

public class BettingLogic {

    private final InventoryComponent inventory;
    private int balance;
    private int bet;
    private final int multiplier;

    public BettingLogic(int multiplier, InventoryComponent inventory) {
        this.multiplier = multiplier;
        this.inventory = inventory;
        this.balance = inventory.getProcessor();
        this.bet = 0;
    }

    public int getBalance() {
        return balance;
    }

    public int getBet() {
        return bet;
    }

    public void placeBet(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Bet must be positive");
        if (amount > balance) throw new IllegalArgumentException("Not enough balance");

        bet = amount;
        inventory.addProcessor(-bet);
        balance = inventory.getProcessor();
    }

    public void adjustBet(int delta) {
        bet = Math.max(0, bet + delta);
    }

    public int calculateWinnings() {
        return bet * multiplier;
    }

    public void onWin() {
        int winnings = calculateWinnings();
        inventory.addProcessor(winnings);
        balance = inventory.getProcessor();
    }

    public void onTie() {
        inventory.addProcessor(bet);
        balance = inventory.getProcessor();
    }

    public void onLose() {
        // Already subtracted bet, nothing more to do
    }
}

package com.csse3200.game.components.minigames;

import com.csse3200.game.components.player.InventoryComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BettingLogicTest {

    InventoryComponent mockInventory;
    BettingLogic logic;

    @BeforeEach
    void setUp() {
        mockInventory = mock(InventoryComponent.class);
        when(mockInventory.getProcessor()).thenReturn(100); // starting balance
        logic = new BettingLogic(2, mockInventory); // 2x multiplier
    }

    @Test
    void testInitialValues() {
        assertEquals(100, logic.getBalance());
        assertEquals(0, logic.getBet());
    }

    @Test
    void testPlaceBetValid() {
        logic.placeBet(30);
        assertEquals(30, logic.getBet());
        verify(mockInventory).addProcessor(-30);
    }

    @Test
    void testPlaceBetInvalidNegative() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> logic.placeBet(-10));
        assertEquals("Bet must be positive", ex.getMessage());
    }

    @Test
    void testPlaceBetExceedsBalance() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> logic.placeBet(200));
        assertEquals("Not enough balance", ex.getMessage());
    }

    @Test
    void testAdjustBet() {
        logic.placeBet(20);
        logic.adjustBet(10);
        assertEquals(30, logic.getBet());

        logic.adjustBet(-50);
        assertEquals(0, logic.getBet()); // cannot go below 0
    }

    @Test
    void testCalculateWinnings() {
        logic.placeBet(25);
        assertEquals(50, logic.calculateWinnings()); // 25 * 2
    }

    @Test
    void testOnWin() {
        logic.placeBet(20);
        logic.onWin();
        verify(mockInventory).addProcessor(40); // winnings = bet * 2
    }

    @Test
    void testOnTie() {
        logic.placeBet(15);
        logic.onTie();
        verify(mockInventory).addProcessor(15); // bet refunded
    }

    @Test
    void testOnLose() {
        logic.placeBet(10);
        logic.onLose();
        verify(mockInventory).addProcessor(-10);
    }

    // === New tests for double and split logic ===

    @Test
    void testCanDoubleTrueWhenEnoughBalance() {
        logic.placeBet(40);
        when(mockInventory.getProcessor()).thenReturn(100);
        assertTrue(logic.canDouble(), "Should be able to double with enough balance");
    }

    @Test
    void testCanDoubleFalseWhenNotEnoughBalance() {
        logic.placeBet(60);
        when(mockInventory.getProcessor()).thenReturn(50);
        assertFalse(logic.canDouble(), "Should not be able to double with low balance");
    }

    @Test
    void testDoubleBetDeductsFunds() {
        logic.placeBet(30);
        when(mockInventory.getProcessor()).thenReturn(70); // enough funds
        logic.doubleBet();
        verify(mockInventory, times(2)).addProcessor(-30);
    }

    @Test
    void testDoubleBetThrowsWhenInsufficientBalance() {
        logic.placeBet(80);
        when(mockInventory.getProcessor()).thenReturn(60); // not enough to double
        Exception ex = assertThrows(IllegalArgumentException.class, logic::doubleBet);
        assertEquals("Not enough balance", ex.getMessage());
    }

    @Test
    void testDoubleWinAwardsDoubleWinnings() {
        logic.placeBet(20);
        logic.doubleWin();
        verify(mockInventory).addProcessor(80); // (20 * 2) * 2
    }

    @Test
    void testDoubleTieRefundsDoubleBet() {
        logic.placeBet(25);
        logic.doubleTie();
        verify(mockInventory).addProcessor(50); // bet * 2 refunded
    }

    @Test
    void testSplitPlacesEqualSecondBet() {
        logic.placeBet(30);
        logic.split();
        // placeBet() is called again, so another -30 happens
        verify(mockInventory, times(2)).addProcessor(-30);
    }

    @Test
    void testCalculateWinningsAfterDoubleBet() {
        logic.placeBet(10);
        logic.doubleBet();
        assertEquals(10, logic.getBet(), "After doubling, bet should stay same unless logic modifies it");
        assertEquals(20 * 2, 2 * logic.calculateWinnings());
    }
}

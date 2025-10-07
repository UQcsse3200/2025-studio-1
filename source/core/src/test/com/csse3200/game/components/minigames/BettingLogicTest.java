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
        logic = new BettingLogic(2, mockInventory); // multiplier 2x
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
        verify(mockInventory).addProcessor(40); // 20 * 2
    }

    @Test
    void testOnTie() {
        logic.placeBet(15);
        logic.onTie();
        verify(mockInventory).addProcessor(15); // bet returned
    }

    @Test
    void testOnLose() {
        logic.placeBet(10);
        logic.onLose();
        // No additional call to inventory
        verify(mockInventory, never()).addProcessor(10);
    }
}

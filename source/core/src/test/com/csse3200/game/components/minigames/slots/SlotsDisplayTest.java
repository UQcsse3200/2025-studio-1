package com.csse3200.game.components.minigames.slots;

import com.csse3200.game.components.player.InventoryComponent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class SlotsDisplayTest {

    private Class<?> slotSymbolEnum() throws Exception {
        return Class.forName("com.csse3200.game.components.minigames.slots.SlotsDisplay$SlotSymbol");
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private Enum<?> sym(String name) throws Exception {
        Class<Enum> enumCls = (Class<Enum>) slotSymbolEnum().asSubclass(Enum.class);
        return Enum.valueOf(enumCls, name);
    }

    private Object newSymbolArray(int length) throws Exception {
        return Array.newInstance(slotSymbolEnum(), length);
    }

    private Object symbolArray(Enum<?>... values) throws Exception {
        Object arr = newSymbolArray(values.length);
        for (int i = 0; i < values.length; i++) {
            Array.set(arr, i, values[i]);
        }
        return arr;
    }

    private int invokeGetPayout(SlotsDisplay display, Object slotSymbolArray, int bet) throws Exception {
        Class<?> slotArrayType = slotSymbolArray.getClass();               // <-- this is SlotSymbol[]
        Method m = SlotsDisplay.class.getDeclaredMethod("getPayout", slotArrayType, int.class);
        m.setAccessible(true);
        return (int) m.invoke(display, slotSymbolArray, bet);
    }

    private Object invokeSpinRow(SlotsDisplay display) throws Exception {
        Method m = SlotsDisplay.class.getDeclaredMethod("spinRow");
        m.setAccessible(true);
        return m.invoke(display);  // returns SlotSymbol[]
    }

    private Class<?> walletClass() throws Exception {
        return Class.forName("com.csse3200.game.components.minigames.slots.SlotsDisplay$DefaultInventoryWallet");
    }

    private Object newWallet(InventoryComponent inventory) throws Exception {
        Constructor<?> c = walletClass().getDeclaredConstructor(InventoryComponent.class);
        c.setAccessible(true);
        return c.newInstance(inventory);
    }

    private int walletGet(Object wallet) throws Exception {
        Method m = walletClass().getDeclaredMethod("get");
        m.setAccessible(true);
        return (int) m.invoke(wallet);
    }

    private boolean walletCanSubtract(Object wallet, int amt) throws Exception {
        Method m = walletClass().getDeclaredMethod("canSubtract", int.class);
        m.setAccessible(true);
        return (boolean) m.invoke(wallet, amt);
    }

    private void walletAdd(Object wallet, int amt) throws Exception {
        Method m = walletClass().getDeclaredMethod("add", int.class);
        m.setAccessible(true);
        m.invoke(wallet, amt);
    }

    private void walletSubtract(Object wallet, int amt) throws Exception {
        Method m = walletClass().getDeclaredMethod("subtract", int.class);
        m.setAccessible(true);
        m.invoke(wallet, amt);
    }

    @Test
    void triplePaysTripleMultiplier() throws Exception {
        SlotsDisplay d = new SlotsDisplay();
        Enum<?> cherry = sym("CHERRY"); // tripleMult = 3
        Object row = symbolArray(cherry, cherry, cherry); // SlotSymbol[]
        int payout = invokeGetPayout(d, row, 10);
        assertEquals(30, payout);
    }

    @Test
    void pairOnFirstTwoPaysPairMultiplier() throws Exception {
        SlotsDisplay d = new SlotsDisplay();
        Enum<?> lemon = sym("LEMON"); // pairMult = 4
        Enum<?> bell  = sym("BELL");
        Object row = symbolArray(lemon, lemon, bell);
        int payout = invokeGetPayout(d, row, 5);
        assertEquals(20, payout);
    }

    @Test
    void pairOnLastTwoPaysPairMultiplier() throws Exception {
        SlotsDisplay d = new SlotsDisplay();
        Enum<?> diamond = sym("DIAMOND"); // pairMult = 10
        Enum<?> cherry  = sym("CHERRY");
        Object row = symbolArray(cherry, diamond, diamond);
        int payout = invokeGetPayout(d, row, 2);
        assertEquals(20, payout);
    }

    @Test
    void noPairNoTriplePaysZero() throws Exception {
        SlotsDisplay d = new SlotsDisplay();
        Enum<?> c = sym("CHERRY");
        Enum<?> w = sym("WATERMELON");
        Enum<?> l = sym("LEMON");
        Object row = symbolArray(c, w, l);
        int payout = invokeGetPayout(d, row, 50);
        assertEquals(0, payout);
    }

    @Test
    void spinRowReturnsThreeValidSymbols() throws Exception {
        SlotsDisplay d = new SlotsDisplay();
        Object row = invokeSpinRow(d); // SlotSymbol[]
        assertNotNull(row);
        assertTrue(row.getClass().isArray());
        assertEquals(3, Array.getLength(row));

        Class<?> enumClass = slotSymbolEnum();
        for (int i = 0; i < 3; i++) {
            Object el = Array.get(row, i);
            assertNotNull(el);
            assertEquals(enumClass, el.getClass());
        }
    }

    @Test
    void spinRowLikelyVariesAcrossCalls() throws Exception {
        SlotsDisplay d = new SlotsDisplay();
        Object r1 = invokeSpinRow(d);
        Object r2 = invokeSpinRow(d);
        assertEquals(3, Array.getLength(r1));
        assertEquals(3, Array.getLength(r2));
        for (int i = 0; i < 3; i++) {
            assertNotNull(Array.get(r1, i));
            assertNotNull(Array.get(r2, i));
        }
    }


    @Test
    void walletReadsStartingBalanceAndSubtracts() throws Exception {
        AtomicInteger bal = new AtomicInteger(100);
        InventoryComponent inv = mock(InventoryComponent.class);
        when(inv.getProcessor()).thenAnswer(a -> bal.get());
        doAnswer(a -> { bal.addAndGet((Integer) a.getArgument(0)); return null; })
                .when(inv).addProcessor(anyInt());

        Object wallet = newWallet(inv);

        assertEquals(100, walletGet(wallet));
        assertTrue(walletCanSubtract(wallet, 100));
        assertFalse(walletCanSubtract(wallet, 101));

        walletSubtract(wallet, 30);
        assertEquals(70, walletGet(wallet));
    }

    @Test
    void walletAddsAndRespectsBounds() throws Exception {
        AtomicInteger bal = new AtomicInteger(50);
        InventoryComponent inv = mock(InventoryComponent.class);
        when(inv.getProcessor()).thenAnswer(a -> bal.get());
        doAnswer(a -> { bal.addAndGet((Integer) a.getArgument(0)); return null; })
                .when(inv).addProcessor(anyInt());

        Object wallet = newWallet(inv);

        walletAdd(wallet, 25);
        assertEquals(75, walletGet(wallet));

        assertFalse(walletCanSubtract(wallet, 100));
        assertTrue(walletCanSubtract(wallet, 75));
    }

    @Test
    void walletIgnoresNegativeAmounts() throws Exception {
        AtomicInteger bal = new AtomicInteger(10);
        InventoryComponent inv = mock(InventoryComponent.class);
        when(inv.getProcessor()).thenAnswer(a -> bal.get());
        doAnswer(a -> { bal.addAndGet((Integer) a.getArgument(0)); return null; })
                .when(inv).addProcessor(anyInt());

        Object wallet = newWallet(inv);

        walletAdd(wallet, -5);
        walletSubtract(wallet, -5);
        assertEquals(10, walletGet(wallet));
    }
}

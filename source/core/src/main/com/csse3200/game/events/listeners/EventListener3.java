package com.csse3200.game.events.listeners;

/**
 * An even listener with 3 arguments
 *
 * @param <T0> The first argument to pass onwards
 * @param <T1> The second argument to pass onwards
 * @param <T2> The third argument to pass onwards
 */
@FunctionalInterface
public interface EventListener3<T0, T1, T2> extends EventListener {
    void handle(T0 arg0, T1 arg1, T2 arg2);
}
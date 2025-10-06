package com.csse3200.game.events.listeners;

/**
 * An event listener with two arguments
 *
 * @param <T0> The first argument to pass onwards
 * @param <T1> The second argument to pass onwards
 */
@FunctionalInterface
public interface EventListener2<T0, T1> extends EventListener {
    void handle(T0 arg0, T1 arg1);
}


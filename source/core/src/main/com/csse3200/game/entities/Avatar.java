package com.csse3200.game.entities;

/**
 * Record class to keep track of statistics to be later injected into the player factory
 */
public record Avatar(String id, String displayName, String texturePath, int baseHealth, int baseDamage,
                     float moveSpeed) {
}
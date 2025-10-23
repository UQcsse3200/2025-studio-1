package com.csse3200.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;


/**
 * simple class to create avatars stats.
 */
public class AvatarRegistry {
    private static final String CONFIG_PATH = "configs/avatars.json";
    private static Avatar current; //storage system for the avatar to be able to be extracted later

    /**
     * simple class to create avatars stats.
     */
    private AvatarRegistry() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * finds the file with all the information about the avatars and constructs a list with all the avatars and their
     * information
     *
     * @return the list with all the information about the avatar currently in the game to be displayed
     */
    public static List<Avatar> getAll() {
        List<Avatar> avatars = new ArrayList<>();
        JsonValue root = new JsonReader().parse(Gdx.files.internal(CONFIG_PATH));
        for (JsonValue avatar : root) {
            avatars.add(new Avatar(
                    avatar.getString("id"),
                    avatar.getString("displayName"),
                    avatar.getString("texturePath"),
                    avatar.getInt("baseHealth"),
                    avatar.getInt("baseDamage"),
                    avatar.getFloat("moveSpeed"),
                    avatar.getString("atlas")
            ));
        }
        return avatars;
    }

    /**
     * Store the chosen avatar by the player
     *
     * @param avatar the avatar chosen by the player
     */
    public static void set(Avatar avatar) {
        current = avatar;
    }

    /**
     * grabs the selected avatar
     *
     * @return the avatar chosen by the player
     */
    public static Avatar get() {
        return current;
    }

    public static Avatar byId(String id) {
        if (id == null || id.isBlank()) return null;
        for (Avatar a : getAll()) {
            // If Avatar is a record, this is a.id(); if it's a POJO, change to a.getId()
            if (id.equals(a.id())) {
                return a;
            }
        }
        return null;
    }

}
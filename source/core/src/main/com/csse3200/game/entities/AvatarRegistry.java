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
    private static Avatar current; //storage system for the avatar to be able to be extracted later
    private static final String CONFIG_PATH = "configs/avatars.json";

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
                    avatar.getFloat("moveSpeed")
            ));
        }
        return avatars;
    }

    public static void set(Avatar avatar) {
        current = avatar;
    }

    public static Avatar get() {
        return current;
    }
}
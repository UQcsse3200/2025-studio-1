package com.csse3200.game.entities;

import java.util.ArrayList;
import java.util.List;


/**
 * simple class to create avatars stats.
 * TODO: This will be replaced by a jason file to extract information but this suffices for a prototype
 */
public class AvatarRegistry {
    private static Avatar current; //storage system for the avatar to be able to be extracted later

    public static List<Avatar> getAll() {
        List<Avatar> list = new ArrayList<>();
        list.add(new Avatar("scout", "Scout", "images/avatars/burger.png", 80, 10, 5.0f));
        list.add(new Avatar("soldier", "Soldier", "images/avatars/fighter.png", 120, 15, 3.8f));
        list.add(new Avatar("engineer", "Engineer", "images/avatars/firecommander.png", 100, 8, 4.2f));
        return list;
    }

    public static void set(Avatar avatar) {
        current = avatar;
    }

    public static Avatar get() {
        return current;
    }
}
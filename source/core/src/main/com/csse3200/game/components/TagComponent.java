package com.csse3200.game.components;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

// DEPRECATED CLASS DO NOT USE KEPT FOR REFERENCE

/**
 * Component which can be used to assign a "tag" to an entity, thus allowing its category
 * to be identified
 * eg. A pistol entity can be given the tag "ranged", which allows it to be identified as a
 * ranged weapon, utilising different logic than melee weapons
 */

public class TagComponent extends Component {
    public enum Tag {
        RANGED,
        MELEE
    }

    /**
     * The tag identifying the type of entity
     */

    private final EnumSet<Tag> tags = EnumSet.noneOf(Tag.class);

    /**
     * Tag component constructor
     * @param tag a string assigned to the entity
     */
    public TagComponent() {}

    public TagComponent(Collection<Tag> tagsToAdd) {
        if (tagsToAdd == null) {
            throw new IllegalArgumentException("tags cannot be null");
        }
        tags.addAll(tagsToAdd);
    }

    public boolean has(Tag tag) {
        return tags.contains(tag);
    }

    public boolean hasAny(Tag... any) {
        for (Tag t : any) if (tags.contains(t)) return true;
        return false;
    }

    public boolean hasAll(Tag... all) {
        for (Tag t : all) if (!tags.contains(t)) return false;
        return true;
    }

    public Set<Tag> getAll() {
        return EnumSet.copyOf(tags);
    }

    public TagComponent addAll(Collection<Tag> add) {
        tags.addAll(add);
        return this;
    }

    public TagComponent add(Tag tag) {
        tags.add(tag);
        return this;
    }
}

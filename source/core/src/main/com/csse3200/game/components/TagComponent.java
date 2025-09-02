package com.csse3200.game.components;

/**
 * Component which can be used to assign a "tag" to an entity, thus allowing its category
 * to be identified
 * eg. A pistol entity can be given the tag "ranged", which allows it to be identified as a
 * ranged weapon, utilising different logic than melee weapons
 */

public class TagComponent extends Component {

    /**
     * The tag identifying the type of entity
     */

    private final String tag;

    /**
     * Tag component constructor
     * @param tag a string assigned to the entity
     */

    public TagComponent(String tag) {

        this.tag = tag;
    }

    /**
     * Gives the entity's tag
     * @return tag
     */

    public String getTag() {

        return tag;
    }

}

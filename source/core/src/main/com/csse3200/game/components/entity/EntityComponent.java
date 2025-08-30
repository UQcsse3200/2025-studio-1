package com.csse3200.game.components.entity;

import com.csse3200.game.components.Component;

public class EntityComponent extends Component {
    private String name;
    private int id;
    private String type;

    /**
     * @param id the unique id for every entity to help identify it
     * @param name of the entity
     * @param type an enity can be of several types(eg: weapons, main player, enemy, etc)
     */

    public EntityComponent(String name, int id, String type) {
      this.name = name;
      this.id = id;
      this.type = type;
    }

    public EntityComponent() {}

    /**
     *
     * @return the name of the entity
     */
    public String getName() {
      return name;
    }

    /**
     *
     * @param name takes name of the entity and set it to that
     *
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     *
     * @return the id of a specific entity
     */
    public int getId() {return id;}

    /**
     *
     * @param id sets the entity id to the given value
     */
    public void setId(int id) {this.id = id;}

    /**
     *
     * @return the type of the selected entity
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type defines the type of entity(eg: weapon, player, enemy)
     */

    public void setType(String type) {
        this.type = type;
    }


    /**
     * displays the key information of the entity like name, type, id
     */
    public String display(){
        return("Name : " + getName() + " " +
                "Id : " + getId() + " " +
            "Type : " + getType());
    }
}



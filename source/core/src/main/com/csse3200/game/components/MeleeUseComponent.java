package com.csse3200.game.components;

public class MeleeUseComponent extends Component {

    @Override
    public void create() {
        entity.getEvents().addListener("use", this::use);

    }

    private void use() {
        // attack
    }
}

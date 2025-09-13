package com.csse3200.game.components.enemy;

import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.Component;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.physics.components.PhysicsComponent;


public class ShrinkFixtureComponent extends Component {
    private final float radius;   // 世界单位半径，如 0.15f
    private final boolean sensor; // true=只触发不阻挡（子弹通常为 true）

    public ShrinkFixtureComponent(float radius, boolean sensor) {
        this.radius = radius;
        this.sensor = sensor;
    }

    @Override
    public void create() {
        PhysicsComponent pc = entity.getComponent(PhysicsComponent.class);
        if (pc == null || pc.getBody() == null) return;

        Body body = pc.getBody();

        Array<Fixture> copy = new Array<>(body.getFixtureList());
        for (Fixture f : copy) {
            body.destroyFixture(f);
        }

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.isSensor = sensor; // 命中判定但不阻挡

        body.createFixture(def);
        shape.dispose();
    }

}

package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.StationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class InteractableStationFactory {

    /**
     * Creates the interactable computer bench for upgrading weapons
     * @return the bench
     */
    public static Entity createComputerBench() {
        Entity bench = createBaseStation();
        bench.addComponent(new TextureRenderComponent("images/computerBench.png"));
        bench.addComponent(new StationComponent());
        bench.getComponent(TextureRenderComponent.class).scaleEntity();
        bench.scaleHeight(2.5f);
        bench.getComponent(TextureRenderComponent.class);
        PhysicsUtils.setScaledCollider(bench, 0.5f, 0.3f);
        bench.getComponent(ColliderComponent.class).setAsBoxAligned(new Vector2(0.5f, 0.3f),
                PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.TOP);

        return bench;
    }

    /**
     * Creates a base interactable station
     * @return the station
     */
    public static Entity createBaseStation() {
        Entity base = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.STATION));
        base.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.StaticBody);
        base.getComponent(ColliderComponent.class).setSensor(true);
        return base;

    }


    private InteractableStationFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}

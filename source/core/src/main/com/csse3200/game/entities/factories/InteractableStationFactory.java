package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.stations.StationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Benches;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class InteractableStationFactory {



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

    /**
     * Create an interactable station
     * @param benchType the type of station
     * @return the station
     */
    public static Entity createStation(Benches benchType) {
        Entity bench = createBaseStation();
        BenchConfig config = benchType.getConfig();
        bench.addComponent(new TextureRenderComponent(config.texturePath));
        bench.addComponent(new StationComponent(config));
        bench.getComponent(TextureRenderComponent.class).scaleEntity();
        bench.scaleHeight(2.5f);
        bench.getComponent(TextureRenderComponent.class);
        PhysicsUtils.setScaledCollider(bench, 1f, 1f);
        bench.getComponent(ColliderComponent.class).setAsBoxAligned(new Vector2(1f, 1f),
                PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.CENTER);
        return bench;
    }


    private InteractableStationFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}

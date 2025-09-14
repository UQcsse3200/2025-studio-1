package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.StationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Benches;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;

public class InteractableStationFactory {

    /**
     * Creates the interactable computer bench for upgrading weapons
     * @return the bench
     */
//    public static Entity createComputerBench() {
//        Entity bench = createBaseStation();
//        bench.addComponent(new TextureRenderComponent("images/computerBench.png"));
//        bench.addComponent(new StationComponent(ItemTypes.COMPUTER_BENCH.config));
//        bench.getComponent(TextureRenderComponent.class).scaleEntity();
//        bench.scaleHeight(2.5f);
//        bench.getComponent(TextureRenderComponent.class);
//        PhysicsUtils.setScaledCollider(bench, 0.5f, 0.3f);
//        bench.getComponent(ColliderComponent.class).setAsBoxAligned(new Vector2(0.5f, 0.3f),
//                PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.TOP);
//
//        return bench;
//    }

    /**
     * Creates an interactable health bench to upgrade total player health.
     * @return A new health bench.
     */
//    public static Entity createHealthBench() {
//        Entity healthBench = createBaseStation();
//        healthBench.addComponent(new TextureRenderWithRotationComponent("images/healthBench.png"));
//        healthBench.addComponent(new StationComponent());
//        healthBench.getComponent(TextureRenderComponent.class).scaleEntity();
//        healthBench.scaleHeight(2.5f);
//        healthBench.getComponent(TextureRenderComponent.class);
//        PhysicsUtils.setScaledCollider(healthBench, 0.5f, 0.3f);
//        healthBench.getComponent(ColliderComponent.class).setAsBoxAligned(new Vector2(0.5f, 0.3f),
//                PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.TOP);
//
//        return healthBench;
//    }

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

    public static Entity createStation(Benches benchType) {
        Entity bench = createBaseStation();
        BenchConfig config = benchType.getConfig();
        bench.addComponent(new TextureRenderComponent(config.texturePath));
        bench.addComponent(new StationComponent(config));
        bench.getComponent(TextureRenderComponent.class).scaleEntity();
        bench.scaleHeight(2.5f);
        bench.getComponent(TextureRenderComponent.class);
        PhysicsUtils.setScaledCollider(bench, 0.5f, 0.3f);
        bench.getComponent(ColliderComponent.class).setAsBoxAligned(new Vector2(0.5f, 0.3f),
                PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.TOP);

        return bench;
    }


    private InteractableStationFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}

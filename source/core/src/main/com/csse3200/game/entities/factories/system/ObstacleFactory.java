package com.csse3200.game.entities.factories.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.BreakablePlatformComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;


/**
 * Builds common static props and triggers used by the level (trees, floors, desks, crates, etc.).
 * Each method returns a ready-to-place Entity with the right components already attached.
 * Where needed we set a StaticBody so the object does not move, and assign a collider layer
 * to decide if it should block the player (OBSTACLE) or just detect overlap (DEFAULT + sensor).
 */
public class ObstacleFactory {

    /**
     * Creates a tree entity.
     *
     * @return entity
     */
    public static Entity createTree() {
        Entity tree = new Entity()
                .addComponent(new TextureRenderComponent("images/tree.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        tree.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        tree.getComponent(TextureRenderComponent.class).scaleEntity();
        tree.scaleHeight(2.5f);
        PhysicsUtils.setScaledCollider(tree, 0.5f, 0.2f);
        return tree;
    }

    /**
     * Creates a Marble Platform for Fancy Rooms
     *
     * @return entity
     */
    public static Entity createMarblePlatform() {
        Entity MarblePlatform = new Entity()
                .addComponent(new TextureRenderComponent("images/MarblePlatform.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        MarblePlatform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        MarblePlatform.getComponent(TextureRenderComponent.class).scaleEntity();
        MarblePlatform.scaleHeight(0.5f);
        PhysicsUtils.setScaledCollider(MarblePlatform, 1f, 0.75f);
        return MarblePlatform;
    }

    public static Entity createShipmentBoxes() {
        Entity ShipmentBoxes = new Entity()
                .addComponent(new TextureRenderComponent("images/ShipmentBoxLid.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        ShipmentBoxes.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        ShipmentBoxes.getComponent(TextureRenderComponent.class).scaleEntity();
        ShipmentBoxes.scaleHeight(0.05f);
        PhysicsUtils.setScaledCollider(ShipmentBoxes, 1f, 0.75f);
        return ShipmentBoxes;
    }

    public static Entity createShipmentCrane() {
        Entity ShipmentCrane = new Entity()
                .addComponent(new TextureRenderComponent("images/ShipmentCrane.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        ShipmentCrane.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        ShipmentCrane.getComponent(TextureRenderComponent.class).scaleEntity();
        ShipmentCrane.scaleHeight(0.05f);
        PhysicsUtils.setScaledCollider(ShipmentCrane, 1f, 0.75f);
        return ShipmentCrane;
    }

    public static Entity createConveyor() {
        Entity ShipmentCrane = new Entity()
                .addComponent(new TextureRenderComponent("images/Conveyor.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        ShipmentCrane.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        ShipmentCrane.getComponent(TextureRenderComponent.class).scaleEntity();
        ShipmentCrane.scaleHeight(0.05f);
        PhysicsUtils.setScaledCollider(ShipmentCrane, 1f, 0.75f);
        return ShipmentCrane;
    }

    /**
     * Long, thin floor piece for platforms/walkways.
     * Solid so the player can stand on it.
     *
     * @return a static floor Entity
     */
    public static Entity createLongFloor() {
        Entity longFloor =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/general/Test.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        longFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        longFloor.getComponent(TextureRenderComponent.class).scaleEntity();
        longFloor.scaleHeight(0.6f);
        PhysicsUtils.setScaledCollider(longFloor, 1f, 1f);
        return longFloor;
    }

    /**
     * Create visible floor
     * 
     * @return a visible static floor Entity
     */
    public static Entity createVisibleLongFloor() {
        Entity longFloor =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/general/LongFloor.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        longFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        longFloor.getComponent(TextureRenderComponent.class).scaleEntity();
        longFloor.scaleHeight(2f);
        PhysicsUtils.setScaledCollider(longFloor, 1f, 1f);
        return longFloor;
    }

    /**
     * A very tall wall-like block (same art as thick floor but much taller).
     * Good for visual dividers in the background.
     *
     * @return a tall static prop
     */
    public static Entity createBigThickFloor() {
        Entity bigThickFloor =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/general/ThickFloor.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        bigThickFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        bigThickFloor.getComponent(TextureRenderComponent.class).scaleEntity();
        bigThickFloor.scaleHeight(20f);
        PhysicsUtils.setScaledCollider(bigThickFloor, 1f, 1f);
        return bigThickFloor;
    }

    /**
     * Decorative railing. Visual-only: no physics/collision so it never blocks the player.
     *
     * @return a simple sprite Entity
     */
    public static Entity createRailing() {
        Entity railing =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/general/Railing.png"));

        railing.getComponent(TextureRenderComponent.class).scaleEntity();
        railing.scaleHeight(0.5f);
        return railing;
    }

    /**
     * Short stair-like prop the player cannot pass through.
     *
     * @return a static solid stair block
     */
    public static Entity createSmallStair() {
        Entity smallStair =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/general/SmallStair.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        smallStair.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        smallStair.getComponent(TextureRenderComponent.class).scaleEntity();
        smallStair.scaleHeight(1f);
        PhysicsUtils.setScaledCollider(smallStair, 1f, 1f);
        return smallStair;
    }

    /**
     * Regular thick floor block (short version of the big wall).
     *
     * @return a static ground piece
     */
    public static Entity createThickFloor() {
        Entity thickFloor =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/general/ThickFloor.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        thickFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        thickFloor.getComponent(TextureRenderComponent.class).scaleEntity();
        thickFloor.scaleHeight(3f);
        PhysicsUtils.setScaledCollider(thickFloor, 1f, 1f);
        return thickFloor;
    }

    // Added the thin floor on the map where the computer is placed
    public static Entity createThinFloor() {
        Entity thinFloor =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/general/ThinFloor3.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        thinFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        thinFloor.getComponent(TextureRenderComponent.class).scaleEntity();
        thinFloor.scaleHeight(0.8f);
        PhysicsUtils.setScaledCollider(thinFloor, 1f, 0.9f);
        return thinFloor;
    }

    /**
     * creating the platform used in reception room
     **/
    public static Entity createplatform2() {
        Entity platform2 =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/platform-2.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        platform2.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        platform2.getComponent(TextureRenderComponent.class).scaleEntity();
        platform2.scaleHeight(3f);
        PhysicsUtils.setScaledCollider(platform2, 0.7f, 0.55f);
        return platform2;
    }

    /**
     * creating the clock used in reception room
     **/
    public static Entity createholoclock() {
        Entity clockSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/holo-clock.png"));
        clockSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        clockSpawn.scaleHeight(2f);
        return clockSpawn;
    }

    /**
     * creating the platform for Office area
     **/
    public static Entity createOfficeElevatorPlatform() {
        Entity platform =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/Office and elevator/Platform for elevator.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        platform.getComponent(TextureRenderComponent.class).scaleEntity();
        platform.scaleHeight(3f);
        // Thin collider aligned to the top so the player stands on the platform surface
        Vector2 colliderSize = platform.getScale().cpy().scl(0.9f, 0.10f);
        // Lower the collider slightly to account for transparent pixels above the platform surface
        float offsetDown = 1.10f;
        Vector2 colliderPos = new Vector2(
                platform.getScale().x / 2f,
                platform.getScale().y - (colliderSize.y / 2f) - offsetDown);
        platform.getComponent(ColliderComponent.class).setAsBox(colliderSize, colliderPos);
        return platform;
    }

    /**
     * creating the platform for Elevator area
     **/
    public static Entity createElevatorPlatform() {
        Entity platform =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/Office and elevator/Office platform.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        platform.getComponent(TextureRenderComponent.class).scaleEntity();
        platform.scaleHeight(3f);
        Vector2 colliderSize = platform.getScale().cpy().scl(0.9f, 0.10f);
        float offsetDown = 1.10f;
        Vector2 colliderPos = new Vector2(
                platform.getScale().x / 2f,
                platform.getScale().y - (colliderSize.y / 2f) - offsetDown);
        platform.getComponent(ColliderComponent.class).setAsBox(colliderSize, colliderPos);
        return platform;
    }

    /**
     * creating the help desk used in reception room
     **/
    public static Entity createdesk_reception() {
        Entity desk_receptionSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/desk_reception.png"));
        desk_receptionSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        desk_receptionSpawn.scaleHeight(3f);
        return desk_receptionSpawn;
    }

    /**
     * creating the comic stand used in reception room
     **/
    public static Entity createcomic_stand() {
        Entity comic_standSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/comics.png"));
        comic_standSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        comic_standSpawn.scaleHeight(1.5f);
        return comic_standSpawn;
    }

    /**
     * Creates a bit bigger platforms for Main hall
     **/
    public static Entity createplatform3() {
        Entity platform3 =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/platform-3.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        platform3.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        platform3.getComponent(TextureRenderComponent.class).scaleEntity();
        platform3.scaleHeight(3f);
        PhysicsUtils.setScaledCollider(platform3, 0.7f, 0.5f);
        return platform3;
    }

    /**
     * creates Sofa in bottom left in main hall
     **/
    public static Entity createMhall_sofa() {
        Entity Mhall_sofaSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/Mhall-sofa.png"));
        Mhall_sofaSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        Mhall_sofaSpawn.scaleHeight(3f);
        return Mhall_sofaSpawn;
    }

    /**
     * creates a screen decoration for main hall
     **/
    public static Entity createMhall_screen() {
        Entity Mhall_screenSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/Mhall-screen.png"));
        Mhall_screenSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        Mhall_screenSpawn.scaleHeight(1.5f);
        return Mhall_screenSpawn;
    }

    /**
     * creates a holographic decoration for main hall
     **/
    public static Entity createMhall_holo() {
        Entity Mhall_holoSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/Mhall-holo.png"));
        Mhall_holoSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        Mhall_holoSpawn.scaleHeight(1.5f);
        return Mhall_holoSpawn;
    }

    /**
     * Purple spawn pad prop. Solid so it rests on the ground like other props.
     *
     * @return a static pad entity
     */
    public static Entity createPurpleSpawnPad() {
        Entity purpSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/spawn_pads/SpawnPadPurple.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        purpSpawn.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        purpSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        purpSpawn.scaleHeight(0.7f);
        PhysicsUtils.setScaledCollider(purpSpawn, 1f, 1f);
        return purpSpawn;
    }

    /**
     * Red spawn pad prop. Identical behaviour to the purple pad.
     *
     * @return a static pad entity
     */
    public static Entity createRedSpawnPad() {
        Entity purpSpawn =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/spawn_pads/SpawnPadRed.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        purpSpawn.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        purpSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
        purpSpawn.scaleHeight(0.7f);
        PhysicsUtils.setScaledCollider(purpSpawn, 1f, 1f);
        return purpSpawn;
    }

    /**
     * Wooden crate that blocks the player (useful for cover or decoration).
     *
     * @return a static crate entity
     */
    public static Entity createCrate() {
        Entity crate =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/office/Crate.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        crate.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        crate.getComponent(TextureRenderComponent.class).scaleEntity();
        crate.scaleHeight(1f);
        PhysicsUtils.setScaledCollider(crate, 1f, 1f);
        return crate;
    }

    /**
     * Front-facing office desk placed on the thin floor. Solid to act like furniture.
     *
     * @return a static desk entity
     */
    public static Entity createOfficeDesk() {
        Entity officeDesk =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/office/officeDesk4.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        officeDesk.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        officeDesk.getComponent(TextureRenderComponent.class).scaleEntity();
        officeDesk.scaleHeight(2.0f);
        PhysicsUtils.setScaledCollider(officeDesk, 1f, 1f);
        return officeDesk;
    }


    /**
     * Glowing energy pod that acts as a solid prop on the floor.
     * Collider is slightly shorter (0.9) so it seats nicely on tiles when scaled.
     *
     * @return a static energy pod entity
     */
    public static Entity createLargeEnergyPod() {
        Entity energyPod =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/EnergyPod.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        energyPod.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        energyPod.getComponent(TextureRenderComponent.class).scaleEntity();
        energyPod.scaleHeight(1.5f);
        PhysicsUtils.setScaledCollider(energyPod, 1f, 0.9f);
        return energyPod;
    }

    /**
     * Green futuristic storage crate.
     *
     * @return a static crate entity (green)
     */
    public static Entity createStorageCrateGreen() {
        Entity crate =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/storage_crate_green2.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        crate.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        crate.getComponent(TextureRenderComponent.class).scaleEntity();
        crate.scaleHeight(1.5f);
        PhysicsUtils.setScaledCollider(crate, 1f, 1f);
        return crate;
    }

    /**
     * Dark futuristic storage crate.
     *
     * @return a static crate entity (dark)
     */
    public static Entity createStorageCrateDark() {
        Entity crate =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/storage_crate_dark2.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        crate.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        crate.getComponent(TextureRenderComponent.class).scaleEntity();
        crate.scaleHeight(1.5f);
        PhysicsUtils.setScaledCollider(crate, 1f, 1f);
        return crate;
    }

    /**
     * Creates a large security camera entity.
     * Decorative only; no physics or collider.
     *
     * @return A large security camera entity
     */
    public static Entity createLargeSecurityCamera() {
        Entity cam = new Entity()
                .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/SecurityCamera3.png"));
        cam.getComponent(TextureRenderComponent.class).scaleEntity();
        cam.scaleHeight(1.7f);
        return cam;
    }

    /**
     * Creates a security monitor entity.
     * Static and collidable for gameplay interactions.
     *
     * @return A static collidable security monitor entity
     */
    public static Entity createSecurityMonitor() {
        Entity monitor =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/Security/Monitor.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        monitor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        monitor.getComponent(TextureRenderComponent.class).scaleEntity();
        monitor.scaleHeight(2f);
        PhysicsUtils.setScaledCollider(monitor, 0.7f, 0.7f);
        return monitor;
    }

    /**
     * Creates a security platform entity.
     * Static and collidable to act as a solid prop or walkway.
     *
     * @return A static collidable security platform entity
     */
    public static Entity createSecurityPlatform() {
        Entity platform =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/Security/Platform.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
                        .addComponent(new BreakablePlatformComponent());

        platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        platform.getComponent(TextureRenderComponent.class).scaleEntity();
        platform.scaleHeight(1.0f);
        PhysicsUtils.setScaledCollider(platform, 0.3f, 0.6f);
        return platform;
    }


    /**
     * Creates a red security light entity.
     * Decorative only; no physics or collider.
     *
     * @return A decorative red light entity
     */
    public static Entity createRedLight() {
        Entity redLight =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/Security/RedLight.png"));

        redLight.getComponent(TextureRenderComponent.class).scaleEntity();
        redLight.scaleHeight(1f);
        return redLight;
    }

    /**
     * Creates a security system console entity.
     * Static and collidable for gameplay interactions.
     *
     * @return A static collidable security system entity
     */
    public static Entity createSecuritySystem() {
        Entity console =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/Security/SecuritySystem.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        console.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        console.getComponent(TextureRenderComponent.class).scaleEntity();
        console.scaleHeight(2.0f);
        PhysicsUtils.setScaledCollider(console, 0.7f, 0.7f);
        return console;
    }

    /**
     * Creates a laboratory main station entity with collision.
     * Acts as a solid, collidable obstacle in the Research Room.
     *
     * @return A static collidable laboratory entity
     */
    public static Entity createLaboratory() {
        Entity lab = new Entity()
                .addComponent(new TextureRenderComponent("foreg_sprites/Research/Laboratory.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        lab.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        lab.getComponent(TextureRenderComponent.class).scaleEntity();
        lab.scaleHeight(3.0f);
        PhysicsUtils.setScaledCollider(lab, 0.8f, 0.8f);
        return lab;
    }

    /**
     * Creates a microscope entity with collision.
     * The microscope is a static obstacle that blocks player movement.
     *
     * @return A static collidable microscope entity
     */
    public static Entity createMicroscope() {
        Entity scope = new Entity()
                .addComponent(new TextureRenderComponent("foreg_sprites/Research/Microscope.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        scope.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        scope.getComponent(TextureRenderComponent.class).scaleEntity();
        scope.scaleHeight(1.6f);
        PhysicsUtils.setScaledCollider(scope, 0.6f, 0.8f);
        return scope;
    }

    /**
     * Creates a research desk entity with collision.
     * The desk serves as a static obstacle that blocks movement.
     *
     * @return A static collidable research desk entity
     */
    public static Entity createResearchDesk() {
        Entity desk = new Entity()
                .addComponent(new TextureRenderComponent("foreg_sprites/Research/ResearchDesk.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        desk.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        desk.getComponent(TextureRenderComponent.class).scaleEntity();
        desk.scaleHeight(2.8f);
        PhysicsUtils.setScaledCollider(desk, 0.8f, 0.7f);
        return desk;
    }

    /**
     * Creates a research pod entity with collision.
     * Pods are large static obstacles in the Research Room.
     *
     * @return A static collidable research pod entity
     */
    public static Entity createResearchPod() {
        Entity pod = new Entity()
                .addComponent(new TextureRenderComponent("foreg_sprites/Research/ResearchPod.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        pod.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        pod.getComponent(TextureRenderComponent.class).scaleEntity();
        pod.scaleHeight(1.6f);
        PhysicsUtils.setScaledCollider(pod, 0.6f, 0.9f);
        return pod;
    }


    /**
     * Server rack (first variant, lighter colour).
     *
     * @return a static server rack entity
     */
    public static Entity createServerRack1() {
        Entity serverRack =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/furniture/ServerRack.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        serverRack.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        serverRack.getComponent(TextureRenderComponent.class).scaleEntity();
        serverRack.scaleHeight(1f);
        PhysicsUtils.setScaledCollider(serverRack, 1f, 1f);
        return serverRack;
    }

    /**
     * Server rack (second variant, darker colour).
     *
     * @return a static server rack entity
     */
    public static Entity createServerRack2() {
        Entity serverRack =
                new Entity()
                        .addComponent(new TextureRenderComponent("foreg_sprites/furniture/ServerRack2.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        serverRack.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        serverRack.getComponent(TextureRenderComponent.class).scaleEntity();
        serverRack.scaleHeight(1f);
        PhysicsUtils.setScaledCollider(serverRack, 1f, 1f);
        return serverRack;
    }

    /**
     * Makes a static door, no collision so that the player can pass through.
     */
    public static Entity createDoor() {
        Entity door = new Entity()
                .addComponent(new TextureRenderComponent("images/KeycardDoor.png"));
        door.getComponent(TextureRenderComponent.class).scaleEntity();
        door.scaleHeight(1.8f);
        return door;
    }

    public static Entity createWall(float width, float height) {
        Entity wall = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        wall.setScale(width, height);
        return wall;
    }

    /**
     * Thin trigger line used for doors/exits.
     * - Layer: DEFAULT
     * - Sensor: true (detects overlap, does not push)
     * You can attach your own component to react on contact (e.g., change room).
     *
     * @param width  world width of the trigger
     * @param height world height of the trigger
     * @return a non-blocking trigger entity
     */
    public static Entity createDoorTrigger(float width, float height) {
        Entity trigger = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.DEFAULT))
                .addComponent(new SolidColorRenderComponent(Color.BLACK));
        trigger.getComponent(ColliderComponent.class).setSensor(true);
        trigger.setScale(width, height);
        return trigger;
    }

    private ObstacleFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}

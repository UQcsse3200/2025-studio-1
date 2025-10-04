package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.friendlynpc.NpcDialogueDataComponent;
import com.csse3200.game.components.friendlynpc.TipComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.components.friendlynpc.*;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.*;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;

public class FriendlyNPCFactory {
    public static Entity createTip() {
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/!animation.atlas", TextureAtlas.class);
        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.addAnimation("float",       0.12f, Animation.PlayMode.LOOP);
        Entity tip = new Entity().addComponent(arc);
        arc.scaleEntity();
        arc.startAnimation("float");
        return tip;
    }

    public static Entity createTest(Entity player) {
        Entity test = new Entity()
                .addComponent(new TextureRenderComponent("images/nurse_npc.png"))
                .addComponent(new NpcDialogueDataComponent(
                        "Friendly NPC", "", new String[]{
                        "Hello",
                        "Click the dialog box to continue to the next sentence.",
                        "At the end, this sentence will automatically close."
                }
                ))
                .addComponent(new DialogueDisplay());
        var data = test.getComponent(NpcDialogueDataComponent.class);
        var ui   = test.getComponent(DialogueDisplay.class);
        test.getComponent(TextureRenderComponent.class).scaleEntity();
        test.addComponent(new TipComponent(test, player, 3f));
        test.addComponent(new NpcInterationComponent(player, 3f));
        return test;
    }

    /**
     * Creates a Guidance NPC entity.
     * <p>
     * The Guidance NPC is a floating robot that provides story background,
     * player guidance, and mission objectives. It uses the animation atlas
     * ("guidance_npc.atlas") to loop through the "robot_fire" frames,
     * giving the effect of hovering with a jet flame.
     *
     * @param player The player Entity, used to attach dialogue and interaction logic.
     * @return A new Entity representing the Guidance NPC with dialogue, tips, and animation.
     */
    public static Entity createGuidanceNpc(Entity player) {
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/guidance_npc.atlas", TextureAtlas.class);

        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.addAnimation("robot_fire", 0.12f, Animation.PlayMode.LOOP);

        Entity npc = new Entity()
                .addComponent(arc)
                .addComponent(new NpcDialogueDataComponent(
                        "Guide", "", new String[]{
                        "Welcome, pilot.",
                        "Follow the beacons to reach the safe zone.",
                        "Ping me if you need directions again."
                }))
                .addComponent(new DialogueDisplay())
                .addComponent(new TipComponent(null, player, 3f))
                .addComponent(new NpcInterationComponent(player, 3f));

        arc.scaleEntity();
        arc.startAnimation("robot_fire");
        return npc;
    }

    /**
     * Creates an Assister NPC entity.
     * <p>
     * The Assister is a friendly teammate NPC equipped with a gun.
     * It uses an animation atlas ("assister_npc.atlas") that defines
     * both left and right walking cycles. At runtime, the active
     * animation can be switched depending on movement direction.
     *
     * @return A new Entity representing the Assister NPC with walking animations.
     */
    public static Entity createAssisterNpc() {
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/assister_npc.atlas", TextureAtlas.class);

        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);

        arc.addAnimation("walk_right", 0.10f, Animation.PlayMode.LOOP);
        arc.addAnimation("walk_left",  0.10f, Animation.PlayMode.LOOP);

        Entity npc = new Entity().addComponent(arc);
        arc.scaleEntity();
        arc.startAnimation("walk_right"); // default facing
        return npc;
    }

    /**
     * Creates a Nurse NPC entity.
     * <p>
     * The Nurse is a static friendly NPC that represents a medic/healer.
     * It uses a single PNG texture ("medic_npc.png") without animation,
     * and can be used to restore or support the player’s health.
     *
     * @return A new Entity representing the Nurse NPC with a scaled texture.
     */
    public static Entity createNurseNpc() {
        Entity npc = new Entity()
                .addComponent(new TextureRenderComponent("images/nurse_npc.png"));
        npc.getComponent(TextureRenderComponent.class).scaleEntity();
        return npc;
    }

    public static Entity createPartner(Entity player) {
        Entity partner = new Entity()
                .addComponent(new TextureRenderComponent("images/partner.png"))
                .addComponent(new PartnerFollowComponent(player))
                .addComponent(new AutoCompanionShootComponent());

        partner.getComponent(TextureRenderComponent.class).scaleEntity();
        partner.setScale(0.7f, 0.7f);
        return partner;
    }

}
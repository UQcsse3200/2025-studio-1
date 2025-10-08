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

import com.csse3200.game.components.friendlynpc.*;
import java.util.List;
import java.util.Collections;

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
                .addComponent(new DialogueDisplay())
                .addComponent(new NpcHealingComponent(player, 50))
                .addComponent(new AssistorTaskComponent(player));
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
    public static Entity createGuidanceNpc(Entity player, List<Vector2> waypoints) {
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/guidance_npc.atlas", TextureAtlas.class);

        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.addAnimation("robot_fire", 0.12f, Animation.PlayMode.LOOP);

        Entity npc = new Entity()
                .addComponent(arc)
                .addComponent(new HoverBobComponent(0.08f, 2.0f))
                .addComponent(new NpcTwoOptionMenuComponent())
                .addComponent(new NpcLeadComponent(List.of(
                        new Vector2(12f, 7f),
                        new Vector2(18f, 7f),
                        new Vector2(25f, 12f)
                ), 2.2f, 0.6f))
                .addComponent(new NpcProximityGateComponent(player, 3f))
                .addComponent(new NpcDebugKeyInteractionComponent())
                .addComponent(new NpcDialogueDataComponent(
                        "Guide!", "", new String[]{
                        "Welcome, pilot.",
                        "Follow the beacons to reach the safe zone.",
                        "Ping me if you need directions again."
                }))
                .addComponent(new DialogueDisplay());

        npc.addComponent(new TipComponent(npc, player, 3f));
        npc.addComponent(new NpcInterationComponent(player, 3f));

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
    public static Entity createAssisterNpc(Entity player) {
        Entity assistor = new Entity()
                .addComponent(new TextureRenderComponent("images/Assistor.png"))
                .addComponent(new NpcDialogueDataComponent(
                        "Friendly NPC", "", new String[]{
                        "Hello, my friend",
                        "Do you need some help?",
                        "I have a gift for you!"
                }
                ))
                .addComponent(new DialogueDisplay())
                .addComponent(new AssistorTaskComponent(player));
        var data = assistor.getComponent(NpcDialogueDataComponent.class);
        var ui   = assistor.getComponent(DialogueDisplay.class);
        assistor.getComponent(TextureRenderComponent.class).scaleEntity();
        assistor.addComponent(new TipComponent(assistor, player, 3f));
        assistor.addComponent(new NpcInterationComponent(player, 3f));
        assistor.getComponent(TextureRenderComponent.class).scaleEntity();
        assistor.setScale(1.1f, 1.1f);
        return assistor;
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
    public static Entity createNurseNpc(Entity player) {
        Entity npc = new Entity()
                .addComponent(new TextureRenderComponent("images/nurse_npc.png"))
                .addComponent(new NpcDialogueDataComponent(
                        "Nurse", "", new String[]{
                        "Hello! I'm here to help.",
                        "Let me check your vitals...",
                        "You're all patched up now!"
                }))
                .addComponent(new DialogueDisplay())
                .addComponent(new NpcHealingComponent(player, 25)
                        .setCooldownMillis(30_000));
        npc.getComponent(TextureRenderComponent.class).scaleEntity();
        npc.addComponent(new TipComponent(npc, player, 3f));
        npc.addComponent(new NpcInterationComponent(player, 3f));

        return npc;
    }

    public static Entity createPartner(Entity player) {
        Entity partner = new Entity()
                .addComponent(new TextureRenderComponent("images/partner.png"))
                .addComponent(new PartnerFollowComponent(player))
                .addComponent(new CompanionFollowShootComponent())
                .addComponent(new AutoCompanionShootComponent())
                .addComponent(new RemoteOpenComponent());

        partner.getComponent(TextureRenderComponent.class).scaleEntity();
        partner.setScale(0.7f, 0.7f);
        return partner;
    }

}
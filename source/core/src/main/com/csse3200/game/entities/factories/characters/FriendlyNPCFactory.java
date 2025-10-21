package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.friendlynpc.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
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
import com.csse3200.game.components.friendlynpc.NpcAttackBoostComponent;

import java.util.List;

public class FriendlyNPCFactory {
    public static Entity createTip() {
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/!animation.atlas", TextureAtlas.class);
        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.addAnimation("float", 0.12f, Animation.PlayMode.LOOP);
        Entity tip = new Entity().addComponent(arc);
        arc.scaleEntity();
        arc.startAnimation("float");
        return tip;
    }

    public static Entity createTest(Entity player) {
        Entity test = InteractableStationFactory.createBaseStation()
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
        test.getComponent(TextureRenderComponent.class).scaleEntity();
        test.addComponent(new TipComponent(test, player, 3f));
        test.addComponent(new NpcInteractionComponent());
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
        Entity npc = InteractableStationFactory.createBaseStation()
                .addComponent(new TextureRenderComponent("images/guidance_friendly_npc.png"))
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
        npc.addComponent(new NpcInteractionComponent());

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
        Entity assistor = InteractableStationFactory.createBaseStation()
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
        assistor.getComponent(TextureRenderComponent.class).scaleEntity();
        assistor.addComponent(new TipComponent(assistor, player, 3f));
        assistor.addComponent(new NpcInteractionComponent());
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
        Entity npc = InteractableStationFactory.createBaseStation()
                .addComponent(new TextureRenderComponent("images/nurse_npc.png"))
                .addComponent(new NpcDialogueDataComponent(
                        "Nurse", "", new String[]{
                        "Hello! I'm here to help.",
                        "Let me check your vitals...",
                        "You're all patched up now! I've also boosted your power!"
                }))
                .addComponent(new DialogueDisplay())
                // + 50 HP
                // If at full health, + 50 shield
                .addComponent(new NpcHealingComponent(player, 50, 50, 60_000)
                        .setCooldownMillis(30_000))
                // Add attack boost: +10 attack for 15 seconds
                .addComponent(new NpcAttackBoostComponent(player, 10, 15_000)
                        .setCooldownMillis(30_000))
                 .addComponent(new ShieldDisplay()
                         .setIconPosition(6f, 22f)
                         .setIconSize(16f));
        npc.getComponent(TextureRenderComponent.class).scaleEntity();
        npc.addComponent(new TipComponent(npc, player, 3f));
        npc.addComponent(new NpcInteractionComponent());

        return npc;
    }

    public static Entity createPartner(Entity player) {
        Entity partner = InteractableStationFactory.createBaseStation()
                .addComponent(new TextureRenderComponent("images/partner.png"))
                .addComponent(new PartnerFollowComponent(player))
                .addComponent(new CompanionFollowShootComponent())
                .addComponent(new AutoCompanionShootComponent())
                .addComponent(new RemoteOpenComponent());

        partner.getComponent(TextureRenderComponent.class).scaleEntity();
        partner.setScale(0.7f, 0.7f);

        partner.addComponent(new Component() {

            private final float STOP_RADIUS = 1.0f;
            private final float TELEPORT_R = 5.0f;
            private final float SPEED = 8.0f;
            private final Vector2 TELEPORT_OFFSET = new Vector2(0.8f, 0f);

            @Override
            public void update() {
                if (player == null) return;
                float dt = 0.016f;
                try {
                    dt = com.csse3200.game.services.ServiceLocator.getTimeSource().getDeltaTime();
                } catch (Exception ignored) {
                }

                Vector2 myPos = entity.getPosition();
                Vector2 plPos = player.getPosition();

                Vector2 toPlayer = plPos.cpy().sub(myPos);
                float d2 = toPlayer.len2();

                if (d2 > TELEPORT_R * TELEPORT_R) {
                    entity.setPosition(plPos.x + TELEPORT_OFFSET.x, plPos.y + TELEPORT_OFFSET.y);
                    return;
                }

                if (d2 <= STOP_RADIUS * STOP_RADIUS) {
                    return;
                }

                if (!toPlayer.isZero()) {
                    toPlayer.nor().scl(SPEED * dt);
                    entity.setPosition(myPos.x + toPlayer.x, myPos.y + toPlayer.y);
                }
            }
        });
        return partner;
    }
}
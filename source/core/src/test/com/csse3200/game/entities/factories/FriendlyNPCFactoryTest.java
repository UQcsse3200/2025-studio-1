package com.csse3200.game.entities.factories.characters;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FriendlyNPCFactoryTest {

    @Test
    void testCreateTip() {
        Entity tip = FriendlyNPCFactory.createTip();
        assertNotNull(tip);
        assertNotNull(tip.getComponent(com.csse3200.game.rendering.AnimationRenderComponent.class));
    }

    @Test
    void testCreateTestNpc() {
        Entity player = new Entity(); // mock一个player
        Entity testNpc = FriendlyNPCFactory.createTest(player);
        assertNotNull(testNpc);
        assertNotNull(testNpc.getComponent(com.csse3200.game.rendering.TextureRenderComponent.class));
        assertNotNull(testNpc.getComponent(com.csse3200.game.components.friendlynpc.NpcDialogueDataComponent.class));
    }

    @Test
    void testCreateGuidanceNpc() {
        Entity player = new Entity();
        Entity npc = FriendlyNPCFactory.createGuidanceNpc(player);
        assertNotNull(npc);
        assertNotNull(npc.getComponent(com.csse3200.game.rendering.AnimationRenderComponent.class));
        assertNotNull(npc.getComponent(com.csse3200.game.components.friendlynpc.NpcDialogueDataComponent.class));
    }

    @Test
    void testCreateAssisterNpc() {
        Entity npc = FriendlyNPCFactory.createAssisterNpc();
        assertNotNull(npc);
        assertNotNull(npc.getComponent(com.csse3200.game.rendering.AnimationRenderComponent.class));
    }

    @Test
    void testCreateNurseNpc() {
        Entity npc = FriendlyNPCFactory.createNurseNpc();
        assertNotNull(npc);
        assertNotNull(npc.getComponent(com.csse3200.game.rendering.TextureRenderComponent.class));
    }
}

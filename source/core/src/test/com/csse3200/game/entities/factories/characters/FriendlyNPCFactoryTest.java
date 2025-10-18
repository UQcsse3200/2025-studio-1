package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.components.friendlynpc.*;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FriendlyNPCFactory.
 * These tests verify that each factory method correctly attaches
 * expected components and sets appropriate scaling values.
 * LibGDX dependencies are mocked to allow headless testing.
 */
class FriendlyNPCFactoryTest {

    /**
     * Sets up mocked LibGDX environment and ResourceService for headless testing.
     */
    @BeforeAll
    static void boot() {
        Gdx.app = mock(Application.class);

        // Mock ResourceService for both atlases and textures
        ResourceService rs = mock(ResourceService.class);

        // Mock a Texture so TextureRenderComponent.scaleEntity() has dimensions
        Texture fakeTex = mock(Texture.class);
        when(fakeTex.getWidth()).thenReturn(64);
        when(fakeTex.getHeight()).thenReturn(64);
        when(rs.getAsset(anyString(), eq(Texture.class))).thenReturn(fakeTex);

        // Mock a TextureAtlas with at least one region for any animation name
        TextureAtlas fakeAtlas = mock(TextureAtlas.class);
        AtlasRegion region = mock(AtlasRegion.class);
        when(region.getRegionWidth()).thenReturn(32);
        when(region.getRegionHeight()).thenReturn(32);
        Array<AtlasRegion> regions = new Array<>();
        regions.add(region);
        when(fakeAtlas.findRegions(anyString())).thenReturn(regions);
        when(rs.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(fakeAtlas);

        ServiceLocator.registerResourceService(rs);
    }

    /**
     * Verifies that Guidance NPC includes all expected behaviour components.
     */
    @Test
    void createGuidanceNpc_attachesExpectedComponents() {
        Entity player = new Entity();
        Entity npc = FriendlyNPCFactory.createGuidanceNpc(player, List.of());

        assertNotNull(npc.getComponent(AnimationRenderComponent.class));
        assertNotNull(npc.getComponent(HoverBobComponent.class));
        assertNotNull(npc.getComponent(NpcTwoOptionMenuComponent.class));
        assertNotNull(npc.getComponent(NpcLeadComponent.class));
        assertNotNull(npc.getComponent(NpcProximityGateComponent.class));
        assertNotNull(npc.getComponent(NpcDebugKeyInteractionComponent.class));
        assertNotNull(npc.getComponent(NpcDialogueDataComponent.class));
        assertNotNull(npc.getComponent(TipComponent.class));
        assertNotNull(npc.getComponent(NpcInteractionComponent.class));
    }

    /**
     * Ensures Assister NPC has dialogue, helper components, and correct scaling.
     */
    @Test
    void createAssisterNpc_hasDialogueHelpers_andScale() {
        Entity player = new Entity();
        Entity assister = FriendlyNPCFactory.createAssisterNpc(player);

        assertNotNull(assister.getComponent(TextureRenderComponent.class));
        assertNotNull(assister.getComponent(NpcDialogueDataComponent.class));
        assertNotNull(assister.getComponent(AssistorTaskComponent.class));
        assertNotNull(assister.getComponent(TipComponent.class));
        assertNotNull(assister.getComponent(NpcInteractionComponent.class));

        assertEquals(1.1f, assister.getScale().x, 1e-5);
        assertEquals(1.1f, assister.getScale().y, 1e-5);
    }

    /**
     * Confirms Nurse NPC provides healing and interaction components.
     */
    @Test
    void createNurseNpc_hasHealingAndTips() {
        Entity player = new Entity();
        Entity nurse = FriendlyNPCFactory.createNurseNpc(player);

        assertNotNull(nurse.getComponent(TextureRenderComponent.class));
        assertNotNull(nurse.getComponent(NpcDialogueDataComponent.class));
        assertNotNull(nurse.getComponent(NpcHealingComponent.class));
        assertNotNull(nurse.getComponent(TipComponent.class));
        assertNotNull(nurse.getComponent(NpcInteractionComponent.class));
    }

    /**
     * Validates Partner NPC includes companion behaviours and correct scaling.
     */
    @Test
    void createPartner_hasCompanionBehaviours_andScale() {
        Entity player = new Entity();
        Entity partner = FriendlyNPCFactory.createPartner(player);

        assertNotNull(partner.getComponent(TextureRenderComponent.class));
        assertNotNull(partner.getComponent(PartnerFollowComponent.class));
        assertNotNull(partner.getComponent(CompanionFollowShootComponent.class));
        assertNotNull(partner.getComponent(AutoCompanionShootComponent.class));
        assertNotNull(partner.getComponent(RemoteOpenComponent.class));

        assertEquals(0.7f, partner.getScale().x, 1e-5);
        assertEquals(0.7f, partner.getScale().y, 1e-5);
    }
}

package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.boss.*;
import com.csse3200.game.components.enemy.EnemyDeathRewardComponent;
import com.csse3200.game.components.enemy.EnemyMudBallAttackComponent;
import com.csse3200.game.components.enemy.EnemyMudRingSprayComponent;
import com.csse3200.game.components.npc.BossAnimationController;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BossFactory tests without extra helper files.
 * Provides an in-file TestResourceService to feed atlases/textures.
 */
class BossFactoryTest {

    // ---- Minimal in-file resource stub (no separate file) ----
    static class TestResourceService extends ResourceService {
        private final Map<String, TextureAtlas> atlases = new HashMap<>();
        private final Map<String, Texture> textures = new HashMap<>();

        <T> void put(String path, T asset, Class<T> type) {
            if (type == TextureAtlas.class) atlases.put(path, (TextureAtlas) asset);
            else if (type == Texture.class) textures.put(path, (Texture) asset);
        }

        @Override @SuppressWarnings("unchecked")
        public <T> T getAsset(String filepath, Class<T> type) {
            if (type == TextureAtlas.class) return (T) atlases.get(filepath);
            if (type == Texture.class) return (T) textures.get(filepath);
            return null;
        }

        @Override public void clearAllAssets() {
            atlases.values().forEach(a -> a.getTextures().forEach(Texture::dispose));
            atlases.clear();
            textures.values().forEach(Texture::dispose);
            textures.clear();
        }
    }

    private static HeadlessApplication app;
    private static TestResourceService resources;

    @BeforeAll
    static void boot() {
        app = new HeadlessApplication(new ApplicationAdapter(){});
        resources = new TestResourceService();
        ServiceLocator.registerResourceService(resources);

        // Seed exact assets BossFactory requests.
        resources.put("images/Robot_1.atlas", makeAtlas("Idle","attack","fury","die"), TextureAtlas.class);
        resources.put("images/boss_idle.atlas", makeAtlas("idle","phase2","angry"), TextureAtlas.class);
        resources.put("images/boss3_phase2.atlas", makeAtlas("phase1","phase2","phase3"), TextureAtlas.class);
        resources.put("images/Boss_3.png", make1x1(), Texture.class);
        resources.put("images/blackhole1.png", make1x1(), Texture.class);
        resources.put("images/laserball.png", make1x1(), Texture.class);
        resources.put("images/warning.png", make1x1(), Texture.class);
        resources.put("images/missle.png", make1x1(), Texture.class);
    }

    @AfterAll
    static void shutdown() {
        if (resources != null) resources.clearAllAssets();
        if (app != null) app.exit();
    }

    // ---------- Helpers ----------
    private static Texture make1x1() {
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.drawPixel(0,0, 0xFFFFFFFF);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static TextureAtlas makeAtlas(String... regionNames) {
        TextureAtlas atlas = new TextureAtlas();
        Texture base = make1x1();
        for (String n : regionNames) {
            atlas.addRegion(n, new TextureRegion(base));
        }
        return atlas;
    }

    private static Entity playerWithInventory() {
        return new Entity().addComponent(new InventoryComponent(10));
    }

    // ---------- Tests ----------
    @Test
    void createBaseBoss2_hasAnimationAndPhysics() {
        Entity base = BossFactory.createBaseBoss2(playerWithInventory());
        assertNotNull(base.getComponent(PhysicsComponent.class));
        assertNotNull(base.getComponent(PhysicsMovementComponent.class));
        assertNotNull(base.getComponent(ColliderComponent.class));
        assertEquals(PhysicsLayer.NPC, base.getComponent(HitboxComponent.class).getLayer());
        assertNotNull(base.getComponent(TouchAttackComponent.class));

        AnimationRenderComponent arc = base.getComponent(AnimationRenderComponent.class);
        assertNotNull(arc);
        assertTrue(arc.hasAnimation("idle"));
        assertTrue(arc.hasAnimation("phase2"));
        assertTrue(arc.hasAnimation("angry"));
        assertNotNull(base.getComponent(BossFactory.ApplyInitialBoss2Setup.class));
    }

    @Test
    void createBoss3_hasTexturesAttacksAndPhases() {
        Entity boss3 = BossFactory.createBoss3(playerWithInventory());

        assertNotNull(boss3.getComponent(PhysicsComponent.class));
        assertNotNull(boss3.getComponent(ColliderComponent.class));
        assertEquals(PhysicsLayer.NPC, boss3.getComponent(HitboxComponent.class).getLayer());
        assertNotNull(boss3.getComponent(CombatStatsComponent.class));
        assertNotNull(boss3.getComponent(WeaponsStatsComponent.class));
        assertNotNull(boss3.getComponent(EnemyDeathRewardComponent.class));
        assertNotNull(boss3.getComponent(DamageReductionComponent.class));
        assertNotNull(boss3.getComponent(AttackProtectionComponent.class));
        assertNotNull(boss3.getComponent(AttackProtectionDisplay.class));
        assertNotNull(boss3.getComponent(TextureRenderComponent.class));
        assertNotNull(boss3.getComponent(EnemyMudBallAttackComponent.class));
        assertNotNull(boss3.getComponent(EnemyMudRingSprayComponent.class));

        AnimationRenderComponent phaseArc = boss3.getComponent(AnimationRenderComponent.class);
        assertNotNull(phaseArc);
        assertTrue(phaseArc.hasAnimation("phase1"));
        assertTrue(phaseArc.hasAnimation("phase2"));
        assertTrue(phaseArc.hasAnimation("phase3"));

        assertNotNull(boss3.getComponent(Boss3HealthPhaseSwitcher.class));
    }

    @Test
    void createFireball_setsProjectileScaleAndLayer() {
        Vector2 from = new Vector2(1,2);
        Entity e = BossFactory.createFireball(from, new Vector2(3,4));
        assertNotNull(e.getComponent(PhysicsComponent.class));
        assertNotNull(e.getComponent(ColliderComponent.class));
        assertEquals(PhysicsLayer.ENEMY_PROJECTILE, e.getComponent(HitboxComponent.class).getLayer());
        assertNotNull(e.getComponent(CombatStatsComponent.class));
        assertNotNull(e.getComponent(WeaponsStatsComponent.class));
        assertNotNull(e.getComponent(TextureRenderComponent.class));
        assertNotNull(e.getComponent(TouchAttackComponent.class));
        assertNotNull(e.getComponent(PhysicsProjectileComponent.class));
        assertEquals(from, e.getPosition());
    }

    @Test
    void createMissle_setsProjectileScaleAndLayer() {
        Vector2 from = new Vector2(7,8);
        Entity e = BossFactory.createMissle(from);
        assertNotNull(e.getComponent(PhysicsComponent.class));
        assertNotNull(e.getComponent(ColliderComponent.class));
        assertEquals(PhysicsLayer.ENEMY_PROJECTILE, e.getComponent(HitboxComponent.class).getLayer());
        assertNotNull(e.getComponent(CombatStatsComponent.class));
        assertNotNull(e.getComponent(WeaponsStatsComponent.class));
        assertNotNull(e.getComponent(TextureRenderComponent.class));
        assertNotNull(e.getComponent(TouchAttackComponent.class));
        assertEquals(from, e.getPosition());
    }

    @Test
    void createWarning_assignsTextureAndPosition() {
        Vector2 pos = new Vector2(4,9);
        Entity e = BossFactory.createWarning(pos);
        assertNotNull(e.getComponent(TextureRenderComponent.class));
        assertEquals(pos, e.getPosition());
    }

    @Test
    void createBlackhole_assignsTextureAndPosition() {
        Vector2 pos = new Vector2(2,3);
        Entity e = BossFactory.createBlackhole(pos, playerWithInventory());
        assertNotNull(e.getComponent(TextureRenderComponent.class));
        assertEquals(pos, e.getPosition());
    }

    @Test
    void createBaseNPC_hasAIAndTouch() {
        Entity e = BossFactory.createBaseNPC(playerWithInventory());
        assertNotNull(e.getComponent(PhysicsComponent.class));
        assertNotNull(e.getComponent(PhysicsMovementComponent.class));
        assertNotNull(e.getComponent(ColliderComponent.class));
        assertEquals(PhysicsLayer.NPC, e.getComponent(HitboxComponent.class).getLayer());
        assertNotNull(e.getComponent(TouchAttackComponent.class));
        assertNotNull(e.getComponent(com.csse3200.game.ai.tasks.AITaskComponent.class));
    }

    @Test
    void getDefaultCocoonPositions_returnsThreePositions() {
        Vector2[] ps = BossFactory.getDefaultCocoonPositions();
        assertNotNull(ps);
        assertEquals(3, ps.length);
    }
}

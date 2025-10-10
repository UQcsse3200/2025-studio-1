package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.*;
import com.csse3200.game.components.player.*;
import com.csse3200.game.entities.Avatar;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.characters.PlayerConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class PlayerFactoryTests {

    MockedStatic<FileLoader> mocked;
    MockedStatic<PhysicsUtils> physicsUtilsMock;
    EntityService entityService;

    @BeforeEach
    void setup() {
        mocked = mockStatic(FileLoader.class);

        PhysicsService physicsService = mock(PhysicsService.class);
        ServiceLocator.registerPhysicsService(physicsService);

        InputComponent ic = mock(InputComponent.class);
        InputFactory inputFactory = mock(InputFactory.class);
        InputService inputService = mock(InputService.class);
        ServiceLocator.registerInputService(inputService);
        when(inputService.getInputFactory()).thenReturn(inputFactory);
        when(inputFactory.createForPlayer()).thenReturn(ic);

        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);

        Avatar mockAvatar = mock(Avatar.class);
        when(mockAvatar.atlas()).thenReturn("bogos binted?");
        AvatarRegistry.set(mockAvatar);

        TextureAtlas mockAtlas = mock(TextureAtlas.class);
        TextureAtlas.AtlasRegion atlasRegion = mock(TextureAtlas.AtlasRegion.class);
        when(atlasRegion.getRegionWidth()).thenReturn(64);
        when(atlasRegion.getRegionHeight()).thenReturn(64);

        // Make findRegion return a valid region
        when(mockAtlas.findRegion(anyString())).thenReturn(atlasRegion);

        // Make resource service return this atlas
        when(resourceService.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(mockAtlas);

        physicsUtilsMock = mockStatic(PhysicsUtils.class);
        physicsUtilsMock.when(() -> PhysicsUtils.setScaledCollider(any(), anyFloat(), anyFloat()))
                .thenAnswer(invocation -> null); // do nothing

        entityService = mock(EntityService.class);

        ServiceLocator.registerEntityService(entityService);


    }

    @Test
    void safeLoadWhenNoConfig() {
        mocked.when(() -> FileLoader.read(PlayerConfig.class, "configs/player.json", FileLoader.Location.LOCAL))
                .thenReturn(null);

        PlayerConfig cfg = PlayerFactory.getStats();
        Assertions.assertEquals(0, cfg.gold);
        Assertions.assertEquals(100, cfg.health);
        Assertions.assertEquals(10, cfg.baseAttack);
    }

    @Test
    void safeLoadPlayerConfigWithFile() {
        PlayerConfig mockConfig = new PlayerConfig();
        mockConfig.gold = 50;
        mockConfig.health = 80;
        mockConfig.baseAttack = 15;

        mocked.when(() ->
                FileLoader.read(
                        eq(PlayerConfig.class),
                        anyString(),
                        any(FileLoader.Location.class)
                )
        ).thenReturn(Optional.of(mockConfig));

        PlayerConfig cfg = PlayerFactory.getStats();
        assertEquals(50, cfg.gold);
        assertEquals(80, cfg.health);
        assertEquals(15, cfg.baseAttack);
    }

    @Test
    void createPlayerTest() {
        PlayerConfig mockConfig = new PlayerConfig();
        mockConfig.gold = 50;
        mockConfig.health = 80;
        mockConfig.baseAttack = 15;
        PlayerFactory.setStats(mockConfig);


        Entity player = PlayerFactory.createPlayer();
        assertTrue(player.hasComponent(PhysicsComponent.class));
        assertTrue(player.hasComponent(ColliderComponent.class));
        assertTrue(player.getComponent(ColliderComponent.class).getLayer() == PhysicsLayer.PLAYER);
        assertTrue(player.hasComponent(PlayerActions.class));
        assertTrue(player.hasComponent(CombatStatsComponent.class));
        assertTrue(player.hasComponent(WeaponsStatsComponent.class));
        assertTrue(player.hasComponent(AmmoStatsComponent.class));
        assertTrue(player.hasComponent(InventoryComponent.class));
        assertTrue(player.hasComponent(ItemPickUpComponent.class));
        assertTrue(player.hasComponent(PlayerStatsDisplay.class));
        assertTrue(player.hasComponent(PlayerInventoryDisplay.class));
        assertTrue(player.hasComponent(StaminaComponent.class));
        assertTrue(player.hasComponent(AnimationRenderComponent.class));
        assertTrue(player.hasComponent(PowerupComponent.class));
        assertTrue(player.hasComponent(PlayerAnimationController.class));
        assertTrue(player.hasComponent(PlayerEquipComponent.class));
        assertTrue(player.hasComponent(ArmourEquipComponent.class));
        assertTrue(player.hasComponent(InteractComponent.class));

        assertEquals(player.getComponent(WeaponsStatsComponent.class).getCoolDown(), 0.2f, 0.00001);
        assertTrue(player.hasComponent(PowerComponent.class));
    }

    @Test
    void createPlayerWithArrowTest() {
        PlayerConfig mockConfig = new PlayerConfig();
        mockConfig.gold = 50;
        mockConfig.health = 80;
        mockConfig.baseAttack = 15;
        PlayerFactory.setStats(mockConfig);


        Entity player = PlayerFactory.createPlayerWithArrowKeys();
        assertTrue(player.hasComponent(PhysicsComponent.class));
        assertTrue(player.hasComponent(ColliderComponent.class));
        assertTrue(player.hasComponent(PlayerActions.class));
        assertTrue(player.hasComponent(CombatStatsComponent.class));
        assertTrue(player.hasComponent(WeaponsStatsComponent.class));
        assertTrue(player.hasComponent(InventoryComponent.class));
        assertTrue(player.hasComponent(ItemPickUpComponent.class));
        assertTrue(player.hasComponent(PlayerStatsDisplay.class));
        assertTrue(player.hasComponent(PlayerInventoryDisplay.class));
        assertTrue(player.hasComponent(StaminaComponent.class));
        assertTrue(player.hasComponent(AnimationRenderComponent.class));
        assertTrue(player.hasComponent(PlayerAnimationController.class));
        assertEquals(player.getComponent(WeaponsStatsComponent.class).getCoolDown(), 0.2f, 0.00001);
    }

    @Test
    void verifyAllAddAnimationCalls() {
        PlayerConfig mockConfig = new PlayerConfig();
        mockConfig.gold = 50;
        mockConfig.health = 80;
        mockConfig.baseAttack = 15;
        PlayerFactory.setStats(mockConfig);

        try (MockedConstruction<AnimationRenderComponent> mockAnimators =
                     mockConstruction(AnimationRenderComponent.class, (mock, context) -> {
                     })) {

            // Act
            PlayerFactory.createPlayer();

            // Get the animator that was created
            AnimationRenderComponent animator = mockAnimators.constructed().getFirst();

            // Verify each call made inside add_animations()
            verify(animator).addAnimation("right_run", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_run", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_jump", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("left_jump", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("right_stand", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_stand", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_walk", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_walk", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_crouch", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_crouch", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("left_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("right_fall", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("left_fall", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).startAnimation("right_stand");
        }
    }

    @Test
    void verifyAllAddAnimationCallsWithArrow() {
        PlayerConfig mockConfig = new PlayerConfig();
        mockConfig.gold = 50;
        mockConfig.health = 80;
        mockConfig.baseAttack = 15;
        PlayerFactory.setStats(mockConfig);

        try (MockedConstruction<AnimationRenderComponent> mockAnimators =
                     mockConstruction(AnimationRenderComponent.class, (mock, context) -> {
                     })) {

            // Act
            PlayerFactory.createPlayerWithArrowKeys();

            // Get the animator that was created
            AnimationRenderComponent animator = mockAnimators.constructed().getFirst();

            // Verify each call made inside add_animations()
            verify(animator).addAnimation("right_run", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_run", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_jump", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("left_jump", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("right_stand", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_stand", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_walk", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_walk", 0.1f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_crouch", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("left_crouch", 0.2f, Animation.PlayMode.LOOP);
            verify(animator).addAnimation("right_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("left_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("right_fall", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).addAnimation("left_fall", 0.1f, Animation.PlayMode.NORMAL);
            verify(animator).startAnimation("right_stand");
        }
    }


    @AfterEach
    void after() throws NoSuchFieldException, IllegalAccessException {
        mocked.close();
        java.lang.reflect.Field field = PlayerFactory.class.getDeclaredField("stats");
        field.setAccessible(true);
        field.set(null, null);
        physicsUtilsMock.close();
    }
}

package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class TextureRenderWithRotationComponentTest {

    TextureRenderWithRotationComponent comp;
    EntityService entityService;
    @BeforeEach
    void setup() {
        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);

        entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);

        Entity cameraEntity = new Entity();
        CameraComponent cameraComponent = mock(CameraComponent.class);
        Camera camera = mock(Camera.class);
        when(camera.unproject(any())).thenReturn(new Vector3(0, 0, 0));
        when(cameraComponent.getCamera()).thenReturn(camera);
        cameraEntity.addComponent(cameraComponent);

        Array<Entity> array = new Array<>();
        array.add(cameraEntity);
        when(entityService.getEntities()).thenReturn(array);


        Texture texture = mock(Texture.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
        comp = new TextureRenderWithRotationComponent("hello");


    }
    @Test
    void setGetRotation() {
        assertEquals(0, comp.getRotation(), 0.0001f);
        comp.setRotation(10f);
        assertEquals(10f, comp.getRotation(), 0.0001f);
    }

    @Test
    void getTexturePath() {
        assertEquals("hello", comp.getTexturePath());
    }

    @Test
    void rotationFollowsMouseForWeapon() {
        Entity gun = new Entity();
        MagazineComponent magazineComponent = mock(MagazineComponent.class);
        WeaponsStatsComponent weaponsStatsComponent = mock(WeaponsStatsComponent.class);
        gun.addComponent(magazineComponent)
                .addComponent(weaponsStatsComponent)
                .addComponent(comp);

        //No rotation
        gun.setPosition(0, 0);
        comp.draw(mock(SpriteBatch.class));
        assertEquals(0, comp.getRotation(), 0.0001f);

        //90 degree rotation
        gun.setPosition(0, -10);
        comp.draw(mock(SpriteBatch.class));
        assertEquals(90, comp.getRotation(), 0.0001f);

        //180 degree rotation
        gun.setPosition(10, 0);
        comp.draw(mock(SpriteBatch.class));
        assertEquals(180, comp.getRotation(), 0.0001f);
    }

    @Test
    void bulletFacesMouseInitiallyButNotAfter() {
        Entity bullet = new Entity();
        WeaponsStatsComponent weaponsStatsComponent = mock(WeaponsStatsComponent.class);
        bullet.addComponent(weaponsStatsComponent)
                .addComponent(comp);

        //Rotates initially
        bullet.setPosition(0, -10);
        comp.draw(mock(SpriteBatch.class));
        assertEquals(90, comp.getRotation(), 0.0001f);

        //No rotation after moving
        bullet.setPosition(50, -10);
        comp.draw(mock(SpriteBatch.class));
        assertEquals(90, comp.getRotation(), 0.0001f);
    }

    @Test
    void rocketFollowsEnemy() {
        Entity rocket = new Entity();
        WeaponsStatsComponent weaponsStatsComponent = mock(WeaponsStatsComponent.class);
        when(weaponsStatsComponent.getRocket()).thenReturn(true);
        rocket.addComponent(weaponsStatsComponent)
                .addComponent(comp);

        //Register player to track distance to enemies
        Entity player = new Entity();
        player.setPosition(0, 0);
        ServiceLocator.registerPlayer(player);

        //Rotates initially
        rocket.setPosition(0, 0);

        //Hitbox for enemy
        HitboxComponent hitboxComponent = mock(HitboxComponent.class);
        when(hitboxComponent.getLayer()).thenReturn(PhysicsLayer.NPC);

        //Close enemy
        Entity enemy1 = new Entity();
        enemy1.addComponent(hitboxComponent);
        enemy1.setPosition(0,10);

        //Far enemy
        Entity enemy2 = new Entity();
        enemy2.addComponent(hitboxComponent);
        enemy2.setPosition(1000, 0);

        //Get rid of camera comp and use enemies instead
        Array<Entity> array = new Array<>();
        array.add(enemy1);
        array.add(enemy2);
        when(entityService.getEntities()).thenReturn(array);

        comp.draw(mock(SpriteBatch.class));
        //Should rotate towards enemy 1
        assertEquals(90, comp.getRotation(), 0.0001f);

        //Now make enemy 2 closer barely
        enemy2.setPosition(0, -9);
        comp.draw(mock(SpriteBatch.class));
        assertEquals(-90, comp.getRotation(), 0.0001f);
    }
}


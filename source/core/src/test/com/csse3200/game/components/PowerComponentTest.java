package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PowerComponentTest {
    PowerComponent pc;
    Entity player;
    EntityService entityService;
    Entity currItem;
    @BeforeEach
    void setup() {
        player = new Entity();
        player.setPosition(0, 0.99F);
        player.addComponent(new InventoryComponent(1000));
        player.addComponent(new PlayerActions());
        currItem = new Entity();
        currItem.addComponent(new WeaponsStatsComponent(100));
        currItem.addComponent(new MagazineComponent(100));
        player.getComponent(InventoryComponent.class).setCurrItem(currItem);
        player.addComponent(new PowerupComponent());
        entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);
        pc = new PowerComponent(player);
    }

    @Test
    void nullTagDoesNothing() {
        Array<Entity> entities = new Array<>();
        entities.add(new Entity());
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        assertNull(player.getComponent(PowerupComponent.class).getEquippedWeapon());
    }

    @Test
    void rapidFirePowerupWorks() {
        Array<Entity> entities = new Array<>();
        Entity rapidFire = mock(Entity.class);
        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("rapidfire"));
        Vector2 mockPos = mock(Vector2.class);
        when(mockPos.dst(any(Vector2.class))).thenReturn(0.5f);
        when(rapidFire.getCenterPosition()).thenReturn(mockPos);

        entities.add(rapidFire);
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        assertEquals(player.getComponent(PowerupComponent.class).getEquippedWeapon(), currItem);
        verify(rapidFire).dispose();
        assertFalse(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
    }

    @Test
    void unlimitedAmmoPowerupWorks() {
        Array<Entity> entities = new Array<>();
        Entity rapidFire = mock(Entity.class);
        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("unlimitedammo"));
        Vector2 mockPos = mock(Vector2.class);
        when(mockPos.dst(any(Vector2.class))).thenReturn(0.5f);
        when(rapidFire.getCenterPosition()).thenReturn(mockPos);

        entities.add(rapidFire);
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        assertEquals(player.getComponent(PowerupComponent.class).getEquippedWeapon(), currItem);
        verify(rapidFire).dispose();
        assertFalse(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
    }

    @Test
    void aimbotPowerupWorks() {
        Array<Entity> entities = new Array<>();
        Entity rapidFire = mock(Entity.class);
        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("aimbot"));
        Vector2 mockPos = mock(Vector2.class);
        when(mockPos.dst(any(Vector2.class))).thenReturn(0.5f);
        when(rapidFire.getCenterPosition()).thenReturn(mockPos);

        entities.add(rapidFire);
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        assertEquals(player.getComponent(PowerupComponent.class).getEquippedWeapon(), currItem);
        verify(rapidFire).dispose();
        assertFalse(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
    }

    @Test
    void doubleProcessorsPowerupWorks() {
        Array<Entity> entities = new Array<>();
        Entity rapidFire = mock(Entity.class);
        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("doubleprocessors"));
        Vector2 mockPos = mock(Vector2.class);
        when(mockPos.dst(any(Vector2.class))).thenReturn(0.5f);
        when(rapidFire.getCenterPosition()).thenReturn(mockPos);

        entities.add(rapidFire);
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        verify(rapidFire).dispose();
        assertFalse(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
    }

    @Test
    void invalidTagDoesNothing() {
        Array<Entity> entities = new Array<>();
        Entity rapidFire = mock(Entity.class);
        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("bogos binted?"));
        Vector2 mockPos = mock(Vector2.class);
        when(mockPos.dst(any(Vector2.class))).thenReturn(0.5f);
        when(rapidFire.getCenterPosition()).thenReturn(mockPos);

        entities.add(rapidFire);
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        assertTrue(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
        assertNull(player.getComponent(PowerupComponent.class).getEquippedWeapon());
    }

    @Test
    void tooFarDoesNothing() {
        Array<Entity> entities = new Array<>();
        Entity rapidFire = mock(Entity.class);
        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("rapidfire"));
        Vector2 mockPos = mock(Vector2.class);
        when(mockPos.dst(any(Vector2.class))).thenReturn(1f);
        when(rapidFire.getCenterPosition()).thenReturn(mockPos);

        entities.add(rapidFire);
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        assertTrue(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
        assertNull(player.getComponent(PowerupComponent.class).getEquippedWeapon());

        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("unlimitedammo"));
        pc.update();
        assertTrue(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
        assertNull(player.getComponent(PowerupComponent.class).getEquippedWeapon());

        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("doubleprocessors"));
        pc.update();
        assertTrue(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
        assertNull(player.getComponent(PowerupComponent.class).getEquippedWeapon());

        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("aimbot"));
        pc.update();
        assertTrue(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
        assertNull(player.getComponent(PowerupComponent.class).getEquippedWeapon());

    }

    @Test
    void noWeaponDoesNothing() {
        Array<Entity> entities = new Array<>();
        Entity rapidFire = mock(Entity.class);
        when(rapidFire.getComponent(TagComponent.class)).thenReturn(new TagComponent("rapidfire"));
        Vector2 mockPos = mock(Vector2.class);
        when(mockPos.dst(any(Vector2.class))).thenReturn(0.5f);
        when(rapidFire.getCenterPosition()).thenReturn(mockPos);
        player.getComponent(InventoryComponent.class).setCurrItem(null);
        entities.add(rapidFire);
        when(entityService.getEntities()).thenReturn(entities);
        pc.update();
        assertEquals(player.getComponent(PowerupComponent.class).getEquippedWeapon(), null);
        assertTrue(player.getComponent(PowerupComponent.class).getEffects().isEmpty());
    }


}

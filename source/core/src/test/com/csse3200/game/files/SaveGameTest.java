package com.csse3200.game.files;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Avatar;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class SaveGameTest {

    private TestGameStateStats testedStats;
    private final String fileName = "saveFileValid.json";
    private static final SaveGame saveGame = spy(new SaveGame());
    private static SaveGame.GameState testState = new SaveGame.GameState();
    private SaveGame.itemInInven inventoryTest = new SaveGame.itemInInven();
    private SaveGame.information playerInfoTest = new SaveGame.information();
    private SaveGame.GameState testedState = SaveGame.loadGame("test/files/saveFileValid.json");

    private static final String AREA_ID = "Test#Area";
    private static final int MAX_HEALTH = 10;
    private static final int INITIAL_HEALTH = 1;
    private static final float POS_X = 1.0f;
    private static final float POS_Y = 1.0f;
    private static final double FLOAT_EPS = 1e-4;
    private static final int INVENTORY_PROCESSOR = 1;
    private static final int AMMO_RESERVE = 1;
    private static final int EXPECTED_ROUND_NUMBER = 1;

    private static final ArrayList<String> Armours =
            new ArrayList<>(Arrays.asList("Armour1", "Armour2"));
    private static final Set<String> Areas = new HashSet<>();

    static class FakeEntity extends Entity {
        private final List<Component> comps = new ArrayList<>();
        private Vector2 pos = new Vector2();

        @Override
        public <T extends Component> T getComponent(Class<T> type) {
            for (Component c : comps) {
                if (type.isInstance(c)) {
                    @SuppressWarnings("unchecked") T cast = (T) c;
                    return cast;
                }
            }
            return null;
        }

        @Override
        public Vector2 getPosition() {
            return pos.cpy();
        }

        @Override
        public void setPosition(Vector2 p) {
            this.pos = p.cpy();
        }

        @Override
        public Entity addComponent(Component component) {
            comps.add(component);
            component.setEntity(this);
            return this;
        }
    }

    @BeforeAll
    public static void fakeGameState() {

        FakeEntity player = new FakeEntity();
        player.addComponent(new StaminaComponent());
        player.addComponent(new CombatStatsComponent(MAX_HEALTH));
        player.addComponent(new InventoryComponent(INVENTORY_PROCESSOR));
        player.addComponent(new AmmoStatsComponent(AMMO_RESERVE));
        player.setPosition(new Vector2(POS_X, POS_Y));
        Avatar playerAvatarTest = mock(Avatar.class);
        AvatarRegistry.set(playerAvatarTest);
        player.getComponent(CombatStatsComponent.class).setHealth(INITIAL_HEALTH);

        testState.setArea(AREA_ID);
        testState.setArmour(Armours);

        testState.setWave(EXPECTED_ROUND_NUMBER);
        testState.setPlayer(player);
        InventoryComponent fakeInventory = player.getComponent(InventoryComponent.class);

        testState.setLoadedInventory(fakeInventory);

        Areas.add(AREA_ID);
        testState.setAreasVisited(Areas);

    }

    @Test
    @DisplayName("Test game loads valid format file")
    public void gameStateReadTest() {

    }

    @Test
    public void inventoryGetsSetTest() {

    }

    @Test
    public void setPlayerInfoTest() {

    }

    @Test
    public void invalidSaveFails() {

    }

    @Test
    public void saveFileComparitor() {

    }
}

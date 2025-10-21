package com.csse3200.game.files;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Avatar;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class SaveGameTest {

//    private final String fileName = "saveFileValid.json";
    private static final SaveGame.GameState testState = new SaveGame.GameState();
    private final SaveGame.itemInInven inventoryTest = new SaveGame.itemInInven();
    private static SaveGame.information playerInfo = new SaveGame.information();
//    private SaveGame.GameState testedState = SaveGame.loadGame("test/files/saveFileValid.json");

    private static final String AREA_ID = "Test#Area";
    private static final int MAX_HEALTH = 10;
    private static final int INITIAL_HEALTH = 1;
    private static final float POS_X = 1.0f;
    private static final float POS_Y = 1.0f;
    private static final double FLOAT_EPS = 1e-4;
    private static final int INVENTORY_PROCESSOR = 1;
    private static final int AMMO_RESERVE = 1;
    private static final int EXPECTED_ROUND_NUMBER = 1;
    private static TestGameStats.PlayerStatTest testStats;
    private static final int keyCard = 1;
    private static FakeEntity player;

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
    static void fakeGameState() {
        player = new FakeEntity();
        player.addComponent(new StaminaComponent());
        player.addComponent(new CombatStatsComponent(MAX_HEALTH));
        player.addComponent(new InventoryComponent(INVENTORY_PROCESSOR));
        player.addComponent(new AmmoStatsComponent(AMMO_RESERVE));
        player.setPosition(new Vector2(POS_X, POS_Y));
        player.getComponent(InventoryComponent.class).setKeycardLevel(keyCard);
        Avatar playerAvatarTest = new Avatar("testAvatar", "test", "testAvatarTex", 10, 5, 2, " ");

        AvatarRegistry.set(playerAvatarTest);
        player.getComponent(CombatStatsComponent.class).setHealth(INITIAL_HEALTH);

        InventoryComponent fakeInventory = player.getComponent(InventoryComponent.class);

        testState.setLoadedInventory(fakeInventory);

        testStats = new TestGameStats.PlayerStatTest();
    }

    @Test
    @DisplayName("Test game loads valid format file")
    void gameStateReadTest() {
        testState.setArmour(Armours);
        assertEquals(Armours, testState.getArmour());
        testState.setWave(EXPECTED_ROUND_NUMBER);
        assertEquals(1, testState.getWave());
        testState.setArea(AREA_ID);
        assertEquals(AREA_ID, testState.getGameArea());
        Areas.add(AREA_ID);
        testState.setAreasVisited(Areas);
        assertEquals(Areas, testState.getAreasVisited());
    }

    @Test
    void inventoryGetsSetTest() {
        // IMPLEMENT ME
    }

    @Test
    void getPlayerInfoTest() {
        testState.setPlayer(player);
        playerInfo = testState.getPlayer();
        assertEquals( testStats.avatarLoad, playerInfo.avatar);
        assertEquals(testStats.stamLoad, playerInfo.maxStamina);
        assertEquals(testStats.ammoLoad, playerInfo.ammoReserve);
        assertEquals( testStats.processorLoad, playerInfo.processor);
        assertEquals(testStats.keyCardLvl, playerInfo.keyCardLevel);
        assertEquals( testStats.healthLoad, playerInfo.currentHealth);
        assertEquals(testStats.possLoad, playerInfo.playerPos);
        assertEquals(testStats.maxHealth, playerInfo.maxHealth);
    }

    @Test
    void invalidSaveFails() {
        // IMPLEMENT ME
    }

    @Test
    void saveFileComparitor() {
        // IMPLEMENT ME
    }
}

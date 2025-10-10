package com.csse3200.game.services;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Avatar;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.SaveGame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(GameExtension.class)
class SaveLoadServiceTest {

    private static final String SLOT_ID = "slot1";
    private static final String AREA_ID = "Area#Test";
    private static final int MAX_HEALTH = 100;
    private static final int INITIAL_HEALTH = 80;
    private static final float POS_X = 5.0f;
    private static final float POS_Y = 7.0f;
    private static final double FLOAT_EPS = 1e-4;
    private static final int INVENTORY_PROCESSES = 1;
    private static final int EXPECTED_ROUND_NUMBER = 0;

    @Test
    void save_setsExpectedSnapshot_withoutFileIO() {
        CombatStatsComponent stats = new CombatStatsComponent(MAX_HEALTH);
        stats.setHealth(INITIAL_HEALTH);

        InventoryComponent inv = new InventoryComponent(INVENTORY_PROCESSES);
        AmmoStatsComponent ammTest = new AmmoStatsComponent(INVENTORY_PROCESSES);
        StaminaComponent stamTest = new StaminaComponent();

        FakeEntity player = new FakeEntity();
        player.addComponent(stats);
        player.addComponent(inv);
        player.addComponent(ammTest);
        player.addComponent(stamTest);
        player.setPosition(new Vector2(POS_X, POS_Y));
        Avatar playerAvatarTest = AvatarRegistry.getAll().get(1);
        AvatarRegistry.set(playerAvatarTest);

        //test to mock
        ServiceLocator.registerDiscoveryService(mock(DiscoveryService.class));
        ServiceLocator.registerDifficulty(mock(Difficulty.class));
        ServiceLocator.registerPlayer(player);

        GameArea area = mock(GameArea.class);
        ServiceLocator.registerGameArea(area);

        when(area.getEntities()).thenReturn(List.of(player));
        when(area.toString()).thenReturn(AREA_ID);



        SaveLoadService service = new SaveLoadService();

        final SaveGame.GameState[] captured = new SaveGame.GameState[1];
        try (MockedStatic<FileLoader> mocked = mockStatic(FileLoader.class)) {
            mocked.when(() -> FileLoader.write(
                            any(SaveGame.GameState.class),
                            anyString(),
                            any(FileLoader.Location.class),
                            anyBoolean())
                    )
                    .thenAnswer(invocation -> {
                        captured[0] = invocation.getArgument(0);
                        return null; // static void
                    });

            boolean ok = service.save(SLOT_ID, area);

            Assertions.assertTrue(ok, "save() should return true");
            Assertions.assertNotNull(captured[0], "Expected a single write() call");

            SaveGame.GameState out = captured[0];

            // snapshot fields
            Assertions.assertEquals(AREA_ID, out.getGameArea());
            Assertions.assertEquals(INITIAL_HEALTH, out.getPlayer().currentHealth,
                    "Health should originate from CombatStatsComponent");
            Assertions.assertEquals(POS_X, out.getPlayer().playerPos.x, FLOAT_EPS);
            Assertions.assertEquals(POS_Y, out.getPlayer().playerPos.y, FLOAT_EPS);
            Assertions.assertEquals(EXPECTED_ROUND_NUMBER, out.getWave());
//            Assertions.assertEquals(,out.getPlayer().avatar);
            Assertions.assertNotNull(out.getInventory(), "inventory list should be initialized (may be empty)");

            // optional: verify call shape
            mocked.verify(() -> FileLoader.write(
                    same(out),
                    anyString(),
                    any(FileLoader.Location.class),
                    eq(true)
            ));
        }
    }

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

}
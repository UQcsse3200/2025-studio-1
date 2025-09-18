package com.csse3200.game.services;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
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
    private static final int EXPECTED_ROUND_NUMBER = 2;

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

    @Test
    void save_setsExpectedSnapshot_withoutFileIO() {
        CombatStatsComponent stats = new CombatStatsComponent(MAX_HEALTH);
        stats.setHealth(INITIAL_HEALTH);

        InventoryComponent inv = new InventoryComponent(INVENTORY_PROCESSES);

        FakeEntity player = new FakeEntity();
        player.addComponent(stats);
        player.addComponent(inv);
        player.setPosition(new Vector2(POS_X, POS_Y));

        GameArea area = mock(GameArea.class);
        when(area.getEntities()).thenReturn(List.of(player));
        when(area.toString()).thenReturn(AREA_ID);

        SaveLoadService service = new SaveLoadService();

        final SaveLoadService.PlayerInfo[] captured = new SaveLoadService.PlayerInfo[1];
        try (MockedStatic<FileLoader> mocked = mockStatic(FileLoader.class)) {
            mocked.when(() ->
                            FileLoader.writeClass(any(SaveLoadService.PlayerInfo.class),
                                    anyString(),
                                    any(FileLoader.Location.class)))
                    .thenAnswer(invocation -> {
                        captured[0] = invocation.getArgument(0);
                        return null;
                    });

            boolean ok = service.save(SLOT_ID, area);

            Assertions.assertTrue(ok, "save() should return true");
            Assertions.assertNotNull(captured[0], "Expected a single writeClass() call");

            SaveLoadService.PlayerInfo out = captured[0];

            // Assert snapshot fields
            Assertions.assertEquals(AREA_ID, out.areaId);
            Assertions.assertEquals(INITIAL_HEALTH, out.Health, "Health should originate from CombatStatsComponent");
            Assertions.assertEquals(POS_X, out.position.x, FLOAT_EPS);
            Assertions.assertEquals(POS_Y, out.position.y, FLOAT_EPS);
            Assertions.assertEquals(EXPECTED_ROUND_NUMBER, out.RoundNumber);
            Assertions.assertNotNull(out.inventory, "inventory list should be initialized (may be empty)");
        }
    }
}
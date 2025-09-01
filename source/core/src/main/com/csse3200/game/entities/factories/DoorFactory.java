import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class DoorFactory {
    public static Entity createDoor(float x, float y, Runnable onEntered) {
        Entity door = new Entity()
                .addComponent(new PhysicsComponent()) // must come first
                .addComponent(new ColliderComponent()
                        .setLayer(PhysicsLayer.OBSTACLE)
                        .setSensor(true)) // so it doesn't block movement
                .addComponent(new DoorComponent(onEntered))
                .addComponent(new TextureRenderComponent("images/door.png"));

        door.getComponent(TextureRenderComponent.class).scaleEntity();
        door.setPosition(x, y);

        return door;
    }
}
package com.csse3200.game.components;

/**
 * Component tagging an entity with the room/area it belongs to.
 * Typically added to the player when spawning into a {@link com.csse3200.game.areas.GameArea}.
 */
public class RoomIdComponent extends Component {
    private String roomId;

    public RoomIdComponent(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId must not be null or blank");
        }
        this.roomId = roomId;
    }

    /** Returns the identifier for the room/area this entity belongs to. */
    public String getRoomId() { return roomId; }

    public RoomIdComponent setRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    @Override
    public String toString() {
        return "RoomIdComponent[" + roomId + "]";
    }
}

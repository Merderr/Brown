package net.runelite.api.events;


import lombok.Data;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;

/**
 * An event where a {@link GameObject} on a {@link Tile} has been replaced.
 */
@Data
public class GameObjectChanged {
    /**
     * The affected tile.
     */
    private Tile tile;
    /**
     * The game object that has been replaced.
     */
    private GameObject previous;
    /**
     * The new game object on the tile.
     */
    private GameObject gameObject;
}

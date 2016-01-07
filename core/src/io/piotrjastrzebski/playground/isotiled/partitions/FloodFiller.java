package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * Created by EvilEntity on 07/01/2016.
 */
class FloodFiller {
	private FloodFiller () {
	}

	public static void floodFill (int x, int y, TileMap tileMap, ObjectSet<Tile> out) {
		floodFill(x, y, 0, 0, tileMap.mapWidth - 1, tileMap.mapHeight - 1, tileMap, out);
	}

	public static void floodFill (int x, int y, MapRegion region, TileMap tileMap, ObjectSet<Tile> out) {
		floodFill(x, y, region.x, region.y, region.x + region.size - 1, region.y + region.size - 1, tileMap, out);
	}

	private static Array<Tile> queue = new Array<>(false, 16);
	private static IntMap<Tile> processed = new IntMap<>();

	/**
	 * Find all connected tiles starting from tile at x, y within given bounds
	 */
	public static void floodFill (int x, int y, int sx, int sy, int ex, int ey, TileMap tileMap, ObjectSet<Tile> out) {
		out.clear();
		processed.clear();
		queue.clear();
		Tile start = tileMap.getTileAt(x, y);
		if (start == null)
			throw new AssertionError("Tile cant be null here!");
		queue.add(start);
		while (queue.size > 0) {
			Tile tile = queue.removeIndex(0);
			if (tile.type != start.type)
				continue;
			if (processed.containsKey(tile.id))
				continue;
			processed.put(tile.id, tile);
			out.add(tile);
			Tile west = getWestEdge(tile, sx, sy, ex, ey, tileMap);
			Tile east = getEastEdge(tile, sx, sy, ex, ey, tileMap);

			for (int nx = west.x; nx <= east.x; nx++) {
				Tile next = tileMap.getTileAt(nx, west.y);
				if (next == null)
					throw new AssertionError("Tile cant be null here!");
				processed.put(next.id, next);
				out.add(next);
				Tile north = tileMap.getTileAt(nx, west.y + 1);
				if (north != null && north.x >= sx && north.x <= ex && north.y >= sy && north.y <= ey && north.type == start.type
					&& !processed.containsKey(north.id)) {
					queue.add(north);
				}
				Tile south = tileMap.getTileAt(nx, west.y - 1);
				if (south != null && south.x >= sx && south.x <= ex && south.y >= sy && south.y <= ey && south.type == start.type
					&& !processed.containsKey(south.id)) {
					queue.add(south);
				}
			}
		}
	}

	private static Tile getWestEdge (Tile tile, int sx, int sy, int ex, int ey, TileMap tiles) {
		return getEdge(tile, -1, sx, sy, ex, ey, tiles);
	}

	private static Tile getEastEdge (Tile tile, int sx, int sy, int ex, int ey, TileMap tiles) {
		return getEdge(tile, 1, sx, sy, ex, ey, tiles);
	}

	private static Tile getEdge (Tile tile, int offset, int sx, int sy, int ex, int ey, TileMap tiles) {
		while (true) {
			Tile next = tiles.getTileAt(tile.x + offset, tile.y);
			if (next != null && next.x >= sx && next.x <= ex && next.y >= sy && next.y <= ey && next.type == tile.type) {
				tile = next;
			} else {
				break;
			}
		}
		return tile;
	}
}

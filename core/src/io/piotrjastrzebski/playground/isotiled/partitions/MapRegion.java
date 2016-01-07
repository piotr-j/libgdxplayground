package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;

import java.util.Comparator;

/**
 * Created by EvilEntity on 07/01/2016.
 */
class MapRegion {
	private static Pool<SubRegion> subRegionPool = new Pool<SubRegion>() {
		@Override protected SubRegion newObject () {
			return new SubRegion();
		}
	};
	public int id;
	public final int x;
	public final int y;
	public final int size;
	public IntArray tiles = new IntArray();
	public Array<SubRegion> subs = new Array<>();

	public MapRegion (int id, int x, int y, int size) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.size = size;
	}

	public void addTile (Tile tile) {
		if (!contains(tile.x, tile.y))
			throw new AssertionError("Tile outside of region!");
		// proper ids?
		tiles.add(tile.id);
		tile.region = this;
	}

	public boolean contains (float x, float y) {
		return this.x <= x && this.x + size >= x && this.y <= y && this.y + size >= y;
	}

	private static ObjectSet<Tile> found = new ObjectSet<>();
	private static ObjectSet<Tile> added = new ObjectSet<>();
	public void rebuild (TileMap tileMap) {
		added.clear();
		subRegionPool.freeAll(subs);
		subs.clear();
		for (int tx = x; tx < x + size; tx++) {
			for (int ty = y; ty < y + size; ty++) {
				found.clear();
				Tile tile = tileMap.getTileAt(tx, ty);
				if (tile == null || added.contains(tile)) continue;
				SubRegion sub = subRegionPool.obtain().init(subs.size, this);
				subs.add(sub);
				FloodFiller.floodFill(tx, ty, this, tileMap, found);
				added.addAll(found);
				for (Tile t : found) {
					sub.add(t);
				}
			}
		}
	}

	public void rebuildSubRegions(TileMap tileMap) {
		for (SubRegion sub : subs) {
			sub.rebuild(tileMap);
		}
	}

	public SubRegion getSubRegionAt (int x, int y) {
		for (SubRegion sub : subs) {
			if (sub.contains(x, y)) return sub;
		}
		return null;
	}

	public static class SubRegion implements Pool.Poolable {
		private static Comparator<Tile> tileXComp = new Comparator<Tile>() {
			@Override public int compare (Tile o1, Tile o2) {
				if (o1.y == o2.y) return o1.x - o2.x;
				return o1.y - o2.y;
			}
		};

		private static Comparator<Tile> tileYComp = new Comparator<Tile>() {
			@Override public int compare (Tile o1, Tile o2) {
				if (o1.x == o2.x) return o1.y - o2.y;
				return o1.x - o2.x;
			}
		};

		public MapRegion parent;
		public int id;
		public int tileType;
		public Array<Tile> tiles = new Array<>();
		public IntArray edgeIds = new IntArray();
		// used for debug rendering, kinda bad...
		public final Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), .75f);

		public SubRegion () {}

		public SubRegion init(int id, MapRegion parent) {
			this.id = id;
			this.parent = parent;
			return this;
		}

		public void rebuild(TileMap map) {
			map.clearEdges(this);
			edgeIds.clear();
			// tiles are sorted so it is easier to find next tile in order
			tiles.sort(tileXComp);
			findHorizontalEdges(map, -1, 0);
			findHorizontalEdges(map, 1, 1);
			tiles.sort(tileYComp);
			findVerticalEdges(map, -1, 0);
			findVerticalEdges(map, 1, 1);
		}

		private void findHorizontalEdges (TileMap map, int offsetY, int endOffsetY) {
			// we need to find 2 types of edges, edges in this sub region, and edges in regions to the side
			int id = 0;
			// go over all tiles in this region
			while (id < tiles.size) {
				// first tile of this edge
				Tile start = tiles.get(id);
				// check if tile below this one belongs to this sub, if so go to next one
				Tile next = map.getTileAt(start.x, start.y + offsetY);
				// we are at the edge
				if (next == null) {
					id++;
					continue;
				}
				// this should never be null, as we skip the bottom edge of the map
				if (next.subRegion == this) {
					id++;
					continue;
				}
				int otherType = next.type;
				Tile end = start;
				// go in a given direction until we encounter a tile that doesnt match
				for (int i = id + 1; i < tiles.size ; i++) {
					Tile tile = tiles.get(i);
					// next row, we are done
					if (end.y != tile.y) break;
					// next tile is not adjacent to last one
					if (end.x + 1 != tile.x) break;
					next = map.getTileAt(tile.x, tile.y + offsetY);
					// we dont want edges inside the regions
					if (next == null) break;
					if (next.subRegion == this) break;
					// if type of the other tile changes, we want an edge
					if (otherType != next.type) break;
					end = tile;
					id = i;
				}
				int edge = map.setHorizontalEdge(this, start.x, start.y + endOffsetY, end.x - start.x + 1);
				edgeIds.add(edge);
				id++;
			}
		}

		private void findVerticalEdges (TileMap map, int offsetX, int endOffsetX) {
			// we need to find 2 types of edges, edges in this sub region, and edges in regions to the side
			int id = 0;
			// go over all tiles in this region
			while (id < tiles.size) {
				// first tile of this edge
				Tile start = tiles.get(id);
				// check if tile below this one belongs to this sub, if so go to next one
				Tile next = map.getTileAt(start.x + offsetX, start.y);
				// we are at the edge
				if (next == null) {
					id++;
					continue;
				}
				// this should never be null, as we skip the bottom edge of the map
				if (next.subRegion == this) {
					id++;
					continue;
				}
				int otherType = next.type;
				Tile end = start;
				// go in a given direction until we encounter a tile that doesnt match
				for (int i = id + 1; i < tiles.size ; i++) {
					Tile tile = tiles.get(i);
					// next row, we are done
					if (end.x != tile.x) break;
					// next tile is not adjacent to last one
					if (end.y + 1 != tile.y) break;
					next = map.getTileAt(tile.x + offsetX, tile.y);
					// we dont want edges inside the regions
					if (next == null) break;
					if (next.subRegion == this) break;
					// if type of the other tile changes, we want an edge
					if (otherType != next.type) break;
					end = tile;
					id = i;
				}
				int edge = map.setVerticalEdge(this, start.x + endOffsetX, start.y, end.y - start.y + 1);
				edgeIds.add(edge);
				id++;
			}
		}

		public void add (Tile t) {
			tiles.add(t);
			t.subRegion = this;
			// we assume that all tiles in this sub region are of the same type
			tileType = t.type;
		}

		@Override public void reset () {
			id = -1;
			tileType = -1;
			tiles.clear();
			parent = null;
		}

		public boolean contains (int x, int y) {
			for (Tile tile : tiles) {
				if (tile.x == x && tile.y == y) return true;
			}
			return false;
		}
	}
}

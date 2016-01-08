package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;

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
		if (!isInBounds(tile.x, tile.y))
			throw new AssertionError("Tile outside of region!");
		// proper ids?
		tiles.add(tile.id);
		tile.region = this;
	}

	public boolean isInBounds (int x, int y) {
		return this.x <= x && this.x + size >= x && this.y <= y && this.y + size >= y;
	}

	public void clear (TileMap tileMap) {
		for (SubRegion sub : subs) {
			sub.clear(tileMap);
		}

		added.clear();
		subRegionPool.freeAll(subs);
		subs.clear();
	}

	private static ObjectSet<Tile> found = new ObjectSet<>();
	private static ObjectSet<Tile> added = new ObjectSet<>();
	public void rebuild (TileMap tileMap) {
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

	@Override public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MapRegion mapRegion = (MapRegion)o;

		return id == mapRegion.id;

	}

	@Override public int hashCode () {
		return id;
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
		/** contains localized ids for tiles in this sub region */
		private IntSet tilePositions = new IntSet();
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
			int currentId = 0;
			// go over all tiles in this region
			while (currentId < tiles.size) {
				// first tile of this edge
				Tile start = tiles.get(currentId);
				// check if tile below this one belongs to this sub, if so go to next one
				Tile next = map.getTileAt(start.x, start.y + offsetY);
				// we are at the edge
				if (next == null) {
					currentId++;
					continue;
				}
				// this should never be null, as we skip the bottom edge of the map
				if (next.subRegion == this) {
					currentId++;
					continue;
				}
				int otherType = next.type;
				Tile end = start;
				// go in a given direction until we encounter a tile that doesnt match
				for (int i = currentId + 1; i < tiles.size ; i++) {
					Tile tile = tiles.get(i);
					// next row, we are done
					if (end.y != tile.y) break;
					// next tile is not adjacent to last one
					if (end.x + 1 != tile.x) break;
					next = map.getTileAt(tile.x, tile.y + offsetY);
					// no tile, its at the map bounds
					if (next == null) break;
					// we dont want edges inside the regions
					if (next.subRegion == this) break;
					// if type of the other tile changes, we want an edge
					if (otherType != next.type) break;
					end = tile;
					currentId = i;
				}
				int edge = map.setHorizontalEdge(this, start.x, start.y + endOffsetY, end.x - start.x + 1);
				edgeIds.add(edge);
				currentId++;
			}
		}

		private void findVerticalEdges (TileMap map, int offsetX, int endOffsetX) {
			// we need to find 2 types of edges, edges in this sub region, and edges in regions to the side
			int currentId = 0;
			// go over all tiles in this region
			while (currentId < tiles.size) {
				// first tile of this edge
				Tile start = tiles.get(currentId);
				// check if tile below this one belongs to this sub, if so go to next one
				Tile next = map.getTileAt(start.x + offsetX, start.y);
				// we are at the edge
				if (next == null) {
					currentId++;
					continue;
				}
				// this should never be null, as we skip the bottom edge of the map
				if (next.subRegion == this) {
					currentId++;
					continue;
				}
				int otherType = next.type;
				Tile end = start;
				// go in a given direction until we encounter a tile that doesnt match
				for (int i = currentId + 1; i < tiles.size ; i++) {
					Tile tile = tiles.get(i);
					// next row, we are done
					if (end.x != tile.x) break;
					// next tile is not adjacent to last one
					if (end.y + 1 != tile.y) break;
					next = map.getTileAt(tile.x + offsetX, tile.y);
					// no tile, its at the map bounds
					if (next == null) break;
					// we dont want edges inside the regions
					if (next.subRegion == this) break;
					// if type of the other tile changes, we want an edge
					if (otherType != next.type) break;
					end = tile;
					currentId = i;
				}
				int edge = map.setVerticalEdge(this, start.x + endOffsetX, start.y, end.y - start.y + 1);
				edgeIds.add(edge);
				currentId++;
			}


		}

		public void add (Tile tile) {
			if (tiles.contains(tile, true)) throw new AssertionError("Tile already added!");
			tiles.add(tile);
			tile.subRegion = this;
			// we assume that all tiles in this sub region are of the same type
			tileType = tile.type;
			tilePositions.add(toLocalId(tile.x, tile.y));
		}

		@Override public void reset () {
			id = -1;
			tileType = -1;
			tiles.clear();
			tilePositions.clear();
			parent = null;
		}

		/**
		 * Translate global coordinates to local id
		 */
		private int toLocalId(int x, int y) {
			return (x - parent.x) + (y - parent.y) * parent.size;
		}

		public boolean contains (int x, int y) {
			return parent.isInBounds(x, y) && tilePositions.contains(toLocalId(x, y));
		}

		public void clear (TileMap tileMap) {
			tileMap.clearEdges(this);
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			SubRegion subRegion = (SubRegion)o;

			if (id != subRegion.id)
				return false;
			if (tileType != subRegion.tileType)
				return false;
			return parent != null ? parent.equals(subRegion.parent) : subRegion.parent == null;

		}

		@Override public int hashCode () {
			int result = parent != null ? parent.hashCode() : 0;
			result = 31 * result + id;
			result = 31 * result + tileType;
			return result;
		}
	}
}

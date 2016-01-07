package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;

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

	public static class SubRegion implements Pool.Poolable{
		public MapRegion parent;
		public int id;
		public IntArray tiles = new IntArray();
		// used for debug rendering, kinda bad...
		public final Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), .75f);

		public SubRegion () {}

		public SubRegion init(int id, MapRegion parent) {
			this.id = id;
			this.parent = parent;
			return this;
		}

		public void rebuild(TileMap map) {

		}

		public void add (Tile t) {
			tiles.add(t.id);
		}

		@Override public void reset () {
			id = -1;
			tiles.clear();
			parent = null;
		}
	}
}

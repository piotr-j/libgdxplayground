package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

/**
 * Created by EvilEntity on 07/01/2016.
 */
class MapRegion {
	public int id;
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public IntArray tiles = new IntArray();
	public Array<SubRegion> subRegions = new Array<>();

	public MapRegion (int id, int x, int y, int width, int height) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void addTile (Tile tile) {
		if (!contains(tile.x, tile.y))
			throw new AssertionError("Tile outside of region!");
		// proper ids?
		tiles.add(tile.id);
	}

	public boolean contains (float x, float y) {
		return this.x <= x && this.x + width >= x && this.y <= y && this.y + height >= y;
	}

	public class SubRegion {
		public MapRegion parent;
	}
}

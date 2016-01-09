package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;

/**
 * Created by EvilEntity on 07/01/2016.
 */
class TileMap {
	public final int mapWidth;
	public final int mapHeight;
	public final int regionSize;
	public final int regionsX;
	public final int regionsY;
	public final MapRegion[] regions;
	public final Tile[] tiles;

	public TileMap (int[] map, int mapWidth, int mapHeight, int regionSize) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.regionSize = regionSize;

		regionsX = mapWidth / regionSize;
		regionsY = mapHeight / regionSize;

		regions = new MapRegion[regionsX * regionsY];
		for (int x = 0; x < regionsX; x++) {
			for (int y = 0; y < regionsY; y++) {
				MapRegion region = new MapRegion(x + y * regionsX, x * regionSize, y * regionSize, regionSize);
				regions[region.id] = region;
			}
		}

		tiles = new Tile[mapWidth * mapHeight];
		for (int x = 0; x < TiledPartitionV2Test.MAP_WIDTH; x++) {
			for (int y = 0; y < TiledPartitionV2Test.MAP_HEIGHT; y++) {
				Tile tile = new Tile(x + y * TiledPartitionV2Test.MAP_WIDTH, x, y);
				// magic incantation to get correct id from the map above
				tile.setType(map[x + (TiledPartitionV2Test.MAP_HEIGHT - 1 - y) * TiledPartitionV2Test.MAP_WIDTH]);
				tiles[tile.id] = tile;
				addTileToRegion(tile);
			}
		}
	}

	public Tile getTileAt (int x, int y) {
		if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight)
			return null;
		return tiles[x + y * mapWidth];
	}

	public MapRegion getRegionAt (int x, int y) {
		int rx = x / regionSize;
		int ry = y / regionSize;
		if (rx < 0 || ry < 0 || rx >= regionsX || ry >= regionsY)
			return null;
		return regions[rx + ry * (TiledPartitionV2Test.MAP_WIDTH / TiledPartitionV2Test.REGION_SIZE)];
	}

	private void addTileToRegion (Tile tile) {
		MapRegion region = getRegionAt(tile.x, tile.y);
		if (region == null)
			throw new AssertionError("Region cant be null here!");
		region.addTile(tile);
	}

	public Tile getTile (int id) {
		return tiles[id];
	}

	private Array<MapRegion> rebuildQueue = new Array<>();
	/**
	 * Rebuild all regions
	 */
	public void rebuild () {
		rebuildQueue.clear();
		for (MapRegion region : regions) {
			rebuildQueue.add(region);
		}
		execRebuild();
	}

	private void validateHashes () {
		IntIntMap edgeHashes = new IntIntMap();
		for (Edge edge : edges) {
			int hc = edge.hashCode();
			edgeHashes.put(hc, edgeHashes.get(hc, 0) + 1);
		}
		Gdx.app.log("", "Duped edge hashes: ");
		for (IntIntMap.Entry entry : edgeHashes) {
			if (entry.value > 1) {
				Gdx.app.log("", "" + entry.key);
				throw new AssertionError("Duped edge hash!");
			}
		}

		IntIntMap regionHashes = new IntIntMap();
		IntIntMap subRegionHashes = new IntIntMap();
		for (MapRegion region : regions) {
			int hc = region.hashCode();
			regionHashes.put(hc, regionHashes.get(hc, 0) + 1);
			for (MapRegion.SubRegion sub : region.subs) {
				hc = sub.hashCode();
				subRegionHashes.put(hc, subRegionHashes.get(hc, 0) + 1);
			}
		}

		Gdx.app.log("", "Duped region hashes:");
		for (IntIntMap.Entry entry : regionHashes) {
			if (entry.value > 1) {
				Gdx.app.log("", ""+ entry.key);
				throw new AssertionError("Duped region hash!");
			}
		}
		Gdx.app.log("", "Duped sub region hashes:");
		for (IntIntMap.Entry entry : subRegionHashes) {
			if (entry.value > 1) {
				Gdx.app.log("", "" + entry.key);
				throw new AssertionError("Duped sub region hash!");
			}
		}
	}

	private void execRebuild() {
		for (MapRegion region : rebuildQueue) {
			region.clear(this);
		}
		for (MapRegion region : rebuildQueue) {
			region.rebuild(this);
		}
		// note this is separate as it depends on surrounding regions being updated
		for (MapRegion region : rebuildQueue) {
			region.rebuildSubRegions(this);
		}
		// FIXME fails when tile is changed!!!
		validateHashes();
	}

	/**
	 * Rebuild region at x, y and surrounding
	 */
	public void rebuild (int x, int y) {
		rebuildQueue.clear();
		rebuildRegionAt(x, y);
		rebuildRegionAt(x -regionSize, y);
		rebuildRegionAt(x +regionSize, y);
		rebuildRegionAt(x, y -regionSize);
		rebuildRegionAt(x, y +regionSize);
		execRebuild();
	}

	private void rebuildRegionAt (int x, int y) {
		MapRegion region = getRegionAt(x, y);
		if (region != null) rebuildQueue.add(region);
	}

	public void clearEdges (MapRegion.SubRegion region) {
		// TODO need to make sure that his crap works
		// find all edges with this region and clear them
		for (int i = 0; i < region.edgeIds.size; i++) {
			int id = region.edgeIds.get(i);
			Edge edge = idToEdge.get(id, null);
			if (edge == null) continue;
			if (edge.subA == region) edge.subA = null;
			if (edge.subB == region) edge.subB = null;
			if (edge.subA == null && edge.subB == null) {
				idToEdge.remove(id);
				Edge.free(edge);
			}
		}
		region.edgeIds.clear();
	}

	private Edge getEdge(int x, int y, int length, boolean horizontal) {
		int id = packEdgeId(x, y, length, horizontal);
		if (idToEdge.containsKey(id)) {
			return idToEdge.get(id);
		}
		// need proper way of getting those
		Edge edge = Edge.obtain().init(id, x, y, length, horizontal);
		edges.add(edge);
		idToEdge.put(id, edge);
		return edge;
	}

	public int setHorizontalEdge (MapRegion.SubRegion region, int x, int y, int length) {
		Edge edge = getEdge(x, y, length, true);
		edge.add(region);
		return edge.id;
	}

	public Array<Edge> edges = new Array<>();
	public IntMap<Edge> idToEdge = new IntMap<>();
	public int setVerticalEdge (MapRegion.SubRegion region, int x, int y, int length) {
		Edge edge = getEdge(x, y, length, false);
		edge.add(region);
		return edge.id;
	}

	public MapRegion.SubRegion getSubRegionAt (int x, int y) {
		MapRegion region = getRegionAt(x, y);
		if (region == null) return null;
		return region.getSubRegionAt(x, y);
	}

	private final static int MAX_LENGTH = (int)Math.pow(2, 4);
	private final static int MAX_X = (int)Math.pow(2, 13);
	private final static int MAX_Y = (int)Math.pow(2, 13);
	private int packEdgeId(int x, int y, int length, boolean horizontal) {
		if (length >= MAX_LENGTH) throw new AssertionError("Length >= " + MAX_LENGTH);
		if (x >= MAX_X) throw new AssertionError("x >= " + MAX_X);
		if (y >= MAX_Y) throw new AssertionError("y >= " + MAX_Y);
		// pack data into unique id
		// 1 bit - hor/vert, << 30
		// 4 bits - len, max 16 lets say << 26
		// 13 bits - x << 13
		// 13 bits - y
		int dir = horizontal?1:0;
		return dir << 30 | length << 26 | x << 13 | y;
	}

	public Edge getEdge (int id) {
		return idToEdge.get(id, null);
	}

	/**
	 * find all regions connected to the one at x, y, with specified degree of separation
	 */
	public ObjectSet<MapRegion.SubRegion> getConnectedSubsAt (int x, int y, int dos, ObjectSet<MapRegion.SubRegion> out) {
		if (dos < 0) return out;
		MapRegion.SubRegion region = getSubRegionAt(x, y);
		if (region == null) return out;
		tmpInts.clear();
		getConnectedSubsTo(region, dos, out);

		int dupes = 0;
		int max = 0;
		for (IntIntMap.Entry entry : tmpInts.entries()) {
			if (entry.value > 1) {
				if (entry.value > max) max = entry.value;
				dupes++;
			}
		}
		Gdx.app.log("", "Total regions " + tmpInts.size);
		Gdx.app.log("", "Duped regions " + dupes);
		Gdx.app.log("", "Max dupes " + max);
		return out;
	}

	public ObjectSet<MapRegion.SubRegion> getConnectedSubsTo (MapRegion.SubRegion region, int dos, ObjectSet<MapRegion.SubRegion> out) {
		getConnectedSubs(region, dos, filterSimilar, out);
		return out;
	}

	public Array<Edge> touched = new Array<>();
	public Array<MapRegion.SubRegion> touchedRegions = new Array<>();
	public Array<MapRegion.SubRegion> regionsEdges = new Array<>();
	private ObjectSet<MapRegion.SubRegion> getConnectedSubs (final MapRegion.SubRegion region, final int dos, SubRegionFilter filter,
		final ObjectSet<MapRegion.SubRegion> out) {

		// sub id max is 63, so << 7 should be fine
		int id = region.parent.id << 7 + region.id;
		tmpInts.put(id, tmpInts.get(id, 0) + 1);
		touchedRegions.add(region);
		out.add(region);

		Array<MapRegion.SubRegion> tmp = new Array<>();
		tmp.add(region);

		int offset = 0;
		for (int i = 0; i < dos; i++) {
			// cache as it will grow
			int length = tmp.size;
			for (int j = offset; j < length; j++) {
				MapRegion.SubRegion sub = tmp.get(j);
				touchedRegions.add(sub);
				final IntArray ids = sub.edgeIds;
				for (int k = 0; k < ids.size; k++) {
					final Edge edge = getEdge(ids.get(k));
					touched.add(edge);
					if (edge.subA != sub
						&& filter.accept(sub, edge.subA)
						&& !tmp.contains(edge.subA, true)) {
						tmp.add(edge.subA);
					}
					if (edge.subB != sub
						&& filter.accept(sub, edge.subB)
						&& !tmp.contains(edge.subB, true)) {
						tmp.add(edge.subB);
					}
				}
			}
			offset = length;
			Gdx.app.log("", "step "+ i + " Added " + (tmp.size - length));
		}
		out.addAll(tmp);
		return out;
	}

	private SubRegionFilter filterSimilar = new SubRegionFilter() {
		@Override public boolean accept (MapRegion.SubRegion that, MapRegion.SubRegion other) {
			if (that.tileType == 0) {
				return that.tileType == other.tileType;
			} else {
				return other.tileType == 1 || other.tileType == 2;
			}
		}
	};
	private SubRegionFilter filterExact = new SubRegionFilter() {
		@Override public boolean accept (MapRegion.SubRegion that, MapRegion.SubRegion other) {
			return that.tileType == other.tileType;
		}
	};
	private SubRegionFilter filterAll = new SubRegionFilter() {
		@Override public boolean accept (MapRegion.SubRegion that, MapRegion.SubRegion other) {
			return true;
		}
	};

	private Array<MapRegion.SubRegion> tmpRegions = new Array<>();
	private ObjectSet<MapRegion.SubRegion> tmpSet = new ObjectSet<>();
	private IntIntMap tmpInts = new IntIntMap();
	public ObjectSet<MapRegion.SubRegion> expandSubRegions (ObjectSet<MapRegion.SubRegion> subRegions, int times) {
		tmpInts.clear();
		int totalFound = 0;
		for (int i = 0; i < times; i++) {
			tmpRegions.clear();
			// no add all, buu
			for (MapRegion.SubRegion subRegion : subRegions) {
				tmpRegions.add(subRegion);
			}
			for (MapRegion.SubRegion subRegion : tmpRegions) {
				tmpSet.clear();
				// since we go over same subs many times, we waste a ton of work, gotta fix that....
				getConnectedSubs(subRegion, 1, filterAll, tmpSet);
				int found = tmpSet.size;
				totalFound += found;
				int current = subRegions.size;
				subRegions.addAll(tmpSet);
				Gdx.app.log("", "found " + found +", added " + (subRegions.size - current));
			}
		}
		// with large times, we find orders of magnitude more then we add
		Gdx.app.log("", "found total " + totalFound +", added " + subRegions.size);

//		subRegions.addAll(tmpSet);
		int dupes = 0;
		int max = 0;
		for (IntIntMap.Entry entry : tmpInts.entries()) {
			if (entry.value > 1) {
				if (entry.value > max) max = entry.value;
//				Gdx.app.log("", "Dupe : " + entry.key);
				dupes++;
			}
		}
		Gdx.app.log("", "Total regions " + tmpInts.size);
		Gdx.app.log("", "Duped regions " + dupes);
		Gdx.app.log("", "Max dupes " + max);

		return subRegions;
	}

	public interface SubRegionFilter {
		boolean accept(MapRegion.SubRegion that, MapRegion.SubRegion other);
	}

	public static class Edge implements Pool.Poolable{
		private static Pool<Edge> pool = new Pool<Edge>() {
			@Override protected Edge newObject () {
				return new Edge();
			}
		};

		public static Edge obtain () {
			return pool.obtain();
		}

		public static void free (Edge edge) {
			pool.free(edge);
		}

		public int id;
		public int x;
		public int y;
		public int length;
		public boolean horizontal;
		public MapRegion.SubRegion subA;
		public MapRegion.SubRegion subB;
		// color for debug, buuuu
		public final Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), .5f);
		protected Edge() {}

		public Edge init (int id, int x, int y, int length, boolean horizontal) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.length = length;
			this.horizontal = horizontal;
			return this;
		}

		public void add (MapRegion.SubRegion region) {
			if (subA == null) {
				subA = region;
			} else if (subB == null) {
				subB = region;
			} else {
				throw new AssertionError("There can only be 2 sub regions per edge!");
			}
		}

		@Override public void reset () {
			subA = null;
			subB = null;
			id = -1;
			x = -1;
			y = -1;
			length = -1;
			horizontal = false;
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Edge edge = (Edge)o;
			return id == edge.id;

		}

		@Override public int hashCode () {
			// if must be unique
			return id;
		}

		@Override public String toString () {
			return "Edge{" +
				"id=" + id +
				", x=" + x +
				", y=" + y +
				", length=" + length +
				 (horizontal?", horizontal":", vertical") +
				'}';
		}
	}
}

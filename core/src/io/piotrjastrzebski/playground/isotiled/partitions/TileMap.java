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
		return getConnectedSubsTo(region, dos, out);
	}

	public ObjectSet<MapRegion.SubRegion> getConnectedSubsTo (MapRegion.SubRegion region, int dos, ObjectSet<MapRegion.SubRegion> out) {
		test.clear();
		getConnectedSubs(region, dos, out);
//		Gdx.app.log("Tested: ", String.valueOf(test.size));
//		Gdx.app.log("Edges: ", test.toString());
//		for (IntIntMap.Entry entry : test.entries()) {
//			if (entry.value > 1) {
//				Gdx.app.log("Dupe edge : ", entry.value + "x " +getEdge(entry.key).toString());
//			}
//		}

		return out;
	}

	IntIntMap test = new IntIntMap();
	private ObjectSet<MapRegion.SubRegion> getConnectedSubs (final MapRegion.SubRegion region, final int dos, final ObjectSet<MapRegion.SubRegion> out) {

		// region was already processed
		if (out.contains(region)) return out;
		out.add(region);
		if (dos == 0) return out;

		final IntArray ids = region.edgeIds;
		for (int i = 0; i < ids.size; i++) {
			final Edge edge = getEdge(ids.get(i));
//			test.put(edge.id, test.get(edge.id, 0) + 1);
//			if (test.get(edge.id, 0) > 1) continue;
			// todo pass in the action
			if (edge.subA != region
				&& defaultFilter.accept(region, edge.subA)
				&& !out.contains(edge.subA)) {
				getConnectedSubs(edge.subA, dos -1, out);
			}
			if (edge.subB != region
				&& defaultFilter.accept(region, edge.subB)
				&& !out.contains(edge.subB)) {
				getConnectedSubs(edge.subB, dos -1, out);
			}
		}
		return out;
	}

	private SubRegionFilter defaultFilter = new SubRegionFilter() {
		@Override public boolean accept (MapRegion.SubRegion that, MapRegion.SubRegion other) {
//			if (that.tileType == 0) {
//				return that.tileType == other.tileType;
//			} else {
//				return other.tileType == 1 || other.tileType == 2;
//			}
//			return that.tileType == other.tileType;
			return true;
		}
	};

	public void expandSubRegions (ObjectSet<MapRegion.SubRegion> subRegions) {
		// works, but super inefficient...
		Array<MapRegion.SubRegion> tmpRegions = new Array<>();
		for (MapRegion.SubRegion subRegion : subRegions) {
			tmpRegions.add(subRegion);
		}
		ObjectSet<MapRegion.SubRegion> tmpSet = new ObjectSet<>();
		for (MapRegion.SubRegion welp : tmpRegions) {
			tmpSet.clear();
			getConnectedSubs(welp, 1, tmpSet);
			subRegions.addAll(tmpSet);
		}
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

		@Override public String toString () {
			return "Edge{" +
				"id=" + id +
				", x=" + x +
				", y=" + y +
				", length=" + length +
				", horizontal=" + horizontal +
				'}';
		}
	}
}

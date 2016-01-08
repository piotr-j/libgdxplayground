package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;

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
		// find all edges with this region and clear them
		for (int i = 0; i < region.edgeIds.size; i++) {
			int id = region.edgeIds.get(i);
			Edge edge = idToEdge.get(id, null);
			if (edge == null) continue;
			edge.subRegions.removeValue(region, true);
			if (edge.subRegions.size == 0) {
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

	public static class Edge {
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
		public Array<MapRegion.SubRegion> subRegions = new Array<>();
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
			if (!subRegions.contains(region, true))
				subRegions.add(region);
		}
	}
}

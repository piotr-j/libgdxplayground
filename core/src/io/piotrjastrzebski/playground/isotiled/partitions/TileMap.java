package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import io.piotrjastrzebski.playground.isotiled.partitions.MapRegion.SubRegion;

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
	public final Array<Room> rooms;
	private final ObjectSet<Room> roomsTouched;

	public TileMap (int[] map, int mapWidth, int mapHeight, int regionSize) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.regionSize = regionSize;

		rooms = new Array<>();
		roomsTouched = new ObjectSet<>();

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
			scheduleRegionRebuild(region);
		}
		execRebuild();
	}

	private void validateHashes () {
		IntIntMap edgeHashes = new IntIntMap();
		for (Edge edge : idToEdge.values()) {
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
			for (SubRegion sub : region.subs) {
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
		rebuildRooms(rebuildQueue);
		validateHashes();
	}

	private NeighbourData tmpData = new NeighbourData();
	private ObjectSet<SubRegion> tmpSubs = new ObjectSet<>();
	public ObjectMap<SubRegion, Room> subToRoom = new ObjectMap<>();
	private void rebuildRooms (Array<MapRegion> rebuildQueue) {
		tmpSubs.clear();
		for (Room room : roomsTouched) {
			for (SubRegion subRegion : room.subRegions) {
				subToRoom.remove(subRegion);
			}
			rooms.removeValue(room, true);
			Room.free(room);
		}
		roomsTouched.clear();

		for (MapRegion region : rebuildQueue) {
			Array<SubRegion> subs = region.subs;
			// in our queue all subs should be out of rooms
			for (SubRegion startSub : subs) {
				if (tmpSubs.contains(startSub))
					continue;
				tmpData.reset();
				// FIXME this doesnt handle room splitting
				// we probably wont have more then 999...
				// need to do something with the rooms
				getConnectedSubs(startSub, 999, filterSimilar, tmpData);
				// find room to add to
				Room room = null;
				// since all subs are connected, they belong to same room
				for (SubRegion subRegion : tmpData.subRegions) {
					if (subToRoom.containsKey(subRegion)) {
						room = subToRoom.get(subRegion);
						break;
					}
				}
				if (room == null) {
					room = Room.obtain();
					rooms.add(room);
				}
				// startSub should be in the data as well
				for (SubRegion subRegion : tmpData.subRegions) {
					room.add(subRegion);
					subToRoom.put(subRegion, room);
					tmpSubs.add(subRegion);
				}
			}
		}
	}
	/**
	 * Rebuild region at x, y and surrounding
	 */
	public void rebuild (int x, int y) {
		rebuildQueue.clear();
		scheduleRegionRebuildAt(x, y);
		scheduleRegionRebuildAt(x -regionSize, y);
		scheduleRegionRebuildAt(x +regionSize, y);
		scheduleRegionRebuildAt(x, y -regionSize);
		scheduleRegionRebuildAt(x, y +regionSize);
		execRebuild();
	}

	private void scheduleRegionRebuildAt (int x, int y) {
		scheduleRegionRebuild(getRegionAt(x, y));
	}

	private void scheduleRegionRebuild (MapRegion region) {
		if (region != null) rebuildQueue.add(region);
	}

	public void clear (SubRegion region) {
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
			Room room = subToRoom.remove(region);
			if (room != null) {
				room.remove(region);
				if (room.subRegions.size == 0) {
					rooms.removeValue(room, true);
					Room.free(room);
					roomsTouched.remove(room);
				} else {
					roomsTouched.add(room);
				}
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
		idToEdge.put(id, edge);
		return edge;
	}

	public int setHorizontalEdge (SubRegion region, int x, int y, int length) {
		Edge edge = getEdge(x, y, length, true);
		edge.add(region);
		return edge.id;
	}

	public IntMap<Edge> idToEdge = new IntMap<>();
	public int setVerticalEdge (SubRegion region, int x, int y, int length) {
		Edge edge = getEdge(x, y, length, false);
		edge.add(region);
		return edge.id;
	}

	public SubRegion getSubRegionAt (int x, int y) {
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
	public NeighbourData getConnectedSubsAt (int x, int y, int dos, NeighbourData out) {
		if (dos < 0) return out;
		SubRegion region = getSubRegionAt(x, y);
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
//		Gdx.app.log("", "Total regions " + tmpInts.size);
//		Gdx.app.log("", "Duped regions " + dupes);
//		Gdx.app.log("", "Max dupes " + max);
		return out;
	}

	public NeighbourData getConnectedSubsTo (SubRegion region, int dos, NeighbourData out) {
		getConnectedSubs(region, dos, filterSimilar, out);
		return out;
	}

	private IntIntMap tmpInts = new IntIntMap();
	public Array<Edge> touched = new Array<>();
	public Array<SubRegion> touchedRegions = new Array<>();
	public Array<SubRegion> regionsEdges = new Array<>();
	private NeighbourData getConnectedSubs (final SubRegion region, final int dos, SubRegionFilter filter,
		final NeighbourData out) {
		out.reset();
		// add first region
		out.subRegions.add(region);
		out.offsets.add(0);
		out.offsets.add(1);
		// expand to desired degree
		expandSubRegions(out, dos, filter);
		return out;
	}

	public NeighbourData expandSubRegions (NeighbourData out, int times, SubRegionFilter filter) {
		if (out.subRegions.size == 0) return out;
		Array<SubRegion> tmpRegions = out.subRegions;
		int size = out.subRegions.size;
		int offset = tmpRegions.size - out.offsets.get(out.offsets.size -1);
		int i = 0;
		for (; i < times; i++) {
			// cache as it will grow
			int length = tmpRegions.size;
			for (int j = offset; j < length; j++) {
				SubRegion sub = tmpRegions.get(j);
				touchedRegions.add(sub);
				final IntArray ids = sub.edgeIds;
				for (int k = 0; k < ids.size; k++) {
					final Edge edge = getEdge(ids.get(k));
					touched.add(edge);
					if (edge.subA != sub
						&& filter.accept(sub, edge.subA)
						&& !tmpRegions.contains(edge.subA, true)) {
						tmpRegions.add(edge.subA);
					}
					if (edge.subB != sub
						&& filter.accept(sub, edge.subB)
						&& !tmpRegions.contains(edge.subB, true)) {
						tmpRegions.add(edge.subB);
					}
				}
			}
			offset = length;
			out.offsets.add(tmpRegions.size);
			Gdx.app.log("", "step "+ i + " Added " + (tmpRegions.size - length));
			if (tmpRegions.size - length == 0) {
				Gdx.app.log("", "Reached max added");
				break;
			}
		}
		out.degreeOfSeparation += i;
		Gdx.app.log("", "Expanded from " + size+ " to " + tmpRegions.size);
		return out;
	}

	public void expandSubRegions (NeighbourData out, int times) {
		expandSubRegions(out, times, filterSimilar);
	}

	/**
	 * Result of neighbour search
	 */
	public static class NeighbourData implements Pool.Poolable {
		// we could try ObjectSet for faster(?) contains()
		public Array<SubRegion> subRegions = new Array<>();
		public IntArray offsets = new IntArray();
		public int degreeOfSeparation;

		@Override public void reset () {
			subRegions.clear();
			offsets.clear();
			degreeOfSeparation = 0;
		}

		/**
		 * get view for given degreeOfSeparation
		 */
		public Array<SubRegion> get(int degreeOfSeparation, Array<SubRegion> out) {
			if (subRegions.size == 0) return out;
			if (degreeOfSeparation > this.degreeOfSeparation) return out;
			int offset = offsets.items[degreeOfSeparation];
			int size = offsets.items[degreeOfSeparation + 1];
			if (size > subRegions.size) {
				Gdx.app.log("", "welp!");
			}
			for (int ii = offset; ii < size; ii++) {
				out.add(subRegions.get(ii));
			}
			return out;
		}

		protected void add (Array<SubRegion> toAdd) {
			for (SubRegion subRegion : toAdd) {
				if (!subRegions.contains(subRegion, true))
					subRegions.add(subRegion);
			}
		}
	}

	public interface SubRegionFilter {
		boolean accept(SubRegion that, SubRegion other);
	}

	private SubRegionFilter filterSimilar = new SubRegionFilter() {
		@Override public boolean accept (SubRegion that, SubRegion other) {
			if (that.tileType == 0) {
				return that.tileType == other.tileType;
			} else {
				return other.tileType == 1 || other.tileType == 2;
			}
		}
	};
	private SubRegionFilter filterExact = new SubRegionFilter() {
		@Override public boolean accept (SubRegion that, SubRegion other) {
			return that.tileType == other.tileType;
		}
	};
	private SubRegionFilter filterAll = new SubRegionFilter() {
		@Override public boolean accept (SubRegion that, SubRegion other) {
			return true;
		}
	};

}

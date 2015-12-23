package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Comparator;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledPartitionTest extends BaseScreen {
	public final static float SCALE = 32f; // size of single tile in pixels
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280/SCALE;
	public final static float VP_HEIGHT = 720/SCALE;

	public final static int REGION_SIZE = 8;
	public final static int MAP_WIDTH = REGION_SIZE * 5;
	public final static int MAP_HEIGHT = REGION_SIZE * 3;
	public final static int[] map = new int[] {
		0, 0, 0, 0, 0, 0, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 1, 1, 1, 1, 1, 0,  0, 1, 1, 2, 1, 1, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 1, 0, 0, 0, 1, 0,  0, 1, 0, 0, 0, 1, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 1, 1, 0, 0, 0, 1, 0,  1, 1, 0, 0, 0, 1, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 1, 0, 0, 1, 1, 1, 0,  2, 0, 0, 1, 1, 1, 0, 0,  1, 0, 1, 1, 1, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 1, 1, 1, 1, 0, 0, 0,  1, 1, 2, 1, 0, 0, 0, 0,  1, 1, 1, 0, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,

		1, 0, 0, 0, 0, 0, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 2, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 2,  2, 0, 1, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  1, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 1, 1, 1, 1,  1, 1, 1, 1, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 2, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 2, 0, 0, 0,  0, 0, 0, 1, 1, 1, 1, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,

		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 2, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,
	};

	Array<Tile> tiles = new Array<>();
	Array<Region> regions = new Array<>();

	public TiledPartitionTest (GameReset game) {
		super(game);
		for (int y = 0; y < MAP_HEIGHT; y++) {
			for (int x = 0; x < MAP_WIDTH; x++) {
				Tile tile = new Tile(x + y * MAP_WIDTH, x, y);
				tile.setType(map[x + (MAP_HEIGHT - 1 - y) * MAP_WIDTH]);
				tiles.add(tile);
			}
		}
		for (int x = 0; x < MAP_WIDTH; x += REGION_SIZE) {
			for (int y = 0; y < MAP_HEIGHT; y += REGION_SIZE) {
				regions.add(new Region(x, y));
			}
		}
		gameCamera.position.set(VP_WIDTH / 2, VP_HEIGHT / 2, 0);

		rebuildRegions();
	}

	private void rebuildRegions () {
		for (int i = 0; i < regions.size; i++) {
			regions.get(i).rebuild();
		}
		for (int i = 0; i < regions.size; i++) {
			regions.get(i).rebuildSubs();
		}
	}

	private void rebuildRegions (Vector2 cs) {
		Array<Region> toRebuild = new Array<>();
		for (int i = 0; i < regions.size; i++) {
			Region region = regions.get(i);
			if (region.bounds.contains(cs)){
				region.rebuild();
				toRebuild.add(region);
				int x = (int)region.bounds.x/REGION_SIZE;
				int y = (int)region.bounds.y/REGION_SIZE;
				rebuildRegion(x -1, y, toRebuild);
				rebuildRegion(x +1, y, toRebuild);
				rebuildRegion(x, y +1, toRebuild);
				rebuildRegion(x, y -1, toRebuild);
				break;
			}
		}

		for (Region region : toRebuild) {
			region.rebuildSubs();
		}
	}

	private void rebuildRegion (int x, int y, Array<Region> toRebuild) {
		if (x < 0) return;
		if (x >= 5) return;
		if (y < 0) return;
		if (y >= 3) return;
		regions.get(x * 3 + y).rebuild();
		toRebuild.add(regions.get(x * 3 + y));
	}

	private Vector2 cs = new Vector2();
	@Override public void render (float delta) {
		super.render(delta);

		if (filling) {
			fillTimer += delta;
			if (fillTimer > .25f) {
				fillTimer-=.25f;
				resumeFloodFill(ffRegion, found);
			}
		}
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Tile tile : tiles) {
			tile.render(renderer, delta);
		}

		for (Region region : regions) {
			region.update(cs);
			region.renderSubs(renderer);
			region.renderEdges(renderer);
		}

		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		for (Region region : regions) {
			region.render(renderer);
		}
		renderer.end();
	}
	Region ffRegion;
	ObjectSet<Tile> found = new ObjectSet<>();
	protected void floodFill (int x, int y, Region region, ObjectSet<Tile> found) {
		Tile start = getTile(x, y, region);
		if (start == null) return;
		ffRegion = region;
		targetType = start.type;
		processed.clear();
		queue.clear();
		queue.add(start);
		resumeFloodFill(region, found);
	}

	protected void resumeFloodFill (Region region, ObjectSet<Tile> found) {
		int iters = 0;
		while (queue.size > 0) {
			ff(region, found);
			iters++;
			if (iters >= maxIters) {
				filling = true;
				break;
			}
		}
	}

	protected void ff(Region region, ObjectSet<Tile> found) {
		Tile tile = queue.removeIndex(0);
		if (tile.type == targetType) {
			if (processed.containsKey(tile.id)) return;
			visitTile(tile, found);
			Tile west = getEdge(tile, -1, region);
			Tile east = getEdge(tile, 1, region);

			for (int i = west.x; i <= east.x; i++) {
				Tile n = getTile(i, west.y, region);
				visitTile(n, found);
				Tile north = getTile(i, west.y + 1, region);
				if (north != null && north.type == targetType) {
					addToQueue(north);
				}
				Tile south = getTile(i, west.y - 1, region);
				if (south != null && south.type == targetType) {
					addToQueue(south);
				}
			}
		}
	}

	protected void floodFillFull (int x, int y, Region region, ObjectSet<Tile> found) {
		Tile start = getTile(x, y, region);
		if (start == null) return;
		ffRegion = region;
		targetType = start.type;
		processed.clear();
		queue.clear();
		queue.add(start);
		while (queue.size > 0) {
			ff(region, found);
		}
	}

	protected Tile getEdge (Tile tile, int offset, Region region) {
		while (true) {
			Tile next = getTile(tile.x + offset, tile.y, region);
			if (next != null && next.type == targetType) {
				tile = next;
			} else {
				break;
			}
		}
		return tile;
	}

	boolean filling;
	float fillTimer;
	int maxIters = 4;

	int targetType;
	Array<Tile> queue = new Array<>();
	IntMap<Tile> processed = new IntMap<>();

	private void addToQueue (Tile tile) {
		if (tile == null || processed.containsKey(tile.id)) return;
		queue.add(tile);
	}

	private void visitTile (Tile tile, ObjectSet<Tile> found) {
		processed.put(tile.id, tile);
		tile.tint.set(1, .5f, .5f, 1);
		tile.a = 1;
		found.add(tile);
	}

	private void addToQueue(int x, int y, Region region) {
		addToQueue(getTile(x, y, region));
	}

	protected Tile getTile (int x, int y, Region region) {
		if (region == null) {
			if (x < 0 || x >= MAP_WIDTH) return null;
			if (y < 0 || y >= MAP_HEIGHT) return null;
		} else {
			if (x < region.x || x >= region.x + REGION_SIZE) return null;
			if (y < region.y || y >= region.y + REGION_SIZE) return null;
		}
		int index = x + y * MAP_WIDTH;
		if (index < 0 || index >= tiles.size) return null;
		return tiles.get(index);
	}

	private class Tile {
		public int id;
		public int x;
		public int y;
		public Rectangle bounds = new Rectangle();
		public Color color = new Color();
		public Color tint = new Color();
		public int type;

		public Tile (int id, int x, int y) {
			this.id = id;
			this.x = x;
			this.y = y;
			bounds.set(x, y, 1, 1);
			setType(0);
		}

		public void setType (int type) {
			this.type = type;
			switch (type) {
			case 0: // grass
				// some variation so we know wtf is going on
//				color.set(.1f, MathUtils.random(0.7f, .9f), MathUtils.random(.1f, .2f), 1);
				color.set(0, 1, 0, 1);
				break;
			case 1: // wall
				color.set(Color.FIREBRICK);
				break;
			case 2: // door
				color.set(1f, .8f, .6f, 1);
				break;
			}
		}

		Color tmp = new Color();
		float a;
		public void render(ShapeRenderer renderer, float delta) {
			a = MathUtils.clamp(a -= 2f*delta, 0, 1);
			renderer.setColor(tmp.set(color).lerp(tint, a));
			renderer.rect(x, y, 1, 1);
		}

		public void setColor(Color color){
			this.color.set(color);
		}

		@Override public String toString () {
			return "Tile{" +x + ", " + y + ", id="+id+"}";
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Tile tile = (Tile)o;
			return id == tile.id;
		}

		@Override public int hashCode () {
			return id;
		}
	}

	private class Region {
		public int x;
		public int y;
		public Rectangle bounds = new Rectangle();
		public Array<Tile> tiles = new Array<>();
		public SubRegion[] subs = new SubRegion[REGION_SIZE * REGION_SIZE];
		public boolean selected;

		public Region (int x, int y) {
			this.x = x;
			this.y = y;
			bounds.set(x, y, REGION_SIZE, REGION_SIZE);
			for (int sx = 0; sx < REGION_SIZE; sx++) {
				for (int sy = 0; sy < REGION_SIZE; sy++) {
					subs[sx + sy * REGION_SIZE] = new SubRegion();
				}
			}
		}

		public void update(Vector2 cs) {
			selected = bounds.contains(cs);
			if (selected) {
				for (SubRegion sub : subs) {
					sub.select(cs);
				}
			} else {
				for (SubRegion sub : subs) {
					sub.selected = false;
				}
			}
			// TODO select neighbours
		}

		private Tile getEdge (Tile tile, int offset, ObjectSet<Tile> available) {
			while (true) {
				Tile next = getTile(tile.x + offset, tile.y, this);
				if (next != null && available.contains(next)) {
					if (next.x > x && next.y > y) {
						Tile left = getTile(next.x - 1, next.y, null);
						Tile south = getTile(next.x, next.y - 1, null);
						if (left != null && left.type == next.type &&
							south != null && south.type == next.type) {
							return next;
						}
					}
					tile = next;
				} else {
					break;
				}
			}
			return tile;
		}

		int ids;
		ObjectSet<Tile> added = new ObjectSet<>();
		public void rebuild () {
			for (int i = 0; i < ids; i++) {
				subs[i].id = -1;
				subs[i].tiles.clear();
				subs[i].edges.clear();
			}
			ids = 0;
			added.clear();
			for (int sx = 0; sx < REGION_SIZE; sx++) {
				for (int sy = 0; sy < REGION_SIZE; sy++) {
					found.clear();
					SubRegion sub = subs[ids];
					sub.id = ids;
					Tile tile = getTile(x + sx, y + sy, this);
					if (tile == null || added.contains(tile)) continue;

					floodFillFull(x + sx, y + sy, this, found);
					added.addAll(found);
					for (Tile f : found) {
						sub.tiles.add(f);
					}
					ids++;
				}
			}
		}

		public void rebuildSubs() {
			for (int i = 0; i < ids; i++) {
				subs[i].rebuild();
			}
		}

		private float margin = 0.015f;
		public void render (ShapeRenderer renderer) {
			if (selected) {
				renderer.setColor(0, 1, 1, 1);
			} else {
				renderer.setColor(1, .7f, 0, 1);
			}
			renderer.rect(x + margin, y + margin, REGION_SIZE-2*margin, REGION_SIZE-2*margin);
		}

		public void renderSubs (ShapeRenderer renderer) {
			for (int i = 0; i < ids; i++) {
				subs[i].render(renderer);
			}
		}

		public void renderEdges (ShapeRenderer renderer) {
			for (int i = 0; i < ids; i++) {
				subs[i].renderEdges(renderer);
			}
		}

		protected Comparator<Tile> sortX = new Comparator<Tile>() {
			@Override public int compare (Tile o1, Tile o2) {
				if (o1.y == o2.y) return o1.x - o2.x;
				return o1.y - o2.y;
			}
		};

		protected Comparator<Tile> sortY = new Comparator<Tile>() {
			@Override public int compare (Tile o1, Tile o2) {
				if (o1.x == o2.x) return o1.y - o2.y;
				return o1.x - o2.x;
			}
		};

		public class SubRegion {
			private Rectangle tmp = new Rectangle();
			public int id = -1;
			public boolean selected;
			public Array<Tile> tiles = new Array<>();
			public Array<Edge> edges = new Array<>();

			public SubRegion () {

			}

			public void rebuild() {
				// find vertical edges
				tiles.sort(sortX);
				findHorEdges(0, -1, 0);
				findHorEdges(MAP_HEIGHT -1, 1, 1);
				// find horizontal edges
				tiles.sort(sortY);
				findVertEdges(0, -1, 0);
				findVertEdges(MAP_WIDTH -1, 1, 1);
			}

			private void findHorEdges(int skipY, int offsetY, int endOffsetY) {
				int last = 0;
				while (last < tiles.size) {
					Tile start = tiles.get(last);
					// not strictly needed, skip edges of the map
					if (start.y == skipY) {
						last++;
						continue;
					}
					Tile test = getTile(start.x, start.y + offsetY, null);
					// check if next tile is part of this sub region
					if (test != null && tiles.contains(test, true)) {
						last++;
						continue;
					}
					// test is from other region
					int otherSubRegionId = getSubRegionId(test);
					Tile end = start;
					// go in a given direction until we encounter a tile that doesnt match
					for (int i = last + 1; i < tiles.size ; i++) {
						Tile tile = tiles.get(i);
						// next row, we are done
						if (end.y != tile.y) break;
						// next tile is not adjacent to last one
						if (end.x + 1 != tile.x) break;
						test = getTile(tile.x, tile.y + offsetY, null);
						// we dont want edges inside the regions
						if (test != null && tiles.contains(test, true)) break;
						// if type of the other tile changes, we want an edge
						if (otherSubRegionId != getSubRegionId(test)) break;
						end = tile;
						last = i;
					}
					// set up edge
					Edge edge = TiledPartitionTest.this.getEdge(start.x, start.y + endOffsetY, end.x - start.x + 1, true);
					edge.add(this);
					edges.add(edge);
					// start again from next tile
					last++;
				}
			}

			private void findVertEdges(int skipX, int offsetX, int endOffsetX) {
				int last = 0;
				while (last < tiles.size) {
					Tile start = tiles.get(last);
					// not strictly needed, skip edges of the map
					if (start.x == skipX) {
						last++;
						continue;
					}
					Tile test = getTile(start.x + offsetX, start.y, null);
					if (test != null && tiles.contains(test, true)) {
						last++;
						continue;
					}
					int otherSubRegionId = getSubRegionId(test);
					Tile end = start;
					for (int i = last + 1; i < tiles.size ; i++) {
						Tile tile = tiles.get(i);
						if (end.x != tile.x) break;
						if (end.y + 1 != tile.y) break;
						test = getTile(tile.x + offsetX, tile.y, null);
						if (test != null && tiles.contains(test, true)) break;
						if (otherSubRegionId != getSubRegionId(test)) break;
						end = tile;
						last = i;
					}
					Edge edge = TiledPartitionTest.this.getEdge(start.x + endOffsetX, start.y, end.y - start.y + 1, false);
					edge.add(this);
					edges.add(edge);
					last++;
				}
			}

			public void select (Vector2 cs) {
				for (Tile tile : tiles) {
					if (tmp.set(tile.x, tile.y, 1, 1).contains(cs)) {
						selected = true;
						return;
					}
				}
				selected = false;
			}

			public void renderEdges (ShapeRenderer renderer) {
				if (!selected) return;
				for (Edge edge : edges) {
					edge.render(renderer);
				}
			}

			public void render (ShapeRenderer renderer) {
				for (int i = 0; i < tiles.size; i++) {
					Tile tile = tiles.get(i);
					float c = i/(float)tiles.size;
					if (selected) {
						renderer.setColor(0, 0, 1, .5f);
//						renderer.setColor(c, c, c, .5f);
					} else {
//						c = .5f + id /(float)ids/2;
//						renderer.setColor(c, c, c, .5f);
						renderer.setColor(1, 1, 1, .2f);
					}
					renderer.rect(tile.x, tile.y, 1, 1);
				}
				if (!selected) return;
				renderedExtras.clear();
				int type = tiles.first().type;
				for (int i = 0; i < edges.size; i++) {
					Edge edge = edges.get(i);
					for (int j = 0; j < edge.connected.size; j++) {
						SubRegion subRegion = edge.connected.get(j);
						if (subRegion != this && !renderedExtras.contains(subRegion)) {
							int other = subRegion.tiles.first().type;
							if (type == 1 && type == other) {
								subRegion.renderExtra(renderer);
								renderedExtras.add(subRegion);
							} else if (type != 1 && other != 1) {
								subRegion.renderExtra(renderer);
								renderedExtras.add(subRegion);
							}
						}
					}
				}
			}
			ObjectSet<SubRegion> renderedExtras = new ObjectSet<>();

			private void renderExtra (ShapeRenderer renderer) {
				renderer.setColor(0, 0, 1, .33f);
				for (int i = 0; i < tiles.size; i++) {
					Tile tile = tiles.get(i);
					renderer.rect(tile.x, tile.y, 1, 1);
				}
			}
		}
	}

	private Edge tempEdge = new Edge(0, 0, 0, true);
	private Array<Edge> dumb = new Array<>();
	public Edge getEdge (int x, int y, int len, boolean horizontal) {
		tempEdge.set(x, y, len, horizontal);
		int id = dumb.indexOf(tempEdge, false);
		if (id > 0) {
			return dumb.get(id);
		}
		Edge edge = new Edge(x, y, len, horizontal);
		dumb.add(edge);
		return edge;
	}

	public void removeEdges(Region region) {
		// we want to clear all edges from that and surrounding regions

	}

	public class Edge {
		public Array<Region.SubRegion> connected = new Array<>();
		public int x;
		public int y;
		public int len;
		public boolean horizontal;
		private Color color = new Color(MathUtils.random(),MathUtils.random(),MathUtils.random(), 1);

		public Edge (int x, int y, int len, boolean horizontal) {
			this.x = x;
			this.y = y;
			this.len = len;
			this.horizontal = horizontal;
		}

		public void set (int x, int y, int len, boolean horizontal) {
			this.x = x;
			this.y = y;
			this.len = len;
			this.horizontal = horizontal;
		}

		public void render (ShapeRenderer renderer) {
			renderer.setColor(color);
			renderer.rect(x+0.1f, y+0.1f, (horizontal?len:1)-.2f, (horizontal?1:len)-.2f);
		}

		public void add(Region.SubRegion region) {
			connected.add(region);
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Edge edge = (Edge)o;

			if (x != edge.x)
				return false;
			if (y != edge.y)
				return false;
			if (len != edge.len)
				return false;
			return horizontal == edge.horizontal;

		}

		@Override public int hashCode () {
			int result = x;
			result = 31 * result + y;
			result = 31 * result + len;
			result = 31 * result + (horizontal ? 1 : 0);
			return result;
		}
	}
	protected int getSubRegionId (Tile tile) {
		// TODO this is obviously dumb
		// do we want region and sub region id hash?
		for (Region region : regions) {
			for (int i = 0; i < region.ids; i++) {
				if (region.subs[i].tiles.contains(tile, true)) {
					return i;
				}
			}
		}
		return -1;
	}

	private Vector3 temp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		// fairly dumb
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		if (button == Input.Buttons.LEFT) {
			// todo create a wall at position
			for (Tile tile : tiles) {
				if (tile.bounds.contains(cs)) {
					int next = tile.type + 1;
					if (next > 2) next = 0;
					tile.setType(next);
					break;
				}
			}
			rebuildRegions(cs);
		} else if (button == Input.Buttons.RIGHT) {
			for (Tile tile : tiles) {
				if (tile.bounds.contains(cs)) {
					int next = tile.type - 1;
					if (next < 0) next = 2;
					tile.setType(next);
					break;
				}
			}
			rebuildRegions(cs);
		} else if (button == Input.Buttons.MIDDLE) {
			for (Region region : regions) {
				if (region.bounds.contains(cs)) {
					found.clear();
					floodFill((int)cs.x, (int)cs.y, region, found);
					break;
				}

			}
		}
		return true;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		return super.mouseMoved(screenX, screenY);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, TiledPartitionTest.class);
	}
}

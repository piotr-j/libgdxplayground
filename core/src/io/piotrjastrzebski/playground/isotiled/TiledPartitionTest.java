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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledPartitionTest extends BaseScreen {
	public final static float SCALE = 32f; // size of single tile in pixels
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280/SCALE;
	public final static float VP_HEIGHT = 720/SCALE;

	private final static int REGION_SIZE = 8;
	private final static int MAP_WIDTH = REGION_SIZE * 5;
	private final static int MAP_HEIGHT = REGION_SIZE * 3;
	//																	48, 24
	private final static int[] map = new int[] {
		1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,

		1, 0, 0, 0, 0, 0, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 2, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 2,  2, 0, 1, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  1, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,

		1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,
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
//		regions.get(0).rebuild();
	}

	private void rebuildRegions () {
		for (Region region : regions) {
			region.rebuild();
		}
	}

	private void rebuildRegion (Vector2 cs) {
		for (Region region : regions) {
			if (region.bounds.contains(cs)) region.rebuild();
		}
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
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Tile tile : tiles) {
			tile.render(renderer, delta);
		}
		renderer.end();

//		Gdx.gl.glEnable(GL20.GL_BLEND);
//		renderer.setColor(1, 0, 1, 0.1f);
//		renderer.begin(ShapeRenderer.ShapeType.Filled);
//		for (Tile tile : found) {
//			renderer.rect(tile.x + .4f, tile.y + .4f, .2f, .2f);
//		}
//		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		for (Region region : regions) {
			region.update(cs);
			region.render(renderer);
		}

		renderer.end();
	}
	Region ffRegion;
	Array<Tile> found = new Array<>();
	private void floodFill (int x, int y, Region region, Array<Tile> found) {
		Tile start = get(x, y, region);
		if (start == null) return;
		ffRegion = region;
		targetType = start.type;
		processed.clear();
		queue.clear();
		queue.add(start);
		resumeFloodFill(region, found);
	}

	private void resumeFloodFill (Region region, Array<Tile> found) {
//		int iters = 0;
		while (queue.size > 0) {
			Tile tile = queue.removeIndex(0);
			if (tile.type == targetType) {
				if (processed.containsKey(tile.id)) continue;
				visitTile(tile, found);
				Tile west = getEdge(tile, -1, region);
				Tile east = getEdge(tile, 1, region);

				for (int i = west.x; i <= east.x; i++) {
					Tile n = get(i, west.y, region);
					visitTile(n, found);
					Tile north = get(i, west.y + 1, region);
					if (north != null && north.type == targetType) {
						addToQueue(north);
					}
					Tile south = get(i, west.y - 1, region);
					if (south != null && south.type == targetType) {
						addToQueue(south);
					}
				}
			}
//			iters++;
//			if (iters >= maxIters) {
//				filling = true;
//				break;
//			}
		}
	}

	private Tile getEdge (Tile tile, int offset, Region region) {
		while (true) {
			Tile next = get(tile.x + offset, tile.y, region);
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
	private void floodFill (int x, int y, Array<Tile> found) {
		floodFill(x, y, null, found);
	}

	private void addToQueue (Tile tile) {
		if (tile == null || processed.containsKey(tile.id)) return;
		queue.add(tile);
	}

	private void visitTile (Tile tile, Array<Tile> found) {
		processed.put(tile.id, tile);
		tile.tint.set(1, .5f, .5f, 1);
		tile.a = 1;
		found.add(tile);
	}

	private void resumeFloodFillSlow (Region region) {
//		int iters = 0;
		while (queue.size > 0) {
			Tile tile = queue.removeIndex(0);
			if (tile.type == targetType) {
				if (processed.containsKey(tile.id)) continue;
				processed.put(tile.id, tile);
				tile.tint.set(1, .5f, .5f, 1);
				tile.a = 1;
				addToQueue(tile.x - 1, tile.y, region);
				addToQueue(tile.x + 1, tile.y, region);
				addToQueue(tile.x, tile.y + 1, region);
				addToQueue(tile.x, tile.y - 1, region);
			}
//			iters++;
//			if (iters >= maxIters) {
//				filling = true;
//				break;
//			}
		}
	}

	private void addToQueue(int x, int y, Region region) {
		addToQueue(get(x, y, region));
	}

	private Tile get(int x, int y, Region region) {
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
				color.set(.1f, MathUtils.random(0.7f, .9f), MathUtils.random(.1f, .2f), 1);
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
			return "Tile{" +x + ", " + y + "}";
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Tile tile = (Tile)o;
			return id != tile.id;
		}

		@Override public int hashCode () {
			return id;
		}
	}

	private class Region {
		public int x;
		public int y;
		public IntArray sides = new IntArray();
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
					subs[sx + sy * REGION_SIZE] = new SubRegion(sx, sy);
				}
			}
		}

		private Rectangle tmp = new Rectangle();
		public void update(Vector2 cs) {
			selected = bounds.contains(cs);
			SubRegion selected = null;
			for (SubRegion sub : subs) {
				tmp.set(x + sub.sx, y + sub.sy, 1, 1);
				sub.selected = false;
				if (tmp.contains(cs)) {
					selected = sub;
				}
			}
			if (selected != null) {
				for (SubRegion sub : subs) {
					if (sub.id == selected.id) {
						sub.selected = true;
					}
				}
			}
			// TODO select neighbours
		}

		int ids;
		public void rebuild () {
			ids = 0;
			for (SubRegion sub : subs) {
				sub.id = -1;
			}
			for (int i = 0; i < subs.length; i++) {
				SubRegion sub = subs[i];
				if (sub.id >= 0) continue;
				found.clear();
				floodFill(x + sub.sx, y + sub.sy, this, found);
				resumeFloodFill(this, found);
//				filling = false;

				for (Tile tile : found) {
					getWorld(tile.x, tile.y).id = ids;
				}
				ids++;
			}
		}

		private float margin = 0.015f;
		public void render (ShapeRenderer renderer) {
			if (selected) {
				renderer.setColor(Color.CYAN);
			} else {
				renderer.setColor(Color.GOLD);
			}
			renderer.rect(x + margin, y + margin, REGION_SIZE-2*margin, REGION_SIZE-2*margin);
			for (SubRegion sub : subs) {
				float c = .5f + sub.id /(float)ids/2;
				renderer.setColor(c, c, c, .25f);
				if (sub.selected) {
					renderer.setColor(Color.CYAN);
				}
				renderer.rect(x + sub.sx + .1f, y + sub.sy + .1f, .8f, .8f);
				sub.selected = false;
			}
		}

		private SubRegion getWorld(int x, int y) {
			x -= this.x;
			y -= this.y;
			return subs[x + y * REGION_SIZE];
		}

		private SubRegion get(int x, int y) {
			return subs[x + y * REGION_SIZE];
		}

		public class SubRegion {
			public final int sx;
			public final int sy;
			public int id = -1;
			public boolean selected;

			public SubRegion (int sx, int sy) {

				this.sx = sx;
				this.sy = sy;
			}
		}
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
			rebuildRegion(cs);
		} else if (button == Input.Buttons.RIGHT) {
			for (Tile tile : tiles) {
				if (tile.bounds.contains(cs)) {
					int next = tile.type - 1;
					if (next < 0) next = 2;
					tile.setType(next);
					break;
				}
			}
			rebuildRegion(cs);
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

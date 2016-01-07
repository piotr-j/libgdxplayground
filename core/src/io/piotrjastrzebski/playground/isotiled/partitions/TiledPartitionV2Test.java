package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledPartitionV2Test extends BaseScreen {
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
	Array<MapRegion> regions = new Array<>();

	public TiledPartitionV2Test (GameReset game) {
		super(game);
		int regionsX = MAP_WIDTH / REGION_SIZE;
		int regionsY = MAP_HEIGHT / REGION_SIZE;

		regions.ensureCapacity(regionsX * regionsY);
		// set size to max so it wont complain when we use #set(int, T)
		regions.size = regionsX * regionsY;
		for (int x = 0; x < regionsX; x++) {
			for (int y = 0; y < regionsY; y++) {
				MapRegion region = new MapRegion(x + y * regionsX, x * REGION_SIZE, y * REGION_SIZE, REGION_SIZE, REGION_SIZE);
				regions.set(region.id, region);
			}
		}

		tiles.ensureCapacity(MAP_HEIGHT * MAP_WIDTH);
		// set size to max so it wont complain when we use #set(int, T)
		tiles.size = MAP_HEIGHT * MAP_WIDTH;
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				Tile tile = new Tile(x + y * MAP_WIDTH, x, y);
				// magic incantation to get correct id from the map above
				tile.setType(map[x + (MAP_HEIGHT - 1 - y) * MAP_WIDTH]);
				tiles.set(tile.id, tile);
				addTileToRegion(tile);
			}
		}

		gameCamera.position.set(VP_WIDTH / 2, VP_HEIGHT / 2, 0);

		Gdx.app.log("", "F2 - toggle draw debug pointer");
		Gdx.app.log("", "F3 - toggle draw debug flood fill, l click - ff all, r click - ff region");
		Gdx.app.log("", "F4 - toggle debug tile type setter");
	}

	private void addTileToRegion (Tile tile) {
		MapRegion region = getRegionAt(tile.x, tile.y);
		if (region == null) throw new AssertionError("Region cant be null here!");
		region.addTile(tile);
	}

	private MapRegion getRegionAt (int x, int y) {
		int rx = x / REGION_SIZE;
		int ry = y / REGION_SIZE;
		if (rx < 0 || ry < 0 || rx >= MAP_WIDTH / REGION_SIZE || ry >= MAP_HEIGHT / REGION_SIZE) return null;
		return regions.get(rx + ry * (MAP_WIDTH / REGION_SIZE));
	}

	private Tile getTileAt (int x, int y) {
		if (x < 0 || y < 0 || x >= MAP_WIDTH || y >= MAP_HEIGHT) return null;
		return tiles.get(x + y * MAP_WIDTH);
	}

	private boolean debugTileType = true;
	private boolean drawDebugPointer = false;
	private boolean drawDebugFloodFill = false;
	private Vector2 cs = new Vector2();
	@Override public void render (float delta) {
		super.render(delta);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Tile tile : tiles) {
			tile.render(renderer, delta);
		}
		drawDebugPointer();
		if (drawDebugFloodFill) {
			renderer.setColor(Color.GOLD);
			renderer.getColor().a = .75f;
			for (Tile tile : found) {
				renderer.rect(tile.x + .025f, tile.y + .025f, .95f, .95f);
			}
		}
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.CYAN);
		for (MapRegion region : regions) {
			region.render(renderer, delta);
		}
		renderer.end();
	}

	private void floodFill(int x, int y, ObjectSet<Tile> out) {
		floodFill(x, y, 0, 0, MAP_WIDTH -1, MAP_HEIGHT -1, out);
	}

	private void floodFill(int x, int y, MapRegion region, ObjectSet<Tile> out) {
		floodFill(x, y, region.x, region.y, region.x + region.width -1, region.y + region.height -1, out);
	}

	private ObjectSet<Tile> found = new ObjectSet<>();
	private Array<Tile> queue = new Array<>(false, 16);
	private IntMap<Tile> processed = new IntMap<>();
	/**
	 * Find all connected tiles starting from tile at x, y within given bounds
	 */
	private void floodFill(int x, int y, int sx, int sy, int ex, int ey, ObjectSet<Tile> out) {
		out.clear();
		processed.clear();
		queue.clear();
		Tile start = getTileAt(x, y);
		if (start == null) throw new AssertionError("Tile cant be null here!");
		queue.add(start);
		while (queue.size > 0) {
			Tile tile = queue.removeIndex(0);
			if (tile.type != start.type) continue;
			if (processed.containsKey(tile.id)) continue;
			processed.put(tile.id, tile);
			out.add(tile);
			Tile west = getWestEdge(tile, sx, sy, ex, ey);
			Tile east = getEastEdge(tile, sx, sy, ex, ey);

			for (int nx = west.x; nx <= east.x; nx++) {
				Tile next = getTileAt(nx, west.y);
				if (next == null) throw new AssertionError("Tile cant be null here!");
				processed.put(next.id, next);
				out.add(next);
				Tile north = getTileAt(nx, west.y + 1);
				if (north != null
					&& north.x >= sx && north.x <= ex && north.y >= sy && north.y <= ey
					&& north.type == start.type
					&& !processed.containsKey(north.id)) {
					queue.add(north);
				}
				Tile south = getTileAt(nx, west.y - 1);
				if (south != null
					&& south.x >= sx && south.x <= ex && south.y >= sy && south.y <= ey
					&& south.type == start.type
					&& !processed.containsKey(south.id)) {
					queue.add(south);
				}
			}
		}
	}

	private Tile getWestEdge (Tile tile, int sx, int sy, int ex, int ey) {
		return getEdge(tile, -1, sx, sy, ex, ey);
	}

	private Tile getEastEdge (Tile tile, int sx, int sy, int ex, int ey) {
		return getEdge(tile, 1, sx, sy, ex, ey);
	}

	private Tile getEdge (Tile tile, int offset, int sx, int sy, int ex, int ey) {
		while (true) {
			Tile next = getTileAt(tile.x + offset, tile.y);
			if (next != null
				&& next.x >= sx && next.x <= ex && next.y >= sy && next.y <= ey
				&& next.type == tile.type) {
				tile = next;
			} else {
				break;
			}
		}
		return tile;
	}

	private void drawDebugPointer () {
		if (drawDebugPointer) {
			int x = (int)cs.x;
			int y = (int)cs.y;

			MapRegion region = getRegionAt(x, y);
			if (region == null) throw new AssertionError("Region cant be null here!");
			renderer.setColor(Color.MAGENTA);
			renderer.getColor().a = .5f;
			for (Tile tile : region.tiles) {
				renderer.rect(tile.x+.05f, tile.y+.05f, .9f, .9f);
			}
			renderer.getColor().a = 1f;
			renderer.setColor(Color.MAGENTA);
			renderer.rect(region.x, region.y, region.width, 0.25f);
			renderer.rect(region.x, region.y, 0.25f, region.height);
			renderer.rect(region.x + region.width - 0.25f, region.y, 0.25f, region.height);
			renderer.rect(region.x, region.y + region.height - 0.25f, region.width, 0.25f);

			Tile tile = getTileAt(x, y);
			if (tile == null) throw new AssertionError("Tile cant be null here!");
			renderer.setColor(Color.PINK);
			renderer.rect(tile.x, tile.y, 1, 1);
			renderer.setColor(Color.RED);
			renderer.circle(cs.x, cs.y, .1f, 16);
		}
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.F2:
			drawDebugPointer = !drawDebugPointer;
			break;
		case Input.Keys.F3:
			drawDebugFloodFill = !drawDebugFloodFill;
			break;
		case Input.Keys.F4:
			debugTileType = !debugTileType;
			break;
		}
		return super.keyDown(keycode);
	}

	private Vector3 temp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		// fairly dumb
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		int x = (int)cs.x;
		int y = (int)cs.y;
		// need to change type or something
		if (drawDebugFloodFill) {
			if (button == Input.Buttons.LEFT) {
				floodFill(x, y, found);
			} else if (button == Input.Buttons.RIGHT) {
				MapRegion region = getRegionAt(x, y);
				floodFill(x, y, region, found);
			} else if (button == Input.Buttons.MIDDLE) {
				found.clear();
			}
		} else if (debugTileType) {
			Tile tile = getTileAt(x, y);
			if (tile != null) {
				if (button == Input.Buttons.LEFT) {
					tile.type++;
					if (tile.type > 2) tile.type = 0;
				} else if (button == Input.Buttons.RIGHT) {
					tile.type--;
					if (tile.type < 0) tile.type = 2;
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
		PlaygroundGame.start(args, TiledPartitionV2Test.class);
	}
}

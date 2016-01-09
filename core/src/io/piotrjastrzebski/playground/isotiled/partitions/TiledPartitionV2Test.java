package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import net.mostlyoriginal.api.plugin.extendedcomponentmapper.M;

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
		0, 0, 0, 0, 0, 0, 0, 0,  0, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 1, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 1, 1, 1, 1, 1, 0,  0, 1, 1, 2, 1, 1, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 1, 0, 0, 0, 1, 0,  0, 1, 0, 0, 0, 1, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 1, 1, 0, 0, 0, 0, 0,  1, 1, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 1, 0, 0, 1, 1, 1, 1,  2, 0, 0, 1, 1, 1, 1, 1,  1, 0, 1, 0, 1, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 1, 1, 1, 1, 0, 0, 1,  1, 1, 2, 1, 0, 0, 0, 0,  1, 1, 1, 0, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,

		1, 0, 0, 0, 0, 0, 0, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 2, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 2,  2, 0, 1, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  1, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 1, 1, 1, 1,  1, 1, 1, 1, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 2, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 1, 0, 1,  0, 0, 0, 0, 2, 0, 0, 0,  0, 0, 0, 1, 1, 1, 1, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 1,  0, 0, 1, 0, 0, 1, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 0,  0, 0, 1, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		1, 0, 0, 0, 0, 0, 0, 0,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,

		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 1, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  1, 1, 0, 1, 1, 1, 1, 1,  1, 1, 0, 2, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,
		0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 1,  1, 1, 1, 1, 1, 1, 1, 1,  1, 1, 1, 1, 1, 1, 1, 1,
	};

	TileMap tileMap;

	public TiledPartitionV2Test (GameReset game) {
		super(game);

		tileMap = new TileMap(map, MAP_WIDTH, MAP_HEIGHT, REGION_SIZE);
		tileMap.rebuild();

		gameCamera.position.set(VP_WIDTH / 2, VP_HEIGHT / 2, 0);

		Gdx.app.log("", "F2 - toggle draw debug pointer");
		Gdx.app.log("", "F3 - toggle draw debug flood fill, l click - ff all, r click - ff region");
		Gdx.app.log("", "F4 - toggle debug tile type setter");
		Gdx.app.log("", "F5 - toggle draw debug tile over mouse");
		Gdx.app.log("", "F6 - toggle draw sub regions");
		Gdx.app.log("", "F7 - toggle draw all edges");
		Gdx.app.log("", "[ - degreeOfSeparation--");
		Gdx.app.log("", "] - degreeOfSeparation++");
	}

	private boolean debugTileType = true;
	private boolean drawDebugOverTile = true;
	private boolean drawDebugPointer = false;
	private boolean drawDebugFloodFill = false;
	private boolean drawDebugSubRegions = false;
	private boolean drawAllEdges = false;
	private int degreeOfSeparation = 1;
	private Vector2 cs = new Vector2();
//	private ObjectSet<MapRegion.SubRegion> subRegions = new ObjectSet<>();
	private Array<MapRegion.SubRegion> tmpRegions = new Array<>();
	private TileMap.NeighbourData data = new TileMap.NeighbourData();
	int lastX = -1;
	int lastY = -1;
	int lastDoS = -1;
	@Override public void render (float delta) {
		super.render(delta);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Tile tile : tileMap.tiles) {
			tile.render(renderer, delta);
		}
		if (drawDebugPointer) {
			drawDebugPointer();
		}
		if (drawDebugSubRegions) {
			drawSubRegions();
		}
		if (drawDebugFloodFill) {
			drawFloodFill();
		}
		if (drawAllEdges) {
			for (TileMap.Edge edge : tileMap.edges) {
				renderer.setColor(edge.color);
				renderer.rect(edge.x + .1f, edge.y + .1f, edge.horizontal ? edge.length - .2f : .8f,
					edge.horizontal ? .8f : edge.length - .2f);
			}
		}

		int x = (int)cs.x;
		int y = (int)cs.y;
		// cache to save the battery :d
		if (lastX != x || lastY != y || lastDoS != degreeOfSeparation) {
			lastX = x;
			lastY = y;
			lastDoS = degreeOfSeparation;
			data.reset();
			tileMap.touched.clear();
			tileMap.touchedRegions.clear();
			tileMap.regionsEdges.clear();
			// TODO this is broken for high degreeOfSeparation
			tileMap.getConnectedSubsAt(x, y, degreeOfSeparation, data);

//		tileMap.getConnectedSubsAt(x, y, 0, subRegions);
//		tileMap.expandSubRegions(subRegions, degreeOfSeparation);
		}

		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLACK);
		for (MapRegion.SubRegion sub : data.subRegions) {
			for (Tile tile : sub.tiles) {
				renderer.rect(tile.x+.05f, tile.y+.05f, .9f, .9f);
			}
		}
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.BLACK);
		renderer.getColor().a = .1f;
		tmpRegions.clear();
		for (int i = 0; i < data.degreeOfSeparation; i++) {
			// NOTE tmpRegions should be cleared in general usage, we dont so we can show progression with blending
			data.get(i, tmpRegions);
			for (MapRegion.SubRegion sub : tmpRegions) {
				for (Tile tile : sub.tiles) {
					renderer.rect(tile.x+.1f, tile.y+.1f, .8f, .8f);
				}
			}
		}

		/*
		renderer.setColor(Color.FOREST);
		renderer.getColor().a = .5f;
		for (MapRegion.SubRegion sub : data.subRegions) {
			for (Tile tile : sub.tiles) {
				renderer.rect(tile.x+.1f, tile.y+.1f, .8f, .8f);
			}
		}
		renderer.setColor(Color.VIOLET);
		renderer.getColor().a = .5f;
		for (MapRegion.SubRegion sub : data.subRegions) {
			for (Tile tile : sub.tiles) {
				renderer.rect(tile.x+.3f, tile.y+.3f, .4f, .4f);
			}
		}

		renderer.setColor(Color.CYAN);
		renderer.getColor().a = .5f;
		for (TileMap.Edge edge : tileMap.touched) {
			renderer.rect(edge.x + .1f, edge.y + .1f, edge.horizontal ? edge.length - .2f : .8f,
				edge.horizontal ? .8f : edge.length - .2f);
		}
		*/
		/*
		MapRegion.SubRegion sub = tileMap.getSubRegionAt(x, y);
		if (sub != null) {
			renderer.setColor(sub.color);
			for (Tile tile : sub.tiles) {
				renderer.rect(tile.x+.05f, tile.y+.05f, .9f, .9f);
			}
			IntArray ids = sub.edgeIds;
			for (int i = 0; i < ids.size; i++) {
				TileMap.Edge edge = tileMap.getEdge(ids.get(i));
				for (MapRegion.SubRegion region : edge.subRegions) {
					if (region == sub) continue;
					if (region.tileType != sub.tileType) continue;
					renderer.setColor(sub.color);
					renderer.getColor().a = .5f;
					for (Tile tile : region.tiles) {
						renderer.rect(tile.x+.05f, tile.y+.05f, .9f, .9f);
					}
				}
//				renderer.setColor(edge.color);
//				renderer.rect(edge.x + .1f, edge.y + .1f, edge.horizontal ? edge.length - .2f : .8f,
//					edge.horizontal ? .8f : edge.length - .2f);
			}

		}
		*/
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.WHITE);
		for (MapRegion region : tileMap.regions) {
			renderer.rect(region.x, region.y, region.size, region.size);
		}
		if (drawDebugOverTile) {
			drawTileOver();
		}
		renderer.end();
	}

	private void drawTileOver () {
		renderer.setColor(Color.BLACK);
		Tile tile = tileMap.getTileAt((int)cs.x, (int)cs.y);
		if (tile != null) {
			renderer.rect(tile.x, tile.y, 1, 1);
		}
	}

	private void drawFloodFill () {
		renderer.setColor(Color.GOLD);
		renderer.getColor().a = .75f;
		for (Tile tile : found) {
			renderer.rect(tile.x + .025f, tile.y + .025f, .95f, .95f);
		}
	}

	private void drawSubRegions () {
		for (MapRegion region : tileMap.regions) {
			for (MapRegion.SubRegion sub : region.subs) {
				renderer.setColor(sub.color);
				for (Tile tile : sub.tiles) {
					renderer.rect(tile.x+.05f, tile.y+.05f, .9f, .9f);
				}
			}
		}
	}

	private ObjectSet<Tile> found = new ObjectSet<>();

	private void drawDebugPointer () {
		int x = (int)cs.x;
		int y = (int)cs.y;

		MapRegion region = tileMap.getRegionAt(x, y);
		if (region == null) throw new AssertionError("Region cant be null here!");
		renderer.setColor(Color.MAGENTA);
		renderer.getColor().a = .5f;
		for (int id = 0; id < region.tiles.size; id++) {
			Tile tile = tileMap.getTile(region.tiles.get(id));
			renderer.rect(tile.x+.05f, tile.y+.05f, .9f, .9f);
		}
		renderer.getColor().a = 1f;
		renderer.setColor(Color.MAGENTA);
		renderer.rect(region.x, region.y, region.size, 0.25f);
		renderer.rect(region.x, region.y, 0.25f, region.size);
		renderer.rect(region.x + region.size - 0.25f, region.y, 0.25f, region.size);
		renderer.rect(region.x, region.y + region.size - 0.25f, region.size, 0.25f);

		Tile tile = tileMap.getTileAt(x, y);
		if (tile == null) throw new AssertionError("Tile cant be null here!");
		renderer.setColor(Color.PINK);
		renderer.rect(tile.x, tile.y, 1, 1);
		renderer.setColor(Color.RED);
		renderer.circle(cs.x, cs.y, .1f, 16);
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
		case Input.Keys.F5:
			drawDebugOverTile = !drawDebugOverTile;
			break;
		case Input.Keys.F6:
			drawDebugSubRegions = !drawDebugSubRegions;
			break;
		case Input.Keys.F7:
			drawAllEdges = !drawAllEdges;
			break;
		case Input.Keys.LEFT_BRACKET:
			degreeOfSeparation = Math.max(degreeOfSeparation -1, 0);
			Gdx.app.log("", "degreeOfSeparation = " + degreeOfSeparation);
			break;
		case Input.Keys.RIGHT_BRACKET:
			degreeOfSeparation = Math.min(degreeOfSeparation +1, 32);
			Gdx.app.log("", "degreeOfSeparation = " + degreeOfSeparation);
			break;
		}
		return super.keyDown(keycode);
	}

	@Override public boolean scrolled (int amount) {
		degreeOfSeparation = Math.max(Math.min(degreeOfSeparation + amount, 32), 0);
		Gdx.app.log("", "degreeOfSeparation = " + degreeOfSeparation);

		return super.scrolled(amount);
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
				FloodFiller.floodFill(x, y, tileMap, found);
			} else if (button == Input.Buttons.RIGHT) {
				MapRegion region = tileMap.getRegionAt(x, y);
				FloodFiller.floodFill(x, y, region, tileMap, found);
			} else if (button == Input.Buttons.MIDDLE) {
				found.clear();
			}
		} else if (debugTileType) {
			Tile tile = tileMap.getTileAt(x, y);
			if (tile != null) {
				if (button == Input.Buttons.LEFT) {
					tile.type++;
					if (tile.type > 2) tile.type = 0;
					tileMap.rebuild(tile.x, tile.y);;
				} else if (button == Input.Buttons.RIGHT) {
					tile.type--;
					if (tile.type < 0) tile.type = 2;
					tileMap.rebuild(tile.x, tile.y);;
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

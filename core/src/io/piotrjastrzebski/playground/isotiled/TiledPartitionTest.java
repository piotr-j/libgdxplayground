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

	Array<Tile> tiles = new Array<>();
	Array<Region> regions = new Array<>();
	public TiledPartitionTest (GameReset game) {
		super(game);
		Tile tile;
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				tiles.add(tile = new Tile(x * MAP_HEIGHT + y, x, y));
				if (x%2 == 0 && y%2==0) {
					tile.color.set(0, .8f, 0, 1);
				} else {
					tile.color.set(.1f, 0.6f, .1f, 1);
				}
			}
		}
		for (int x = 0; x < MAP_WIDTH; x += REGION_SIZE) {
			for (int y = 0; y < MAP_HEIGHT; y += REGION_SIZE) {
				regions.add(new Region(x, y));
			}
		}
		gameCamera.position.set(VP_WIDTH/2, VP_HEIGHT/2, 0);
	}

	private Vector2 cs = new Vector2();
	@Override public void render (float delta) {
		super.render(delta);

		if (filling) {
			fillTimer += delta;
			if (fillTimer > .25f) {
				fillTimer-=.25f;
				resumeFloodFill();
			}
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		// todo draw tiles
		for (Tile tile : tiles) {
			tile.render(renderer, delta);
		}

		renderer.end();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		// todo draw partitions
		for (Region region : regions) {
			region.update(cs);
			region.render(renderer);
		}

		renderer.end();
	}

	private void floodFill (int x, int y, Region region) {
		Tile start = get(x, y, region);
		if (start == null) return;
		targetColor = start.color.toIntBits();
		processed.clear();
		queue.clear();
		queue.add(start);
		resumeFloodFill(region);
	}

	private void resumeFloodFill (Region region) {
		int iters = 0;
		while (queue.size > 0) {
			Tile tile = queue.removeIndex(0);
			if (tile.color.toIntBits() == targetColor) {
				if (processed.containsKey(tile.id)) continue;
				visitTile(tile);
				Tile west = getEdge(tile, -1, region);
				Tile east = getEdge(tile, 1, region);

				for (int i = west.x; i <= east.x; i++) {
					Tile n = get(i, west.y, region);
					visitTile(n);
					Tile north = get(i, west.y + 1, region);
					if (north != null && north.color.toIntBits() == targetColor) {
						addToQueue(north);
					}
					Tile south = get(i, west.y - 1, region);
					if (south != null && south.color.toIntBits() == targetColor) {
						addToQueue(south);
					}
				}
			}
			iters++;
			if (iters >= maxIters) {
				filling = true;
				break;
			}
		}
	}

	private Tile getEdge (Tile tile, int offset, Region region) {
		while (true) {
			Tile next = get(tile.x + offset, tile.y, region);
			if (next != null && next.color.toIntBits() == targetColor) {
				tile = next;
			} else {
				break;
			}
		}
		return tile;
	}

	boolean filling;
	float fillTimer;
	int maxIters = 25;

	int targetColor;
	Array<Tile> queue = new Array<>();
	IntMap<Tile> processed = new IntMap<>();
	private void floodFill (int x, int y) {
		Tile start = get(x, y);
		if (start == null) return;
		targetColor = start.color.toIntBits();
		processed.clear();
		queue.clear();
		queue.add(start);
		resumeFloodFill();
	}

	private void resumeFloodFill () {
		int iters = 0;
		while (queue.size > 0) {
			Tile tile = queue.removeIndex(0);
			if (tile.color.toIntBits() == targetColor) {
				if (processed.containsKey(tile.id)) continue;
				visitTile(tile);
				Tile west = getEdge(tile, -1);
				Tile east = getEdge(tile, 1);

				for (int i = west.x; i <= east.x; i++) {
					Tile n = get(i, west.y);
					visitTile(n);
					Tile north = get(i, west.y + 1);
					if (north != null && north.color.toIntBits() == targetColor) {
						addToQueue(north);
					}
					Tile south = get(i, west.y - 1);
					if (south != null && south.color.toIntBits() == targetColor) {
						addToQueue(south);
					}
				}
			}
			iters++;
			if (iters >= maxIters) {
				filling = true;
				break;
			}
		}
	}

	private Tile getEdge (Tile tile, int offset) {
		while (true) {
			Tile next = get(tile.x + offset, tile.y);
			if (next != null && next.color.toIntBits() == targetColor) {
				tile = next;
			} else {
				break;
			}
		}
		return tile;
	}

	private void addToQueue (Tile tile) {
		if (tile == null || processed.containsKey(tile.id)) return;
		queue.add(tile);
	}

	private void visitTile(Tile tile) {
		processed.put(tile.id, tile);
		tile.tint.set(1, .5f, .5f, 1);
		tile.a = 1;
	}

	private void resumeFloodFill2 () {
		int iters = 0;
		while (queue.size > 0) {
			Tile tile = queue.removeIndex(0);
			if (tile.color.toIntBits() == targetColor) {
				if (processed.containsKey(tile.id)) continue;
				processed.put(tile.id, tile);
				tile.tint.set(1, .5f, .5f, 1);
				tile.a = 1;
				addToQueue(tile.x - 1, tile.y);
				addToQueue(tile.x + 1, tile.y);
				addToQueue(tile.x, tile.y + 1);
				addToQueue(tile.x, tile.y - 1);
			}
			iters++;
			if (iters >= maxIters) {
				filling = true;
				break;
			}
		}
	}

	private void addToQueue(int x, int y) {
		addToQueue(get(x, y));
	}

	private Tile get(int x, int y) {
		if (x < 0 || x >= MAP_WIDTH) return null;
		if (y < 0 || y >= MAP_HEIGHT) return null;
		int index = y + x * MAP_HEIGHT;
		if (index < 0 || index >= tiles.size) return null;
		return tiles.get(index);
	}

	private Tile get(int x, int y, Region region) {
		if (x < region.x || x >= region.x + REGION_SIZE) return null;
		if (y < region.y || y >= region.y + REGION_SIZE) return null;
		int index = y + x * MAP_HEIGHT;
		if (index < 0 || index >= tiles.size) return null;
		return tiles.get(index);
	}

	private static class Tile {
		public int id;
		public int x;
		public int y;
		public boolean isWall;
		public Rectangle bounds = new Rectangle();
		public Color color = new Color(Color.BROWN);
		public Color tint = new Color(Color.WHITE);

		public Tile (int id, int x, int y) {
			this.id = id;
			this.x = x;
			this.y = y;
			bounds.set(x, y, 1, 1);
		}

		Color tmp = new Color();
		float a;
		public void render(ShapeRenderer renderer, float delta) {
			a = MathUtils.clamp(a -= .25f*delta, 0, 1);
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

	private static class Region {
		public int x;
		public int y;
		public IntArray sides = new IntArray();
		public Rectangle bounds = new Rectangle();
		public Array<Tile> tiles = new Array<>();
		public boolean selected;

		public Region (int x, int y) {
			this.x = x;
			this.y = y;
			bounds.set(x, y, REGION_SIZE, REGION_SIZE);
		}

		public void update(Vector2 cs) {
			selected = bounds.contains(cs);
		}

		public void rebuild() {

		}

		private float margin = 0.015f;
		public void render (ShapeRenderer renderer) {
			if (selected) {
				renderer.setColor(Color.CYAN);
			} else {
				renderer.setColor(Color.RED);
			}
			renderer.rect(x + margin, y + margin, REGION_SIZE-2*margin, REGION_SIZE-2*margin);
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
					tile.isWall = !tile.isWall;
					if (tile.isWall) {
						tile.color.set(Color.BROWN);
					} else {
						if (tile.x % 2 == 0 && tile.y % 2 == 0) {
							tile.color.set(0, .8f, 0, 1);
						} else {
							tile.color.set(.1f, 0.6f, .1f, 1);
						}
					}
					break;
				}
			}
		} else if (button == Input.Buttons.RIGHT) {
			floodFill((int)cs.x, (int)cs.y);
		} else if (button == Input.Buttons.MIDDLE) {
			for (Region region : regions) {
				if (region.bounds.contains(cs)) {
					floodFill((int)cs.x, (int)cs.y, region);
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

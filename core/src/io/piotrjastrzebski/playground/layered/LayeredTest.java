package io.piotrjastrzebski.playground.layered;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class LayeredTest extends BaseScreen {
	private static final String TAG = LayeredTest.class.getSimpleName();

	private Layer[] layers;
	private static final int WIDTH = 10;
	private static final int HEIGHT = 10;
	private static final int LAYER_COUNT = 3;
	private int currentLevel = 0;

	public LayeredTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);

		layers = new Layer[LAYER_COUNT];
		for (int i = 0; i < LAYER_COUNT; i++) {
			layers[i] = new Layer(i, WIDTH, HEIGHT);
		}
	}

	private Vector3 cp = new Vector3();
	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();
		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			if (currentLevel < layers.length -1) {
				currentLevel++;
			}
			Gdx.app.log(TAG, "Current level = " + currentLevel);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			if (currentLevel > 0) {
				currentLevel--;
			}
			Gdx.app.log(TAG, "Current level = " + currentLevel);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			gameCamera.unproject(cp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			int x = (int)cp.x;
			int y = (int)cp.y;
			if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
				Layer layer = layers[currentLevel];
				Tile tile = layer.tiles[x][y];
				tile.type++;
				if (tile.type > Tile.TileType_Rock) {
					tile.type = Tile.TileType_Empty;
				}
			}
		}

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Layer layer : layers) {
			layer.render(renderer, currentLevel);
		}
		renderer.end();
	}

	protected static class Layer {
		public int level;
		public Tile[][] tiles;
		public int width;
		public int height;

		public Layer (int level, int width, int height) {
			this.level = level;
			this.width = width;
			this.height = height;
			tiles = new Tile[width][height];
			for (int x = 0; x < width; x++) {
				tiles[x] = new Tile[height];
				for (int y = 0; y < height; y++) {
					tiles[x][y] = new Tile(x, y, level);
					if (level == 1) {
						tiles[x][y].type = Tile.TileType_Ground;
					}
				}
			}
		}

		public void render (ShapeRenderer renderer, int currentLevel) {
			for (Tile[] tileRow : tiles) {
				for (Tile tile : tileRow) {
					tile.render(renderer, currentLevel);
				}
			}
		}
	}

	protected static class Tile {
		public final static int TileType_Empty = 0;
		public final static int TileType_Ground = 1;
		public final static int TileType_Grass = 2;
		public final static int TileType_Rock = 3;
		private final int x;
		private final int y;
		private int level;
		public int type = TileType_Empty;

		public Tile (int x, int y, int level) {
			this.x = x;
			this.y = y;
			this.level = level;
		}

		private final static float outlineSize = 0.025f;
		private final static float colorSclPerStep = 0.2f;
		private final static Color tmp = new Color();
		public void render (ShapeRenderer renderer, int currentLevel) {
			switch (type) {
			case TileType_Empty:
				return;
			case TileType_Ground:
				tmp.set(Color.BROWN);
				break;
			case TileType_Grass:
				tmp.set(Color.FOREST);
				break;
			case TileType_Rock:
				tmp.set(Color.GRAY);
				break;
			}
			int diff = currentLevel - level;
			tmp.mul(1 - colorSclPerStep * diff);
			renderer.setColor(tmp);

			renderer.rect(x, y, 1, 1);

			renderer.setColor(Color.BLACK);
			renderer.rect(x, y, outlineSize, 1);
			renderer.rect(x, y, 1, outlineSize);
			renderer.rect(x + 1 - outlineSize, y, outlineSize, 1);
			renderer.rect(x, y + 1 - outlineSize, 1, outlineSize);
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, LayeredTest.class);
	}
}

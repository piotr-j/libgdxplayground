package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Simple trail behind touch drag
 * A lot of stuff left for optimizations
 */
public class InfiniteWorldTest extends BaseScreen {
	private static final String TAG = InfiniteWorldTest.class.getSimpleName();
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	private final static float WORLD_CHUNK_SIZE = 4;
	private OrthographicCamera camera;
	private ExtendViewport viewport;
	private ShapeRenderer shapes;

	public InfiniteWorldTest (GameReset game) {
		// ignore this
		super(game);
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);
		shapes = new ShapeRenderer();
		float cx = VP_WIDTH/2;
		float cy = VP_HEIGHT/2;
		chunks.add(new WorldChunk(cx, cy, Color.RED));
		chunks.add(new WorldChunk(cx- WORLD_CHUNK_SIZE, cy, Color.GREEN));
		chunks.add(new WorldChunk(cx, cy- WORLD_CHUNK_SIZE, Color.BLUE));
		chunks.add(new WorldChunk(cx- WORLD_CHUNK_SIZE, cy- WORLD_CHUNK_SIZE, Color.WHITE));
		updateWorldBounds();
	}

	private void updateWorldBounds () {
		worldBounds.set(chunks.first().bounds);
		for (WorldChunk chunk : chunks) {
			worldBounds.merge(chunk.bounds);
		}
	}

	private Array<WorldChunk> chunks = new Array<>();
	private Rectangle viewBounds = new Rectangle();
	private Rectangle worldBounds = new Rectangle();
	private final float playerSpeed = 5;
	private final float viewSizeX = 3;
	private final float viewSizeY = 2;
	private Vector2 playerPos = new Vector2(VP_WIDTH/2, VP_HEIGHT/2);
	private int px;
	private int py;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(clear.r, clear.g, clear.b, clear.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		// setPosition player with WASD or arrows
		if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
			playerPos.y += playerSpeed * delta;
		} else if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			playerPos.y -= playerSpeed * delta;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			playerPos.x -= playerSpeed * delta;
		} else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			playerPos.x += playerSpeed * delta;
		}
		// update view to match players position
		viewBounds.set(playerPos.x - viewSizeX/2, playerPos.y - viewSizeY/2, viewSizeX, viewSizeY);
		px = (int)(playerPos.x / WORLD_CHUNK_SIZE);
		py = (int)(playerPos.y / WORLD_CHUNK_SIZE);
		if (playerPos.x < 0) px--;
		if (playerPos.y < 0) py--;

		// check if we need to move chunks around
		if (!worldBounds.contains(viewBounds)) {
			Gdx.app.log(TAG, "Player x = " + px + ", y = " + py);
			// player is near the edge of the world
			if (worldBounds.x > viewBounds.x) {
				// over left edge, setPosition stuff from the right to the left
				Gdx.app.log(TAG, "LEFT");
				for (WorldChunk chunk : chunks) {
					if (chunk.x > px) {
						chunk.setPosition(chunk.bounds.x - WORLD_CHUNK_SIZE * 2, chunk.bounds.y);
					}
				}
			} else if (worldBounds.x + worldBounds.width < viewBounds.x + viewBounds.width) {
				// over left edge, setPosition stuff from the left to the right
				Gdx.app.log(TAG, "RIGHT");
				for (WorldChunk chunk : chunks) {
					if (chunk.x < px) {
						chunk.setPosition(chunk.bounds.x + WORLD_CHUNK_SIZE * 2, chunk.bounds.y);
					}
				}
			}
			if (worldBounds.y > viewBounds.y) {
				// over bottom edge, setPosition stuff from the top to the bottom
				Gdx.app.log(TAG, "BOTTOM");
				for (WorldChunk chunk : chunks) {
					if (chunk.y >= py) {
						chunk.setPosition(chunk.bounds.x, chunk.bounds.y - WORLD_CHUNK_SIZE * 2);
					}
				}
			} else if (worldBounds.y + worldBounds.height < viewBounds.y + viewBounds.height) {
				// over left edge, setPosition stuff from the bottom to the top
				Gdx.app.log(TAG, "TOP");
				for (WorldChunk chunk : chunks) {
					if (chunk.y < py -1) {
						chunk.setPosition(chunk.bounds.x, chunk.bounds.y + WORLD_CHUNK_SIZE * 2);
					}
				}
			}

			updateWorldBounds();
		}

		// draw stuff
		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);

		float cx = VP_WIDTH/2;
		float cy = VP_HEIGHT/2;
		for (int x = -5; x < 5; x++) {
			for (int y = -3; y < 3; y++) {
				float wx = cx + x * WORLD_CHUNK_SIZE;
				float wy = cy + y * WORLD_CHUNK_SIZE;
				float c = .25f;
				if (x % 2 == 0) {
					if (y % 2 == 0) {
						shapes.setColor(c, 0, 0, 1);
					} else {
						shapes.setColor(0, 0, c, 1);
					}
				} else {
					if (y % 2 == 0) {
						shapes.setColor(0, c, 0, 1);
					} else {
						shapes.setColor(c, c, c, 1);
					}
				}
//				shapes.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), .5f);
				shapes.rect(wx, wy, WORLD_CHUNK_SIZE, WORLD_CHUNK_SIZE);
			}
		}


		for (WorldChunk chunk : chunks) {
			shapes.setColor(chunk.color);
			Rectangle bounds = chunk.bounds;
			shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		shapes.setColor(Color.GRAY);
		shapes.circle(playerPos.x, playerPos.y, .25f, 16);
		shapes.end();

		shapes.begin(ShapeRenderer.ShapeType.Line);
		shapes.setColor(Color.CYAN);
		shapes.rect(viewBounds.x, viewBounds.y, viewBounds.width, viewBounds.height);
		shapes.setColor(Color.MAGENTA);
		shapes.rect(worldBounds.x, worldBounds.y, worldBounds.width, worldBounds.height);
		shapes.end();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		viewport.update(width, height, true);
	}

	@Override public void dispose () {
		super.dispose();
		shapes.dispose();
	}

	private static class WorldChunk {
		// position on the chunk grid
		public int x, y;
		public Rectangle bounds = new Rectangle(0, 0, WORLD_CHUNK_SIZE, WORLD_CHUNK_SIZE);
		public Color color = new Color(Color.WHITE);

		WorldChunk (float x, float y, Color color) {
			this.color.set(color);
			setPosition(x, y);
		}

		void setPosition (float x, float y) {
			bounds.setPosition(x, y);
			this.x = (int)(x / WORLD_CHUNK_SIZE);
			this.y = (int)(y / WORLD_CHUNK_SIZE);
			if (x < 0) this.x--;
			if (y < 0) this.y--;
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, InfiniteWorldTest.class);
	}
}

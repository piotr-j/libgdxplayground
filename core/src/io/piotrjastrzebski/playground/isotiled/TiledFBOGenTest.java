package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.badlogic.gdx.utils.async.ThreadUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.tiledgentest.OpenNoise;

import java.util.Random;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledFBOGenTest extends BaseScreen {
	protected static final String TAG = TiledFBOGenTest.class.getSimpleName();
	public final static float SCALE = 64f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	OrthogonalTiledMapRenderer mapRenderer;
	int mapTilesInFboTile = 8;
	FBOTile[] fboTiles;
	int width = 50;
	int height = 50;

	FrameBuffer noiseFBO;
	TextureRegion noiseRegion;
	AsyncExecutor executor;
	AsyncResult<AsyncNoiseRegion> noiseRegionResult;

	long seed = 1463323526411L;

	public TiledFBOGenTest (final GameReset game) {
		super(game);
		// fields in super class
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		// centre of the map
		gameCamera.position.set(50, 50, 0);
		gameCamera.update();
		guiCamera = new OrthographicCamera();
		guiViewport = new ScreenViewport(guiCamera);

		TiledMap map = new TmxMapLoader().load("tiled/simple.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE * 2, batch);
		TiledMapTileLayer base = (TiledMapTileLayer)map.getLayers().get(0);
		width = base.getWidth();
		height = base.getHeight();
		OpenNoise noise = new OpenNoise((int)(SCALE * 4), .75d, seed);

		executor = new AsyncExecutor(4);
		noiseRegionResult = executor.submit(new AsyncNoiseRegion(noise, width * 19, height * 10));

		noiseFBO = new FrameBuffer(Pixmap.Format.RGB888, (int)(width * SCALE), (int)(height * SCALE), false);

		// we know how big the map is
		// TODO use limited amount of tiles
		int fboTileWidth = MathUtils.ceil(width / (float)mapTilesInFboTile);
		int fboTileHeight = MathUtils.ceil(height / (float) mapTilesInFboTile);
		fboTiles = new FBOTile[fboTileWidth * fboTileHeight];
		for (int x = 0; x < fboTileWidth; x++) {
			for (int y = 0; y < fboTileHeight; y++) {
				int index = index(x, y, fboTileWidth);
				FBOTile tile = new FBOTile(x * mapTilesInFboTile, y * mapTilesInFboTile, mapTilesInFboTile, base, renderer, noise, executor);
				fboTiles[index] = tile;
			}
		}

		multiplexer.addProcessor(
			new GestureDetector(new GestureDetector.GestureAdapter(){
				Vector3 start = new Vector3();
				@Override public boolean touchDown (float x, float y, int pointer, int button) {
					gameCamera.unproject(start.set(x, y, 0));
					return false;
				}

				@Override public boolean pan (float x, float y, float deltaX, float deltaY) {
					gameCamera.unproject(temp.set(x, y, 0));
					gameCamera.position.add(start.x - temp.x, start.y - temp.y, 0);
					gameCamera.update();
					return true;
				}}));
	}

	protected static int index (int x, int y, int width) {
		return x + y * width;
	}

	protected static int x (int index, int width) {
		return index / width;
	}

	protected static int y (int index, int width) {
		return index % width;
	}

	protected static class FBOTile implements Disposable {
		protected final int x;
		protected final int y;
		protected final int size;
		protected final TiledMapTileLayer base;
		protected final ShapeRenderer renderer;
		protected final OpenNoise noise;
		protected final int apron;
		protected FrameBuffer fbo;
		protected TextureRegion region;
		protected AsyncResult<AsyncFBOTileBuild> asyncResult;
		protected AsyncExecutor executor;
		protected boolean load;
		protected boolean loaded;

		public FBOTile (int x, int y, int size, TiledMapTileLayer base, ShapeRenderer renderer, OpenNoise noise,
			AsyncExecutor executor) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.base = base;
			this.renderer = renderer;
			this.noise = noise;
			this.executor = executor;
			int fboSize = (int)(size * SCALE);
			fbo = new FrameBuffer(Pixmap.Format.RGBA8888, fboSize, fboSize, false);
			region = new TextureRegion(fbo.getColorBufferTexture());
			region.flip(false, true);
			region.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

			apron = size/3;
		}


		public void draw (SpriteBatch batch) {
			if (loaded) {
				batch.draw(region, x, y, size, size);
			} else {
				load = true;
			}
		}
		AsyncFBOTileBuild task;
		public void update(){
			if (!loaded && load) {
				if (asyncResult == null) {
					task = new AsyncFBOTileBuild(this);
					asyncResult = executor.submit(task);
				} else if(asyncResult.isDone()) {
					AsyncFBOTileBuild result = asyncResult.get();
					fbo.begin();
					Gdx.gl.glClearColor(.75f, 0, .75f, 1);
					Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
					Gdx.gl.glEnable(GL20.GL_BLEND);
					Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

					renderer.getProjectionMatrix().setToOrtho2D(0, 0, size, size);
					// gotta call that so it will be applied
					renderer.updateMatrices();
					renderer.begin(ShapeRenderer.ShapeType.Filled);
					result.render(renderer);
					renderer.end();
					fbo.end();
					loaded = true;
				}
			}
		}

		@Override public void dispose () {
			fbo.dispose();
		}

		public void hide () {
			if (asyncResult != null && !asyncResult.isDone()) {
				if (task != null) task.cancel();
			}
			fbo.begin();
			Gdx.gl.glClearColor(.75f, 0, .75f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			fbo.end();
			load = false;
			loaded = false;
			asyncResult = null;
		}

		public static class AsyncFBOTileBuild implements AsyncTask<AsyncFBOTileBuild> {

			private FBOTile tile;
			protected Random rng = new Random();
			private IntArray tileIds = new IntArray();
			private FloatArray circles = new FloatArray();
			private int inTileSteps = 5;
			public volatile boolean cancelled;
			public AsyncFBOTileBuild (FBOTile tile) {
				this.tile = tile;

			}

			@Override public AsyncFBOTileBuild call () throws Exception {
				for (int ox = -tile.apron; ox < tile.size + tile.apron; ox++) {
					for (int oy = -tile.apron; oy < tile.size + tile.apron; oy++) {
						if (cancelled) return this;
						TiledMapTileLayer.Cell cell = tile.base.getCell(tile.x + ox, tile.y + oy);
						if (cell == null)
							continue;
						TiledMapTile tt = cell.getTile();
						tileIds.add(tt.getId());
						long seed = (tile.x + ox) * 104717 + (tile.y + oy);
						rng.setSeed(seed);
						for (int tx = 0; tx < inTileSteps; tx++) {
							for (int ty = 0; ty < inTileSteps; ty++) {
								float fx = ox + rng.nextFloat() / inTileSteps + tx / (float)inTileSteps;
								float fy = oy + rng.nextFloat() / inTileSteps + ty / (float)inTileSteps;
								float radius = .25f + rng.nextFloat() * 0.25f / 3;
								float nv = (float)(.5d + tile.noise.getNoise(tile.x + fx, tile.y + fy) / 2d);
								nv = MathUtils.clamp(nv, 0, 1);
								circles.add(nv);
								circles.add(fx);
								circles.add(fy);
								circles.add(radius);
							}
						}
					}
				}
				// artificial slow down
				Thread.sleep(250);
				return this;
			}

			public void render (ShapeRenderer renderer) {
				if (cancelled) return;
				int steps = inTileSteps * inTileSteps;
				for (int i = 0; i < tileIds.size; i++) {
					Color color = getTileColor(tileIds.get(i));
					for (int j = 0; j < steps; j++) {
						// tile id * number of steps in each tile * values per tile
						int offset = i * steps * 4 + j * 4;
						float nv = circles.get(offset);
						renderer.setColor(color);
						renderer.getColor().a *= nv;
						float fx = circles.get(offset + 1);
						float fy = circles.get(offset + 2);
						float radius = circles.get(offset + 3);
						renderer.circle(fx - radius/2, fy - radius/2, radius, 16);
					}
				}
			}

			public void cancel () {
				cancelled = true;
			}
		}
	}

	protected static Color getTileColor (int tileId) {
		switch (tileId) {
		case 1: return Color.NAVY;
		case 2: return Color.BLUE;
		case 3: return Color.YELLOW;
		case 4: return Color.GREEN;
		case 5: return Color.GRAY;
		case 6: return Color.DARK_GRAY;
		case 7: return Color.ORANGE;
		case 8: return Color.BROWN;
		}
		return Color.WHITE;
	}

	private Rectangle bounds = new Rectangle();
	boolean drawNoise = false;
	@Override public void render (float delta) {
		super.render(delta);

		if (noiseRegionResult != null && noiseRegionResult.isDone()) {
			AsyncNoiseRegion result = noiseRegionResult.get();
			noiseRegion = new TextureRegion(noiseFBO.getColorBufferTexture());
			noiseRegion.flip(false, true);
			noiseFBO.begin();
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			renderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
			// gotta call that so it will be applied
			renderer.updateMatrices();
			renderer.begin(ShapeRenderer.ShapeType.Filled);
			// thats a lot of pixels!
			for (int x = 0; x < width * 10; x++) {
				for (int y = 0; y < height * 10; y++) {
					float value = result.get(x, y);
					renderer.setColor(value, value, value, 1);
					renderer.circle(x/10f -0.5f, y/10f -.05f, .1f, 8);
				}
			}
			renderer.end();
			noiseFBO.end();
			noiseRegion.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
			noiseRegionResult = null;
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			drawNoise = !drawNoise;
		}

		bounds.set(
			gameCamera.position.x - gameCamera.viewportWidth/2 * gameCamera.zoom,
			gameCamera.position.y - gameCamera.viewportHeight/2 * gameCamera.zoom,
			gameCamera.viewportWidth * gameCamera.zoom,
			gameCamera.viewportHeight * gameCamera.zoom
		);

//		mapRenderer.setView(gameCamera);
//		mapRenderer.render();

		for (FBOTile tile : fboTiles) {
			tile.update();
		}

		batch.setProjectionMatrix(gameCamera.combined);
		batch.enableBlending();
		batch.begin();
		if (drawNoise) {
			if (noiseRegion != null) {
				batch.draw(noiseRegion, 0, 0, width, height);
			}
		} else {
			for (FBOTile tile : fboTiles) {
				if (bounds.x < tile.x + tile.size && bounds.x + bounds.width > tile.x && bounds.y < tile.y + tile.size
					&& bounds.y + bounds.height > tile.y) {
					tile.draw(batch);
				} else {
					tile.hide();
				}
			}
		}
		batch.end();

	}

	@Override public boolean scrolled (int amount) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom * 0.1f * amount , 0.1f, 4f);
		gameCamera.update();
		return true;
	}

	@Override public void resize (int width, int height) {
		gameViewport.update(width, height, false);
		guiViewport.update(width, height, true);
	}

	@Override public void dispose () {
		super.dispose();
		for (FBOTile tile : fboTiles) {
			tile.dispose();
		}
		noiseFBO.dispose();
	}

	public static class AsyncNoiseRegion implements AsyncTask<AsyncNoiseRegion> {

		private final OpenNoise noise;
		private final int width;
		private final int height;
		private final float[] result;

		public AsyncNoiseRegion (OpenNoise noise, int width, int height) {
			this.noise = noise;
			this.width = width;
			this.height = height;
			result = new float[width * height];
		}

		@Override public AsyncNoiseRegion call () throws Exception {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					result[x + y * width] = (float)(.25d + noise.getNoise(x/10f, y/10f) / 2d);
				}
			}
			return this;
		}

		public float get(int x, int y) {
			return result[x + y * width];
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, TiledFBOGenTest.class);
	}
}

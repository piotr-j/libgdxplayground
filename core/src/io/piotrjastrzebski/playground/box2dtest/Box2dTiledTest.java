package io.piotrjastrzebski.playground.box2dtest;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 31/07/15.
 */
public class Box2dTiledTest extends BaseScreen {
	public final static float SCALE = 32f; // size of single tile in pixels
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280/SCALE;
	public final static float VP_HEIGHT = 720/SCALE;

	private final Texture box;
	private final OrthogonalTiledMapRenderer mapRenderer;

	World world;
	Array<Box> boxes = new Array<>();
	Box2DDebugRenderer debugRenderer;
	boolean debugDraw = true;
	private int moveVert;
	private int moveHor;

	public Box2dTiledTest (GameReset game) {
		super(game);
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		debugRenderer = new Box2DDebugRenderer();
		world = new World(new Vector2(0, -10), true);
		box = new Texture("badlogic.jpg");

		TiledMap map = new TmxMapLoader().load("tiled/simple.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE, batch);

		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
		for (int x = 0; x < layer.getWidth(); x++) {
			for (int y = 0; y < layer.getHeight(); y++) {
				TiledMapTileLayer.Cell cell = layer.getCell(x, y);
				TiledMapTile tile = cell.getTile();
				int id = tile.getId();
				// you need to know the ids, they start at 1
				// object layer can be used as well
				if (id == 3) {
					// sand
					createBox(x, y, box);
				} else if (id == 7) {
					// lava
					createBox(x, y, box);
				}
			}
		}

		// center on map, map is 100x100
		gameCamera.position.set(50, 50, 0);
	}

	private void createBox (float x, float y, Texture texture) {
		Box box = new Box(x, y, 0, texture);

		BodyDef def = new BodyDef();
		// offset so center is in center of the tile
		def.position.set(x + .5f, y + .5f);
		def.type = BodyDef.BodyType.StaticBody;
		box.body = world.createBody(def);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(box.width / 2, box.height / 2);
		box.body.createFixture(shape, 1);
		shape.dispose();

		boxes.add(box);
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.step(1f / 60f, 6, 4);

		for (Box box : boxes) {
			box.update();
		}
		if (moveVert > 0) {
			gameCamera.position.y += 10 * delta;
		} else if (moveVert < 0) {
			gameCamera.position.y -= 10 * delta;
		}
		if (moveHor > 0) {
			gameCamera.position.x += 10 * delta;
		} else if (moveHor < 0) {
			gameCamera.position.x -= 10 * delta;
		}
		gameCamera.update();

		draw();
	}

	private void draw () {
		mapRenderer.setView(gameCamera);
		mapRenderer.render();
		if (debugDraw) {
			debugRenderer.render(world, gameCamera.combined);
		}
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Box box : boxes) {
			box.draw(batch);
		}
		batch.end();
	}

	private class Box {
		public Body body;
		public Texture texture;
		public float x;
		public float y;
		public float rot;
		private float width;
		private float height;
		private int srcWidth;
		private int srcHeight;

		public Box (float x, float y, float rotation, Texture texture) {
			this.x = x;
			this.y = y;
			this.rot = rotation;
			this.texture = texture;
			srcWidth = texture.getWidth();
			width = .5f;
			srcHeight = texture.getHeight();
			height = .5f;
		}

		public void update () {
			Vector2 position = body.getPosition();
			x = position.x;
			y = position.y;
			rot = body.getAngle() * MathUtils.radiansToDegrees;
		}

		public void draw (Batch batch) {
			batch.draw(texture, x - width / 2, y - height / 2, width / 2, height / 2, width, height, 1, 1, rot, 0, 0, srcWidth,
				srcHeight, false, false);
		}
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.Z:
			debugDraw = !debugDraw;
			break;
		case Input.Keys.UP:
		case Input.Keys.W:
			moveVert++;
			break;
		case Input.Keys.DOWN:
		case Input.Keys.S:
			moveVert--;
			break;
		case Input.Keys.LEFT:
		case Input.Keys.A:
			moveHor--;
			break;
		case Input.Keys.RIGHT:
		case Input.Keys.D:
			moveHor++;
			break;
		}
		return super.keyDown(keycode);
	}

	@Override public boolean keyUp (int keycode) {
		switch (keycode) {
		case Input.Keys.UP:
		case Input.Keys.W:
			moveVert--;
			break;
		case Input.Keys.DOWN:
		case Input.Keys.S:
			moveVert++;
			break;
		case Input.Keys.LEFT:
		case Input.Keys.A:
			moveHor++;
			break;
		case Input.Keys.RIGHT:
		case Input.Keys.D:
			moveHor--;
			break;
		}
		return super.keyDown(keycode);
	}

	@Override public void dispose () {
		super.dispose();
		box.dispose();
	}
}

package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Not quite raycast
 */
public class TiledRayCastTest extends BaseScreen {
	public final static float SCALE = 32f; // size of single tile in pixels
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280/SCALE;
	public final static float VP_HEIGHT = 720/SCALE;

	private final Texture box;
	private final OrthogonalTiledMapRenderer mapRenderer;
	private final ShapeDrawer drawer;

	Array<Collider> colliders = new Array<>();
	Array<Collider> intersecting = new Array<>();
	boolean debugDraw = true;
	private int moveVert;
	private int moveHor;

	private Vector2 clickStart = new Vector2();
	private Vector2 clickEnd = new Vector2();
	private Vector2 segment = new Vector2();

	public TiledRayCastTest(GameReset game) {
		super(game);
		Pixmap pixmap = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.CLEAR);
		pixmap.fill();
		pixmap.setColor(Color.WHITE);
		pixmap.fillRectangle(1, 1, 1, 1);
		drawer = new ShapeDrawer(batch, new TextureRegion(new Texture(pixmap), 1, 1, 1, 1));
		pixmap.dispose();

		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);

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
					createRect(x, y, box);
				} else if (id == 7) {
					// lava
					createPoly(x, y, box);
				}
			}
		}

		// center on map, map is 100x100
		gameCamera.position.set(50, 50, 0);
	}

	private void createRect(float x, float y, Texture texture) {
		RectCollider collider = new RectCollider(texture, x, y, 0);

		colliders.add(collider);
	}

	private void createPoly(float x, float y, Texture texture) {
		PolygonCollider collider = new PolygonCollider(texture, x, y, 0);

		colliders.add(collider);
	}

	@Override public void render (float delta) {
		super.render(delta);

		for (Collider box : colliders) {
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
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Collider box : colliders) {
			box.draw(batch);
		}

		if (debugDraw) {
			for (Collider box : colliders) {
				box.draw(drawer);
			}
			// check overlaps
			drawer.line(clickStart.x, clickStart.y, clickEnd.x, clickEnd.y, .05f, Color.RED, Color.GREEN);

			intersecting.clear();
			// this is very naive as we simply check all
			// something smarter should filter this first, like quad tree
			for (Collider collider : colliders) {
				if (collider.intersects(clickStart, clickEnd)) {
					intersecting.add(collider);
				}
			}

			colliders.sort((o1, o2) -> {
				return Float.compare(o1.distanceToStart, o2.distanceToStart);
			});

			for (int i = 0; i < intersecting.size; i++) {
				if (i == 0) {
					shapes.setColor(Color.RED);
				} else {
					shapes.setColor(Color.GREEN);
				}
				Collider collider = intersecting.get(i);
				shapes.circle(collider.x + .5f, collider.y + .5f, .4f, .1f);
			}
		}
		batch.end();
	}

	private static class Collider {
		public Texture texture;
		public float x;
		public float y;
		public float rot;
		public float distanceToStart;

		public Collider(Texture texture, float x, float y, float rot) {
			this.texture = texture;
			this.x = x;
			this.y = y;
			this.rot = rot;
		}

		public void update () {

		}

		public void draw (ShapeDrawer drawer) {

		}

		public void draw (Batch batch) {

		}

		public boolean intersects(Vector2 from, Vector2 to) {
			return false;
		}
	}

	private static class RectCollider extends Collider {
		private float width;
		private float height;
		private int srcWidth;
		private int srcHeight;

		private Rectangle rectangle;

		public RectCollider(Texture texture, float x, float y, float rotation) {
			super(texture, x, y, rotation);

			srcWidth = texture.getWidth();
			width = .5f;
			srcHeight = texture.getHeight();
			height = .5f;
			rectangle = new Rectangle(x + .25f, y + .25f, width, height);
		}

		public void update () {

		}

		public void draw (ShapeDrawer drawer) {
			drawer.setColor(Color.CYAN);
			drawer.rectangle(rectangle, .1f);
		}

		public void draw (Batch batch) {
			batch.draw(texture, x + width / 2, y + height / 2, width / 2, height / 2, width, height, 1, 1, rot, 0, 0, srcWidth,
				srcHeight, false, false);
		}

		@Override
		public boolean intersects(Vector2 from, Vector2 to) {
			if (!Intersector.intersectSegmentRectangle(from, to, rectangle)) return false;
			distanceToStart = from.dst(x, y);
			return true;
		}
	}

	private static class PolygonCollider extends Collider {
		private float width;
		private float height;
		private int srcWidth;
		private int srcHeight;
		private Polygon polygon;

		public PolygonCollider(Texture texture, float x, float y, float rotation) {
			super(texture, x, y, rotation);
			srcWidth = texture.getWidth();
			width = .5f;
			srcHeight = texture.getHeight();
			height = .5f;
			// just a rect
			polygon = new Polygon(new float[]{
					0, 0,
					0,  height,
					width, height,
					width, 0
			});
			polygon.setPosition(x + .25f, y + .25f);
		}

		public void update () {

		}

		public void draw (ShapeDrawer drawer) {
			drawer.setColor(Color.MAGENTA);
			drawer.polygon(polygon, .1f);
		}

		public void draw (Batch batch) {
			batch.draw(texture, x + width / 2, y + height / 2, width / 2, height / 2, width, height, 1, 1, rot, 0, 0, srcWidth,
					srcHeight, false, false);
		}

		@Override
		public boolean intersects(Vector2 from, Vector2 to) {
			if (!Intersector.intersectSegmentPolygon(from, to, polygon)) return false;
			distanceToStart = from.dst(x, y);
			return true;
		}
	}

	Vector2 v2 = new Vector2();
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		gameViewport.unproject(v2.set(screenX, screenY));
		clickStart.set(clickEnd);
		clickEnd.set(v2);
		//segment.set(clickEnd.x - clickStart.x, clickEnd.y - clickStart.y);

		return super.touchDown(screenX, screenY, pointer, button);
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


	public static void main (String[] args) {
		PlaygroundGame.start(args, TiledRayCastTest.class);
	}
}

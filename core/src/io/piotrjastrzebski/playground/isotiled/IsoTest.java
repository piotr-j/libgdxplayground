package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class IsoTest extends BaseScreen {
	IsometricTiledMapRenderer mapRenderer;
	Matrix4 isoTransform;
	Matrix4 invIsotransform;
	Polygon tile;
	TiledMapTileLayer mapLayer;
	public IsoTest (GameReset game) {
		super(game);
		TiledMap map = new TmxMapLoader().load("tiled/iso2.tmx");
		mapRenderer = new IsometricTiledMapRenderer(map, INV_SCALE, batch);
		mapLayer = (TiledMapTileLayer)map.getLayers().get(0);


		tile = new Polygon();
		tile.setVertices(new float[]{0, 0.5f, 1f, 0f, 2f, 0.5f, 1f, 1f});

		// create the isometric transform
		isoTransform = new Matrix4();
		isoTransform.idt();

		// isoTransform.translate(0, 32, 0);
		float sqrt = (float)(Math.sqrt(2.0)/2.0);
		isoTransform.scale(sqrt, sqrt / 2.0f, 1.0f);
		isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);

		// ... and the inverse matrix
		invIsotransform = new Matrix4(isoTransform);
		invIsotransform.inv();

		gameCamera.position.x += 10;
		gameCamera.update();
	}

	@Override public void render (float delta) {
		super.render(delta);
		mapRenderer.setView(gameCamera);
		mapRenderer.render();

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.circle(cs.x, cs.y, 0.1f, 16);
		renderer.setColor(Color.CYAN);

		int x = (int)((isoPos.y)/2f - .5f);
		int y = (int)((isoPos.x)/2f + .5f);
		TiledMapTileLayer.Cell cell = mapLayer.getCell(x, y);
		if (cell != null) {
			Gdx.app.log("x", x + ", " + y);
			float x1 = y + x;
			float y2 = x * .5f - y * .5f;

			tile.setPosition(x1, y2);
			renderer.setColor(Color.RED);
			renderer.polygon(tile.getTransformedVertices());
		}

		renderer.end();
	}

	private Vector3 isoPos = new Vector3();
	private Vector3 translateWorldToIso (Vector2 vec) {
		isoPos.set(vec.x, vec.y, 0);
		isoPos.mul(invIsotransform);
		return isoPos;
	}

	@Override public void dispose () {
		super.dispose();
		mapRenderer.getMap().dispose();
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		translateWorldToIso(cs);
		return super.mouseMoved(screenX, screenY);
	}

	Vector2 cs = new Vector2();
	Vector3 temp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		// dumb, dont do this!
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		temp.sub(gameCamera.position).scl(0.1f);
		gameCamera.position.add(temp.x, temp.y, 0);
		gameCamera.update();
		return true;
	}

	@Override public boolean scrolled (int amount) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom * amount * 0.1f, 0.1f, 3f);
		gameCamera.update();
		return true;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, IsoTest.class);
	}
}

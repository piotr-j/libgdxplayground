package io.piotrjastrzebski.playground.fogofwar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class FogOfWarTest extends BaseScreen {
	TiledMap map;
	OrthogonalTiledMapRenderer mapRenderer;

	FogOfWar fogOfWar;
	Texture lightTex;
	Texture unitTex;
	Array<Unit> units = new Array<>();
	Array<FogOfWar.LoSObject> unitsLoS = new Array<>();
	Unit selectedUnit;
	Vector2 center = new Vector2();

	public FogOfWarTest (GameReset game) {
		super(game);

		// we will use tiled map as background
		map = new TmxMapLoader().load("tiled/simple.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE, batch);
		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
		// center on the map
		center.set(layer.getWidth()/2, layer.getHeight()/2);
		gameCamera.position.set(center.x, center.y, 0);

		// 128x128
		lightTex = new Texture("fogofwar/los.png");
		// 32x32
		unitTex = new Texture("box2d/circle32.png");


		fogOfWar = new FogOfWar(13, 13, 256, 256);

		units.add(selectedUnit = new Unit(unitTex, lightTex, 1, 1).setPos(center.x, center.y));

		unitsLoS.addAll(units);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		gameCamera.update();

		mapRenderer.setView(gameCamera);
		mapRenderer.render();

		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Unit unit : units) {
			unit.draw(batch);
		}
		batch.end();

		fogOfWar.draw(batch, gameCamera, unitsLoS);
	}

	public static class Unit implements FogOfWar.LoSObject {
		public TextureRegion view;
		public TextureRegion lineOfSight;
		public float width, height;
		public Vector2 pos = new Vector2();

		public Unit (Texture viewTex, Texture losTex, float width, float height) {
			this.view = new TextureRegion(viewTex);
			this.lineOfSight = new TextureRegion(losTex);
			this.width = width;
			this.height = height;
		}

		public Unit setPos(float x, float y) {
			pos.set(x, y);
			return this;
		}

		public void draw(Batch batch) {
			batch.draw(view, pos.x - width/2, pos.y - height/2, width, height);
		}

		public void drawLoS(Batch batch) {
			batch.draw(lineOfSight, pos.x - width*2, pos.y - height*2, width*4, height*4);
		}
	}

	public static class FogOfWar implements Disposable {
		// TODO
		// do we need to interpolate movement stuff?
		// low res with blur?
		// persistence
		// darken fow after some time
		public float alpha = 0.8f;
		FoWTile[] tiles;

		public FogOfWar (int tilesX, int tilesY, int tileWidth, int tileHeight) {
			tiles = new FoWTile[tilesX * tilesY];
			for (int y = 0; y < tilesY; y++) {
				for (int x = 0; x < tilesX; x++) {
					int id = x + y * tilesX;
					tiles[id] = new FoWTile(id, x, y, tileWidth, tileHeight, INV_SCALE);
				}
			}
		}

		public void draw (Batch batch, OrthographicCamera gameCamera, Array<LoSObject> loSObjects) {
			// NOTE easy win, draw and update only whats visible
//			batch.disableBlending();
			for (FoWTile tile : tiles) {
				tile.update(batch, loSObjects);
			}
//			batch.enableBlending();
			batch.setProjectionMatrix(gameCamera.combined);
			batch.setColor(1, 1, 1, alpha);
			batch.begin();
			for (FoWTile tile : tiles) {
				tile.draw(batch);
			}
			batch.end();
		}

		@Override public void dispose () {
			for (FoWTile tile : tiles) {
				tile.dispose();
			}
		}

		public class FoWTile implements Disposable {
			public FrameBuffer fbo;
			public TextureRegion fboReg;
			public int id;
			public float x, y;
			public float width, height;
			public Matrix4 projection;

			public FoWTile (int id, int x, int y, int width, int height, float scale) {
				this.id = id;
				this.x = x * width * scale;
				this.y = y * height * scale;
				this.width = width * scale;
				this.height = height * scale;

				projection = new Matrix4();
				projection.setToOrtho2D(this.x, this.y, this.width, this.height);

				// TODO correct format and blend func
				fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
				fbo.begin();
				Gdx.gl.glClearColor(0, 0, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				fbo.end();

				fboReg = new TextureRegion(fbo.getColorBufferTexture());
				fboReg.flip(false, true);
			}

			public void update (Batch batch, Array<LoSObject> loSObjects) {
				fbo.begin();
				batch.setProjectionMatrix(projection);
				batch.begin();
				for (LoSObject loSObject : loSObjects) {
					loSObject.drawLoS(batch);
				}
				batch.end();
				fbo.end();
			}

			public void draw (Batch batch) {
				batch.draw(fboReg, x, y, width, height);
			}

			@Override public void dispose () {
				if (fbo != null) fbo.dispose();
			}
		}

		public interface LoSObject {
			void drawLoS(Batch batch);
		}
	}

	Vector3 temp = new Vector3();
	int dragButton;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		dragButton = button;
		updateTouch(dragButton);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		updateTouch(dragButton);
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		updateTouch(button);
		dragButton = -1;
		return true;
	}

	private void updateTouch (int button) {
		if (button == Input.Buttons.LEFT) {
			selectedUnit.setPos(temp.x, temp.y);
		} else if (button == Input.Buttons.RIGHT) {
			temp.sub(gameCamera.position).scl(0.1f);
			gameCamera.position.add(temp.x, temp.y, 0);
		}
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.SPACE:
			gameCamera.position.set(center.x, center.y, 0);
			selectedUnit.setPos(center.x, center.y);
		}
		return false;
	}

	@Override public boolean scrolled (int amount) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom *= (1f + .15f * amount), 0.1f, 3.0f);
		return super.scrolled(amount);
	}

	@Override public void dispose () {
		super.dispose();
		lightTex.dispose();
		unitTex.dispose();
		fogOfWar.dispose();
	}
}

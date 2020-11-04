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
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

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

	private Array<Blend> blends;

	public FogOfWarTest (GameReset game) {
		super(game);

		// we will use tiled map as background
		map = new TmxMapLoader().load("tiled/simple.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE, batch);
		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
		// center on the map
		center.set(layer.getWidth()/2, layer.getHeight()/2);
		gameCamera.position.set(center.x, center.y, 0);


		blends = new Array<>();
		blends.add(new Blend(GL20.GL_ONE, "GL_ONE"));
		blends.add(new Blend(GL20.GL_ZERO, "GL_ZERO"));
		blends.add(new Blend(GL20.GL_SRC_COLOR, "GL_SRC_COLOR"));
		blends.add(new Blend(GL20.GL_ONE_MINUS_SRC_COLOR, "GL_ONE_MINUS_SRC_COLOR"));
		blends.add(new Blend(GL20.GL_SRC_ALPHA, "GL_SRC_ALPHA"));
		blends.add(new Blend(GL20.GL_ONE_MINUS_SRC_ALPHA, "GL_ONE_MINUS_SRC_ALPHA"));
		blends.add(new Blend(GL20.GL_DST_ALPHA, "GL_DST_ALPHA"));
		blends.add(new Blend(GL20.GL_ONE_MINUS_DST_ALPHA, "GL_ONE_MINUS_DST_ALPHA"));
		blends.add(new Blend(GL20.GL_DST_COLOR, "GL_DST_COLOR"));
		blends.add(new Blend(GL20.GL_ONE_MINUS_DST_COLOR, "GL_ONE_MINUS_DST_COLOR"));

		// 128x128
		lightTex = new Texture("fogofwar/los3.png");
		// 32x32
		unitTex = new Texture("box2d/circle32.png");


		fogOfWar = new FogOfWar(13, 13, 256, 256);

		units.add(selectedUnit = new Unit(unitTex, lightTex, 1, 1).setPos(center.x, center.y));

		unitsLoS.addAll(units);

		VisWindow window = new VisWindow("Settings");
		window.setPosition(0, 0);

		window.add(createGUI(fogOfWar)).row();
		window.pack();
		root.addActor(window);
	}

	private Actor createGUI (final Blended blended) {
		VisTable content = new VisTable(true);
		{
			content.add(new VisLabel("SrcFunc", "small")).right();
			final VisSelectBox<Blend> sb = new VisSelectBox<>();
			sb.setItems(blends);
			sb.setSelected(blend(blended.srcFunc));
			sb.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					blended.srcFunc(sb.getSelected().func);
				}
			});
			content.add(sb).row();
		}
		{
			content.add(new VisLabel("DstFunc", "small")).right();
			final VisSelectBox<Blend> sb = new VisSelectBox<>();
			sb.setItems(blends);
			sb.setSelected(blend(blended.dstFunc));
			sb.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					blended.dstFunc(sb.getSelected().func);
				}
			});
			content.add(sb).row();
		}
		final VisSelectBox<Blend> sbSrcAlpha = new VisSelectBox<>();
		sbSrcAlpha.setDisabled(!blended.separate);
		final VisSelectBox<Blend> sbDstAlpha = new VisSelectBox<>();
		sbDstAlpha.setDisabled(!blended.separate);
		final VisCheckBox separateCB = new VisCheckBox("Separate?", blended.separate);
		content.add(separateCB).colspan(2).row();
		separateCB.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				blended.separate(separateCB.isChecked());
				sbSrcAlpha.setDisabled(!blended.separate);
				sbDstAlpha.setDisabled(!blended.separate);
			}
		});
		{
			content.add(new VisLabel("SrcAlphaFunc", "small"));
			sbSrcAlpha.setItems(blends);
			sbSrcAlpha.setSelected(blend(blended.srcFuncAlpha));
			sbSrcAlpha.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					blended.srcFuncAlpha(sbSrcAlpha.getSelected().func);
				}
			});
			content.add(sbSrcAlpha).row();
		}
		{
			content.add(new VisLabel("DstAlphaFunc", "small"));
			sbDstAlpha.setItems(blends);
			sbDstAlpha.setSelected(blend(blended.dstFuncAlpha));
			sbDstAlpha.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					blended.dstFuncAlpha(sbDstAlpha.getSelected().func);
				}
			});
			content.add(sbDstAlpha).row();
		}
		return content;
	}

	private Blend blend (int func) {
		for (Blend blend : blends) {
			if (blend.func == func) return blend;
		}
		return null;
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

		stage.act(delta);
		stage.draw();
		batch.setColor(Color.WHITE);
	}

	private static class Unit implements FogOfWar.LoSObject {
		public TextureRegion view;
		public TextureRegion lineOfSight;
		public float width, height;
		public Vector2 pos = new Vector2();
		public Rectangle bounds = new Rectangle();

		public Unit (Texture viewTex, Texture losTex, float width, float height) {
			this.view = new TextureRegion(viewTex);
			this.lineOfSight = new TextureRegion(losTex);
			this.width = width;
			this.height = height;
		}

		public Unit setPos(float x, float y) {
			pos.set(x, y);
			// centered
			bounds.set(x - width/2, y - height/2, width, height);
			return this;
		}

		public void draw(Batch batch) {
			batch.draw(view, pos.x - width/2, pos.y - height/2, width, height);
		}

		public void drawLoS(Batch batch) {
			batch.draw(lineOfSight, pos.x - width * 2, pos.y - height * 2, width * 4, height * 4);
		}

		@Override public Rectangle bounds () {
			return bounds;
		}
	}

	private static class FogOfWar extends Blended implements Disposable {
		// TODO
		// do we need to interpolate movement stuff?
		// low res with blur?
		// persistence
		// darken fow after some time
		public float alpha = 0.8f;
		FoWTile[] tiles;

		public FogOfWar (int tilesX, int tilesY, int tileWidth, int tileHeight) {
			super();
			tiles = new FoWTile[tilesX * tilesY];
			for (int y = 0; y < tilesY; y++) {
				for (int x = 0; x < tilesX; x++) {
					int id = x + y * tilesX;
					tiles[id] = new FoWTile(id, x, y, tileWidth, tileHeight, INV_SCALE);
				}
			}
		}

		@Override void rebuild () {
			for (FoWTile tile : tiles) {
				tile.rebuild();
			}
		}

		public void draw (Batch batch, OrthographicCamera gameCamera, Array<LoSObject> loSObjects) {
			// NOTE easy win, draw and update only whats visible
			if (separate) {
				batch.setBlendFunctionSeparate(srcFunc, dstFunc, srcFuncAlpha, dstFuncAlpha);
			} else {
				batch.setBlendFunction(srcFunc, dstFunc);
			}

			for (FoWTile tile : tiles) {
				tile.update(batch, loSObjects);
			}
			batch.setProjectionMatrix(gameCamera.combined);
			batch.setColor(1, 1, 1, alpha);
			// regular blend for drawing final tiles
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
			public int fboWidth, fboHeight;
			private Rectangle bounds;
			public Matrix4 projection;

			public FoWTile (int id, int x, int y, int width, int height, float scale) {
				this.id = id;
				this.x = x * width * scale;
				this.y = y * height * scale;
				this.width = width * scale;
				this.height = height * scale;
				fboWidth = width;
				fboHeight = height;

				// works quite well for fog of war kind of thing
				srcFunc = GL20.GL_ZERO;
				dstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;

				bounds = new Rectangle(x, y, width, height);

				projection = new Matrix4();
				projection.setToOrtho2D(this.x, this.y, this.width, this.height);

				rebuild();

				fboReg = new TextureRegion(fbo.getColorBufferTexture());
				fboReg.flip(false, true);
			}

			void rebuild () {
				if (fbo == null) {
					fbo = new FrameBuffer(Pixmap.Format.RGBA8888, fboWidth, fboHeight, false);
				}
				fbo.begin();
				// here you can setup the background that will be erased
				// we use simple clear for fog of war, for terrain you would draw whatever you need
				Gdx.gl.glClearColor(0, 0, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				fbo.end();
			}

			public void update (Batch batch, Array<LoSObject> loSObjects) {
				// dont start batch unless theres something to draw
				boolean started = false;
				for (LoSObject loSObject : loSObjects) {
					if (bounds.overlaps(loSObject.bounds())) {
						if (!started) {
							started = true;
							fbo.begin();
							batch.setProjectionMatrix(projection);
							batch.begin();
						}
						loSObject.drawLoS(batch);
					}
				}
				if (started) {
					batch.end();
					fbo.end();
				}
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
			Rectangle bounds();
		}
	}

	private static class Blended {
		boolean separate = false;
		int srcFunc = GL20.GL_SRC_ALPHA;
		int dstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
		int srcFuncAlpha = GL20.GL_SRC_ALPHA;
		int dstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

		void rebuild() {

		}

		public void separate (boolean separate) {
			this.separate = separate;
			rebuild();
		}

		public void srcFunc (int func) {
			srcFunc = func;
			rebuild();
		}

		public void dstFunc (int func) {
			dstFunc = func;
			rebuild();
		}

		public void srcFuncAlpha (int func) {
			srcFuncAlpha = func;
			rebuild();
		}

		public void dstFuncAlpha (int func) {
			dstFuncAlpha = func;
			rebuild();
		}
	}

	private static class Blend {
		final int func;
		final String name;

		public Blend (int func, String name) {
			this.func = func;
			this.name = name;
		}

		@Override public String toString () {
			return name;
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Blend blend = (Blend)o;

			return func == blend.func;
		}

		@Override public int hashCode () {
			return func;
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

	@Override public boolean scrolled (float amountX, float amountY) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom *= (1f + .15f * amountX), 0.1f, 3.0f);
		return super.scrolled(amountX, amountY);
	}

	@Override public void dispose () {
		super.dispose();
		lightTex.dispose();
		unitTex.dispose();
		fogOfWar.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, FogOfWarTest.class);
	}
}

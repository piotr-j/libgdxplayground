package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Self contained test for proper touch/mouse handling
 *
 * Created by PiotrJ on 05/10/15.
 */
public class SceneTest extends ApplicationAdapter implements InputProcessor {
	// we will use 32px/unit in world
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	// this is our "target" resolution, not that the window can be any size, it is not bound to this one
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	private OrthographicCamera camera;
	private ExtendViewport viewport;
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private Stage stage;
	private Table root;

	@Override public void create () {
		VisUI.load();
		Drawable white = VisUI.getSkin().getDrawable("white");
		camera = new OrthographicCamera();
		// pick a viewport that suits your thing, ExtendViewport is a good start
		viewport = new ExtendViewport(720, 1280, camera);
		// ShapeRenderer so we can see our touch point
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		stage = new Stage(viewport, batch);
		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.debug();
		Value size = Value.percentWidth(.25f, root);
		Value pad = Value.percentWidth(.05f, root);
		{
			Image image = new Image(white);
			root.add(image).size(size).expand().pad(pad).left().top();
		}
		{
			Image image = new Image(white);
			image.setColor(Color.BLUE);
			root.add(image).size(size).expand().pad(pad).right().top().row();
		}
		{
			Image image = new Image(white);
			image.setColor(Color.GREEN);
			root.add(image).size(size).expand().pad(pad).left().bottom();
		}
		{
			Image image = new Image(white);
			image.setColor(Color.RED);
			root.add(image).size(size).expand().pad(pad).right().bottom();
		}
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
	}

	@Override public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act();
		stage.draw();
		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		shapes.circle(tp.x, tp.y, 0.25f, 16);
		shapes.end();
	}

	Vector3 tp = new Vector3();
	boolean dragging;
	@Override public boolean mouseMoved (int screenX, int screenY) {
		// we can also handle mouse movement without anything pressed
//		camera.unproject(tp.set(screenX, screenY, 0));
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		// ignore if its not left mouse button or first touch pointer
		if (button != Input.Buttons.LEFT || pointer > 0) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		dragging = true;
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (!dragging) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT || pointer > 0) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		dragging = false;
		return true;
	}

	@Override public void resize (int width, int height) {
		// viewport must be updated for it to work properly
		viewport.update(width, height, true);
	}

	@Override public void dispose () {
		// disposable stuff must be disposed
		shapes.dispose();
		batch.dispose();
		VisUI.dispose();
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override
	public boolean scrolled (float amountX, float amountY) {
		return false;
	}

	public static class FitScreenViewport extends Viewport {
		private float unitsPerPixel = 1;
		private int targetWidth;
		private int targetHeight;

		public FitScreenViewport (int targetWidth, int targetHeight, Camera camera) {
			this.targetWidth = targetWidth;
			this.targetHeight = targetHeight;
			setCamera(camera);
		}

		@Override
		public void update (int screenWidth, int screenHeight, boolean centerCamera) {
			Vector2 fit = Scaling.fit.apply(screenWidth, screenHeight, targetWidth, targetHeight);
			int offX = (int)(screenWidth - fit.x)/2;
			int offY = (int)(screenHeight - fit.y)/2;
			int wx = (int)fit.x;
			int wy = (int)fit.y;
			setScreenBounds(offX, offY, wx, wy);
			setWorldSize(wx * unitsPerPixel, wy * unitsPerPixel);
			apply(centerCamera);
		}

		public float getUnitsPerPixel () {
			return unitsPerPixel;
		}

		/** Sets the number of pixels for each world unit. Eg, a scale of 2.5 means there are 2.5 world units for every 1 screen pixel.
		 * Default is 1. */
		public void setUnitsPerPixel (float unitsPerPixel) {
			this.unitsPerPixel = unitsPerPixel;
		}
	}


	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowedMode(1280/2, 720/2);
		PlaygroundGame.start(new SceneTest(), config);
	}
}

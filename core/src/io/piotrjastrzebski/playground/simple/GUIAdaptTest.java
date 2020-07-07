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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight;

/**
 * Self contained test for testing scene2d adaptation for different resolutions and orientations
 *
 * Created by PiotrJ on 05/10/15.
 */
public class GUIAdaptTest extends ApplicationAdapter implements InputProcessor {
	private static final String TAG = GUIAdaptTest.class.getSimpleName();

	// we will use 32px/unit in world
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	// this is our "target" resolution, not that the window can be any size, it is not bound to this one
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	public final static int WIDTH = 1280;
	public final static int HEIGHT = 720;

	private OrthographicCamera camera;
	private Viewport viewport;
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private Stage stage;
	private Table root;

	@Override public void create () {
		camera = new OrthographicCamera();
		// pick a viewport that suits your thing, ExtendViewport is a good start
//		viewport = new ExtendViewport(WIDTH, HEIGHT, camera);
		viewport = new ScreenViewport(camera);
		// ShapeRenderer so we can see our touch point
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		stage = new Stage(viewport, batch);
		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.debug();
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
	}

	private void buildGUI (GUIScale guiScale, Orientation orientation) {
		// NOTE every thing that uses a skin must be recreated here, as scale might have changed
		Gdx.app.log(TAG, "Build gui for scale " + guiScale + " and orientation " + orientation);
		root.clear();
		float scale = guiScale.value;
		Drawable white = VisUI.getSkin().getDrawable("white");
		switch (orientation) {
		case LANDSCAPE: {
			{
				VisWindow window = new VisWindow("Player 1");
				root.add(window).size(percentHeight(.25f, root)).expandY().top().left().pad(8 * scale);
				VisImage image = new VisImage(white);
				image.setColor(Color.RED);
				window.add(image).expand().fill();
			}
			{
				VisWindow playWindow = new VisWindow("Play area");
				root.add(playWindow).expand().fill().pad(8 * scale);
			}
			{
				VisWindow window = new VisWindow("Player 2");
				root.add(window).size(percentHeight(.25f, root)).expandY().bottom().right().pad(8 * scale);
				VisImage image = new VisImage(white);
				image.setColor(Color.GREEN);
				window.add(image).expand().fill();
			}

		} break;
		case PORTRAIT: {
			{
				VisWindow window = new VisWindow("Player 1");
				root.add(window).size(percentHeight(.25f, root)).expand().top().left().pad(8 * scale).row();
				VisImage image = new VisImage(white);
				image.setColor(Color.RED);
				window.add(image).expand().fill();
			}
			{
				VisWindow playWindow = new VisWindow("Play area");
				root.add(playWindow).height(percentHeight(.33f, root)).expand().fillX().pad(8 * scale).row();
			}
			{
				VisWindow window = new VisWindow("Player 2");
				root.add(window).size(percentHeight(.25f, root)).expand().bottom().left().pad(8 * scale).row();
				VisImage image = new VisImage(white);
				image.setColor(Color.GREEN);
				window.add(image).expand().fill();
			}
		}break;
		}
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

	protected enum GUIScale {NONE(1), X1(1), X2(2);
		public final float value;

		GUIScale (float value) {
			this.value = value;
		}
	}
	protected enum Orientation {NONE, LANDSCAPE, PORTRAIT}
	private GUIScale guiScale = GUIScale.NONE;
	private Orientation orientation = Orientation.NONE;
	@Override public void resize (int width, int height) {
		// viewport must be updated for it to work properly
		viewport.update(width, height, true);
		// NOTE probably should happen once, as the actual screen size doesnt change on devices
		boolean rebuildGui = false;
		int w = width>height?width:height;
		int h = width>height?height:width;
		if (w < 1280 * .75f && h < 720  * .75f) {
			if (guiScale != GUIScale.X1) {
				Gdx.app.log(TAG, "change to Low res");
				guiScale = GUIScale.X1;
				if (VisUI.isLoaded()) VisUI.dispose();
				VisUI.load(VisUI.SkinScale.X1);
				rebuildGui = true;
			}
		} else {
			if (guiScale != GUIScale.X2) {
				Gdx.app.log(TAG, "change to high res");
				guiScale = GUIScale.X2;
				if (VisUI.isLoaded()) VisUI.dispose();
				VisUI.load(VisUI.SkinScale.X2);
				rebuildGui = true;
			}
		}
		Orientation newOrientation = width > height?Orientation.LANDSCAPE:Orientation.PORTRAIT;
		if (orientation != newOrientation) {
			orientation = newOrientation;
			rebuildGui = true;
		}
		if (rebuildGui) {
			buildGUI(guiScale, orientation);
		}
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

	@Override public boolean scrolled (int amount) {
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
		config.setWindowedMode(1280 * 3 / 4, 720 * 3 / 4);
		PlaygroundGame.start(new GUIAdaptTest(), config);
	}
}

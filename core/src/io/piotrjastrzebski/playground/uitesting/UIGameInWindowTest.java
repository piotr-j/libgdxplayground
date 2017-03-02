package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.simple.Box2dReflectTest;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class UIGameInWindowTest extends BaseScreen {
	private static final String TAG = UIGameInWindowTest.class.getSimpleName();

	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	public final static float W_SIZE = 100 * INV_SCALE;

	private VisWindow window;
	private VisTable container;
	private OrthographicCamera innerCamera;
	private InnerExtendViewport innerViewport;
	private CustomBox2dReflectTest innerView;

	public UIGameInWindowTest (GameReset game) {
		super(game);
		window = new VisWindow("WTF?");
		window.setSize(300, 300);
		window.centerWindow();
		stage.addActor(window);
		container = new VisTable();
		window.add(container).expand().fill().minSize(100, 100);
		window.setResizable(true);

		innerView = new CustomBox2dReflectTest(game);

		innerCamera = new OrthographicCamera();
		// we modify update a bit for it take account for our screenXY not being 0 always
		innerViewport = new InnerExtendViewport(W_SIZE, W_SIZE, innerCamera);
		innerView.setViewport(innerViewport);

		multiplexer.addProcessor(innerView.getMultiplexer());
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		stage.act(delta);
		stage.draw();
		renderWindowed(delta);
	}

	private Vector2 tmp = new Vector2();
	private void renderWindowed (float delta) {
		// get containers position in world coordinates
		tmp.set(container.getX(), container.getY());
		window.localToStageCoordinates(tmp);
		// we are reasonable, so we use ScreenViewport for gui, no need to translate it
		int x = (int)tmp.x;
		int y = (int)tmp.y;
		int width = (int)container.getWidth();
		int height = (int)container.getHeight();
		// doing all this each frame is a waste, a check for same position/size wouldnt hurt probably
		innerViewport.setScreenPosition(x, y);
		innerViewport.update(width, height, false);

		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		innerView.render(delta);
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);


		// draw some stuff, this could be replaced with a screen or whatever
//		renderer.setProjectionMatrix(innerCamera.combined);
//		renderer.begin(ShapeRenderer.ShapeType.Filled);
//		renderer.setColor(Color.CYAN);
//		// large rect bigger then the world size
//		renderer.rect(-5, -5, 10, 10);
//		renderer.setColor(Color.RED);
//		// dot in the middle
//		renderer.circle(-.05f, -.05f, .05f, 16);
//		renderer.end();
//		renderer.begin(ShapeRenderer.ShapeType.Line);
//		renderer.setColor(Color.RED);
//		// world size border
//		renderer.rect(-W_SIZE/2, -W_SIZE/2, W_SIZE, W_SIZE);
//		renderer.end();
		// need to re apply previous innerViewport
		guiViewport.apply();
	}

	@Override public void dispose () {
		super.dispose();
		innerView.dispose();
	}

	private static class CustomBox2dReflectTest extends Box2dReflectTest {
		public CustomBox2dReflectTest (GameReset game) {
			super(game);
		}

		public void setViewport (ExtendViewport viewport) {
			this.gameViewport = viewport;
			gameCamera = (OrthographicCamera)viewport.getCamera();
			gameCamera.zoom = 4;
			gameCamera.update();
		}

		public InputMultiplexer getMultiplexer () {
			return multiplexer;
		}
	}

	private static class InnerExtendViewport extends ExtendViewport {

		public InnerExtendViewport (float minWorldWidth, float minWorldHeight, Camera camera) {
			super(minWorldWidth, minWorldHeight, camera);
		}

		@Override public void update (int screenWidth, int screenHeight, boolean centerCamera) {
			float minWorldWidth = getMinWorldWidth();
			float minWorldHeight = getMinWorldHeight();
			float maxWorldWidth = getMaxWorldWidth();
			float maxWorldHeight = getMaxWorldHeight();
			// Fit min size to the screen.
			float worldWidth = minWorldWidth;
			float worldHeight = minWorldHeight;
			Vector2 scaled = Scaling.fit.apply(worldWidth, worldHeight, screenWidth, screenHeight);

			// Extend in the short direction.
			int viewportWidth = Math.round(scaled.x);
			int viewportHeight = Math.round(scaled.y);
			if (viewportWidth < screenWidth) {
				float toViewportSpace = viewportHeight / worldHeight;
				float toWorldSpace = worldHeight / viewportHeight;
				float lengthen = (screenWidth - viewportWidth) * toWorldSpace;
				if (maxWorldWidth > 0)
					lengthen = Math.min(lengthen, maxWorldWidth - minWorldWidth);
				worldWidth += lengthen;
				viewportWidth += Math.round(lengthen * toViewportSpace);
			} else if (viewportHeight < screenHeight) {
				float toViewportSpace = viewportWidth / worldWidth;
				float toWorldSpace = worldWidth / viewportWidth;
				float lengthen = (screenHeight - viewportHeight) * toWorldSpace;
				if (maxWorldHeight > 0)
					lengthen = Math.min(lengthen, maxWorldHeight - minWorldHeight);
				worldHeight += lengthen;
				viewportHeight += Math.round(lengthen * toViewportSpace);
			}

			setWorldSize(worldWidth, worldHeight);

			// Center, we added getScreenXY() vs the default extend vp
			setScreenBounds(getScreenX() + (screenWidth - viewportWidth) / 2, getScreenY() + (screenHeight - viewportHeight) / 2,
				viewportWidth, viewportHeight);

			apply(centerCamera);
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIGameInWindowTest.class);
	}
}

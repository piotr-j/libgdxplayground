package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 */
public class ScreenViewportScaleTest extends BaseScreen {
	static final int BASE_WIDTH = 720/2;
	static final int BASE_HEIGHT = 1280/2;
	OrthographicCamera vpCamera;
	ScreenViewport screenViewport;

	public ScreenViewportScaleTest (GameReset game) {
		super(game);

		vpCamera = new OrthographicCamera();
		screenViewport = new ScreenViewport(vpCamera);

		VisWindow window = new VisWindow("Test Window");
		stage.addActor(window);

		window.centerWindow();

	}
	Vector3 cursor = new Vector3();
	@Override public void render (float delta) {
		super.render(delta);
		cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		screenViewport.unproject(cursor);

		renderer.setProjectionMatrix(vpCamera.combined);
		renderer.setColor(Color.GRAY);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.rect(0, 0, BASE_WIDTH, BASE_HEIGHT);
		renderer.end();

		renderer.setColor(Color.RED);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.circle(cursor.x, cursor.y, 25);
		renderer.end();

		stage.setViewport(screenViewport);
		stage.act(delta);
		stage.draw();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		float fw = width/(float)BASE_WIDTH;
		float fh = height/(float)BASE_HEIGHT;
		PLog.log("w -> " + width + "/" + BASE_WIDTH + "=" + String.format("%.2f", fw) + ", h -> " + height + "/" + BASE_HEIGHT + "=" + String.format("%.2f", fh));
		float r = Math.min(fw, fh);
		float rr = r;
		if (rr >= 1) {
			rr = MathUtils.round(rr);
		} else if (rr <= .5f){
			rr = .5f;
		} else {
			rr = 1;
		}
		PLog.log("r -> " + String.format("%.2f", r) + ", rr -> " + String.format("%.2f", rr));
		screenViewport.setUnitsPerPixel(1/rr);
		screenViewport.update(width, height, true);
	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowPosition(0, 100);
		config.setWindowedMode(BASE_WIDTH, BASE_HEIGHT);
//		config.setHdpiMode(HdpiMode.Pixels);
		PlaygroundGame.start(args, config, ScreenViewportScaleTest.class);
	}
}

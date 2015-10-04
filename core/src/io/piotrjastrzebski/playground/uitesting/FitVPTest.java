package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class FitVPTest extends BaseScreen {
	OrthographicCamera fitCamera;
	FitViewport fitViewport;
	public FitVPTest (GameReset game) {
		super(game);
		fitCamera = new OrthographicCamera();
		fitViewport = new FitViewport(1000, 1000, fitCamera);
		VisWindow window = new VisWindow("Test Window");
		stage.addActor(window);
		window.centerWindow();
	}
	Vector3 cursor = new Vector3();
	@Override public void render (float delta) {
		super.render(delta);
		cursor.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		fitViewport.unproject(cursor);

		renderer.setProjectionMatrix(fitCamera.combined);
		renderer.setColor(Color.GRAY);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.rect(0, 0, 1000, 1000);
		renderer.end();

		renderer.setColor(Color.RED);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.circle(cursor.x, cursor.y, 25);
		renderer.end();

		stage.setViewport(fitViewport);
		stage.act(delta);
		stage.draw();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		if (fitViewport != null)
			fitViewport.update(width, height, true);
	}
}

package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class VPTest extends BaseScreen {
	public final static float VP_WIDTH = 40f;
	public final static float VP_HEIGHT = 22.5f;
	OrthographicCamera camera;
	ExtendViewport viewport;
	public VPTest (GameReset game) {
		super(game);
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);
	}

	@Override public void render (float delta) {
		super.render(delta);

		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.RED);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		// rect always stays in center with at least 1 unit margin
		renderer.rect(-VP_WIDTH / 2 + 1, -VP_HEIGHT / 2 + 1, VP_WIDTH - 2, VP_HEIGHT - 2);
		renderer.setColor(Color.CYAN);
		// circle in middle with 5 unit radius
		renderer.circle(0, 0, 5, 32);
		renderer.setColor(Color.GREEN);
		// circles stay in corners
		renderer.circle(-VP_WIDTH / 2 + 2, -VP_HEIGHT / 2 + 2, 1, 32);
		renderer.circle(VP_WIDTH /2 - 2, -VP_HEIGHT / 2 + 2, 1, 32);
		renderer.circle(-VP_WIDTH / 2 + 2, VP_HEIGHT / 2 - 2, 1, 32);
		renderer.circle(VP_WIDTH / 2 - 2, VP_HEIGHT / 2 - 2, 1, 32);
		renderer.end();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		viewport.update(width, height, false);
	}
}

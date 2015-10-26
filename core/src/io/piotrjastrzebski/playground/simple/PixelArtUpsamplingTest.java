package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Simple trail behind touch drag
 * A lot of stuff left for optimizations
 */
public class PixelArtUpsamplingTest extends BaseScreen {
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	private OrthographicCamera camera;
	private ExtendViewport viewport;
	private Texture scene;
	private Texture scene2x;
	private Texture scene4x;
	private Texture scene8x;
	private ShaderProgram shader;

	public PixelArtUpsamplingTest (GameReset game) {
		// ignore this
		super(game);
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);

		scene = new Texture("pixels/scene.png");
		scene2x = new Texture("pixels/scene2x.png");
		scene4x = new Texture("pixels/scene4x.png");
		scene8x = new Texture("pixels/scene8x.png");

		shader = new ShaderProgram(Gdx.files.internal("pixels/upscaler.vsh"), Gdx.files.internal("pixels/upscaler.fsh"));
		if (!shader.isCompiled())
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
	}

	@Override public void render (float delta) {
		super.render(delta);
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.circle(tp.x, tp.y, 0.25f, 16);
		renderer.end();
	}

	Vector3 tp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		camera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		camera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		// this check is necessary cus BaseScreen calls this prematurely
		if (viewport != null)
			viewport.update(width, height, true);
	}

	@Override public void dispose () {
		super.dispose();
		scene.dispose();
		scene2x.dispose();
		scene4x.dispose();
		scene8x.dispose();
	}
}

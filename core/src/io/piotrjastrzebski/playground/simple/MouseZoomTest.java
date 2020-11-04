package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Simple hex map test
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class MouseZoomTest extends BaseScreen {
	private static final String TAG = HexTest.class.getSimpleName();

	public MouseZoomTest (final GameReset game) {
		super(game);
		GestureDetector gd = new GestureDetector(new GestureDetector.GestureAdapter(){
			Vector3 sp = new Vector3();
			Vector3 ep = new Vector3();
			@Override public boolean pan (float x, float y, float deltaX, float deltaY) {
				gameCamera.unproject(sp.set(x, y, 0));
				gameCamera.unproject(ep.set(x + deltaX, y + deltaY, 0));
				gameCamera.position.add(-ep.x + sp.x, -ep.y + sp.y, 0);
				gameCamera.update();
				return true;
			}
		});
		multiplexer.addProcessor(0, gd);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setColor(Color.GREEN);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (int x = -100; x < 100; x++) {
			for (int y = -100; y < 100; y++) {
				if (x % 2 == 0) {
					if (y % 2 == 0) {
						renderer.rect(x, y, 1, 1);
					}
				} else {
					if (y % 2 != 0) {
						renderer.rect(x, y, 1, 1);
					}
				}
			}
		}
		renderer.end();

	}

	Vector3 tp = new Vector3();
	@Override
	public boolean scrolled (float amountX, float amountY) {
		gameCamera.unproject(tp.set(Gdx.input.getX(), Gdx.input.getY(), 0 ));
		float oldX = tp.x;
		float oldY = tp.y;
		gameCamera.zoom += amountX * gameCamera.zoom * 0.1f;
		gameCamera.update();

		gameCamera.unproject(tp.set(Gdx.input.getX(), Gdx.input.getY(), 0 ));
		gameCamera.position.add(oldX - tp.x, oldY - tp.y, 0);

		gameCamera.update();
		return true;
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.SPACE: {
			gameCamera.zoom = 1;
			gameCamera.position.set(0, 0, 0);
			gameCamera.update();
		} return true;
		}
		return super.keyDown(keycode);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, MouseZoomTest.class);
	}
}

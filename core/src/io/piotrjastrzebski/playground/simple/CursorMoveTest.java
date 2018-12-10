package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class CursorMoveTest extends BaseScreen {
	private static final String TAG = CursorMoveTest.class.getSimpleName();
	private boolean paused;

	public CursorMoveTest (GameReset game) {
		super(game);
		sx = Gdx.input.getX();
		sy = Gdx.graphics.getHeight() - Gdx.input.getY() -1;
	}

	int sx;
	int sy;
	int ox;
	float timer;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		if (paused) return;
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
//			sx = Gdx.input.getX();
//			sy = Gdx.graphics.getHeight() - Gdx.input.getY() -1;
		}
		timer += delta;
		if (timer >= 1) {
			Gdx.app.log(TAG, Gdx.input.getX()+" "+Gdx.input.getY());
			timer -=1;
			ox += 1;
			Gdx.input.setCursorPosition(sx + ox, sy);
		}
	}

	@Override public void pause () {
		super.pause();
		paused = true;
	}

	@Override public void resume () {
		super.resume();
		paused = false;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		sx = screenX;
		sy = Gdx.graphics.getHeight() - screenY -1;
		ox = 0;
		return super.mouseMoved(screenX, screenY);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280/2;
		config.height = 720/2;
//		config.useHDPI = true;
		PlaygroundGame.start(args, config, CursorMoveTest.class);
	}
}

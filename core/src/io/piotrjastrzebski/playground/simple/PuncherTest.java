package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class PuncherTest extends BaseScreen {
	private final static String TAG = PuncherTest.class.getSimpleName();

	private Texture bgTex;
	private Texture punchTex;
	public PuncherTest (GameReset game) {
		super(game);
		bgTex = new Texture("puncher/bg.png");
		punchTex = new Texture("puncher/punch.png");
	}

	long last = TimeUtils.millis();
	float timer;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		batch.draw(bgTex, -VP_WIDTH/2, -VP_HEIGHT/2, VP_WIDTH, VP_HEIGHT);
		batch.end();
		long now = TimeUtils.millis();
		if ((now - last)/1000f > 0.5f ) {
			last = now;
			Gdx.app.log("", "spawn!");
		}
		timer-=delta;
		if (timer <= 0) {
			timer = 0.5f;
			Gdx.app.log("", "spawn!");
		}
	}

	Vector3 tp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));

		// do stuff
		return false;
	}

	@Override public void dispose () {
		super.dispose();
		bgTex.dispose();
	}
}

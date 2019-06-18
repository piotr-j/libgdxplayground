package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class DrawTest extends BaseScreen {
	private static final String TAG = DrawTest.class.getSimpleName();

	Texture texture;
	FPSLogger fpsLogger;
	public DrawTest (GameReset game) {
		super(game);
		clear.set(0, 0.05f, 0.1f, 1);

		Pixmap pixmap = new Pixmap(512, 512, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
//		texture = new Texture("badlogic.jpg");
		texture = new Texture(pixmap);
		fpsLogger = new FPSLogger();
	}

	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (int i = 0; i < 14_000; i++) {
			batch.draw(texture, 0, 0, 1, 1);
		}
		batch.end();
		fpsLogger.log();
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, DrawTest.class);
	}
}

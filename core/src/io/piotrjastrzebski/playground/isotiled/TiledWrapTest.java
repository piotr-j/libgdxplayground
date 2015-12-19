package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledWrapTest extends BaseScreen {
	public TiledWrapTest (GameReset game) {
		super(game);

	}

	private Vector2 cs = new Vector2();
	@Override public void render (float delta) {
		super.render(delta);

	}

	private Vector3 temp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		// fairly dumb
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		return true;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		return true;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, TiledWrapTest.class);
	}
}

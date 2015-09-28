package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class SplineTrailTest extends BaseScreen {
	public SplineTrailTest (GameReset game) {
		super(game);
	}

	@Override public void render (float delta) {
		super.render(delta);
	}

	Vector3 tp = new Vector3();
	boolean dragging;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = true;
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (!dragging) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = false;
		return true;
	}
}

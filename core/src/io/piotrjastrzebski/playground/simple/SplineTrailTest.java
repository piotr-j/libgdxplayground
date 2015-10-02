package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class SplineTrailTest extends BaseScreen {
	private final static int NUM_POINTS = 16;
	private Array<Vector2> points = new Array<>();
	public SplineTrailTest (GameReset game) {
		super(game);
		for (int i = 0; i < NUM_POINTS; i++) {
			points.add(new Vector2());
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.setColor(Color.CYAN);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Vector2 p : points) {
			renderer.circle(p.x, p.y, 0.1f, 12);
		}
		renderer.end();
	}

	private void addPoint(float x, float y) {
		// shift current points down
		for (int i = NUM_POINTS - 1; i > 0; i--) {
			points.get(i).set(points.get(i - 1));
		}
		// insert new one at the start
		points.get(0).set(x, y);
	}

	Vector3 tp = new Vector3();
	boolean dragging;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = true;
		addPoint(tp.x, tp.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (!dragging) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		addPoint(tp.x, tp.y);
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = false;
		addPoint(tp.x, tp.y);
		return true;
	}
}

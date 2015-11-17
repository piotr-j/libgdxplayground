package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class MouseDragTest extends BaseScreen {
	private final static String TAG = MouseDragTest.class.getSimpleName();

	public MouseDragTest (GameReset game) {
		super(game);
	}

	Array<Vector2> points = new Array<>();
	float timer;
	int dragUpdates;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.25f, .25f, .25f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		timer += delta;
		if (timer > 1.0f) {
			timer-=1.0f;
			points.clear();
			Gdx.app.log("", "Drag updates " + dragUpdates);
			dragUpdates = 0;
		}
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.setColor(Color.MAGENTA);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (int i = points.size - 1; i > 0; i--) {
			Vector2 p = points.get(i);
			renderer.circle(p.x, p.y, 0.1f, 8);
		}
		renderer.end();
	}

	Vector3 tp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		points.add(new Vector2(tp.x, tp.y));
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		points.add(new Vector2(tp.x, tp.y));
		dragUpdates++;
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		points.add(new Vector2(tp.x, tp.y));
		return true;
	}
}

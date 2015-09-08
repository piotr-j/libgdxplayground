package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class CircleTest extends BaseScreen {

	Circle player;
	Circle other;
	public CircleTest (GameReset game) {
		super(game);
		player = new Circle(0, 0, 5);
		other = new Circle(0, 0, 3.5f);
	}

	boolean circleContains = true;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		player.setPosition(pos.x, pos.y);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.BLACK);
		renderer.circle(other.x, other.y, other.radius, 32);
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Line);
		boolean contains = circleContains? player.contains(other) : contains(player, other);
		if (contains) {
			renderer.setColor(0, 1, 0, 1);
		} else {
			renderer.setColor(1, 0, 1, 1);
		}
		renderer.circle(player.x, player.y, player.radius, 32);
		renderer.circle(player.x, player.y, 0.1f, 8);
		renderer.end();
	}

	public boolean contains(Circle c1, Circle c2) {
		float dx = c1.x - c2.x;
		float dy = c1.y - c2.y;
		float distCenterSq = dx * dx + dy * dy;
		float radiusDiff = c1.radius - c2.radius;
		return distCenterSq <= radiusDiff * radiusDiff;
	}

	Vector3 pos = new Vector3();
	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(pos.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.Z) {
			circleContains = !circleContains;
		}
		return super.keyDown(keycode);
	}
}

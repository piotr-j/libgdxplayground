package io.piotrjastrzebski.playground;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class CircleTest implements Screen, InputProcessor {
	protected OrthographicCamera camera;
	protected ExtendViewport viewport;
	protected ShapeRenderer renderer;

	Circle player;
	Circle other;
	public CircleTest () {
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(40,  22.5f, camera);

		renderer = new ShapeRenderer();
		Gdx.input.setInputProcessor(this);

		player = new Circle(0, 0, 5);
		other = new Circle(0, 0, 3.5f);
	}

	boolean circleContains = true;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		player.setPosition(pos.x, pos.y);

		renderer.setProjectionMatrix(camera.combined);
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
		camera.unproject(pos.set(screenX, screenY, 0));
		return true;
	}

	@Override public void resize (int width, int height) {
		viewport.update(width, height, false);
	}

	@Override public void dispose () {
		renderer.dispose();
	}

	@Override public void hide () {dispose();}
	@Override public void show () {}
	@Override public void pause () {}
	@Override public void resume () {}
	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.Z) {
			circleContains = !circleContains;
		}
		return false;
	}
	@Override public boolean keyUp (int keycode) {return false;}
	@Override public boolean keyTyped (char character) {
		return false;
	}
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		return false;
	}
	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return false;
	}
	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		return false;
	}
	@Override public boolean scrolled (int amount) {
		return false;
	}
}

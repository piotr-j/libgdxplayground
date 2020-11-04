package io.piotrjastrzebski.playground.ecs.fancywalltest.processors;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import io.piotrjastrzebski.playground.ecs.ECSTestBase;
import io.piotrjastrzebski.playground.ecs.Input;

/**
 * Created by PiotrJ on 30/09/15.
 */
@Wire
public class CamMover extends BaseSystem implements Input, InputProcessor {

	@Wire(name = ECSTestBase.WIRE_GAME_CAM)
	OrthographicCamera cam;

	int moveX;
	int moveY;
	float speed = 3f;
	@Override protected void processSystem () {
		if (moveX > 0) {
			cam.position.x += speed * world.delta;
		} else if (moveX < 0) {
			cam.position.x -= speed * world.delta;
		}
		if (moveY > 0) {
			cam.position.y += speed * world.delta;
		} else if (moveY< 0) {
			cam.position.y -= speed * world.delta;
		}
		cam.update();
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case com.badlogic.gdx.Input.Keys.W:
		case com.badlogic.gdx.Input.Keys.UP:
			moveY++;
			break;
		case com.badlogic.gdx.Input.Keys.S:
		case com.badlogic.gdx.Input.Keys.DOWN:
			moveY--;
			break;
		case com.badlogic.gdx.Input.Keys.A:
		case com.badlogic.gdx.Input.Keys.LEFT:
			moveX--;
			break;
		case com.badlogic.gdx.Input.Keys.D:
		case com.badlogic.gdx.Input.Keys.RIGHT:
			moveX++;
			break;
		}
		return true;
	}

	@Override public boolean keyUp (int keycode) {
		switch (keycode) {
		case com.badlogic.gdx.Input.Keys.W:
		case com.badlogic.gdx.Input.Keys.UP:
			moveY--;
			break;
		case com.badlogic.gdx.Input.Keys.S:
		case com.badlogic.gdx.Input.Keys.DOWN:
			moveY++;
			break;
		case com.badlogic.gdx.Input.Keys.A:
		case com.badlogic.gdx.Input.Keys.LEFT:
			moveX++;
			break;
		case com.badlogic.gdx.Input.Keys.D:
		case com.badlogic.gdx.Input.Keys.RIGHT:
			moveX--;
			break;
		}
		return false;
	}

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

	@Override public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled (float amountX, float amountY) {
		return false;
	}

	@Override public int priority () {
		return 0;
	}

	@Override public InputProcessor get () {
		return this;
	}
}

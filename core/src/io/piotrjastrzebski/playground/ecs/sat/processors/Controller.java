package io.piotrjastrzebski.playground.ecs.sat.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Input.Keys;
import io.piotrjastrzebski.playground.ecs.Input;
import io.piotrjastrzebski.playground.ecs.sat.SATTest;
import io.piotrjastrzebski.playground.ecs.sat.components.Circle;
import io.piotrjastrzebski.playground.ecs.sat.components.Controllable;
import io.piotrjastrzebski.playground.ecs.sat.components.Polygon;
import io.piotrjastrzebski.playground.ecs.sat.components.Transform;

/**
 * Created by PiotrJ on 27/08/15.
 */
@Wire
public class Controller extends IteratingSystem implements Input, InputProcessor {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Polygon> mPolygon;
	protected ComponentMapper<Circle> mCircle;


	@Wire(name = SATTest.WIRE_GAME_CAM) OrthographicCamera camera;

	public Controller () {
		super(Aspect.all(Transform.class, Controllable.class).one(Polygon.class, Circle.class));
	}

	private int selected = -1;
	int rot = 0;
	int scale = 0;
	boolean reset;
	boolean touching;
	Vector3 touch = new Vector3();
	@Override protected void process (int e) {
		if (selected >= 0 && selected != e) return;

		Polygon polygon = mPolygon.getSafe(e);
		if (polygon != null && polygon.polygon.contains(touch.x, touch.y)) {
			selected = e;
		}

		Circle circle = mCircle.getSafe(e);
		if (circle != null && circle.circle.contains(touch.x, touch.y)) {
			selected = e;
		}

		if (selected == e) {
			Transform transform = mTransform.get(e);
			transform.pos.set(touch.x, touch.y);
			if (reset) {
				transform.rot = 0;
				transform.scale = 1;
			} else {
				transform.rot += 90 * world.delta * rot;
				if (transform.rot < 0) transform.rot = 359;
				if (transform.rot >= 360) transform.rot = 0;
				transform.scale = MathUtils.clamp(transform.scale + world.delta * scale, 0.1f, 3f);
			}
		}
	}

	@Override public int priority () {
		return 1;
	}

	@Override public InputProcessor get () {
		return this;
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Keys.UP:
			scale += 1;
			break;
		case Keys.DOWN:
			scale += -1;
			break;
		case Keys.LEFT:
			rot += -1;
			break;
		case Keys.RIGHT:
			rot += 1;
			break;
		case Keys.SPACE:
			reset = true;
			break;
		}
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		switch (keycode) {
		case Keys.UP:
			scale -= 1;
			break;
		case Keys.DOWN:
			scale -= -1;
			break;
		case Keys.LEFT:
			rot -= -1;
			break;
		case Keys.RIGHT:
			rot -= 1;
			break;
		case Keys.SPACE:
			reset = false;
			break;
	}
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		camera.unproject(touch.set(screenX, screenY, 0));
		touching = true;
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		camera.unproject(touch.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		camera.unproject(touch.set(screenX, screenY, 0));
		touching = false;
		selected = -1;
		return false;
	}

	@Override public boolean checkProcessing () {
		// works onl when touching
		return touching;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

}

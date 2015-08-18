package io.piotrjastrzebski.playground.ecs.jobs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class Selector extends EntityProcessingSystem implements InputProcessor {
	private ComponentMapper<Godlike> mGodlike;
	@Wire OrthographicCamera camera;
	public Selector () {
		super(Aspect.all(Godlike.class));
	}

	@Override protected void begin () {
		if (up && last >= 0) {
			Godlike godlike1 = mGodlike.get(last);
			if (godlike1 != null) {
				godlike1.selected = false;
			} else {
				last = -1;
			}
		}
	}

	IntBag selected = new IntBag();
	int last = -1;
	Rectangle bounds = new Rectangle();
	@Override protected void process (Entity e) {
		if (up) {
			Godlike godlike = mGodlike.get(e);
			bounds.set(godlike.x, godlike.y, godlike.width, godlike.height);
			if (bounds.contains(tp.x, tp.y)) {
				godlike.selected = true;
				last = e.id;
			}
		}
	}

	@Override protected void end () {
		up = false;
	}

	boolean up;
	Vector3 tp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		camera.unproject(tp.set(screenX, screenY, 0));
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		camera.unproject(tp.set(screenX, screenY, 0));
		up = true;
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		camera.unproject(tp.set(screenX, screenY, 0));
		return false;
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
}

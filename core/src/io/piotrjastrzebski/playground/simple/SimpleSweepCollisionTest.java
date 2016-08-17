package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;
import static java.lang.Math.min;

/**
 * Base on
 * http://www.gamedev.net/page/resources/_/technical/game-programming/swept-aabb-collision-detection-and-response-r3084
 */
public class SimpleSweepCollisionTest extends BaseScreen {
	private static final String TAG = SimpleSweepCollisionTest.class.getSimpleName();
	enum CollisionType {SIMPLE}
	CollisionType type = CollisionType.SIMPLE;
	Box wall;
	Box start;
	Box aabb;
	Box target;
	Box drag;
	Vector2 dragOffset = new Vector2();

	public SimpleSweepCollisionTest (GameReset game) {
		super(game);
		wall = new Box(-1f, -6, 2, 12);
		start = new Box(-8, 1.5f, 3, 3);
		target = new Box(5, -1.5f, 3, 3);
		aabb = new Box();
	}

	@Override public void render (float delta) {
		// setPosition our drag target if we have any
		if (drag != null) {
			drag.x1(cs.x + dragOffset.x);
			drag.y1(cs.y + dragOffset.y);
		}

		// update collision
		switch (type) {
		case SIMPLE:
			aabb.set(target);
			if (aabb.overlaps(wall)) {
				// calculate displacement for each side
				float left = wall.x2 - target.x1;
				float right = target.x2 - wall.x1;
				float top = wall.y2 - target.y1;
				float bot = target.y2 - wall.y1;

				// pick smalled one
				boolean horizontal = min(left, right) < min(top, bot);
				if (horizontal) {
					if (left > right) {
						aabb.x2(wall.x1);
					} else {
						aabb.x1(wall.x2);
					}
				} else {
					if (top > bot) {
						aabb.y2(wall.y1);
					} else {
						aabb.y1(wall.y2);
					}
				}
			}
			break;
		}

		// render stuff
		Gdx.gl.glClearColor(0.75f, 0.75f, 0.75f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.setColor(Color.DARK_GRAY);
		renderer.begin(Filled);
		renderer.rect(wall.x1, wall.y1, wall.width, wall.height);
		renderer.end();

		renderer.begin(Line);
		renderer.setColor(1, 1, 1, .5f);
		renderer.line(start.x1, start.y1, target.x1, target.y1);
		renderer.line(start.x2, start.y1, target.x2, target.y1);
		renderer.line(start.x1, start.y2, target.x1, target.y2);
		renderer.line(start.x2, start.y2, target.x2, target.y2);

		renderer.setColor(Color.BLUE);
		renderer.rect(start.x1, start.y1, start.width, start.height);

		renderer.setColor(Color.CYAN);
		renderer.rect(aabb.x1, aabb.y1, aabb.width, aabb.height);

		renderer.setColor(Color.FOREST);
		renderer.rect(target.x1, target.y1, target.width, target.height);

		renderer.end();
	}

	public static class Box {
		public float x1;
		public float y1;
		public float x2;
		public float y2;
		public float width;
		public float height;
		public float vx, vy;

		public Box () {
			this(0, 0, 0, 0);
		}

		public Box (float x, float y, float width, float height) {
			this.x1 = x;
			this.y1 = y;
			this.width = width;
			this.height = height;
			x2 = x1 + width;
			y2 = y1 + height;
		}

		public Box set (Box other) {
			x1 = other.x1;
			y1 = other.y1;
			width = other.width;
			height = other.height;
			x2 = x1 + width;
			y2 = y1 + height;
			return this;
		}

		public boolean overlaps (Box box) {
			return x1 < box.x2 && x2 > box.x1 && y1 < box.y2 && y2 > box.y1;
		}

		public boolean contains (float x, float y) {
			return x1 <= x && x2 >= x && y1 <= y && y2 >= y;
		}

		public void x1 (float x1) {
			this.x1 = x1;
			this.x2 = x1 + width;
		}

		public void y1 (float y1) {
			this.y1 = y1;
			this.y2 = y1 + height;
		}
		public void x2 (float x2) {
			this.x1 = x2 - width;
			this.x2 = x2;
		}

		public void y2 (float y2) {
			this.y1 = y2 - height;
			this.y2 = y2;
		}
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		if (start.contains(cs.x, cs.y)) {
			drag = start;
			dragOffset.set(start.x1, start.y1).sub(cs);
		} else if (target.contains(cs.x, cs.y)) {
			drag = target;
			dragOffset.set(target.x1, target.y1).sub(cs);
		}
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		drag = null;
		return true;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, SimpleSweepCollisionTest.class);
	}
}

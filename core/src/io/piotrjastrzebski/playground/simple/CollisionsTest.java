package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Simple collision detection with spatial component
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class CollisionsTest extends BaseScreen {
	private static final String TAG = CollisionsTest.class.getSimpleName();

	private Array<Collider> colliders = new Array<>();
	private Rectangle bounds = new Rectangle();
	public CollisionsTest (GameReset game) {
		super(game);
		MathUtils.random.setSeed(43);
		for (int i = 0; i < 33; i++) {
			colliders.add(new RectCollider(
				MathUtils.random(-VP_WIDTH/2 + 2, VP_WIDTH/2 - 2),
				MathUtils.random(-VP_HEIGHT/2 + 2, VP_HEIGHT/2 - 2),
				MathUtils.random(1, 3), MathUtils.random(1, 3),
				MathUtils.random(0, 90/5)*5));

			colliders.add(new CircleCollider(
				MathUtils.random(-VP_WIDTH/2 + 2, VP_WIDTH/2 - 2),
				MathUtils.random(-VP_HEIGHT/2 + 2, VP_HEIGHT/2 - 2),
				MathUtils.random(1, 3)/2f));
		}
		MathUtils.random.setSeed(TimeUtils.millis());
	}

	@Override public void render (float delta) {
		for (Collider collider : colliders) {
			collider.update(delta);
		}
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLACK);
		renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

		for (Collider collider : colliders) {
			collider.draw(renderer);
		}

		// TODO simple sspacial partitions
		boolean contains = false;
		for (Collider collider : colliders) {
			contains |= collider.contains(cs.x, cs.y);
		}
		renderer.setColor(Color.RED);
		if (contains) {
			renderer.setColor(Color.GREEN);
		}
		renderer.line(cs.x + .25f, cs.y + .25f, cs.x - .25f, cs.y - .25f);
		renderer.line(cs.x + .25f, cs.y - .25f, cs.x - .25f, cs.y + .25f);
		renderer.circle(cs.x, cs.y, .25f, 16);
		renderer.end();
	}

	private static abstract class Collider {
		public Rectangle aabb = new Rectangle();
		public abstract void update(float delta);
		public abstract void draw(ShapeRenderer renderer);

		public abstract boolean contains (float x, float y);
	}

	private static class CircleCollider extends Collider {
		public Circle shape = new Circle();

		public CircleCollider (float x, float y, float radius) {
			shape.set(x, y, radius);
		}

		@Override public void update (float delta) {
			aabb.set(shape.x - shape.radius, shape.y - shape.radius, shape.radius * 2, shape.radius * 2);
		}

		@Override public void draw (ShapeRenderer renderer) {
			renderer.setColor(Color.WHITE);
			renderer.circle(shape.x, shape.y, shape.radius, 16);
			float x = shape.x;
			float y = shape.y;
			renderer.line(x - .25f, y, x + .25f, y);
			renderer.line(x, y - .25f, x, y + .25f);

			renderer.setColor(Color.CYAN);
			renderer.rect(aabb.x, aabb.y, aabb.width, aabb.height);
		}

		@Override public boolean contains (float x, float y) {
			return aabb.contains(x, y) && shape.contains(x, y);
		}
	}

	private static class RectCollider extends Collider {
		protected float[] verts = new float[8];
		public Polygon shape = new Polygon();

		public RectCollider (float x, float y, float width, float height, float rotation) {
			verts[0] = - width/2;
			verts[1] = - height/2;
			verts[2] = + width/2;
			verts[3] = - height/2;
			verts[4] = + width/2;
			verts[5] = + height/2;
			verts[6] = - width/2;
			verts[7] = + height/2;
			shape.setVertices(verts);
			shape.setPosition(x, y);
			shape.setRotation(rotation);
		}

		@Override public void update (float delta) {
			shape.setRotation(shape.getRotation() + delta * 90);
			aabb.set(shape.getBoundingRectangle());
		}

		@Override public void draw (ShapeRenderer renderer) {
			renderer.setColor(Color.WHITE);
			renderer.polygon(shape.getTransformedVertices());
			float x = shape.getX();
			float y = shape.getY();
			renderer.line(x - .25f, y, x + .25f, y);
			renderer.line(x, y - .25f, x, y + .25f);

			renderer.setColor(Color.CYAN);
			renderer.rect(aabb.x, aabb.y, aabb.width, aabb.height);
		}

		@Override public boolean contains (float x, float y) {
			return aabb.contains(x, y) && shape.contains(x, y);
		}
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		bounds.set(
			gameCamera.position.x - gameCamera.viewportWidth/2 + 1,
			gameCamera.position.y - gameCamera.viewportHeight/2 + 1,
			gameCamera.viewportWidth - 2, gameCamera.viewportHeight -2);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, CollisionsTest.class);
	}
}

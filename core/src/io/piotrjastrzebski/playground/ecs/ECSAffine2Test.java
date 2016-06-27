package io.piotrjastrzebski.playground.ecs;

import com.artemis.*;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 * Created by EvilEntity on 28/07/2015.
 */
public class ECSAffine2Test extends ECSTestBase {

	public ECSAffine2Test (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
	}

	@Override protected void preInit (WorldConfiguration config) {
		config.setSystem(Updater.class);
		config.setSystem(Transformer.class);
		config.setSystem(Spawner.class);
		config.setSystem(Renderer.class);
		config.setSystem(DotRenderer.class);
		config.setSystem(DebugRenderer.class);
	}

	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<GodComponent> mGodComponent;
	Texture texture;
	@Override protected void postInit () {
		world.inject(this, false);
		texture = new Texture("badlogic.jpg");
		TextureRegion region = new TextureRegion(texture);

		int entityId = world.create();
		{
			Transform tm = mTransform.create(entityId);
			tm.set(-2, -2, 4, 4, 45f, 2f, 1f);
			tm.scale.set(1.5f, 1.5f);

			GodComponent gc = mGodComponent.create(entityId);
			gc.region = region;
			gc.tint.set(0, .75f, 0, 1);
			gc.rotation = -45;
//			gc.rotation = 0;
		}

		int entityId2 = world.create();
		{
			Transform tm = mTransform.create(entityId2);
			// since it has a parent, the position is the offset from parents position
			tm.set(1, 5, 2, 2, 0f, 1f, 1f);

			GodComponent gc = mGodComponent.create(entityId2);
			gc.region = region;
			gc.tint.set(0, .75f, .75f, 1);
			gc.rotation = 30;
			gc.parent = entityId;
		}
		int entityId3 = world.create();
		{
			Transform tm = mTransform.create(entityId3);
			// since it has a parent, the position is the offset from parents position
//			tm.set(-1.5f, .5f, 1, 1, 30f, .5f, .5f);
			tm.set(-1.5f, .5f, 1, 1, 45f, .5f, .75f);

			GodComponent gc = mGodComponent.create(entityId3);
			gc.region = region;
			gc.tint.set(.75f, 0, .75f, 1);
			gc.rotation = 60;
			gc.parent = entityId2;
			gc.spawn = true;
			gc.spawnOffset.set(0.5f, 1);
			gc.spawnDirection.set(0, 1);
		}
		int entityId4 = world.create();
		{
			Transform tm = mTransform.create(entityId4);
			// since it has a parent, the position is the offset from parents position
//			tm.set(2.5f, .5f, 1, 1, -30f, .5f, .5f);
			tm.set(2.5f, .5f, 1, 1, -45f, .5f, .75f);

			GodComponent gc = mGodComponent.create(entityId4);
			gc.region = region;
			gc.tint.set(.75f, 0, .75f, 1);
			gc.rotation = 60;
			gc.parent = entityId2;
			gc.spawn = true;
			gc.spawnOffset.set(0.5f, 1);
			gc.spawnDirection.set(0, 1);
		}
	}

	protected static class Updater extends IteratingSystem {
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<GodComponent> mGodComponent;
		public Updater () {
			super(Aspect.all(Transform.class, GodComponent.class));
		}

		boolean rotate = true;
		@Override protected void begin () {
			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
				rotate = !rotate;
			}
		}

		@Override protected void process (int entityId) {
			if (!rotate) return;
			Transform tf = mTransform.get(entityId);
			GodComponent gc = mGodComponent.get(entityId);
			if (gc.rotation != 0) {
				tf.rotation += gc.rotation * world.delta;
				tf.dirty = true;
			}
		}
	}

	protected static class Transformer extends IteratingSystem {
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<GodComponent> mGodComponent;
		public Transformer () {
			super(Aspect.all(Transform.class, GodComponent.class));
		}

		@Override protected void process (int entityId) {
			Transform tf = mTransform.get(entityId);
			GodComponent gc = mGodComponent.get(entityId);
			// the obvious problem with this is the order of operations, if parents id is larger then child, there will be a frame of delay
			if (tf.dirty || gc.parent >= 0) {
				tf.affine2.setToTrnRotScl(tf.position.x + tf.origin.x, tf.position.y + tf.origin.y, tf.rotation, tf.scale.x, tf.scale.y);
//				if (tf.rotation != 0) {
					tf.affine2.translate(-tf.origin.x, -tf.origin.y);
//				}
				if (gc.parent >= 0) {
					Transform ptf = mTransform.get(gc.parent);
					tf.affine2.preMul(ptf.affine2);
				}
				tf.dirty = false;
			}
		}
	}

	protected static class Spawner extends IteratingSystem {
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<GodComponent> mGodComponent;
		protected ComponentMapper<Dot> mDot;
		public Spawner () {
			super(Aspect.all(Transform.class, GodComponent.class));
		}

		Vector2 tmp = new Vector2();
		Vector2 tmp2 = new Vector2();
		Vector2 tmp3 = new Vector2();
		float spawnDelay = .5f;
		@Override protected void process (int entityId) {
			Transform tf = mTransform.get(entityId);
			GodComponent gc = mGodComponent.get(entityId);
			if (gc.spawn) {
				gc.spawnTimer += world.delta;
				if (gc.spawnTimer > spawnDelay) {
					gc.spawnTimer -= spawnDelay;
					int spawned = world.create();
					Dot dot = mDot.create(spawned);
					dot.tint.set(Color.GREEN);

					tmp.set(gc.spawnOffset);
					tf.affine2.applyTo(tmp);
					dot.position.set(tmp);
					tmp2.set(gc.spawnOffset).add(gc.spawnDirection);

					tf.affine2.applyTo(tmp2);
					tmp3.set(tmp2).sub(tmp).nor().scl(5);
					dot.velocity.set(tmp3);
				}
			}
		}
	}


	protected static class Renderer extends IteratingSystem {
		@Wire(name = ECSTestBase.WIRE_GAME_CAM) OrthographicCamera camera;
		@Wire SpriteBatch batch;
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<GodComponent> mGod;
		public Renderer () {
			super(Aspect.all(Transform.class, GodComponent.class));
		}

		@Override protected void begin () {
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
		}

		@Override protected void process (int entityId) {
			Transform transform = mTransform.get(entityId);
			GodComponent gc = mGod.get(entityId);
			batch.setColor(gc.tint);
			batch.draw(gc.region, transform.size.x, transform.size.y, transform.affine2);
		}

		@Override protected void end () {
			batch.end();
		}
	}

	protected static class DebugRenderer extends IteratingSystem {
		@Wire(name = ECSTestBase.WIRE_GAME_CAM) OrthographicCamera camera;
		@Wire ShapeRenderer renderer;
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<GodComponent> mGodComponent;
		public DebugRenderer () {
			super(Aspect.all(Transform.class, GodComponent.class));
		}

		@Override protected void begin () {
			renderer.setProjectionMatrix(camera.combined);
			renderer.begin(ShapeRenderer.ShapeType.Line);
		}

		Vector2 tmp = new Vector2();
		Vector2 tmp2 = new Vector2();
		@Override protected void process (int entityId) {
			Transform tf = mTransform.get(entityId);
			renderer.setColor(Color.CYAN);
			rect(renderer, tf.size.x, tf.size.y, tf.affine2);
			renderer.setColor(Color.ORANGE);
			tmp.set(tf.origin);
			tf.affine2.applyTo(tmp);
			drawCross(renderer, tmp.x, tmp.y, .33f);
			renderer.setColor(Color.MAGENTA);
			tmp.set(0, 0);
			tf.affine2.applyTo(tmp);
			drawCross(renderer, tmp.x, tmp.y, .33f);

			GodComponent gc = mGodComponent.get(entityId);
			if (gc.spawn) {
				renderer.setColor(Color.RED);
				tmp.set(gc.spawnOffset);
				tf.affine2.applyTo(tmp);
				float scale = 1;
				tmp2.set(gc.spawnOffset).add(gc.spawnDirection.x * scale, gc.spawnDirection.y * scale);
//				tmp2.set(gc.spawnDirection.x * .33f, gc.spawnDirection.y * .33f);
				tf.affine2.applyTo(tmp2);
//				renderer.line(tmp.x, tmp.y, tmp.x + tmp2.x, tmp.y + tmp2.y);
				renderer.line(tmp.x, tmp.y, tmp2.x, tmp2.y);
//				drawCross(renderer, tmp.x, tmp.y, .25f);
				drawCross(renderer, tmp2.x, tmp2.y, .25f);
			}

//			renderer.setColor(Color.RED);
//			Affine2 affine2 = tf.affine2;
//			tmp.set(0, 0);
//			affine2.applyTo(tmp);
//			drawCross(renderer, tmp.x, tmp.y, .5f);
//			tmp.set(tf.size.x, 0);
//			affine2.applyTo(tmp);
//			drawCross(renderer, tmp.x, tmp.y, .5f);
//			tmp.set(0, tf.size.y);
//			affine2.applyTo(tmp);
//			drawCross(renderer, tmp.x, tmp.y, .5f);
//			tmp.set(tf.size.x, tf.size.y);
//			affine2.applyTo(tmp);
//			drawCross(renderer, tmp.x, tmp.y, .5f);

		}

		private void rect (ShapeRenderer renderer, float width, float height, Affine2 transform) {
			float x1 = transform.m02;
			float y1 = transform.m12;
			float x2 = transform.m01 * height + transform.m02;
			float y2 = transform.m11 * height + transform.m12;
			float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
			float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
			float x4 = transform.m00 * width + transform.m02;
			float y4 = transform.m10 * width + transform.m12;

			renderer.line(x1, y1, x2, y2);
			renderer.line(x2, y2, x3, y3);
			renderer.line(x3, y3, x4, y4);
			renderer.line(x4, y4, x1, y1);
		}

		private void drawCross (ShapeRenderer renderer, float x, float y, float size) {
			renderer.circle(x, y, size/2, 16);
			renderer.line(x - size, y, x + size, y);
			renderer.line(x, y - size, x, y + size);
		}

		@Override protected void end () {
			renderer.end();
		}
	}

	protected static class Transform extends Component {
		public Vector2 position = new Vector2();
		public Vector2 size = new Vector2();
		public Vector2 origin = new Vector2();
		public float rotation;
		public Vector2 scale = new Vector2(1, 1);
		public Affine2 affine2 = new Affine2();
		public boolean dirty = true;

		public Transform() {}

		public Transform set (float x, float y, float width, float height, float rotation, float originX, float originY) {
			position.set(x, y);
			size.set(width, height);
			this.rotation = rotation;
			origin.set(originX, originY);
			dirty = true;
			return this;
		}
	}

	protected static class GodComponent extends Component {
		// rest of the crap in here
		public TextureRegion region;
		public Color tint = new Color(Color.WHITE);
		public Vector2 velocity = new Vector2();
		public float rotation;
		@EntityId
		public int parent = -1;
		public boolean spawn;
		public Vector2 spawnOffset = new Vector2();
		public float spawnTimer;
		public Vector2 spawnDirection = new Vector2();

		public GodComponent() {}
	}

	protected static class DotRenderer extends IteratingSystem {
		@Wire(name = ECSTestBase.WIRE_GAME_CAM) OrthographicCamera camera;
		@Wire ShapeRenderer renderer;

		protected ComponentMapper<Dot> mDot;

		public DotRenderer () {
			super(Aspect.all(Dot.class));
		}

		@Override protected void begin () {
			renderer.setProjectionMatrix(camera.combined);
			renderer.begin(ShapeRenderer.ShapeType.Filled);
		}

		@Override protected void process (int entityId) {
			Dot dot = mDot.get(entityId);
			dot.position.add(dot.velocity.x * world.delta, dot.velocity.y * world.delta);
			renderer.setColor(dot.tint);
			renderer.circle(dot.position.x, dot.position.y, .1f, 8);
			dot.alive -= world.delta;
			if (dot.alive <= 0) world.delete(entityId);
		}

		@Override protected void end () {
			renderer.end();
		}
	}
	protected static class Dot extends Component {
		// rest of the crap in here
		public Color tint = new Color(Color.WHITE);
		public Vector2 position = new Vector2();
		public Vector2 velocity = new Vector2();
		public float alive = 5;

		public Dot () {}
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, ECSAffine2Test.class);
	}
}

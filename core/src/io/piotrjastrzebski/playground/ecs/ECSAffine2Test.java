package io.piotrjastrzebski.playground.ecs;

import com.artemis.*;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntSet;
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
	protected ComponentMapper<Inheritor> mInheritor;
	Texture texture;
	@Override protected void postInit () {
		world.inject(this, false);
		texture = new Texture("badlogic.jpg");
		TextureRegion region = new TextureRegion(texture);


		// what can we do to make the order of entities not matter?
		// as is, parents must be updated before children, ie have lower id
		// this is easy to break when entities are deleted
		// most obvious way is to make parents know about children, and update them after they are updated
		// but that feels janky
		// inheritors could sort entities by id of the parent, that way the order would be correct. ( doesnt work :( )


		int childB = create(-2f, .5f, 1.5f, 1.5f, 45f, .5f, .75f, 1, 1, region, new Color(.75f, 0, .75f, 1), -60);
		mGodComponent.get(childB).spawn(0.5f, 1f, 0f, 1f);

		int childA = create(1, 5, 2, 2, 0f, 1f, 1f, 1, 1, region, new Color(0, .75f, .75f, 1), 30);

		int childC = create(2.5f, .5f, 1.5f, 1.5f, -45f, 1f, .75f, 1, 1, region, new Color(.75f, 0, .75f, 1), 60);
		mGodComponent.get(childC).spawn(0.5f, 1f, 0f, 1f);


		int root = create(-2, -2, 4, 4, 45f, 2f, 1f, 1.5f, 1.5f, region, new Color(0, .75f, 0, 1), -45f);

		inheritTransform(childA, root);
		inheritTransform(childC, childA);
		inheritTransform(childB, childA);

	}

	private void inheritTransform (int entity, int from) {
		mInheritor.create(entity).from = from;
		mInheritor.create(from).to.add(entity);
	}

	private int create (float x, float y, float width, float height, float rotation, float originX, float originY, float scaleX,
		float scaleY, TextureRegion region, Color color, float rotate) {
		int entityId = world.create();
		Transform tm = mTransform.create(entityId);
		tm.set(x, y, width, height, rotation, originX, originY);
		tm.scale.set(scaleX, scaleY);

		GodComponent gc = mGodComponent.create(entityId);
		gc.region = region;
		gc.tint.set(color);
		gc.rotation = rotate;
		return entityId;
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


	protected static class Inheritor extends Component {
		@EntityId
		public int from = -1;
		@EntityId
		public IntBag to = new IntBag();
		public Inheritor() {}
	}

	protected static class Transformer extends IteratingSystem {
		protected ComponentMapper<Transform> mTransform;
		protected ComponentMapper<Inheritor> mInheritor;

		public Transformer () {
			super(Aspect.all(Transform.class));
		}
		IntSet processed = new IntSet();

		@Override protected void begin () {
			processed.clear();
		}

		@Override protected void process (int entityId) {
			if (processed.contains(entityId)) return;
			processed.add(entityId);
			Transform tf = mTransform.get(entityId);
			// set up our affine2 matrix
			tf.affine2.setToTrnRotScl(tf.position.x + tf.origin.x, tf.position.y + tf.origin.y, tf.rotation, tf.scale.x, tf.scale.y);
			if (tf.origin.x != 0 || tf.origin.y != 0) {
				tf.affine2.translate(-tf.origin.x, -tf.origin.y);
			}
			// update if we have a parent/children
			if (mInheritor.has(entityId)) {
				Inheritor inheritor = mInheritor.get(entityId);
				// if we have a parent, we need its transform
				if (inheritor.from >= 0) {
					// make sure it is processed
					process(inheritor.from);
					Transform ftf = mTransform.get(inheritor.from);
					tf.affine2.preMul(ftf.affine2);
				}
				// if we have children, we need to process them as well
				IntBag ids = inheritor.to;
				for (int i = 0; i < ids.size(); i++) {
					process(ids.get(i));
				}
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

		Vector3 cp = new Vector3();
		float radius = 1;
		float size = 3;
		@Override protected void begin () {
			camera.unproject(cp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			renderer.setProjectionMatrix(camera.combined);
			renderer.begin(ShapeRenderer.ShapeType.Line);
			renderer.setColor(Color.YELLOW);
			renderer.circle(cp.x, cp.y, .075f, 16);
			renderer.setColor(Color.ORANGE);
			renderer.circle(cp.x, cp.y, radius, 16);
			renderer.setColor(Color.RED);
			renderer.rect(cp.x - size/2, cp.y - size/2, size, size);
		}

		Vector2 tmp = new Vector2();
		@Override protected void process (int entityId) {
			Transform tf = mTransform.get(entityId);

			if (rectContains(cp.x, cp.y, tf.size.x, tf.size.y, tf.affine2)) {
				renderer.setColor(Color.YELLOW);
			} else if (rectOverlaps(cp.x, cp.y, radius, tf.size.x, tf.size.y, tf.affine2)) {
				renderer.setColor(Color.ORANGE);
			} else if (rectOverlaps(cp.x - size/2, cp.y - size/2, size, size, tf.size.x, tf.size.y, tf.affine2)) {
				renderer.setColor(Color.RED);
			} else {
				renderer.setColor(Color.CYAN);
			}
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
				end.set(gc.spawnOffset).add(gc.spawnDirection.x * scale, gc.spawnDirection.y * scale);
//				tmp2.set(gc.spawnDirection.x * .33f, gc.spawnDirection.y * .33f);
				tf.affine2.applyTo(end);
//				renderer.line(tmp.x, tmp.y, tmp.x + tmp2.x, tmp.y + tmp2.y);
				renderer.line(tmp.x, tmp.y, end.x, end.y);
//				drawCross(renderer, tmp.x, tmp.y, .25f);
				drawCross(renderer, end.x, end.y, .25f);
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
//			float x1 = transform.m02;
//			float y1 = transform.m12;
//			float x2 = transform.m01 * height + transform.m02;
//			float y2 = transform.m11 * height + transform.m12;
//			float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
//			float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
//			float x4 = transform.m00 * width + transform.m02;
//			float y4 = transform.m10 * width + transform.m12;
//			renderer.line(x1, y1, x2, y2);
//			renderer.line(x2, y2, x3, y3);
//			renderer.line(x3, y3, x4, y4);
//			renderer.line(x4, y4, x1, y1);
 			setRect(width, height, transform, rect);
			renderer.polygon(rect);
		}

		private final static int X1 = 0;
		private final static int Y1 = 1;
		private final static int X2 = 2;
		private final static int Y2 = 3;
		private final static int X3 = 4;
		private final static int Y3 = 5;
		private final static int X4 = 6;
		private final static int Y4 = 7;
		private final float[] rect = new float[8];
		private final float[] rect2 = new float[8];

		private boolean rectContains(float x, float y, float rWidth, float rHeight, Affine2 transform) {
			setRect(rWidth, rHeight, transform, rect);
			return Intersector.isPointInPolygon(rect, 0, 8, x, y);
		}

		private Vector2 start = new Vector2();
		private Vector2 end = new Vector2();
		private Vector2 centre = new Vector2();
		private boolean rectOverlaps (float x, float y, float radius, float rWidth, float rHeight, Affine2 transform) {
			setRect(rWidth, rHeight, transform, rect);
			if (Intersector.isPointInPolygon(rect, 0, 0, x, y)) return true;
			centre.set(x, y);
			for (int i = 0, size = rect.length; i < size; i+=2) {
				start.x = rect[(i % size)];
				start.y = rect[(i + 1) % size];
				end.x = rect[(i + 2) % size];
				end.y = rect[(i + 3) % size];
				if (Intersector.intersectSegmentCircle(start, end, centre, radius * radius)){
					return true;
				}
			}
			return false;
		}
		private boolean rectOverlaps (float x, float y, float width, float height, float rWidth, float rHeight, Affine2 transform) {
			setRect(rWidth, rHeight, transform, rect);
			setRect(x, y, width, height, rect2);
			return Intersector.overlapConvexPolygons(rect, 0, 8, rect2, 0, 8, null);
		}

		private float[] setRect(float width, float height, Affine2 transform, float[] out) {
			out[X1] = transform.m02;
			out[Y1] = transform.m12;
			out[X2] = transform.m01 * height + transform.m02;
			out[Y2] = transform.m11 * height + transform.m12;
			out[X3] = transform.m00 * width + transform.m01 * height + transform.m02;
			out[Y3] = transform.m10 * width + transform.m11 * height + transform.m12;
			out[X4] = transform.m00 * width + transform.m02;
			out[Y4] = transform.m10 * width + transform.m12;
			return out;
		}

		private float[] setRect(float x, float y, float width, float height, float[] out) {
			out[X1] = x;
			out[Y1] = y;
			out[X2] = x + width;
			out[Y2] = y;
			out[X3] = x + width;
			out[Y3] = y + height;
			out[X4] = x;
			out[Y4] = y + height;
			return out;
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

		public GodComponent spawn (float ox, float oy, float dx, float dy) {
			spawn = true;
			spawnOffset.set(ox, oy);
			spawnDirection.set(dx, dy);
			return this;
		}
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

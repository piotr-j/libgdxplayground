package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Effect inspired by Dark Echo game
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class DarkEchoTest extends BaseScreen {
	private static final String TAG = DarkEchoTest.class.getSimpleName();
	private World world;
	private Box2DDebugRenderer debugRenderer;
	protected final static short BOUNDARY_MASK = 1;
	protected final static short ECHO_MASK = 2;
	protected final static short ECHO_CATEGORY = BOUNDARY_MASK;
	protected final static short BOUNDARY_CATEGORY = ECHO_MASK;
	private boolean stepping = true;

	public DarkEchoTest (GameReset game) {
		super(game);
		world = new World(new Vector2(), true);
		debugRenderer = new Box2DDebugRenderer();
		createBoundary();
		gameCamera.zoom = .5f;
		gameCamera.update();
	}

	@Override public boolean scrolled (float amountX, float amountY) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + amountX * 0.01f, 0.1f, 1f);
		return true;
	}

	private Vector3 mouse = new Vector3();
	private boolean touched;
	private float touchTime = 0;
	private float simScale = 1;
	private float updateTimer = 0;
	private float updateStep = 1/60f;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		processInput();

		if (stepping) {
			delta *= simScale;
			updateTimer += delta;
			if (updateTimer >= updateStep) {
				updateTimer -= updateStep;
				if (touched) {
					touchTime += delta;
				}
				Iterator<Echo> it = echos.iterator();
				while (it.hasNext()) {
					Echo echo = it.next();
					echo.update(delta);
					if (echo.isDone()) {
						// TODO pool
						echo.reset();
						it.remove();
					}
				}
				world.step(1 / 30f, 6, 4);
			}
		}
		debugRenderer.render(world, gameCamera.combined);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.GREEN);
		renderer.circle(mouse.x, mouse.y, .1f, 16);
		renderer.setColor(Color.WHITE);
		for (Echo echo : echos) {
			echo.drawDebug(renderer);
		}
		renderer.end();
	}

	private void processInput () {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			stepping = !stepping;
		}
		float scale = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)?5f:1f;
		if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
			gameCamera.position.y += 0.1f * scale;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			gameCamera.position.y -= 0.1f * scale;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			gameCamera.position.x -= 0.1f * scale;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			gameCamera.position.x += 0.1f * scale;
		}
		gameCamera.update();
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
			simScale = MathUtils.clamp(simScale - 0.05f * scale, 0.01f, 2f);
			Gdx.app.log("", "simScale = " + simScale);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
			simScale = MathUtils.clamp(simScale + 0.05f * scale, 0.01f, 2f);
			Gdx.app.log("", "simScale = " + simScale);
		}
	}

	private void spawnEchoFromTouch () {
		// touch time should influence how strong are the echoes, alive time + number of bounces
		int bounces = (int)(touchTime * 3);
		spawnEcho(mouse.x, mouse.y, touchTime, bounces);
		touchTime = 0;
	}

	private final static int echoCount = 16;
	private final static float echoSpeed = 3;
	private void spawnEcho (float cx, float cy, float magnitude, int bounces) {
		Gdx.app.log("", "Echo at " + cx+ ", " + cy + ", alive=" + (magnitude * 5) + ", b=" + bounces);
		float angle = 360f / (echoCount);
		float offset = MathUtils.random(angle);
		for (int i = 0; i < echoCount; i++) {
			float a = angle * i + offset;
			float vx = MathUtils.sinDeg(a) * echoSpeed;
			float vy = MathUtils.cosDeg(a) * echoSpeed;
//			createEcho(cx, cy, vx, vy, magnitude * 20, bounces*3);
			createEcho(cx, cy, vx, vy, 20, 5);
		}
	}

	private Array<Echo> echos = new Array<>();
	private void createEcho (float x, float y, float vx, float vy, float alive, int bounces) {
		Echo echo = new Echo(world);
		echo.init(x, y, vx, vy, alive, bounces);
		echos.add(echo);
	}

	public static class Echo implements Pool.Poolable, RayCastCallback {
		private boolean isDone;
		private Vector2 pos = new Vector2();
		private Vector2 vel = new Vector2();
		private World world;
		private int bounces;
		private float alive;
		private int posCount = 64;
		private CircularBuffer<Vector2> buffer;
		private float maxAlive;

		public Echo (World world) {
			this.world = world;
			buffer = new CircularBuffer<>(posCount, false);
			for (int i = 0; i < posCount; i++) {
				buffer.store(new Vector2(-100, -100));
			}
		}

		public void hit() {
			bounces--;
		}
		private float speed2;
		public void init (float x, float y, float vx, float vy, float alive, int bounces) {
			this.alive = alive;
			this.maxAlive = alive;
			this.bounces = bounces;
			pos.set(x, y);
			speed2 = vel.set(vx, vy).len2();
		}

		private float margin = 0.01f;
		private Vector2 dir = new Vector2();
		private Vector2 tmp = new Vector2();
		public void update(float delta) {
			alive -= delta;
			if (bounces < 0 || alive <= 0) {
				// TODO we want some nice fade when we run out of bounces
				if (alive <= 0)
					isDone = true;
			} else {
				dir.set(pos).add(vel.x * delta, vel.y * delta);
				if (!pos.epsilonEquals(dir, 0.001f))
					world.rayCast(this, pos, dir);
				if (hasCollision) {
					hit();
					tmp.set(vel);
					lastNormal.scl(tmp.dot(lastNormal)).scl(2);
					tmp.sub(lastNormal);
					vel.set(tmp);
					hasCollision = false;
				}
				pos.add(vel.x * delta, vel.y * delta);

				if (!buffer.peak().epsilonEquals(pos, 0.001f)) {
					buffer.store(buffer.read().set(pos));
				}
				// TODO we have points, need to make a mesh from them

			}
		}
		private Vector2 lastPoint = new Vector2();
		private Vector2 lastNormal = new Vector2();
		private boolean hasCollision;
		@Override public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			if (fixture.getBody().getUserData() instanceof Echo) {
				return fraction;
			}
			lastPoint.set(point);
			lastNormal.set(normal);
			hasCollision = true;
			return fraction;
		}

		public void drawDebug(ShapeRenderer renderer) {
			int id = posCount;
			float aa = alive/maxAlive;
			renderer.setColor(1, 1, 1, aa);
			renderer.circle(pos.x, pos.y, .05f, 8);
			for (Vector2 pos : buffer) {
				float a = 1-id/(float)posCount;
				renderer.setColor(1, 1, 1, a * aa);
				renderer.circle(pos.x, pos.y, .025f, 8);
				id--;
			}
			renderer.setColor(Color.GOLD);
			renderer.line(pos.x, pos.y, dir.x, dir.y);
			if (hasCollision) {
//				hasCollision = false;
				renderer.setColor(Color.CYAN);
				renderer.line(lastPoint.x, lastPoint.y, lastPoint.x + lastNormal.x, lastPoint.y + lastNormal.y);
			}

//			for (int i = 0; i < posCount; i++) {
//				Vector2 pos = buffer.read();
//				buffer.store(pos);
//			}
		}

		public boolean isDone () {
			return isDone;
		}

		@Override public void reset () {
			// TODO dont dispose, pool
//			world.destroyBody(body);
//			body = null;
		}
	}

	@Override public void dispose () {
		super.dispose();
		debugRenderer.dispose();
		world.dispose();
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(mouse.set(screenX, screenY, 0));
		return super.mouseMoved(screenX, screenY);
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT) {
			touched = true;
			return true;
		}
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT) {
			spawnEchoFromTouch();
			touched = false;
			return true;
		}
		return false;
	}

	private Body createBoundary () {
		BodyDef def = new BodyDef();
		def.type = BodyDef.BodyType.StaticBody;
		Body body = world.createBody(def);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.restitution = 1;
		fixtureDef.filter.maskBits = BOUNDARY_MASK;
		fixtureDef.filter.categoryBits = BOUNDARY_CATEGORY;
		// left hallway
		ChainShape shape = new ChainShape();
		shape.createChain(new float[] {
			-5.07f, 1.36f,
			-11, 1.36f,
			-11, -1.36f,
			-5.07f, -1.36f});
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
		shape.dispose();

		// right hallway
		shape = new ChainShape();
		shape.createChain(new float[] {
			5.07f, 1.36f,
			11, 1.36f,
			11, -1.36f,
			5.07f, -1.37f});
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
		shape.dispose();

		// top dome
		shape = new ChainShape();
		shape.createChain(createArc(16, 150, -90 + 15f, 5.25f, 0, 0));
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
		shape.dispose();

		// bottom dome
		shape = new ChainShape();
		shape.createChain(createArc(16, 150, 90 + 15f, 5.25f, 0, 0));
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
		shape.dispose();

		return body;
	}

	private float[] createCircle (int points, float centerX, float centerY) {
		return createArc(points, 360, 0, 1, centerX, centerY);
	}

	private float[] createArc (int points, float angle, float centerX, float centerY) {
		return createArc(points, angle, 0, 1, centerX, centerY);
	}

	private float[] createArc (int points, float angle, float angleOffset, float scale, float centerX, float centerY) {
		float[] circle = new float[points * 2];
		for (int i = 0; i < points; i++) {
			float a = angle/(points-1) * i;
			float x = MathUtils.sinDeg(a + angleOffset) * scale + centerX;
			float y = MathUtils.cosDeg(a + angleOffset) * scale + centerY;
			circle[i * 2] = x;
			circle[i * 2 + 1] = y;
		}
		return circle;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, DarkEchoTest.class);
	}

	public static class CircularBuffer<T> implements Iterable<T> {
		private T[] items;
		private boolean resizable;
		private int head;
		private int tail;
		private int size;
		private CircularBufferIterator<T> iterator;

		/** Creates a resizable {@code CircularBuffer}. */
		public CircularBuffer () {
			this(16, true);
		}

		/** Creates a resizable {@code CircularBuffer} with the given initial capacity.
		 * @param initialCapacity the initial capacity of this circular buffer */
		public CircularBuffer (int initialCapacity) {
			this(initialCapacity, true);
		}

		/** Creates a {@code CircularBuffer} with the given initial capacity.
		 * @param initialCapacity the initial capacity of this circular buffer
		 * @param resizable whether this buffer is resizable or has fixed capacity */
		@SuppressWarnings("unchecked")
		public CircularBuffer (int initialCapacity, boolean resizable) {
			this.items = (T[])new Object[initialCapacity];
			this.resizable = resizable;
			this.head = 0;
			this.tail = 0;
			this.size = 0;
		}

		/** Adds the given item to the tail of this circular buffer.
		 * @param item the item to add
		 * @return {@code true} if the item has been successfully added to this circular buffer; {@code false} otherwise. */
		public boolean store (T item) {
			if (size == items.length) {
				if (!resizable) return false;

				// Resize this queue
				resize(Math.max(8, (int)(items.length * 1.75f)));
			}
			size++;
			items[tail++] = item;
			if (tail == items.length) tail = 0;
			return true;
		}

		/** Removes and returns the item at the head of this circular buffer (if any).
		 * @return the item just removed or {@code null} if this circular buffer is empty. */
		public T read () {
			if (size > 0) {
				size--;
				T item = items[head];
				items[head] = null; // Avoid keeping useless references
				if (++head == items.length) head = 0;
				return item;
			}

			return null;
		}

		public T peak () {
			if (size > 0) {
				return items[head];
			}
			return null;
		}

		/** Removes all items from this circular buffer. */
		public void clear () {
			final T[] items = this.items;
			if (tail > head) {
				int i = head, n = tail;
				do {
					items[i++] = null;
				} while (i < n);
			} else if (size > 0) { // NOTE: when head == tail the buffer can be empty or full
				for (int i = head, n = items.length; i < n; i++)
					items[i] = null;
				for (int i = 0, n = tail; i < n; i++)
					items[i] = null;
			}
			this.head = 0;
			this.tail = 0;
			this.size = 0;
		}

		/** Returns {@code true} if this circular buffer is empty; {@code false} otherwise. */
		public boolean isEmpty () {
			return size == 0;
		}

		/** Returns {@code true} if this circular buffer contains as many items as its capacity; {@code false} otherwise. */
		public boolean isFull () {
			return size == items.length;
		}

		/** Returns the number of elements in this circular buffer. */
		public int size () {
			return size;
		}

		/** Returns {@code true} if this circular buffer can be resized; {@code false} otherwise. */
		public boolean isResizable () {
			return resizable;
		}

		/** Sets the flag specifying whether this circular buffer can be resized or not.
		 * @param resizable the flag */
		public void setResizable (boolean resizable) {
			this.resizable = resizable;
		}

		/** Increases the size of the backing array (if necessary) to accommodate the specified number of additional items. Useful
		 * before adding many items to avoid multiple backing array resizes.
		 * @param additionalCapacity the number of additional items */
		public void ensureCapacity (int additionalCapacity) {
			int newCapacity = size + additionalCapacity;
			if (items.length < newCapacity) resize(newCapacity);
		}

		/** Creates a new backing array with the specified capacity containing the current items.
		 * @param newCapacity the new capacity */
		protected void resize (int newCapacity) {
			@SuppressWarnings("unchecked")
			T[] newItems = (T[])ArrayReflection.newInstance(items.getClass().getComponentType(), newCapacity);
			if (tail > head) {
				System.arraycopy(items, head, newItems, 0, size);
			} else if (size > 0) { // NOTE: when head == tail the buffer can be empty or full
				System.arraycopy(items, head, newItems, 0, items.length - head);
				System.arraycopy(items, 0, newItems, items.length - head, tail);
			}
			head = 0;
			tail = size;
			items = newItems;
		}

		@Override public Iterator<T> iterator () {
			if (iterator == null) iterator = new CircularBufferIterator<>(this, true);
			iterator.reset();
			return iterator;
		}

		@Override public void forEach (Consumer<? super T> action) {
			throw new IllegalStateException("NotImplemented");
		}

		@Override public Spliterator<T> spliterator () {
			throw new IllegalStateException("NotImplemented");
		}

		static public class CircularBufferIterator<T> implements Iterator<T>, Iterable<T> {
			private final CircularBuffer<T> buffer;
			private final boolean allowRemove;
			int size;
			int index;
			int head;
			int tail;
			public CircularBufferIterator (CircularBuffer<T> array) {
				this(array, true);
			}

			public CircularBufferIterator (CircularBuffer<T> array, boolean allowRemove) {
				this.buffer = array;
				this.allowRemove = allowRemove;
				reset();
			}

			public boolean hasNext () {
				if (tail > 0) {
					return index != (tail -1);
				} else {
					return index != (size -1);
				}
			}

			public T next () {
				index = (index+1)%size;
				return buffer.items[index];
			}

			public void remove () {
				if (!allowRemove) throw new GdxRuntimeException("Remove not allowed.");
				buffer.read();
				head = buffer.head;
				tail = buffer.tail;
			}

			public void reset () {
				head = buffer.head;
				tail = buffer.tail;
				index = head;
				size = buffer.size;
			}

			public Iterator<T> iterator () {
				return this;
			}
		}
	}

}

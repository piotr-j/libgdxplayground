package io.piotrjastrzebski.playground.box2dtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 31/07/15.
 */
public class InterpolationTest extends BaseScreen {
	protected static Color START_COLOR = new Color(1, 0, 0, 0.2f);
	protected static Color INTERPOLATED_COLOR = new Color(0, 1, 0, 1f);
	protected static Color END_COLOR = new Color(0, 0, 1, 0.2f);

	World world;
	Array<InterpolatedObject> interpolatedObjects = new Array<>();
	Texture largeBox;
	Texture smallBox;
	Texture smallCircle;
	Texture largeCircle;
	Box2DDebugRenderer debugRenderer;
	FPSLogger logger;
	boolean debugDraw = false;

	public InterpolationTest (GameReset game) {
		super(game);
		debugRenderer = new Box2DDebugRenderer();
		world = new World(new Vector2(0, -10), true);
		largeBox = new Texture("box2d/box64.png");
		smallBox = new Texture("box2d/box32.png");
		smallCircle = new Texture("box2d/circle32.png");
		largeCircle = new Texture("box2d/circle64.png");
		createBounds();
		reset();

		logger = new FPSLogger();
	}

	Body groundBody;
	private void createBounds () {
		float halfWidth = VP_WIDTH / 2f - 0.5f;
		float halfHeight = VP_HEIGHT / 2f - 0.5f;
		ChainShape chainShape = new ChainShape();
		chainShape.createLoop(new float[] {-halfWidth, -halfHeight, halfWidth, -halfHeight,
			halfWidth, halfHeight, -halfWidth, halfHeight});
		BodyDef chainBodyDef = new BodyDef();
		chainBodyDef.type = BodyDef.BodyType.StaticBody;
		groundBody = world.createBody(chainBodyDef);
		groundBody.createFixture(chainShape, 0);
		chainShape.dispose();
	}

	private void reset () {
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		for (InterpolatedObject interpolatedObject : interpolatedObjects) {
			world.destroyBody(interpolatedObject.body);
		}
		interpolatedObjects.clear();

		PolygonShape rect = new PolygonShape();
		CircleShape circle = new CircleShape();
		for (int i = 0; i < 50; i++) {
			float x = MathUtils.random(-15, 15);
			float y = MathUtils.random(-8, 8);
			float rotation = MathUtils.random(90);
			switch (MathUtils.random(4)) {
			case 0: {
				rect.setAsBox(1, 1);
				InterpolatedObject io = createIO(x, y, rotation, largeBox, rect);
				io.isBox = true;
				interpolatedObjects.add(io);
			} break;
			case 1: {
				rect.setAsBox(.5f, .5f);
				InterpolatedObject io = createIO(x, y, rotation, smallBox, rect);
				io.isBox = true;
				interpolatedObjects.add(io);
			} break;
			case 2:
				circle.setRadius(1f);
				interpolatedObjects.add(createIO(x, y, rotation, largeCircle, circle));
				break;
			case 3:
				circle.setRadius(.5f);
				interpolatedObjects.add(createIO(x, y, rotation, smallCircle, circle));
				break;
			}
		}
		rect.dispose();
		circle.dispose();
	}

	private InterpolatedObject createIO (float x, float y, float rotation, Texture texture, Shape shape) {
		InterpolatedObject interpolatedObject = new InterpolatedObject(x, y, rotation, texture);
		BodyDef def = new BodyDef();
		def.position.set(x, y);
		def.angle = rotation * MathUtils.degreesToRadians;
		def.type = BodyDef.BodyType.DynamicBody;
		interpolatedObject.body = world.createBody(def);
		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.restitution = .5f;
		fd.friction = .25f;
		fd.density = .5f;
		interpolatedObject.body.createFixture(fd);
		return interpolatedObject;
	}

	boolean simPaused;
	float accumulator;
	float manualStepDelay;
	float rewindDelay;
	int fpsLimit = 60;
	@Override public void render (float delta) {
		long start = TimeUtils.nanoTime();
		manualStepDelay -= delta;
		rewindDelay -= delta;
		if (!simPaused) {
			step(delta);
		} else {
			if (Gdx.input.isKeyPressed(Input.Keys.S)){
				if (manualStepDelay <= 0) {
					step(Gdx.graphics.getDeltaTime());
					manualStepDelay = .1f;
				}
			} else if (Gdx.input.isKeyPressed(Input.Keys.R)){
				if (rewindDelay <= 0) {
					for (InterpolatedObject interpolatedObject : interpolatedObjects) {
						interpolatedObject.rewind();
					}
					rewindDelay = .1f;
				}
			}
		}
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (debugDraw) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			renderer.setProjectionMatrix(gameCamera.combined);
			renderer.begin(ShapeRenderer.ShapeType.Filled);
			for (InterpolatedObject interpolatedObject : interpolatedObjects) {
				interpolatedObject.draw(renderer);
			}
			renderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
			renderer.setProjectionMatrix(gameCamera.combined);
			renderer.begin(ShapeRenderer.ShapeType.Line);
			for (InterpolatedObject interpolatedObject : interpolatedObjects) {
				interpolatedObject.draw(renderer);
			}
			renderer.end();
		} else {
			batch.setProjectionMatrix(gameCamera.combined);
			batch.begin();
			for (InterpolatedObject interpolatedObject : interpolatedObjects) {
				interpolatedObject.draw(batch);
			}
			batch.end();
		}

//		debugRenderer.render(world, gameCamera.combined);

		logger.log();
		if (fpsLimit != 60) {
			long targetFrameTime = TimeUtils.millisToNanos((long)((1000d/fpsLimit)));
			try {
				while ((TimeUtils.nanoTime() - start) < targetFrameTime) {
					Thread.sleep(1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected void step (float delta) {
		accumulator += delta;
		int steps = 0;
		float stepTime = 1 / 30f;
		while (stepTime < accumulator && 3 > steps) {
			world.step(stepTime, 6, 4);
			accumulator -= stepTime;
			steps++;
			for (InterpolatedObject interpolatedObject : interpolatedObjects) {
				interpolatedObject.fixedUpdate();
			}
		}
		float alpha = accumulator / stepTime;
		for (InterpolatedObject interpolatedObject : interpolatedObjects) {
			interpolatedObject.variableUpdate(delta, alpha);
		}
	}

	protected static class InterpolatedTransform {
		// current interpolated values
		public float x, y, rotation;
		// start values
		protected float sx, sy, sRotation;
		// end values
		protected float ex, ey, eRotation;

		public void init (float ix, float iy, float iRotation) {
			sx = x = ey = ix;
			sy = y = ey = iy;
			sRotation = rotation = eRotation = iRotation;
		}

		public void update (float ux, float uy, float uRotation) {
			sx = ex;
			sy = ey;
			sRotation = eRotation;
			ex = ux;
			ey = uy;
			eRotation = uRotation;
		}

		public void interpolate (float alpha) {
			x = sx + (ex - sx) * alpha;
			y = sy + (ey - sy) * alpha;
			float delta = ((eRotation - sRotation + 360 + 180) % 360) - 180;
			rotation = (sRotation + delta * alpha + 360) % 360;
		}
	}

	protected static class InterpolatedObject {
		public Body body;
		public Texture texture;
		private float width;
		private float height;
		private int srcWidth;
		private int srcHeight;
		public boolean isBox;
		private InterpolatedTransform itm;
		FloatArray oldState;

		public InterpolatedObject (float x, float y, float rotation, Texture texture) {
			itm = new InterpolatedTransform();
			itm.init(x, y, rotation);

			this.texture = texture;
			srcWidth = texture.getWidth();
			width = srcWidth * INV_SCALE;
			srcHeight = texture.getHeight();
			height = srcHeight * INV_SCALE;

			oldState = new FloatArray();
		}

		public void fixedUpdate () {
			Vector2 position = body.getPosition();
			float angle = body.getAngle();
			itm.update(position.x, position.y, angle * MathUtils.radiansToDegrees);
			Vector2 velocity = body.getLinearVelocity();
			float angular = body.getAngularVelocity();

			// NOTE this could probably be packed into 1 or 2 ints
			oldState.add(position.x);
			oldState.add(position.y);
			oldState.add(angle);
			oldState.add(velocity.x);
			oldState.add(velocity.y);
			oldState.add(angular);
		}

		public void rewind () {
			if (oldState.size > 6) {
				int id = oldState.size - 6;
				float x = oldState.get(id);
				float y = oldState.get(id + 1);
				float angle = oldState.get(id + 2);
				body.setTransform(x, y, angle);
				body.setLinearVelocity(oldState.get(id + 3), oldState.get(id + 4));
				body.setAngularVelocity(oldState.get(id + 5));
				oldState.size -= 6;
				itm.update(x, y, angle * MathUtils.radiansToDegrees);
				itm.interpolate(1);
			}
		}

		public void variableUpdate (float delta, float alpha) {
			itm.interpolate(alpha);
		}

		public void draw (Batch batch) {
			batch
				.draw(texture, itm.x - width / 2, itm.y - height / 2, width / 2, height / 2, width, height, 1, 1, itm.rotation, 0, 0,
					srcWidth, srcHeight, false, false);
		}

		public void draw (ShapeRenderer renderer) {
			if (isBox) {
				renderer.setColor(START_COLOR);
				renderer.rect(itm.sx - width / 2, itm.sy - height / 2, width / 2, height / 2, width, height, 1, 1, itm.sRotation);

				renderer.setColor(END_COLOR);
				renderer.rect(itm.ex - width / 2, itm.ey - height / 2, width / 2, height / 2, width, height, 1, 1, itm.eRotation);

				renderer.setColor(INTERPOLATED_COLOR);
				renderer.rect(itm.x - width / 2, itm.y - height / 2, width / 2, height / 2, width, height, 1, 1, itm.rotation);

			} else {
				renderer.setColor(START_COLOR);
				renderer.circle(itm.sx, itm.sy, width / 2, 32);

				renderer.setColor(END_COLOR);
				renderer.circle(itm.ex, itm.ey, width / 2, 32);

				renderer.setColor(INTERPOLATED_COLOR);
				renderer.circle(itm.x, itm.y, width / 2, 32);
			}
		}
	}

	Body hitBody;
	Vector3 testPoint = new Vector3();
	QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.getBody() == groundBody)
				return true;

			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	private MouseJoint mouseJoint;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(testPoint.set(screenX, screenY, 0));

		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f,
			testPoint.x + 0.1f, testPoint.y + 0.1f);

		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint.x, testPoint.y);
			def.maxForce = 1000.0f * hitBody.getMass();

			mouseJoint = (MouseJoint) world.createJoint(def);
			hitBody.setAwake(true);
		}

		return super.touchDown(screenX, screenY, pointer, button);
	}
	Vector2 target = new Vector2();

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		gameCamera.unproject(testPoint.set(x, y, 0));
		target.set(testPoint.x, testPoint.y);
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if (mouseJoint != null) {
			mouseJoint.setTarget(target);
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.F5) {
			reset();
		}
		if (keycode == Input.Keys.Z) {
			debugDraw = !debugDraw;
		}
		if (keycode == Input.Keys.A) {
			simPaused = !simPaused;
		}
		return super.keyDown(keycode);
	}

	@Override public void dispose () {
		super.dispose();
		largeBox.dispose();
		smallBox.dispose();
		largeCircle.dispose();
		smallCircle.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 4);
		PlaygroundGame.start(args, config, InterpolationTest.class);
	}
}

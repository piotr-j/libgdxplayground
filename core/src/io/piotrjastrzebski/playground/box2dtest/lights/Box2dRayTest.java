package io.piotrjastrzebski.playground.box2dtest.lights;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 31/07/15.
 */
public class Box2dRayTest extends BaseScreen {
	public final static float STEP_TIME = 1.0f / 60.0f;
	private final static int MAX_STEPS = 3;
	private float stepDiv = 60;
	private float stepTime = 1f/stepDiv;

	World box2dWorld;
	Array<Box> boxes = new Array<>();
	Texture largeBox;
	Texture smallBox;
	Texture smallCircle;
	Texture largeCircle;
	Box2DDebugRenderer debugRenderer;
	boolean debugDraw = false;
//	SimpleRayLight light;
//	FancyRayLight light;
//	FancierRayLight light;
//	PolyRayLight light;
	PolygonSpriteBatch pBatch;

	Light light;
	Texture lightTexture;

	public Box2dRayTest (GameReset game) {
		super(game);
		lightTexture = new Texture("simplelighttest/light.png");
		pBatch = new PolygonSpriteBatch();
		debugRenderer = new Box2DDebugRenderer();
		box2dWorld = new World(new Vector2(0, -10), true);
		largeBox = new Texture("box2d/box64.png");
		smallBox = new Texture("box2d/box32.png");
		smallCircle = new Texture("box2d/circle32.png");
		largeCircle = new Texture("box2d/circle64.png");
		createBounds();
		reset();
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
		groundBody = box2dWorld.createBody(chainBodyDef);
		groundBody.createFixture(chainShape, 0);
		chainShape.dispose();
	}

	private void reset () {
		if (mouseJoint != null) {
			box2dWorld.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		for (Box box : boxes) {
			box2dWorld.destroyBody(box.body);
		}
		boxes.clear();

		for (int i = 0; i < 50; i++) {
			float x = MathUtils.random(-15, 15);
			float y = MathUtils.random(-8, 8);
			float rotation = MathUtils.random(90);
			switch (MathUtils.random(4)) {
			case 0:
				createShape(x, y, rotation, largeBox, true);
				break;
			case 1:
				createShape(x, y, rotation, smallBox, true);
				break;
			case 2:
				createShape(x, y, rotation, largeCircle, false);
				break;
			case 3:
				createShape(x, y, rotation, smallCircle, false);
				break;
			}
		}

		light = new SimpleRayLight(0, 0, 4, box2dWorld);
//		light = new FancyRayLight(0, 0, 4, box2dWorld);
//		light = new FancierRayLight(0, 0, 4, box2dWorld);
//		light = new PolyRayLight(0, 0, 4, box2dWorld, new TextureRegion(lightTexture));
	}

	private void createShape (float x, float y, float rotation, Texture texture, boolean isBox) {
		Box box = new Box(x, y, rotation, texture);
		BodyDef def = new BodyDef();
		def.position.set(x, y);
		def.angle = rotation * MathUtils.degreesToRadians;
		def.type = BodyDef.BodyType.DynamicBody;
		box.body = box2dWorld.createBody(def);
		Shape shape;
		if (isBox) {
			PolygonShape poly = new PolygonShape();
			poly.setAsBox(box.width / 2, box.height / 2);
			shape = poly;
		} else {
			CircleShape circle = new CircleShape();
			circle.setRadius(box.width/2);
			box.body.setTransform(x, y, 0);
			box.body.setFixedRotation(true);
			shape = circle;
		}
		box.body.createFixture(shape, 1);
		box.isBox = isBox;
		shape.dispose();

		boxes.add(box);
	}

	@Override public void render (float delta) {
		super.render(delta);
		update(delta);

		draw();
		stage.act(delta);
		stage.draw();
	}

	private void update (float delta) {
		box2dWorld.step(stepTime, 6, 2);
		fixedUpdate();
		variableUpdate(delta, 1);
	}

	private void fixedUpdate () {
		for (Box box : boxes) {
			box.fixedUpdate();
		}
		light.fixedUpdate();
	}

	private void variableUpdate (float delta, float alpha) {
		for (Box box : boxes) {
			box.variableUpdate(delta, alpha);
		}
	}

	private void draw () {
		Gdx.gl.glClearColor(0.8f, 0.8f, 0.8f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Box box : boxes) {
			box.draw(batch);
		}
		batch.end();
		debugRenderer.render(box2dWorld, gameCamera.combined);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		light.draw(renderer);
		renderer.end();

		pBatch.setProjectionMatrix(gameCamera.combined);
		pBatch.begin();
		light.draw(pBatch);
		pBatch.end();
	}

	private class Box {
		public Body body;
		public Texture texture;
		private Transform start = new Transform();
		private Transform current = new Transform();
		private Transform target = new Transform();
		private float width;
		private float height;
		private int srcWidth;
		private int srcHeight;
		public boolean isBox;

		public Box (float x, float y, float rotation, Texture texture) {
			current.set(x, y, rotation);
			start.set(x, y, rotation);
			target.set(x, y, rotation);

			this.texture = texture;
			srcWidth = texture.getWidth();
			width = srcWidth * INV_SCALE;
			srcHeight = texture.getHeight();
			height = srcHeight * INV_SCALE;
		}

		public void fixedUpdate () {
			Vector2 position = body.getPosition();
			target.set(position.x, position.y, body.getAngle() * MathUtils.radiansToDegrees);
			start.set(current);
		}

		public void variableUpdate (float delta, float alpha) {
			current.interpolate(start, target, alpha);
		}

		public void draw (Batch batch) {
			batch
				.draw(texture, current.x - width / 2, current.y - height / 2, width / 2, height / 2, width, height, 1, 1,
					current.rot, 0, 0, srcWidth, srcHeight, false, false);
		}

		private class Transform {
			public float x;
			public float y;
			public float rot;

			public void set (float x, float y, float rot) {
				this.x = x;
				this.y = y;
				this.rot = rot;
			}

			public void set (Transform other) {
				x = other.x;
				y = other.y;
				rot = other.rot;
			}

			public void interpolate (Transform src, Transform dst, float alpha) {
				// TODO this shit is busted fix!
				x = Interpolation.linear.apply(src.x, dst.x, alpha);
				y = Interpolation.linear.apply(src.y, dst.y, alpha);
				float angle = dst.rot - src.rot;
				angle = angle < 0 ? 360 - (-angle % 360) : angle % 360;
				if (angle > 180) angle -= 360;
				rot = src.rot + angle * alpha;
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
	boolean dragLight;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(testPoint.set(screenX, screenY, 0));

		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		box2dWorld.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f,
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

			mouseJoint = (MouseJoint) box2dWorld.createJoint(def);
			hitBody.setAwake(true);
		} else if (button == Input.Buttons.LEFT) {
			light.setPos(testPoint.x, testPoint.y);
			dragLight = true;
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
		} else if (dragLight){
			light.setPos(testPoint.x, testPoint.y);
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			box2dWorld.destroyJoint(mouseJoint);
			mouseJoint = null;
		} else if (dragLight) {
			light.setPos(testPoint.x, testPoint.y);
			dragLight = false;
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
		if (keycode == Input.Keys.NUM_1) {
			light = new SimpleRayLight(0, 0, 4, box2dWorld);
		}
		if (keycode == Input.Keys.NUM_2) {
			light = 	new FancyRayLight(0, 0, 4, box2dWorld);
		}
		if (keycode == Input.Keys.NUM_3) {
			light = new FancierRayLight(0, 0, 4, box2dWorld);
		}
		if (keycode == Input.Keys.NUM_4) {
			light = new PolyRayLight(0, 0, 4, box2dWorld, new TextureRegion(lightTexture));
		}
		return super.keyDown(keycode);
	}

	@Override public void dispose () {
		super.dispose();
		largeBox.dispose();
		smallBox.dispose();
		largeCircle.dispose();
		smallCircle.dispose();
		pBatch.dispose();
		lightTexture.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, Box2dRayTest.class);
	}

}

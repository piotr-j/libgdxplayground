package io.piotrjastrzebski.playground.box2dtest.iforce2d;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 31/07/15.
 */
public abstract class BaseIForce2dTest extends BaseScreen {
	protected World world;
	protected Box2DDebugRenderer debugRenderer;

	public BaseIForce2dTest (GameReset game) {
		super(game);
		debugRenderer = new Box2DDebugRenderer();
		world = new World(new Vector2(0, -9.8f), true);

		createBounds();
		reset();
	}

	protected Body ground;
	private void createBounds () {
		float halfWidth = VP_WIDTH / 2f - 0.5f;
		float halfHeight = VP_HEIGHT / 2f - 0.5f;
		ChainShape chainShape = new ChainShape();
		chainShape.createLoop(new float[] {-halfWidth, -halfHeight, halfWidth, -halfHeight,
			halfWidth, halfHeight, -halfWidth, halfHeight});
		BodyDef chainBodyDef = new BodyDef();
		chainBodyDef.type = BodyDef.BodyType.StaticBody;
		ground = world.createBody(chainBodyDef);
		ground.createFixture(chainShape, 0);
		chainShape.dispose();
	}

	private void reset () {
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		update(delta);
		world.step(1f / 60f, 6, 4);
		debugRenderer.render(world, gameCamera.combined);
	}

	public abstract void update (float delta);

	protected Body hitBody;
	protected Vector2 cursor = new Vector2();
	protected Vector3 testPoint = new Vector3();
	protected QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.getBody() == ground)
				return true;

			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(testPoint.set(screenX, screenY, 0));
		cursor.set(testPoint.x, testPoint.y);
		return false;
	}

	protected boolean isTouching;
	protected MouseJoint mouseJoint;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		isTouching = true;
		gameCamera.unproject(testPoint.set(screenX, screenY, 0));
		cursor.set(testPoint.x, testPoint.y);
		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f,
			testPoint.x + 0.1f, testPoint.y + 0.1f);

		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			MouseJointDef def = new MouseJointDef();
			def.bodyA = ground;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint.x, testPoint.y);
			def.maxForce = 1000.0f * hitBody.getMass();

			mouseJoint = (MouseJoint) world.createJoint(def);
			hitBody.setAwake(true);
		}

		return super.touchDown(screenX, screenY, pointer, button);
	}

	protected Vector2 target = new Vector2();
	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		gameCamera.unproject(testPoint.set(x, y, 0));
		cursor.set(testPoint.x, testPoint.y);
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
		isTouching = false;
		gameCamera.unproject(testPoint.set(x, y, 0));
		cursor.set(testPoint.x, testPoint.y);
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
		return super.keyDown(keycode);
	}

	@Override public void dispose () {
		super.dispose();
		world.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, BaseIForce2dTest.class);
	}
}

package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Test for reflecting vector with box2d
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class Box2dReflectTest extends BaseScreen implements RayCastCallback {
	private static final String TAG = Box2dReflectTest.class.getSimpleName();
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private Body boundary;
	public Box2dReflectTest (GameReset game) {
		super(game);
		world = new World(new Vector2(), true);
		debugRenderer = new Box2DDebugRenderer();
		boundary = createBoundary();
		gameCamera.zoom = .7f;
//		gameCamera.position.x += 3;
		gameCamera.update();
	}

	private Circle handleA = new Circle(3, -1.25f, 0.25f);
	private Circle handleB = new Circle(0, -.5f, 0.25f);

	private Vector3 mouse = new Vector3();
	private int reflections = 3;
	private Vector2 rayStart = new Vector2();
	private Vector2 rayEnd = new Vector2();
	private Vector2 tmp = new Vector2();
	private Vector2 tmp2 = new Vector2();
	int rayCastId;

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		world.step(1/30f, 6, 4);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.LIGHT_GRAY);
		for (Fixture fixture : boundary.getFixtureList()) {
			ChainShape shape = (ChainShape)fixture.getShape();
			for (int i = 0; i < shape.getVertexCount() -1; i++) {
				shape.getVertex(i, tmp);
				shape.getVertex(i + 1, tmp2);
				renderer.rectLine(tmp.x, tmp.y, tmp2.x, tmp2.y, 0.1f);
				renderer.circle(tmp.x, tmp.y, 0.05f, 16);
				renderer.circle(tmp2.x, tmp2.y, 0.05f, 16);
			}
		}

		rayStart.set(handleA.x, handleA.y);
		rayEnd.set(handleB.x, handleB.y).sub(rayStart).setLength2(900);
		renderer.setColor(Color.MAGENTA);
		Gdx.app.log(TAG, "RayCasts start");
		for (int i = 0; i <= reflections; i++) {
			rayCastId = i;
			tmp.set(rayStart.x + rayEnd.x, rayStart.y + rayEnd.y);
			lastPoint.set(tmp);
			Gdx.app.log(TAG, "RayCast " + rayCastId + " start " + Thread.currentThread());
			world.rayCast(this, rayStart, tmp);
			Gdx.app.log(TAG, "RayCast " + rayCastId + " end" + Thread.currentThread());
			renderer.setColor(Color.MAGENTA);
			renderer.getColor().a = (reflections-i)/(float)(reflections)/2 + .25f;
			renderer.rectLine(rayStart.x, rayStart.y, lastPoint.x, lastPoint.y, 0.1f);
//			renderer.circle(rayStart.x, rayStart.y, 0.05f, 16);
//			renderer.circle(lastPoint.x, lastPoint.y, 0.05f, 16);
//			tmpCircle.set(rayEnd);
			renderer.setColor(Color.CYAN);
			renderer.rectLine(lastPoint.x, lastPoint.y, lastPoint.x + lastNormal.x * 0.5f, lastPoint.y + lastNormal.y * 0.5f, 0.025f);
			// calculate reflected normalized vector
			// r = d - 2(d * n)n
			rayEnd.sub(lastPoint);
			lastNormal.scl(rayEnd.dot(lastNormal)).scl(2);
			rayEnd.sub(lastNormal);
			// set length to be larger then the map, so we will get everything when we ray cast
			rayEnd.setLength2(900);
			// we need to setPosition the start point a bit so the edge we ended on wont get picked up again
			rayEnd.add(lastPoint);
			tmp.set(rayEnd).setLength2(0.0025f);
			rayStart.set(lastPoint.x, lastPoint.y).add(tmp);
		}
		Gdx.app.log(TAG, "RayCasts end");

		renderer.setColor(Color.RED);
		renderer.circle(handleA.x, handleA.y, handleA.radius, 16);

		renderer.setColor(Color.GREEN);
		renderer.circle(handleB.x, handleB.y, handleB.radius, 16);

		renderer.end();
		debugRenderer.render(world, gameCamera.combined);
	}

	private Vector2 lastPoint = new Vector2();
	private Vector2 lastNormal = new Vector2();
	@Override public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		Gdx.app.log(TAG, "RayCast " + rayCastId + " hit " + Thread.currentThread());
		lastPoint.set(point);
		lastNormal.set(normal);
		return fraction;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameViewport.unproject(mouse.set(screenX, screenY, 1));
		return super.mouseMoved(screenX, screenY);
	}

	private Circle drag;
	private Vector2 dragOffset = new Vector2();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameViewport.unproject(mouse.set(screenX, screenY, 1));
		if (button == Input.Buttons.LEFT) {
			if (handleA.contains(mouse.x, mouse.y)) {
				dragOffset.set(mouse.x, mouse.y).sub(handleA.x, handleA.y);
				drag = handleA;
			} else if (handleB.contains(mouse.x, mouse.y)) {
				dragOffset.set(mouse.x, mouse.y).sub(handleB.x, handleB.y);
				drag = handleB;
			}
			return true;
		}
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		gameViewport.unproject(mouse.set(screenX, screenY, 1));
		if (drag != null) {
			drag.setPosition(mouse.x - dragOffset.x, mouse.y - dragOffset.y);
		}
		return super.touchDragged(screenX, screenY, pointer);
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		gameViewport.unproject(mouse.set(screenX, screenY, 1));
		if (button == Input.Buttons.LEFT) {
			if (drag != null) {
				drag = null;
			}
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
		shape.createChain(createArc(64, 150, -90 + 15f, 5.25f, 0, 0));
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
		shape.dispose();

		// bottom dome
		shape = new ChainShape();
		shape.createChain(createArc(64, 150, 90 + 15f, 5.25f, 0, 0));
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

	@Override public void dispose () {
		super.dispose();
		debugRenderer.dispose();
		world.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, Box2dReflectTest.class);
	}
}

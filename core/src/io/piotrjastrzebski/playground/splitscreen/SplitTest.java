package io.piotrjastrzebski.playground.splitscreen;

import box2dLight.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.ArrayList;

public class SplitTest extends BaseScreen {

	static final int RAYS_PER_BALL = 128;
	static final int BALLSNUM = 7;
	static final float LIGHT_DISTANCE = 16f;
	static final float RADIUS = 1f;

	static final float viewportWidth = 30;
	static final float viewportHeight = 30;


	OrthographicCamera camera1;
	OrthographicCamera camera2;
	OrthographicCamera camera3;
	OrthographicCamera camera4;

	SpriteBatch batch;
	BitmapFont font;
	TextureRegion textureRegion;
	Texture bg;

	/** our box2D world **/
	World world;

	Box2DDebugRenderer debugRenderer;

	/** our boxes **/
	ArrayList<Body> balls = new ArrayList<Body>(BALLSNUM);

	/** our ground box **/
	Body groundBody;

	/** our mouse joint **/
	MouseJoint mouseJoint = null;

	/** a hit body **/
	Body hitBody = null;

	/** pixel perfect projection for font rendering */
	Matrix4 normalProjection = new Matrix4();

	boolean showText = true;

	/** BOX2D LIGHT STUFF */
	RayHandler rayHandler;

	ArrayList<Light> lights = new ArrayList<Light>(BALLSNUM);
	int ORIGIN_X = 0;
	int ORIGIN_Y = 0;
	final int FRUSTUM_WIDTH = 300;
	final int FRUSTUM_HEIGHT = 300;
	float sunDirection = -90f;

	public SplitTest (GameReset game) {
		super(game);

		MathUtils.random.setSeed(Long.MIN_VALUE);

		camera1 = new OrthographicCamera(viewportWidth, viewportHeight);
		camera1.position.set(viewportWidth/2, viewportHeight /2, 0);
		camera1.update();

		camera2 = new OrthographicCamera(viewportWidth, viewportHeight);
		camera2.position.set(viewportWidth/2, viewportHeight /2, 0);
		camera2.update();

		camera3 = new OrthographicCamera(viewportWidth, viewportHeight);
		camera3.position.set(viewportWidth/2, viewportHeight /2, 0);
		camera3.update();

		camera4 = new OrthographicCamera(viewportWidth, viewportHeight);
		camera4.position.set(viewportWidth/2, viewportHeight /2, 0);
		camera4.update();

		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.RED);

		textureRegion = new TextureRegion(new Texture("box2d/circle32.png"));
		Pixmap white = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
		white.setColor(Color.WHITE);
		white.drawRectangle(0, 0, 3, 3);
		bg = new Texture(white);
		white.dispose();

		createPhysicsWorld();
		Gdx.input.setInputProcessor(this);

		normalProjection.setToOrtho2D(
			0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		/** BOX2D LIGHT STUFF BEGIN */
		RayHandler.setGammaCorrection(true);
		RayHandler.useDiffuseLight(true);

		rayHandler = new RayHandler(world);
		rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
		rayHandler.setBlurNum(1);

		initPointLights();
		/** BOX2D LIGHT STUFF END */

		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
	}

	Rectangle bounds = new Rectangle();
	Rectangle scissors = new Rectangle();
	@Override
	public void render(float delta) {

		/** Rotate directional light like sun :) */
		if (lightsType == 3) {
			sunDirection += Gdx.graphics.getDeltaTime() * 4f;
			lights.get(0).setDirection(sunDirection);
		}

		boolean stepped = fixedStep(Gdx.graphics.getDeltaTime());

		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glScissor(0, 0, 600, 600);
		Gdx.gl.glViewport(0, 0, 600, 600);
		Gdx.gl.glClearColor(.5f,.5f,.5f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

		ORIGIN_X = 0;
		ORIGIN_Y = 0;
//		bounds.set(ORIGIN_X, ORIGIN_Y, 15, 15);
//		ScissorStack.calculateScissors(camera1, batch.getTransformMatrix(), bounds, scissors);
//		ScissorStack.pushScissors(scissors);
		Gdx.gl.glScissor(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		Gdx.gl.glViewport(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		draw(camera1, Color.RED);
//		ScissorStack.popScissors();


		ORIGIN_X = 300;
		ORIGIN_Y = 0;
//		bounds.set(15, 0, 15, 15);
//		ScissorStack.calculateScissors(camera2, batch.getTransformMatrix(), bounds, scissors);
//		ScissorStack.pushScissors(scissors);
		Gdx.gl.glScissor(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		Gdx.gl.glViewport(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		draw(camera2, Color.GREEN);
//		ScissorStack.popScissors();

		ORIGIN_X = 0;
		ORIGIN_Y = FRUSTUM_HEIGHT;
		bounds.set(0, 15, 15, 15);
//		ScissorStack.calculateScissors(camera2, batch.getTransformMatrix(), bounds, scissors);
//		ScissorStack.pushScissors(scissors);
		Gdx.gl.glScissor(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		Gdx.gl.glViewport(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		draw(camera3, Color.BLUE);
//		ScissorStack.popScissors();

		ORIGIN_X = FRUSTUM_WIDTH;
		ORIGIN_Y = FRUSTUM_HEIGHT;
		bounds.set(15, 15, 15, 15);
//		ScissorStack.calculateScissors(camera2, batch.getTransformMatrix(), bounds, scissors);
//		ScissorStack.pushScissors(scissors);
		Gdx.gl.glScissor(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		Gdx.gl.glViewport(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		draw(camera4, Color.GOLD);
//		ScissorStack.popScissors();
	}

	void draw (OrthographicCamera camera, Color clear){
		Gdx.gl.glClearColor(clear.r, clear.g, clear.b, 1);
//		rayHandler.setAmbientLight(1-clear.r, 1-clear.g, 1-clear.b, 1);
		rayHandler.setAmbientLight(.5f, .5f, .5f, .5f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
//		batch.draw(bg, 0, 0, viewportWidth, viewportHeight);
		for (int i = 0; i < BALLSNUM; i++) {
			Body ball = balls.get(i);
			Vector2 position = ball.getPosition();
			float angle = MathUtils.radiansToDegrees * ball.getAngle();
			batch.draw(
				textureRegion,
				position.x - RADIUS, position.y - RADIUS,
				RADIUS, RADIUS,
				RADIUS * 2, RADIUS * 2,
				1f, 1f,
				angle);
		}
		batch.end();

//		Gdx.gl.glScissor(0, 0, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
//		Gdx.gl.glViewport(0, 0, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		/** BOX2D LIGHT STUFF BEGIN */
		Gdx.gl.glScissor(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		rayHandler.setCombinedMatrix(camera);
		// vp will be changes to this one, after updateAndRender
		rayHandler.useCustomViewport(ORIGIN_X, ORIGIN_Y, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
		rayHandler.updateAndRender();
		/** BOX2D LIGHT STUFF END */

		debugRenderer.render(world, camera.combined);
	}

	void clearLights() {
		if (lights.size() > 0) {
			for (Light light : lights) {
				light.remove();
			}
			lights.clear();
		}
		groundBody.setActive(true);
	}

	void initPointLights() {
		clearLights();
		for (int i = 0; i < BALLSNUM; i++) {
			PointLight light = new PointLight(
				rayHandler, RAYS_PER_BALL, null, LIGHT_DISTANCE, 0f, 0f);
			light.attachToBody(balls.get(i), RADIUS / 2f, RADIUS / 2f);
			light.setColor(
				MathUtils.random(),
				MathUtils.random(),
				MathUtils.random(),
				1f);
			lights.add(light);
		}
	}

	void initConeLights() {
		clearLights();
		for (int i = 0; i < BALLSNUM; i++) {
			ConeLight light = new ConeLight(
				rayHandler, RAYS_PER_BALL, null, LIGHT_DISTANCE,
				0, 0, 0f, MathUtils.random(15f, 40f));
			light.attachToBody(
				balls.get(i),
				RADIUS / 2f, RADIUS / 2f, MathUtils.random(0f, 360f));
			light.setColor(
				MathUtils.random(),
				MathUtils.random(),
				MathUtils.random(),
				1f);
			lights.add(light);
		}
	}

	void initChainLights() {
		clearLights();
		for (int i = 0; i < BALLSNUM; i++) {
			ChainLight light = new ChainLight(
				rayHandler, RAYS_PER_BALL, null, LIGHT_DISTANCE, 1,
				new float[]{-5, 0, 0, 3, 5, 0});
			light.attachToBody(
				balls.get(i),
				MathUtils.random(0f, 360f));
			light.setColor(
				MathUtils.random(),
				MathUtils.random(),
				MathUtils.random(),
				1f);
			lights.add(light);
		}
	}

	void initDirectionalLight() {
		clearLights();

		groundBody.setActive(false);
		sunDirection = MathUtils.random(0f, 360f);

		DirectionalLight light = new DirectionalLight(
			rayHandler, 4 * RAYS_PER_BALL, null, sunDirection);
		lights.add(light);
	}

	private final static int MAX_FPS = 30;
	private final static int MIN_FPS = 15;
	public final static float TIME_STEP = 1f / MAX_FPS;
	private final static float MAX_STEPS = 1f + MAX_FPS / MIN_FPS;
	private final static float MAX_TIME_PER_FRAME = TIME_STEP * MAX_STEPS;
	private final static int VELOCITY_ITERS = 6;
	private final static int POSITION_ITERS = 2;

	float physicsTimeLeft;
	long aika;
	int times;

	private boolean fixedStep(float delta) {
		physicsTimeLeft += delta;
		if (physicsTimeLeft > MAX_TIME_PER_FRAME)
			physicsTimeLeft = MAX_TIME_PER_FRAME;

		boolean stepped = false;
		while (physicsTimeLeft >= TIME_STEP) {
			world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);
			physicsTimeLeft -= TIME_STEP;
			stepped = true;
		}
		return stepped;
	}

	private void createPhysicsWorld() {

		world = new World(new Vector2(0, 0), true);

		debugRenderer = new Box2DDebugRenderer();
		debugRenderer.setDrawBodies(true);


		float halfWidth = viewportWidth / 2f;
		ChainShape chainShape = new ChainShape();
		chainShape.createLoop(new Vector2[] {
			new Vector2(0, 0f),
			new Vector2(viewportWidth, 0f),
			new Vector2(viewportWidth, viewportHeight),
			new Vector2(0, viewportHeight) });
		BodyDef chainBodyDef = new BodyDef();
		chainBodyDef.type = BodyType.StaticBody;
		groundBody = world.createBody(chainBodyDef);
		groundBody.createFixture(chainShape, 0);
		chainShape.dispose();
		createBoxes();
	}

	private void createBoxes() {
		CircleShape ballShape = new CircleShape();
		ballShape.setRadius(RADIUS);

		FixtureDef def = new FixtureDef();
		def.restitution = 0.9f;
		def.friction = 0.01f;
		def.shape = ballShape;
		def.density = 1f;
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = BodyType.DynamicBody;

		for (int i = 0; i < BALLSNUM; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			boxBodyDef.position.x = (float) (Math.random() * 30);
			boxBodyDef.position.y = (float) (Math.random() * 30);
//         boxBodyDef.position.x = 15;
//         boxBodyDef.position.y = 15;
			Body boxBody = world.createBody(boxBodyDef);
			boxBody.createFixture(def);
			balls.add(boxBody);
		}
		ballShape.dispose();
	}

	/**
	 * we instantiate this vector and the callback here so we don't irritate the
	 * GC
	 **/
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

	@Override
	public boolean touchDown(int x, int y, int pointer, int newParam) {
		// translate the mouse coordinates to world coordinates
//      x = x %FRUSTUM_WIDTH;
//      y = y %FRUSTUM_HEIGHT;
		testPoint.set(x, y, 0);
		camera1.unproject(testPoint);

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

		return false;
	}

	/** another temporary vector **/
	Vector2 target = new Vector2();

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		camera1.unproject(testPoint.set(x, y, 0));
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

	@Override
	public void dispose() {
		rayHandler.dispose();
		world.dispose();
	}

	/**
	 * Type of lights to use:
	 * 0 - PointLight
	 * 1 - ConeLight
	 * 2 - ChainLight
	 * 3 - DirectionalLight
	 */
	int lightsType = 0;

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {

		case Input.Keys.NUM_1:
			camera1.zoom+=0.1f;
			Gdx.app.log("", "camera1 zoom++");
			return true;
		case Input.Keys.NUM_2:
			camera1.zoom-=0.1f;
			Gdx.app.log("", "camera1 zoom--");
			return true;

		case Input.Keys.NUM_3:
			camera2.zoom+=0.1f;
			Gdx.app.log("", "camera2 zoom++");
			return true;
		case Input.Keys.NUM_4:
			camera2.zoom-=0.1f;
			Gdx.app.log("", "camera2 zoom--");
			return true;

		case Input.Keys.NUMPAD_1:
			camera1.position.x++;
			Gdx.app.log("", "camera1 x++");
			return true;
		case Input.Keys.NUMPAD_2:
			camera1.position.x--;
			Gdx.app.log("", "camera1 x--");
			return true;

		case Input.Keys.NUMPAD_4:
			camera2.position.x++;
			Gdx.app.log("", "camera2 x++");
			return true;
		case Input.Keys.NUMPAD_5:
			camera2.position.x--;
			Gdx.app.log("", "camera2 x--");
			return true;

		case Input.Keys.F1:
			if (lightsType != 0) {
				initPointLights();
				lightsType = 0;
			}
			return true;

		case Input.Keys.F2:
			if (lightsType != 1) {
				initConeLights();
				lightsType = 1;
			}
			return true;

		case Input.Keys.F3:
			if (lightsType != 2) {
				initChainLights();
				lightsType = 2;
			}
			return true;

		case Input.Keys.F4:
			if (lightsType != 3) {
				initDirectionalLight();
				lightsType = 3;
			}
			return true;

		case Input.Keys.F5:
			for (Light light : lights)
				light.setColor(
					MathUtils.random(),
					MathUtils.random(),
					MathUtils.random(),
					1f);
			return true;

		case Input.Keys.F6:
			for (Light light : lights)
				light.setDistance(MathUtils.random(
					LIGHT_DISTANCE * 0.5f, LIGHT_DISTANCE * 2f));
			return true;

		case Input.Keys.F9:
			rayHandler.diffuseBlendFunc.reset();
			return true;

		case Input.Keys.F10:
			rayHandler.diffuseBlendFunc.set(
				GL20.GL_DST_COLOR, GL20.GL_SRC_COLOR);
			return true;

		case Input.Keys.F11:
			rayHandler.diffuseBlendFunc.set(
				GL20.GL_SRC_COLOR, GL20.GL_DST_COLOR);
			return true;

		case Input.Keys.F12:
			showText = !showText;
			return true;

		default:
			return false;

		}
	}

	@Override
	public boolean mouseMoved(int x, int y) {
		testPoint.set(x, y, 0);
		camera1.unproject(testPoint);
		return false;
	}

	@Override public boolean scrolled (float amountX, float amountY) {
		camera1.rotate((float) amountX * 3f, 0, 0, 1);
		return false;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, SplitTest.class);
	}
}

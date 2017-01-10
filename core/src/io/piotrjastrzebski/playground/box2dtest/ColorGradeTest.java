package io.piotrjastrzebski.playground.box2dtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.util.ColorUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Based on http://www.slickentertainment.com/tech/dev-blog-128-color-grading-another-cool-rendering-trick/
 *
 * Created by PiotrJ on 31/07/15.
 */
public class ColorGradeTest extends BaseScreen {
	private static final String TAG = ColorGradeTest.class.getSimpleName();

	public final static float VP_WIDTH = 40;
	public final static float VP_HEIGHT = 22.5f;
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/32f;
	private final Texture box;

	World world;
	Array<Box> boxes = new Array<>();
	Box2DDebugRenderer debugRenderer;
	boolean debugDraw = true;
	FrameBuffer fbo;
	TextureRegion fboRegion;
	boolean useFbo;
	Texture cgtBase;
	Texture[] cgts = new Texture[4];
	Texture cgtSelected;
	ShaderProgram colorGradeShader;

	public ColorGradeTest (GameReset game) {
		super(game);
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		debugRenderer = new Box2DDebugRenderer();
		world = new World(new Vector2(0, -10), true);
		box = new Texture("badlogic.jpg");
		cgtBase = new Texture("color_grade_base.png");
		cgts[0] = new Texture("color_grade_inverted.png");
		cgts[1] = new Texture("color_grade_sepia.png");
		cgts[2] = new Texture("color_grade_bw.png");
		cgts[3] = new Texture("color_grade_gradient.png");
		cgtSelected = cgts[0];
		createBounds();
		reset();

		fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		fboRegion = new TextureRegion(fbo.getColorBufferTexture());
		fboRegion.flip(false, true);

		colorGradeShader = new ShaderProgram(Gdx.files.internal("shaders/colorgrade.vert"), Gdx.files.internal("shaders/colorgrade.frag"));
		if (!colorGradeShader.isCompiled()) {
			Gdx.app.error(TAG, colorGradeShader.getLog());
			throw new AssertionError("welp");
		}
		colorGradeShader.begin();
		colorGradeShader.setUniformi(colorGradeShader.getUniformLocation("u_texture"), 0);
		colorGradeShader.setUniformi(colorGradeShader.getUniformLocation("u_color_grade"), 1);
		colorGradeShader.end();
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
		for (Box box : boxes) {
			world.destroyBody(box.body);
		}
		boxes.clear();

		for (int i = 0; i < 32; i++) {
			float x = MathUtils.random(-15, 15);
			float y = MathUtils.random(-8, 8);
			float rotation = MathUtils.random(90);
			createBox(x, y, rotation, box);
		}
	}

	private void createBox (float x, float y, float rotation, Texture texture) {
		Box box = new Box(x, y, rotation, texture);
		box.width *= MathUtils.random(.25f, 1f);
		box.height *= MathUtils.random(.25f, 1f);
		box.tint.set(ColorUtils.HSVtoRGB(MathUtils.random(360), MathUtils.random(360), MathUtils.random(360)));

		BodyDef def = new BodyDef();
		def.position.set(x, y);
		def.angle = rotation * MathUtils.degreesToRadians;
		def.type = BodyDef.BodyType.DynamicBody;
		box.body = world.createBody(def);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(box.width / 2, box.height / 2);
		box.body.createFixture(shape, 1);
		shape.dispose();

		boxes.add(box);
	}

	@Override public void render (float delta) {
		super.render(delta);
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			useFbo = !useFbo;
			Gdx.app.log(TAG, "use fbo = " + useFbo);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
			cgtSelected = cgts[0];
			Gdx.app.log(TAG, "inverted");
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
			cgtSelected = cgts[1];
			Gdx.app.log(TAG, "sepia");
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
			cgtSelected = cgts[2];
			Gdx.app.log(TAG, "black white");
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
			cgtSelected = cgts[3];
			Gdx.app.log(TAG, "gradient");
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			Gdx.app.log(TAG, "Reloading shader...");
			ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/colorgrade.vert"), Gdx.files.internal("shaders/colorgrade.frag"));
			if (!shader.isCompiled()) {
				Gdx.app.error(TAG, colorGradeShader.getLog());
			} else {
				colorGradeShader = shader;
				colorGradeShader.begin();
				colorGradeShader.setUniformi(colorGradeShader.getUniformLocation("u_texture"), 0);
				colorGradeShader.setUniformi(colorGradeShader.getUniformLocation("u_color_grade"), 1);
				colorGradeShader.end();
			}
		}
		world.step(1f / 60f, 6, 4);

		for (Box box : boxes) {
			box.update();
		}
		draw();
	}

	private void draw () {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		if (useFbo) {
			fbo.begin();
		}
		batch.setShader(null);
		Gdx.gl.glClearColor(clear.r, clear.g, clear.b, clear.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (debugDraw) {
			debugRenderer.render(world, gameCamera.combined);
		}
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Box box : boxes) {
			box.draw(batch);
		}

		batch.draw(cgtBase, -cgtBase.getWidth() * INV_SCALE * 3/2f, 0, cgtBase.getWidth() * INV_SCALE * 3, cgtBase.getHeight() * INV_SCALE * 3);
		batch.end();

		Gdx.gl.glDisable(GL20.GL_BLEND);
		if (useFbo) {
			fbo.end();
			cgtSelected.bind(1);
			fboRegion.getTexture().bind(0);
			batch.setShader(colorGradeShader);
			batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.begin();
			batch.draw(fboRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();
		}
	}

	private class Box {
		public Body body;
		public Texture texture;
		public Color tint = new Color(Color.WHITE);
		public float x;
		public float y;
		public float rot;
		private float width;
		private float height;
		private int srcWidth;
		private int srcHeight;

		public Box (float x, float y, float rotation, Texture texture) {
			this.x = x;
			this.y = y;
			this.rot = rotation;
			this.texture = texture;
			srcWidth = texture.getWidth();
			width = srcWidth * INV_SCALE /2;
			srcHeight = texture.getHeight();
			height = srcHeight * INV_SCALE /2;
		}

		public void update () {
			Vector2 position = body.getPosition();
			x = position.x;
			y = position.y;
			rot = body.getAngle() * MathUtils.radiansToDegrees;
		}

		public void draw (Batch batch) {
			batch.setColor(tint);
			batch.draw(texture, x - width / 2, y - height / 2, width / 2, height / 2, width, height, 1, 1, rot, 0, 0, srcWidth,
				srcHeight, false, false);
			batch.setColor(Color.WHITE);
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
		return super.keyDown(keycode);
	}

	@Override public void dispose () {
		super.dispose();
		box.dispose();
	}
	public static void main (String[] args) {
		PlaygroundGame.start(args, ColorGradeTest.class);
	}
}

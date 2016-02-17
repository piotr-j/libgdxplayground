package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 16/02/2016.
 */public class BasicBall3dTest extends BaseScreen {

	public static class Ball extends ModelInstance {
		private static Vector3 tmpV = new Vector3();
		private static Quaternion tmpQ = new Quaternion();

		public Vector3 velocity = new Vector3();
		public Vector3 acceleration = new Vector3();
		public Vector3 position = new Vector3();
		public Quaternion rotation = new Quaternion();

		public Ball(Model model, String nodeId) {
			super(model, nodeId, true);
		}

		public void setPosition(float x, float y, float z) {
			transform.setTranslation(position.set(x, y, z));
		}

		private static final float friction = -1.0f;
		public void update(float delta) {
			velocity.add(tmpV.set(velocity).limit(1f).scl(delta*friction)); //simulate some friction

			velocity.add(tmpV.set(acceleration).scl(delta)).limit(5f);
			final float speed = velocity.len();

			if (speed > 0.2f) {
				position.add(tmpV.set(velocity).scl(delta));

				tmpQ.set(tmpV.set(velocity).scl(-1f/speed).crs(Vector3.Y), speed*delta* MathUtils.radiansToDegrees);
				rotation.mulLeft(tmpQ);

				transform.set(position, rotation);
			}
		}
	}

	PerspectiveCamera cam;
	CameraInputController inputController;
	ModelBatch modelBatch;
	DirectionalShadowLight shadowLight;
	ModelBatch shadowBatch;
	Model model;
	Array<ModelInstance> instances;
	Environment environment;
	Ball ball;

	public BasicBall3dTest (GameReset game) {
		super(game);
		modelBatch = new ModelBatch();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		environment.add((shadowLight = new DirectionalShadowLight(1024, 1024, 30f, 30f, 1f, 100f)).set(0.8f, 0.8f, 0.8f, -1f, -.8f,
			-.2f));
		environment.shadowMap = shadowLight;

		shadowBatch = new ModelBatch(new DepthShaderProvider());

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 1, 0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();

		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		modelBuilder.node().id = "floor";
		modelBuilder.part("floor", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material("diffuseGreen", ColorAttribute.createDiffuse(
			Color.GREEN)))
			.box(100f, 1f, 100f);
		Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		modelBuilder.manage(texture);
		modelBuilder.node().id = "ball";
		modelBuilder.part("ball", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates,
			new Material("diffuseRed", TextureAttribute.createDiffuse(texture)))
			.sphere(1f, 1f, 1f, 10, 10);
		model = modelBuilder.end();

		instances = new Array<ModelInstance>();
		instances.add(new ModelInstance(model, "floor"));
		instances.add(ball = new Ball(model, "ball"));

		ball.setPosition(0f, 1f, 0f);

		Gdx.input.setInputProcessor(new InputMultiplexer(this, inputController = new CameraInputController(cam)));
	}

	public void render (float delta) {
		delta = Math.min(1f/10f, delta);
		ball.update(delta);

		inputController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shadowLight.begin(Vector3.Zero, cam.direction);
		shadowBatch.begin(shadowLight.getCamera());
		shadowBatch.render(instances);
		shadowBatch.end();
		shadowLight.end();

		modelBatch.begin(cam);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}

	boolean up, down, left, right;
	@Override
	public boolean keyDown (int keycode) {
		switch(keycode) {
		case Keys.UP: up = true; break;
		case Keys.DOWN: down = true; break;
		case Keys.LEFT: left = true; break;
		case Keys.RIGHT: right = true; break;
		default: return false;
		}
		ball.acceleration.set((right?1:0)+(left?-1:0), 0f, (up?-1:0)+(down?1:0)).scl(2);
		return true;
	}

	@Override
	public boolean keyUp (int keycode) {
		switch(keycode) {
		case Keys.UP: up = false; break;
		case Keys.DOWN: down = false; break;
		case Keys.LEFT: left = false; break;
		case Keys.RIGHT: right = false; break;
		default: return false;
		}
		ball.acceleration.set((right?1:0)+(left?-1:0), 0f, (up?-1:0)+(down?1:0)).scl(2);
		return true;
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
	}

	public void resume () {
	}

	public void resize (int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
		cam.update();
	}

	public void pause () {
	}
	public static void main (String[] args) {
		PlaygroundGame.start(args, BasicBall3dTest.class);
	}
}

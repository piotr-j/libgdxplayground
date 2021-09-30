package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 */
public class SpringTest extends ApplicationAdapter implements InputProcessor {
	public static float SCALE = 2f;
	public static float INV_SCALE = 1.f/ SCALE;
	public static float WIDTH = 1280 * INV_SCALE;
	public static float HEIGHT = 720 * INV_SCALE;

	OrthographicCamera gameCam;
	ExtendViewport gameVP;
	ShapeRenderer renderer;
	SpriteBatch batch;

	Stage stage;

	Spring spring;

	Array<Spring> springs;
	Array<Spring2> springs2;

	float targetY = 100;

	public SpringTest () {
		super();
	}

	@Override public void create () {
		super.create();
		gameCam = new OrthographicCamera();
		gameVP = new ExtendViewport(WIDTH, HEIGHT, gameCam);

		renderer = new ShapeRenderer();
		batch = new SpriteBatch();

		stage = new Stage(gameVP, batch);
//		stage = new Stage(new ScreenViewport(), batch);
		Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

		stage.addListener(new InputListener(){
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

				return false;
			}
		});

		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);

		springs = new Array<>();
		springs2 = new Array<>();

	}

	@Override public void render () {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || springs.size == 0) {
			springs.clear();
			springs.add(new Spring(50, 300, 100, 40, 400));
			springs.add(new Spring(100, 300, 100, 80, 400));
			springs.add(new Spring(150, 300, 100, 160, 400));
			springs.add(new Spring(200, 300, 100, 320, 400));
			springs.add(new Spring(250, 300, 100, 640, 400));

			springs.add(new Spring(350, 300, 100, 100, 100));
			springs.add(new Spring(400, 300, 100, 100, 200));
			springs.add(new Spring(450, 300, 100, 100, 400));
			springs.add(new Spring(500, 300, 100, 100, 800));
			springs.add(new Spring(550, 300, 100, 100, 1600));

			springs2.clear();
			for (int i = 0; i < 9; i++) {
				springs2.add(new Spring2(50 + i * 25, 300, 100, 50f, 10f, i * .1f));
			}
		}

		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		float dt = Gdx.graphics.getDeltaTime();
		renderer.setProjectionMatrix(gameCam.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.BLUE);
		renderer.line(0, targetY, 1000, targetY);
		renderer.setColor(Color.ORANGE);
//		for (Spring spring : springs) {
//			spring.update(dt);
//			spring.render(renderer);
//		}

		for (Spring2 spring2 : springs2) {
			spring2.update(dt);
			spring2.render(renderer);
		}

		renderer.end();
	}

	protected static class Spring2 {
		float x;
		float current;
		float target;
		float mass;
		float stiffness;
		float damping;


		// state
		float prevDiff;
		float velocity;

		public Spring2 (float x, float current, float target, float mass, float stiffness, float dampingRatio) {
			this.x = x;
			this.current = current;
			this.target = target;
			this.mass = mass;
			this.stiffness = stiffness;

			// https://en.wikipedia.org/wiki/Damping
			// damping ratio
			damping = (float)(2 * Math.sqrt(mass * stiffness) * dampingRatio);
		}

		void update (float dt) {
			if (MathUtils.isZero(dt)) return;
			float diff = target - current;

			//ignore divide by delta time since we multiply later anyway
			float diffDelta = diff - prevDiff;
			velocity += (diff * stiffness * dt + diffDelta * damping) / mass;
			prevDiff = diff;

			current = current + velocity * dt;
		}

		void render (ShapeRenderer renderer) {
			renderer.line(x, current, x, target);
			renderer.rect(x-5, current-5, 10, 10);
		}
	}

	private static class Spring {
		Vector2 position = new Vector2();
		Vector2 acceleration = new Vector2();

		final float damping;
		final float spring;

		final float gravity = .98f;
		final float hoverForce = 100;

		float hoverDistance;
		float groundDistance;

		float hoverDelta;
		float lastHoverDelta;

		public Spring (float x, float y, float hoverDistance, float damping, float spring) {
			position.set(x, y);
			this.hoverDistance = hoverDistance;
			this.damping = damping;
			this.spring = spring;

		}

		void update (float dt) {
			acceleration.y -= gravity * gravity * dt;
			position.y += acceleration.y;

			groundDistance = position.y;

			hoverDelta = hoverDistance - groundDistance;

			float dampingForce = hoverDelta - lastHoverDelta;
			dampingForce /= dt;
			dampingForce *= damping;


			float springForce = hoverDelta * spring;

			float force = dampingForce + springForce;

			force = MathUtils.clamp(force, -hoverForce, hoverForce);


			acceleration.y += force * dt;

			lastHoverDelta = hoverDelta;
			if (position.y < 0) {
				position.y = 0;
			}
		}

		void render (ShapeRenderer renderer) {
			renderer.rect(position.x, position.y, 10, 10);
		}

	}


	@Override public void resize (int width, int height) {
		super.resize(width, height);
		gameVP.update(width, height, true);
	}

	@Override public void dispose () {
		super.dispose();
		renderer.dispose();
		batch.dispose();
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			Gdx.app.exit();
		}
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		Vector2 p = gameVP.unproject(new Vector2(screenX, screenY));
		for (Spring2 spring2 : springs2) {
			if (button == Input.Buttons.LEFT) {
				spring2.current = p.y;
				spring2.velocity = 0;
			} else if (button == Input.Buttons.RIGHT) {
				spring2.target = p.y;
				targetY = p.y;
			}
		}
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled (float amountX, float amountY) {
		return false;
	}


	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		PlaygroundGame.start(new SpringTest(), config);
	}
}

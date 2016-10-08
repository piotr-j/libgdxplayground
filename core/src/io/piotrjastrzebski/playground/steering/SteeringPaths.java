package io.piotrjastrzebski.playground.steering;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 *
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates")
public class SteeringPaths extends BaseScreen {

	protected State state;
	public SteeringPaths (GameReset game) {
		super(game);
		clear.set(Color.GRAY);

		state = new State();
		state.path.add(new Vector2(-15, 10));
		state.path.add(new Vector2(-10, -5));
		state.path.add(new Vector2(- 5, 0));
		state.path.add(new Vector2( 0, -5));
		state.path.add(new Vector2(5, 10));
		state.path.add(new Vector2(10, 0));
		state.path.add(new Vector2(15, 0));
		for (int i = 0; i < 1; i++) {
			state.steerables.add(new Steerable(MathUtils.random(-15, 15), MathUtils.random(-10, 10)));
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		state.delta = Math.min(delta, 1/30f);
		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.GREEN);
		renderer.circle(state.target.x, state.target.y, .25f, 16);
		renderer.setColor(Color.ORANGE);
		Array<Vector2> path = state.path;
		for (int i = 0; i < path.size -1; i++) {
			Vector2 p1 = path.get(i);
			Vector2 p2 = path.get(i + 1);
			renderer.rectLine(p1.x, p1.y, p2.x, p2.y, .1f);
		}
		for (Steerable steerable : state.steerables) {
			steerable.process(state, renderer);
		}

		renderer.end();
	}

	static class State {
		public float delta;
		public Vector2 target = new Vector2();
		public Array<Steerable> steerables = new Array<>();
		public Array<Vector2> path = new Array<>();
	}

	static class Steerable {
		private static final float MAX_ACCEL = 10;
		private static final float MAX_VELOCITY = 3;
		Vector2 acceleration = new Vector2();
		Vector2 velocity = new Vector2();
		Vector2 position = new Vector2();
		Vector2 target = new Vector2();
		float angularAcceleration;
		float angularVelocity;
		float rotation;
		float arriveDst = 2;

		static Vector2 tmp1 = new Vector2();
		static Vector2 tmp2 = new Vector2();
		static Vector2 tmp3 = new Vector2();

		public Steerable (float x, float y) {
			position.set(x, y);
			target.set(MathUtils.random(-15, 15), MathUtils.random(-10, 10));
		}

		int currentPathPoint = 0;

		float dt = 1/60f;
		public void process (State state, ShapeRenderer renderer) {
			// update
			Array<Vector2> path = state.path;

			float dt2 = dt * dt;
			int lookAheadFrames = 30;
			// predicted position
//			tmp3.set(position).add((velocity.x * dt + acceleration.x * dt2 * .5f) * lookAheadFrames, (velocity.y * dt + acceleration.y * dt2 * .5f) * lookAheadFrames);

//			float predictedDst = tmp3.dst(target);

			acceleration.set(target).sub(position);
			float dst = acceleration.len();
			acceleration.nor().scl(MAX_ACCEL);
			float velLimit = MAX_VELOCITY;
			if (dst <= arriveDst) {
//				a = (-Vi*Vi)/(2 * arriveDst);
//				acceleration.set(-velocity.x * velocity.x, -velocity.y * velocity.y).scl(.5f*dst);
				acceleration.scl(dst/arriveDst);
				// limiting the vel with accel works reasonable well
				velLimit *= dst/arriveDst;
				// how do we arrive at the target with 0 speed? we need to reverse the accel...

//				tmp3.set(position).add((velocity.x * dt + acceleration.x * dt2 * .5f) * lookAheadFrames, (velocity.y * dt + acceleration.y * dt2 * .5f) * lookAheadFrames);
//				float predictedDst = tmp3.dst(target);


			}
			velocity.add(acceleration.x * dt, acceleration.y * dt).limit(velLimit);
			position.add(velocity.x * dt + acceleration.x * dt2 * .5f, velocity.y * dt + acceleration.y * dt2 * .5f);

			if (currentPathPoint < path.size) {
				if (position.dst(target) <= arriveDst && currentPathPoint < path.size -1) {
					currentPathPoint++;
				}
				target.set(path.get(currentPathPoint));
			}
			// draw
			renderer.setColor(Color.RED);
			renderer.circle(position.x, position.y, .5f, 16);
//			renderer.setColor(1, 1, 1, .75f);
//			renderer.circle(tmp3.x, tmp3.y, .3f, 16);
			renderer.setColor(Color.CYAN);
			renderer.circle(target.x, target.y, .1f, 16);
			renderer.setColor(0, 1, 1, .5f);
			renderer.rectLine(position.x, position.y, target.x, target.y, .05f);
			renderer.setColor(0, 0, 1, 1f);
			renderer.rectLine(position.x, position.y, position.x + acceleration.x, position.y + acceleration.y, .1f);
			renderer.setColor(0, 1, 0, 1f);
			renderer.rectLine(position.x, position.y, position.x + velocity.x, position.y + velocity.y, .1f);
		}
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameViewport.unproject(cs.set(screenX, screenY));
		if (button == Input.Buttons.LEFT) {
			state.target.set(cs);
		}
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, SteeringPaths.class);
	}
}

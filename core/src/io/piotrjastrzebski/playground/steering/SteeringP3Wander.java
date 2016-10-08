package io.piotrjastrzebski.playground.steering;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 * https://gamedevelopment.tutsplus.com/tutorials/understanding-steering-behaviors-wander--gamedev-1624
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates")
public class SteeringP3Wander extends BaseScreen {

	protected State state;
	public SteeringP3Wander (GameReset game) {
		super(game);
		clear.set(Color.GRAY);

		state = new State();
		state.steerables.add(new Steerable(0, 0));

	}


	@Override public void render (float delta) {
		super.render(delta);
		state.delta = Math.min(delta, 1/30f);
		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.GREEN);
		renderer.circle(state.target.x, state.target.y, .25f, 16);
		for (Steerable steerable : state.steerables) {
			steerable.process(state, renderer);
			Vector2 sp = steerable.position;
			if (sp.x < gameCamera.position.x - gameCamera.viewportWidth/2) {
				sp.x = gameCamera.position.x + gameCamera.viewportWidth/2;
			} else if (sp.x > gameCamera.position.x + gameCamera.viewportWidth/2) {
				sp.x = gameCamera.position.x - gameCamera.viewportWidth/2;
			}
			if (sp.y < gameCamera.position.y - gameCamera.viewportHeight/2) {
				sp.y = gameCamera.position.y + gameCamera.viewportHeight/2;
			} else if (sp.y > gameCamera.position.y + gameCamera.viewportHeight/2) {
				sp.y = gameCamera.position.y - gameCamera.viewportHeight/2;
			}
		}

		renderer.end();
	}

	static class State {
		public float delta;
		public Vector2 target = new Vector2();
		public Array<Steerable> steerables = new Array<>();
	}

	static class Steerable {
		float speed = 5;
		float steeringLimit = .2f;
		float mass = 1;
		float arriveRadius = 3;
		float circleRadius = 1.5f;
		float circleOffset = 3;
		float wanderAngle = MathUtils.random(360f);
		Vector2 position = new Vector2();
		Vector2 circleCenter = new Vector2();
		Vector2 velocity = new Vector2();
		Vector2 desiredVelocity = new Vector2();
		Vector2 steering = new Vector2();
		Vector2 displacement = new Vector2();
		Vector2 wanderForce = new Vector2();

		public Steerable (float x, float y) {
			position.set(x, y);
		}

		public void process (State state, ShapeRenderer renderer) {

			circleCenter.set(velocity).nor().scl(circleOffset);

			displacement.set(0, -1).scl(circleRadius);
			displacement.rotate(wanderAngle);
			wanderAngle += MathUtils.random(-10, 10);

			wanderForce.set(circleCenter).add(displacement);

			// update
//			desiredVelocity.set(state.target.x - position.x, state.target.y - position.y);
//			float len = desiredVelocity.len();
//			desiredVelocity.nor().scl(speed);
//			if (len < arriveRadius) {
//				desiredVelocity.scl(len/arriveRadius);
//			}
			steering.set(wanderForce);
			// Adding Forces
			steering.limit(steeringLimit);
			steering.scl(1f/mass);

			velocity.add(steering).limit(speed);
			position.add(velocity.x * state.delta, velocity.y * state.delta);

			// draw
			renderer.setColor(Color.RED);
			renderer.circle(position.x, position.y, .5f, 16);
			renderer.setColor(0, 1, 0, .25f);
			renderer.circle(position.x + circleCenter.x, position.y + circleCenter.y, circleRadius, 16);
			renderer.setColor(0, 0, 1, .66f);
			renderer.rectLine(position.x+ circleCenter.x, position.y+ circleCenter.y, position.x + circleCenter.x+ displacement.x, position.y + circleCenter.y+ displacement.y, .1f);
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
		PlaygroundGame.start(args, SteeringP3Wander.class);
	}
}

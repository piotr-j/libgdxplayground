package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ArriveTest extends BaseScreen {
	private static final String TAG = ArriveTest.class.getSimpleName();

	UFO ufo;
	public ArriveTest (GameReset game) {
		super(game);
		clear.set(.5f, .5f, .5f, 1f);
		ufo = new UFO();
		ufo.position.set(VP_WIDTH/2, VP_HEIGHT/2);
	}

	@Override public void render (float delta) {
		super.render(delta);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			ufo.setTarget(cs);
		}
		ufo.update(delta);
		ufo.draw(renderer);
		renderer.end();
	}


	protected static class UFO {
		private Vector2 tmp = new Vector2();
		public Vector2 position = new Vector2();
		public Vector2 velocity = new Vector2();
		public Vector2 accel = new Vector2();
		public Vector2 target = new Vector2();
		public boolean hasTarget;
		public boolean hadTarget;
		public float maxVel = 5;
		public float maxAccel = 30;
		public float deAccelDst = .5f;
		public float minDst = .005f;

		public void setTarget(Vector2 target) {
			this.target.set(target);
			hasTarget = true;
			hadTarget = true;
		}

		public void update(float delta) {
			float d2 = delta * delta;
			if (!hasTarget) {
				hadTarget = false;
				if (velocity.len2() > .001f) {
					velocity.add(-velocity.x * 2f * delta, -velocity.y * 2f * delta);
					position.add(velocity.x * delta, velocity.y * delta);
				}
			} else {
				hasTarget = false;
				tmp.set(target).sub(position);

				float dst = tmp.len();
				if (dst > minDst) {
					tmp.nor();
					accel.set(tmp).scl(maxAccel);
					float velLimit = maxVel;
					if (dst < deAccelDst) {
						// scale the limit based on the dst so we stop at the target
						velLimit *= dst / deAccelDst;
					}
					// vel = vel + a * dt
					velocity.add(accel.x * delta, accel.y * delta).limit(velLimit);
					// pos = pos + vel * dt + .5f * a * t * t
					position.add(velocity.x * delta + .5f * accel.x * d2, velocity.y * delta + .5f * accel.y * d2);
				}
			}
		}

		public void draw(ShapeRenderer renderer) {
			if (hadTarget) {
				renderer.setColor(Color.CYAN);
				renderer.circle(target.x, target.y, .25f, 16);
				renderer.setColor(Color.MAGENTA);
				renderer.line(position.x, position.y, position.x + tmp.x, position.y + tmp.y);
			}
			renderer.setColor(Color.RED);
			renderer.circle(position.x, position.y, .5f, 32);
		}
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		gameViewport.update(width, height, true);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, ArriveTest.class);
	}
}

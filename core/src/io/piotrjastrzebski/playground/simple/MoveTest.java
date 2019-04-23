package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class MoveTest extends BaseScreen {
	private static final String TAG = MoveTest.class.getSimpleName();

	private final Texture texture;
	Mover mover;

	public MoveTest (GameReset game) {
		super(game);
		clear.set(0, 0.05f, 0.1f, 1);
		texture = new Texture("badlogic.jpg");
		mover = new Mover(texture);
		enableBlending();
	}

	@Override public void render (float delta) {
		super.render(delta);
		mover.update(delta);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		mover.draw(batch);
		batch.end();
	}

	static class Mover {
		float moveDuration;
		float moveTime;
		Vector2 target = new Vector2();
		Vector2 start = new Vector2();
		Vector2 pos = new Vector2();
		Interpolation interpolation = Interpolation.linear;
		Sprite sprite;

		public Mover (Texture texture) {
			sprite = new Sprite(texture);
			// screen is 40x22.5 in this example
			sprite.setSize(1, 1);
			start.set(sprite.getX(), sprite.getY());
		}

		public void move (float x, float y, float duration) {
			start.set(sprite.getX(), sprite.getY());
			target.set(x, y);
			moveDuration = duration;
			moveTime = 0;
		}

		public void update (float delta) {
			if (moveTime <= moveDuration && moveDuration > 0) {
				moveTime += delta;
				float a = moveTime/moveDuration;
				pos.set(start).interpolate(target, a, interpolation);
				sprite.setPosition(pos.x, pos.y);
			} else {
				// random position on screen
				move(MathUtils.random(-19, 19), MathUtils.random(-10.5f, 10.5f), MathUtils.random(2f, 5f));
			}
		}

		public void draw (SpriteBatch batch) {
			sprite.draw(batch);
		}
	}


	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, MoveTest.class);
	}
}

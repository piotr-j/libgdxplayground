package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class FractalTest extends BaseScreen {
	private static final String TAG = FractalTest.class.getSimpleName();
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	public final static float DURATION = .75f;
	public final static int MAX_LEVEL = 8;

	private Fractal fractal;

	public FractalTest (GameReset game) {
		super(game);
		Gdx.app.log(TAG, "Space - restart");
		fractal = new Fractal(-8, -8, 16, 16, 0);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			fractal = new Fractal(-8, -8, 16, 16, 0);
		}
		fractal.update(delta);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		fractal.draw(renderer);
		renderer.end();
	}

	// NOTE this is obviously crap, as it explodes with out of mem with like 10 levels
	private static class Fractal {
		private Color color = new Color(Color.MAGENTA);
		private float sx;
		private float x;
		private float tx;
		private float sy;
		private float y;
		private float ty;
		private final float width;
		private final float height;
		private final int level;
		private float timer;
		private Array<Fractal> splits;

		public Fractal (float x, float y, float width, float height, int level) {
			this.x = sx = tx = x;
			this.y = sy = ty = y;
			this.width = width;
			this.height = height;
			this.level = level;
			timer = (level==0)?0:DURATION;
		}

		public void update (float delta) {
			timer -= delta;
			if (timer >= 0) {
				if (timer <= DURATION/2) {
					float a = DURATION/2-timer;
					a *= 1/(DURATION/2);
					if (sx > tx || sy > ty) {
						color.set(Color.BLUE);
					} else if (sx < tx || sy < ty) {
						color.set(Color.RED);
					}
					x = MathUtils.lerp(sx, tx, a);
					y = MathUtils.lerp(sy, ty, a);
				}
			} else {
				x = tx;
				y = ty;
				color.set(Color.MAGENTA);
			}
			if (timer <= 0 && splits == null && level < MAX_LEVEL) {
				splits = new Array<>();
				if (level == 0) {
					splits.add(new Fractal(x, y, width, height/2, level+1).right());
					splits.add(new Fractal(x, y + height/2, width, height/2, level + 1).left());
				} else if (level % 2 == 1) {
					float w = width/4;
					splits.add(new Fractal(x, y, w, height, level + 1).down());
					splits.add(new Fractal(x + w, y, w, height, level + 1).up());
					splits.add(new Fractal(x + w * 2, y, w, height, level + 1).down());
					splits.add(new Fractal(x + w * 3, y, w, height, level + 1).up());
				} else {
					float h = height/4;
					splits.add(new Fractal(x, y, width, h, level + 1).right());
					splits.add(new Fractal(x, y + h, width, h, level + 1).left());
					splits.add(new Fractal(x, y + h * 2, width, h, level + 1).right());
					splits.add(new Fractal(x, y + h * 3, width, h, level + 1).left());
				}
			} else if (splits != null) {
				for (Fractal split : splits) {
					split.update(delta);
				}
			}
		}

		private Fractal right () {
			tx = x + width/4;
			return this;
		}

		private Fractal left () {
			tx = x - width/4;
			return this;
		}

		private Fractal up () {
			ty = y + height/4;
			return this;
		}

		private Fractal down () {
			ty = y - height/4;
			return this;
		}

		public void draw (ShapeRenderer renderer) {
			if (splits == null || splits.size == 0) {
				renderer.setColor(color);
				renderer.rect(x, y, width, height);
			} else {
				for (Fractal split : splits) {
					split.draw(renderer);
				}
			}
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, FractalTest.class);
	}
}

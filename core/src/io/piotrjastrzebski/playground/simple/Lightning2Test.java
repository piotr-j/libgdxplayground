package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class Lightning2Test extends BaseScreen {
	private static final String TAG = Lightning2Test.class.getSimpleName();

//	MultiLightning lightning = new MultiLightning(0f, 0f, 5f, 0f, 3);
	Array<MultiLightning> lightnings = new Array<>();
	public Lightning2Test (GameReset game) {
		super(game);
		clear.set(0, 0.05f, 0.1f, 1);

		int count = 7;

		for (int i = -count/2; i <= count/2; i++) {
			MultiLightning lightning = new MultiLightning(VP_WIDTH/count * i, VP_HEIGHT/2 - 1, VP_WIDTH/count * i, -VP_HEIGHT/2 + 1, 3);
			lightnings.add(lightning);
		}
	}

	protected static Vector2 rngPointInCircle (float x, float y, float range, Vector2 out) {
		float t = MathUtils.PI2 * MathUtils.random();
		float r = (float)Math.sqrt(MathUtils.random(2f));
		out.set(r * MathUtils.cos(t), r * MathUtils.sin(t));
		out.scl(range);
		out.add(x, y);
		return out;
	}

	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
//		lightning.update(delta);
//		lightning.draw(renderer);
		for (MultiLightning lightning : lightnings) {
			lightning.update(delta);
			lightning.draw(renderer);
		}

		renderer.end();
	}

	protected static class MultiLightning {
		Array<Lightning> lightnings = new Array<>();

		public MultiLightning (float sx, float sy, float ex, float ey, int count) {
			for (int i = 0; i < count; i++) {
				Lightning lightning = new Lightning(sx, sy, ex, ey);
				lightning.rebuildTimer = lightning.rebuildDelay/count * i;
				lightnings.add(lightning);
			}
		}

		public void update (float delta) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
				for (Lightning lightning : lightnings) {
					lightning.rebuild();
				}
			}
			for (Lightning lightning : lightnings) {
				lightning.update(delta);
			}
		}

		public void draw (ShapeRenderer renderer) {
			for (Lightning lightning : lightnings) {
				lightning.draw(renderer);
			}
		}

		public void start (float x, float y) {
			for (Lightning lightning : lightnings) {
				lightning.start(x, y);
			}
		}

		public void end (float x, float y) {
			for (Lightning lightning : lightnings) {
				lightning.end(x, y);
			}
		}
	}

	/**
	 * http://drilian.com/2009/02/25/lightning-bolts/
	 */
	protected static class Lightning {
		Array<Segment> segments = new Array<>();
		Vector2 start = new Vector2();
		Vector2 end = new Vector2();

		public Lightning () {
			this(0, 0, 5, 0);
		}

		public Lightning (float sx, float sy, float ex, float ey) {
			start.set(sx, sy);
			end.set(ex, ey);
			rebuild();
		}

		float alpha = 1;
		boolean autoRebuild = true;
		float rebuildTimer = 0;
		final float rebuildDelay = .5f;
		public void update (float delta) {
			if (rebuildTimer > 0)
				rebuildTimer -= delta;
			if (rebuildTimer <= 0) {
				rebuildTimer = rebuildDelay;
				alpha = 1;
				if (autoRebuild) {
					rebuild();
				}
			}
			alpha = Interpolation.fade.apply(1, 0, 1 - rebuildTimer/rebuildDelay);
		}

		int iterations = 6;
		float displaceNormalScale = .1f;
		float splitChance = .5f;
		float splitAngle = 40;
		float splitLenScale = .7f;
		float intensityScale = .66f;

		private void rebuild () {
			segments.clear();
			segments.add(new Segment(start, end, 1));
			Vector2 midpoint = new Vector2();
			Vector2 normal = new Vector2();
			Vector2 splitEnd = new Vector2();
			for (int i = 0; i < iterations; i++) {
				int size = segments.size;
				for (int j = size -1; j >= 0; j--) {
					// remove a segment and split it into 2(3) new segments
					Segment segment = segments.removeIndex(j);
					float dst = segment.start.dst(segment.end);
					// find the midpoint
					midpoint.set(segment.start).add(segment.end).scl(.5f);
					// displace it along the normal
					normal.set(segment.start).sub(segment.end);
					float displace = dst * displaceNormalScale;
					normal.nor().rotate(90).scl(MathUtils.random(-displace, displace));
					midpoint.add(normal);
//					rngPointInCircle(tmp.x, tmp.y, dst/5, tmp);
					segments.add(new Segment(segment.start, midpoint, segment.intensity));
					segments.add(new Segment(midpoint, segment.end, segment.intensity));
					// add an extra point
					if (MathUtils.random() <= splitChance) {
						splitEnd.set(segment.end).sub(midpoint).rotate(MathUtils.random(-splitAngle, splitAngle)).scl(splitLenScale).add(midpoint);
						segments.add(new Segment(midpoint, splitEnd, segment.intensity * intensityScale));
					}
				}
			}
		}

		public void draw(ShapeRenderer renderer) {
			for (Segment segment : segments) {
				segment.draw(renderer, alpha);
			}

		}

		public void start (float x, float y) {
			start.set(x, y);
		}

		public void end (float x, float y) {
			end.set(x, y);
		}

		protected static class Segment {
			protected final float intensity;
			public Vector2 start = new Vector2();
			public Vector2 end = new Vector2();

			public Segment (Vector2 start, Vector2 end, float intensity) {
				this.intensity = intensity;
				this.start.set(start);
				this.end.set(end);
			}

			public void draw (ShapeRenderer renderer, float alpha) {
				float a = intensity * alpha;
				renderer.setColor(a * .75f, a * .75f, a * 1.25f, 1);
//				renderer.setColor(1, 1, 1, 1);
				renderer.line(start, end);
			}
		}
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
//		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
//			lightning.end(cs.x, cs.y);
//		} else {
//			lightning.start(cs.x, cs.y);
//		}
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
//		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
//			lightning.end(cs.x, cs.y);
//		} else {
//			lightning.start(cs.x, cs.y);
//		}
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		return true;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
//		config.samples = 8;
		PlaygroundGame.start(args, config, Lightning2Test.class);
	}
}

package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class Affine2Test extends BaseScreen {
	private static final String TAG = Affine2Test.class.getSimpleName();
	protected Texture texture;
	protected Array<Thing> things = new Array<>();
	public Affine2Test (GameReset game) {
		super(game);
		texture = new Texture("badlogic.jpg");
		rebuild();
	}

	private void rebuild () {
		things.clear();
		MathUtils.random.setSeed(3458531);
		Thing thing = new Thing();
		thing.tint.set(Color.RED);
		thing.rotate = MathUtils.random(-90, 90);
		thing.region = new TextureRegion(texture);
		thing.position.set(-2, -2);
		thing.origin.set(2, 0);
		thing.size.set(4, 4);
		thing.polygon = rectPoly(4, 4);

		Thing child = new Thing();
		thing.children.add(child);
		child.parent = thing;
		child.tint.set(Color.GREEN);
		child.position.set(1, 4);
		child.rotate = MathUtils.random(-90, 90);
		child.size.set(2, 2);
		child.origin.set(1, 1);
		child.region = new TextureRegion(texture);
		child.polygon = rectPoly(2, 2);

		for (int i = 0; i < 4; i++) {
			Thing child2 = new Thing();
			child.children.add(child2);
			child2.parent = child;
			child2.region = new TextureRegion(texture);
			child2.tint.set(Color.BLUE);
			child2.size.set(1, 1);
			child2.rotate = MathUtils.random(-90, 90);
			child2.position.set(MathUtils.random(-5, 5), MathUtils.random(-5, 5));
			child2.origin.set(.5f, .5f);
			child2.polygon = rectPoly(1, 1);
		}
		things.add(thing);
		MathUtils.random.setSeed(TimeUtils.millis());
	}

	private Polygon rectPoly (float width, float height) {
		return new Polygon(new float[]{0, 0, width, 0, width, height, 0, height});
	}

	float state = 0;
	@Override public void render (float delta) {
		rebuild();
		state += delta;
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		for (Thing thing : things) {
			thing.update(delta, state);
		}

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(1, 1, 1, .5f);
		float sx = (int)-VP_WIDTH/2;
		float sy = (int)-VP_HEIGHT/2;
		int width = (int)(VP_WIDTH) + 2;
		int height = (int)(VP_HEIGHT) + 2;
		// major grid lines
		for (int x = -1; x <= width; x++) {
			renderer.line(sx + x, sy - 1, sx + x, sy + height);
		}
		for (int y = -1; y <= height; y++) {
			renderer.line(sx - 1, sy + y, sx + width, sy + y);
		}
		renderer.end();

		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Thing thing : things) {
			thing.draw(batch);
		}
		batch.end();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		for (Thing thing : things) {
			thing.debugDraw(renderer);
		}
		renderer.end();
	}

	protected static class Thing {
		protected Color tint = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
		protected TextureRegion region;
		protected Polygon polygon;
		protected Vector2 tmp = new Vector2();
		protected Vector2 tmp2 = new Vector2();

		protected Vector2 position = new Vector2();
		protected Vector2 size = new Vector2();
		protected Vector2 origin = new Vector2();
		protected Vector2 scale = new Vector2(1, 1);
		protected float rotation;

		protected Affine2 transform = new Affine2();
		protected Affine2 resultTransform = new Affine2();

		protected Thing parent;
		protected Array<Thing> children = new Array<>();

		protected float rotate;

		public void update (float delta, float state) {
			rotation = state * rotate;
			transform.setToTrnRotScl(position.x + origin.x, position.y + origin.y, rotation, scale.x, scale.y);
			if (origin.x != 0 || origin.y != 0) {
				transform.translate(-origin.x, -origin.y);
				// extra rotation for lulz
				if (false) {
					float ox = origin.x / 2;
					float oy = origin.y / 2;
					transform.translate(ox, oy);
					transform.rotate(-rotation / 2);
					transform.translate(-ox, -oy);
				}
			}

			resultTransform.set(transform);
			if (parent != null) {
//				rotation += rotate * delta;
				resultTransform.preMul(parent.resultTransform);
			} else {
//				rotation += rotate * delta;
			}
			for (Thing child : children) {
				child.update(delta, state);
			}
		}

		public void draw (SpriteBatch batch) {
			batch.setColor(tint);
			batch.draw(region, size.x, size.y, resultTransform);
			for (Thing child : children) {
				child.draw(batch);
			}
		}

		private Vector2 v2 = new Vector2();
		public void debugDraw(ShapeRenderer renderer) {
			tmp.set(origin);
			resultTransform.applyTo(tmp);
			if (polygon != null) {
				renderer.setColor(Color.YELLOW);
				float[] vertices = polygon.getVertices();
				// probably dont rebuild this
				float[] transformed = new float[vertices.length];
				for (int i = 0; i < vertices.length; i+=2) {
					resultTransform.applyTo(v2.set(vertices[i], vertices[i + 1]));
					transformed[i] = v2.x;
					transformed[i + 1] = v2.y;
				}
				renderer.polygon(transformed);
			}
			renderer.setColor(Color.WHITE);
			for (Thing child : children) {
				tmp2.set(child.origin);
				child.resultTransform.applyTo(tmp2);
				renderer.line(tmp, tmp2);
				child.debugDraw(renderer);
			}
		}
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, Affine2Test.class);
	}
}

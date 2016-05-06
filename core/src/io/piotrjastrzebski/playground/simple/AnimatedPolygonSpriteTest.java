package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Comparator;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class AnimatedPolygonSpriteTest extends BaseScreen {
	private static final String TAG = AnimatedPolygonSpriteTest.class.getSimpleName();
	PolygonSpriteBatch polyBatch;
	Texture grassTexture;

	Array<Grass> grasses = new Array<>();

	public AnimatedPolygonSpriteTest (GameReset game) {
		super(game);
		polyBatch = new PolygonSpriteBatch();
		grassTexture = new Texture("grass.png");

		for (int i = 0; i < 200; i++) {
			float x = MathUtils.random(-20f, 19f);
			float y = MathUtils.random(-11f, 10f);
			float size = MathUtils.random(1f, 2f);
			grasses.add(new Grass(x, y, size, grassTexture));
		}

		grasses.sort(new Comparator<Grass>() {
			@Override public int compare (Grass o1, Grass o2) {
				return Float.compare(o2.y, o1.y);
			}
		});
	}

	boolean debug;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .3f, .05f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		polyBatch.setProjectionMatrix(gameCamera.combined);
		polyBatch.begin();
		for (Grass grass : grasses) {
			grass.update(delta);
			grass.draw(polyBatch);
		}
		polyBatch.end();

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			debug = !debug;
		}
		if (debug) {
			renderer.setProjectionMatrix(gameCamera.combined);
			renderer.setColor(Color.CYAN);
			renderer.begin(ShapeRenderer.ShapeType.Line);
			for (Grass grass : grasses) {
				grass.drawDebug(renderer);
			}
			renderer.end();
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			for (Grass grass : grasses) {
				grass.x = MathUtils.random(-20f, 19f);
				grass.y = MathUtils.random(-11f, 10f);
				grass.size = MathUtils.random(1f, 2f);
			}

			grasses.sort(new Comparator<Grass>() {
				@Override public int compare (Grass o1, Grass o2) {
					return Float.compare(o2.y, o1.y);
				}
			});
		}
	}

	public static class Grass {
		protected float x;
		protected float y;
		protected float size;
		private float texSize;
		private final float[] vertices;
		private final float[] verticesOrig;
		private final short[] triangles;
		private final PolygonRegion region;
		private float timer;
		private float timeOffset;
		private float moveScale;

		public Grass (float x, float y, float size, Texture texture) {
			this.x = x;
			this.y = y;
			this.size = size;
			if (texture.getWidth() != texture.getHeight()) throw new AssertionError("Boo!");
			texSize = texture.getWidth();

			verticesOrig = new float[]{
				0, 0, texSize, 0,
				0, texSize/3f, texSize, texSize/3f,
				0, texSize/3f * 2f, texSize, texSize/3f * 2f,
				0, texSize, texSize, texSize
			};
			vertices = new float[verticesOrig.length];
			System.arraycopy(verticesOrig, 0, vertices, 0, verticesOrig.length);

			triangles = new short[]{
				0, 1, 2,
				2, 1, 3,
				2, 3, 4,
				4, 3, 5,
				4, 5, 6,
				6, 5, 7,
			};
			region = new PolygonRegion(new TextureRegion(texture), vertices, triangles);

			timeOffset = MathUtils.random(MathUtils.PI);
			moveScale = MathUtils.random(0.5f, 2f);
		}

		public void update(float delta) {
			timer += delta;

			for (int id = 0; id < vertices.length/4; id++) {
				int i = id * 4;
				float offset = MathUtils.sin(timer + timeOffset) * id * id * moveScale;
				vertices[i] = verticesOrig[i] + offset;
				vertices[i + 1] = verticesOrig[i + 1];
				vertices[i + 2] = verticesOrig[i + 2] + offset;
				vertices[i + 3] = verticesOrig[i + 3];
			}
		}

		public void draw (PolygonSpriteBatch batch) {
			batch.draw(region, x, y, size, size);
		}

		public void drawDebug (ShapeRenderer renderer) {
			float scl = size / texSize;
			for (int i = 0; i < triangles.length; i += 3) {
				int v1 = triangles[i];
				int v2 = triangles[i + 1];
				int v3 = triangles[i + 2];
				renderer.triangle(
					x + vertices[v1 * 2] * scl, y + vertices[v1 * 2 + 1] * scl,
					x + vertices[v2 * 2] * scl, y + vertices[v2 * 2 + 1] * scl,
					x + vertices[v3 * 2] * scl, y + vertices[v3 * 2 + 1] * scl);
			}
		}
	}

	@Override public void dispose () {
		super.dispose();
		grassTexture.dispose();
		polyBatch.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, AnimatedPolygonSpriteTest.class);
	}
}

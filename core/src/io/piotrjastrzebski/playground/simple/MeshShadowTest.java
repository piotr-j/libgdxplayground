package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class MeshShadowTest extends BaseScreen {
	private static final String TAG = MeshShadowTest.class.getSimpleName();

	Array<ShadowCaster> casters = new Array<>();
	public MeshShadowTest (GameReset game) {
		super(game);

		for (int i = 0; i < 20; i++) {
			casters.add(new ShadowCaster(MathUtils.random(-17, 15), MathUtils.random(-10, 10), MathUtils.random(1, 3), MathUtils.random(1, 3)));
			casters.add(new ShadowCaster(MathUtils.random(-17, 15), MathUtils.random(-10, 10), MathUtils.random(.5f, 1.5f)));
		}
	}

	Vector3 lightPos = new Vector3();

	@Override public void render (float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			casters.clear();
			for (int i = 0; i < 20; i++) {
				casters.add(new ShadowCaster(MathUtils.random(-17, 15), MathUtils.random(-10, 10), MathUtils.random(1, 3), MathUtils.random(1, 3)));
				casters.add(new ShadowCaster(MathUtils.random(-17, 15), MathUtils.random(-10, 10), MathUtils.random(.5f, 1.5f)));
			}
		}

		Gdx.gl.glClearColor(.3f, .7f, .3f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);

		renderer.begin(ShapeRenderer.ShapeType.Line);
		// center is in the middle, so we don't have to do anything special to cs
		float len = cs.len();
		lightPos.set(cs.x/len, cs.y/len, Math.min(len, 10));
		renderer.setColor(Color.CYAN);
		renderer.line(0, 0, lightPos.x * lightPos.z, lightPos.y * lightPos.z);
		renderer.setColor(Color.MAGENTA);
		renderer.line(0, 0, lightPos.x, lightPos.y);
		renderer.end();

		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (ShadowCaster caster : casters) {
			caster.update(lightPos);
			caster.draw(renderer);
		}
		for (ShadowCaster caster : casters) {
			caster.update(lightPos);
			caster.drawShape(renderer);
		}
		renderer.end();
	}

	public static class ShadowCaster {
		enum ShadowType {RECT, CIRCLE}
		public ShadowType type;
		public float x;
		public float y;
		public float width;
		public float height;
		public Vector3 lightDir = new Vector3();
		public Color dark = new Color(.25f, .25f, .25f, .33f);
		public Color light = new Color(.33f, .33f, .33f, .15f);
		public Color shape = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);

		public ShadowCaster (float x, float y, float radius) {
			this.x = x;
			this.y = y;
			this.width = radius;
			type = ShadowType.CIRCLE;
		}

		public ShadowCaster (float x, float y, float width, float height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			type = ShadowType.RECT;
		}

		public void update (Vector3 light) {
			// x, y direction, z magnitude
			this.lightDir.set(light);
		}

		float[] vertices = new float[10];
		public void draw (ShapeRenderer renderer) {
			switch (type) {
			case RECT:
				renderer.setColor(dark);
				if (lightDir.x >= 0) {
					if (lightDir.y >= 0) {
						// quadrant 1
						vertices[0] = vertices[4] = x;
						vertices[1] = vertices[5] = y + height;
						vertices[2] = vertices[6] = x + width;
						vertices[3] = vertices[7] = y;
						vertices[8] = x;
						vertices[9] = y;
					} else {
						// quadrant 4
						vertices[0] = vertices[4] = x;
						vertices[1] = vertices[5] = y;
						vertices[2] = vertices[6] = x + width;
						vertices[3] = vertices[7] = y + height;
						vertices[8] = x;
						vertices[9] = y + height;
					}
				} else {
					if (lightDir.y >= 0) {
						// quadrant 2
						vertices[0] = vertices[4] = x;
						vertices[1] = vertices[5] = y;
						vertices[2] = vertices[6] = x + width;
						vertices[3] = vertices[7] = y + height;
						vertices[8] = x + width;
						vertices[9] = y;
					} else {
						// quadrant 3
						vertices[0] = vertices[4] = x;
						vertices[1] = vertices[5] = y + height;
						vertices[2] = vertices[6] = x + width;
						vertices[3] = vertices[7] = y;
						vertices[8] = x + width;
						vertices[9] = y + height;
					}
				}
				for (int i = 4; i < 9; i+=2) {
					vertices[i] += -lightDir.x * lightDir.z;
					vertices[i + 1] += -lightDir.y * lightDir.z;
				}
				renderer.triangle(
					vertices[0], vertices[1],
					vertices[2], vertices[3],
					vertices[6], vertices[7], dark, dark, light
				);
				renderer.triangle(
					vertices[4], vertices[5],
					vertices[6], vertices[7],
					vertices[0], vertices[1], light, light, dark
					);
				renderer.setColor(light);
				renderer.triangle(
					vertices[4], vertices[5],
					vertices[6], vertices[7],
					vertices[8], vertices[9]
				);
				break;
			case CIRCLE:
				renderer.setColor(.25f, .25f, .25f, .33f);
				float cx = x + width;
				float cy = y + width;

				float angle = (float)Math.atan2(lightDir.y, lightDir.x) * MathUtils.radiansToDegrees + 90;
				if (angle < 0) angle += 360;
				renderer.rect(x, y + width, width, 0, width * 2, lightDir.z, 1, 1, angle, dark, dark, light, light);
				renderer.setColor(light);
				renderer.arc(cx - lightDir.x * lightDir.z, cy - lightDir.y * lightDir.z, width, angle, 180, 16);
				break;
			}
		}
		public void drawShape (ShapeRenderer renderer) {
			renderer.setColor(shape);
			switch (type) {
			case RECT:
					renderer.rect(x, y, width, height);
				break;
			case CIRCLE:
				renderer.circle(x + width, y + width, width, 16);
				break;
			}
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, MeshShadowTest.class);
	}
}

package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Comparator;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class BatchShadowTest extends BaseScreen {
	private static final String TAG = BatchShadowTest.class.getSimpleName();

	Texture shadowTexture;
	Array<ShadowCaster> casters = new Array<>();
	public BatchShadowTest (GameReset game) {
		super(game);

		final int width = 4;
		final int height = 8;
		Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		Pixmap.setBlending(Pixmap.Blending.None);

		for (int y = 0; y < height; y++) {
			float a = ((float)y)/(height-1);
			pixmap.setColor(1, 1, 1, a);
//			pixmap.setColor(a, a, a, a);
			pixmap.drawLine(0, y, width, y);
		}

		shadowTexture = new Texture(pixmap);
		shadowTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		ShadowCaster.texture = shadowTexture;
		pixmap.dispose();
		Pixmap.setBlending(Pixmap.Blending.SourceOver);
		addCasters(20);
	}

	private Vector2 v2 (float x, float y) {
		return new Vector2(x, y);
	}

	Vector3 lightPos = new Vector3();

	private void addCasters(int count) {
		for (int i = 0; i < count; i++) {
			switch (MathUtils.random(3)) {
			case 0: { // poly rect
//				casters.add(new ShadowCaster(MathUtils.random(-17f, 15), MathUtils.random(-10f, 10), MathUtils.random(1, 3), MathUtils.random(1, 3)));
				float x = MathUtils.random(-17f, 15);
				float y = MathUtils.random(-10f, 10);
				float w = MathUtils.random(1, 3);
				float h = MathUtils.random(1, 3);
				float angle = MathUtils.random(360);
				Vector2[] vector2s = new Vector2[]{
					v2(x + -w/2, y + -h/2).rotate(angle),
					v2(x + w/2, y - h/2).rotate(angle),
					v2(x + w/2, y + h/2).rotate(angle),
					v2(x + -w/2, y + h/2).rotate(angle)
				};
				casters.add(new ShadowCaster(x, y, vector2s));
			} break;
			case 1: { // circle
//				casters.add(new ShadowCaster(MathUtils.random(-17f, 15), MathUtils.random(-10f, 10), MathUtils.random(.5f, 1.5f)));
				float x = MathUtils.random(-17f, 15);
				float y = MathUtils.random(-10f, 10);
				float r = MathUtils.random(1, 3);
				final int c = MathUtils.random(2, 4) * 6;
				Vector2 tmp = new Vector2();
				Vector2[] vector2s = new Vector2[c];
				float angle = 0;
				float step = 360f/(c-1);
				for (int j = 0; j < c; j++) {
					tmp.set(1, 0).rotate(angle).scl(r);
					vector2s[j] = new Vector2(x, y).add(tmp);
					angle += step;
				}
				casters.add(new ShadowCaster(x, y, vector2s));
			} break;
			case 2: { // polygon aka triangle
				float x = MathUtils.random(-17f, 15);
				float y = MathUtils.random(-10f, 10);
				float angle = MathUtils.random(360);
				Vector2[] vector2s = new Vector2[]{
					v2(x + -1, y + 0).rotate(angle),
					v2(x + 1, y + 0).rotate(angle),
					v2(x + 0, y + .5f).rotate(angle)
				};
				casters.add(new ShadowCaster(x, y, vector2s));
			} break;
			case 3: { // star?
				/*

				 */
				float x = MathUtils.random(-17f, 15);
				float y = MathUtils.random(-10f, 10);
				float angle = MathUtils.random(360);
				Vector2[] vector2s = new Vector2[]{
					v2(x + 0, y + 1).rotate(angle),
					v2(x - 1, y - 1).rotate(angle),
					v2(x + 0, y + 0).rotate(angle),
					v2(x + 1, y - 1).rotate(angle),
//					v2(x + 0, y + 0).rotate(angle),
//					v2(x + 0, y + 0).rotate(angle)
				};
				casters.add(new ShadowCaster(x, y, vector2s));
			} break;
		}
		}
		casters.sort(new Comparator<ShadowCaster>() {
			@Override public int compare (ShadowCaster o1, ShadowCaster o2) {
				return o1.y > o2.y? -1:1;
			}
		});
	}

	float[] quad = new float[4 * 5];
	@Override public void render (float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			casters.clear();
			addCasters(20);
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

		enableBlending();
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (ShadowCaster caster : casters) {
			caster.update(lightPos);
			caster.draw(batch);
		}
//		batch.draw(shadowTexture, 0, 0, shadowTexture.getWidth()/2, shadowTexture.getHeight()/2);

		quad(quad, Color.RED, v2(0, 0), v2(0, 1), v2(1, 1), v2(1, 0));
		batch.draw(shadowTexture, quad, 0, quad.length);

		batch.end();

		enableBlending();
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (ShadowCaster caster : casters) {
//			caster.update(lightPos);
//			caster.draw(renderer);
			caster.update(lightPos);
			caster.drawShape(renderer);
		}
		renderer.end();
	}

	static void quad (float[] quad, Color color, Vector2 bottomLeft, Vector2 bottomRight, Vector2 topRight, Vector2 topLeft) {
		quad(quad, color, bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y, topRight.x, topRight.y, topLeft.x, topLeft.y);
	}

	static void quad (float[] quad, Color color, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		float colorBits = color.toFloatBits();
		final float u = 0;
		final float v = 1;
		final float u2 = 1;
		final float v2 = 0;
		int idx = 0;
		quad[idx++] = x1;
		quad[idx++] = y1;
		quad[idx++] = colorBits;
		quad[idx++] = u;
		quad[idx++] = v;

		quad[idx++] = x2;
		quad[idx++] = y2;
		quad[idx++] = colorBits;
		quad[idx++] = u;
		quad[idx++] = v2;

		quad[idx++] = x3;
		quad[idx++] = y3;
		quad[idx++] = colorBits;
		quad[idx++] = u2;
		quad[idx++] = v2;

		quad[idx++] = x4;
		quad[idx++] = y4;
		quad[idx++] = colorBits;
		quad[idx++] = u2;
		quad[idx++] = v;
	}

	public static class ShadowCaster {
		private Vector2[] polyVerts;

		enum ShadowType {RECT, CIRCLE, POLY}
		public ShadowType type;
		public static Texture texture;
		public float x;
		public float y;
		public float width;
		public float height;
		public Vector3 lightDir = new Vector3();
		public Color dark = new Color(.25f, .25f, .25f, .33f);
//		public Color light = new Color(.25f, .25f, .25f, .33f);
		public Color light = new Color(.33f, .33f, .33f, .05f);
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

		public ShadowCaster (float x, float y, Vector2[] verts) {
			this.x = x;
			this.y = y;
			this.polyVerts = verts;
			type = ShadowType.POLY;
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
			case CIRCLE: {
				renderer.setColor(.25f, .25f, .25f, .33f);
				float cx = x + width;
				float cy = y + width;

				float angle = (float)Math.atan2(lightDir.y, lightDir.x) * MathUtils.radiansToDegrees + 90;
				if (angle < 0)
					angle += 360;
				renderer.rect(x, y + width, width, 0, width * 2, lightDir.z, 1, 1, angle, dark, dark, light, light);
				renderer.setColor(light);
				renderer.arc(cx - lightDir.x * lightDir.z, cy - lightDir.y * lightDir.z, width, angle, 180, 16);
			}break;
			case POLY: {
				renderer.setColor(dark);
				tmpOffset.set(lightDir.x, lightDir.y).scl(-lightDir.z);
				Vector2 first = polyVerts[polyVerts.length-1];
				for (int i = 0; i < polyVerts.length; i++) {
					Vector2 second = polyVerts[i];

					// find out normal
					tmp1.set(first).sub(second).nor().set(-tmp1.y, tmp1.x);
					float dot = tmp1.dot(lightDir.x, lightDir.y);
					if (dot < 0) {
						tmpFirst.set(first).add(tmpOffset);
						tmpSecond.set(second).add(tmpOffset);

						renderer.triangle(
							first.x,first.y,
							second.x, second.y,
							tmpFirst.x, tmpFirst.y, dark, dark, light
						);
						renderer.triangle(
							tmpFirst.x, tmpFirst.y,
							second.x, second.y,
							tmpSecond.x,tmpSecond.y, light, dark, light
						);
					}
//					renderer.rectLine(tmpFirst, tmpSecond, .1f);
					first = second;
				}
				if (false) {
					// we know that we actually have triangles, not polys so whatever
					renderer.setColor(light);
					Vector2 v1 = polyVerts[0];
					Vector2 v2 = polyVerts[1];
					Vector2 v3 = polyVerts[2];
					renderer.triangle(v1.x + tmpOffset.x, v1.y + tmpOffset.y, v2.x + tmpOffset.x, v2.y + tmpOffset.y, v3.x + tmpOffset.x,
						v3.y + tmpOffset.y);
				}
			} break;
			}
		}

		float[] quad = new float[4 * 5];
		Vector2 tmp1 = new Vector2();
		Vector2 tmpOffset = new Vector2();
		Vector2 tmpFirst = new Vector2();
		Vector2 tmpSecond = new Vector2();
		public void draw (SpriteBatch batch) {
			switch (type) {
			case POLY: {

				tmpOffset.set(lightDir.x, lightDir.y).scl(-lightDir.z);
				Vector2 first = polyVerts[polyVerts.length-1];
				for (int i = 0; i < polyVerts.length; i++) {
					Vector2 second = polyVerts[i];

					// find out normal
					tmp1.set(first).sub(second).nor().set(-tmp1.y, tmp1.x);
					float dot = tmp1.dot(lightDir.x, lightDir.y);
					if (dot < 0) {
						tmpFirst.set(first).add(tmpOffset);
						tmpSecond.set(second).add(tmpOffset);

						quad(quad, dark, second, tmpSecond, tmpFirst, first);
						batch.draw(texture, quad, 0, quad.length);
						/*
						renderer.triangle(
							first.x,first.y,
							second.x, second.y,
							tmpFirst.x, tmpFirst.y, dark, dark, light
						);
						renderer.triangle(
							tmpFirst.x, tmpFirst.y,
							second.x, second.y,
							tmpSecond.x,tmpSecond.y, light, dark, light
						);
						*/
					}
//					renderer.rectLine(tmpFirst, tmpSecond, .1f);
					first = second;
				}
			}
			case CIRCLE: {

			} break;
			case RECT: {

			} break;
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
			case POLY:
				Vector2 tmp1 = new Vector2();
				Vector2 first = polyVerts[polyVerts.length-1];
				for (int i = 0; i < polyVerts.length; i++) {
					Vector2 second = polyVerts[i];
					renderer.setColor(Color.CYAN);
					tmp1.set(first).sub(second).nor().set(-tmp1.y, tmp1.x);
//					renderer.rectLine(firs .x, first.y, first.x + tmp1.x, first.y + tmp1.y, .05f);
					float dot = tmp1.dot(lightDir.x, lightDir.y);
					if (dot < 0) {
						renderer.setColor(Color.BLACK);
					} else {
						renderer.setColor(shape.r, shape.g, shape.b, 1);
					}
					renderer.rectLine(first, second, .1f);
					first = second;
				}
				break;
			}
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, BatchShadowTest.class);
	}
}

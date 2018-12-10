package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.Iterator;

/**
 * Created by EvilEntity on 14/06/2016.
 */
public class StutterTest extends ApplicationAdapter implements InputProcessor {
	private final static String TAG = StutterTest.class.getSimpleName();
	public static float SCALE = 32f;
	public static float INV_SCALE = 1.f/ SCALE;
	public static float WIDTH = 1280 * INV_SCALE;
	public static float HEIGHT = 720 * INV_SCALE;

	OrthographicCamera gameCam;
	ExtendViewport gameVP;
	ShapeRenderer renderer;
	SpriteBatch batch;
	Array<Bullet> bullets = new Array<>();
	Texture texture;

	public StutterTest () {
		super();
	}

	@Override public void create () {
		super.create();
		gameCam = new OrthographicCamera();
		gameVP = new ExtendViewport(WIDTH, HEIGHT, gameCam);

		renderer = new ShapeRenderer();

		Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.BLACK);
		pixmap.fillCircle(8, 8, 5);
		pixmap.setColor(Color.GREEN);
		pixmap.fillCircle(8, 8, 3);
		pixmap.setColor(1, 1, 1, .3f);
		pixmap.fillCircle(8, 8, 2);
		pixmap.fillCircle(8, 8, 1);
		texture = new Texture(pixmap);
		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		pixmap.dispose();
		batch = new SpriteBatch();
		Gdx.input.setInputProcessor(this);
	}

	Pool<Bullet> bulletPool = new Pool<Bullet>(4096, 4096 * 2) {
		@Override protected Bullet newObject () {
			return new Bullet(0, 0, .25f);
		}
	};

	Vector2 tmp = new Vector2();
	Vector2 centre = new Vector2(WIDTH/2, HEIGHT/2);
	Rectangle bounds = new Rectangle();
	final float FIXED_DT = 1/120f;
	final float MAX_DT = 1/15f;
	float acc;

	float bulletOffset;
	float spawnTimer;
	float spawnTime = 1/40f;

	@Override public void render () {
		if (touched) {
			centre.set(tp);
		}

		float rdt = Gdx.graphics.getRawDeltaTime();
		if (rdt > MAX_DT) rdt = MAX_DT;

		acc += rdt;
		while (acc >= FIXED_DT) {
			{
				final int count = 12;
				final float step = 360f / count;
				bulletOffset += FIXED_DT * 90;
				if (bulletOffset > step) {
					bulletOffset -= step;
				}
				spawnTimer += FIXED_DT;
				while (spawnTimer >= spawnTime) {
					spawnTimer -= spawnTime;
					float angle = bulletOffset;
					for (int i = 0; i < count; i++) {
						tmp.set(1, 0).rotate(angle);
						Bullet collider = bulletPool.obtain().init(centre.x + tmp.x, centre.y + tmp.y, .25f);
						collider.texture(texture);
						collider.update(0);
						collider.vel.set(tmp).scl(16);
						bullets.add(collider);
						angle += step;
					}
				}
			}

			for (Bullet bullet : bullets) {
				bullet.update(FIXED_DT);
			}
			acc -= FIXED_DT;
		}

		float alpha = acc/ FIXED_DT;

		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(gameCam.combined);
		batch.begin();
		Iterator<Bullet> it = bullets.iterator();
		while (it.hasNext()) {
			Bullet collider = it.next();
			if (!collider.aabb.overlaps(bounds)) {
				bulletPool.free(collider);
				it.remove();
			} else {
				collider.draw(batch, alpha);
			}
		}
		batch.end();

	}

	private static class Bullet {
		public Rectangle aabb = new Rectangle();
		public Vector2 vel = new Vector2();
		private Vector2 prevPos = new Vector2();
		private Vector2 currPos = new Vector2();
		private float radius;
		TextureRegion region;
		public Bullet (float x, float y, float radius) {
			prevPos.set(x, y);
			currPos.set(x, y);
			this.radius = radius;
		}

		public void update (float delta) {
			prevPos.set(currPos);
			if (!vel.isZero()) {
				currPos.x += vel.x * delta;
				currPos.y += vel.y * delta;
			}
			aabb.set(currPos.x - radius, currPos.y - radius, radius * 2, radius * 2);
		}

		public void draw (SpriteBatch batch, float alpha) {
			float x = prevPos.x * (1 - alpha) + currPos.x * alpha;
			float y = prevPos.y * (1 - alpha) + currPos.y * alpha;
			batch.draw(region, x - radius, y - radius, radius * 2, radius * 2);
		}

		public Bullet texture (Texture texture) {
			if (region == null) {
				region = new TextureRegion(texture);
			}
			return this;
		}

		public Bullet init (float x, float y, float radius) {
			currPos.set(x, y);
			prevPos.set(x, y);
			this.radius = radius;
			return this;
		}
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		gameVP.update(width, height, true);
		bounds.set(0, 0, gameVP.getWorldWidth(), gameVP.getWorldHeight());
		centre.set(bounds.width/2, bounds.height/2);
	}

	@Override public void dispose () {
		super.dispose();
		renderer.dispose();
		texture.dispose();
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			Gdx.app.exit();
		}
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	boolean touched;
	Vector2 tp = new Vector2();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameVP.unproject(tp.set(screenX, screenY));
		touched = true;
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		gameVP.unproject(tp.set(screenX, screenY));
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		gameVP.unproject(tp.set(screenX, screenY));
		touched = false;
		return false;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameVP.unproject(tp.set(screenX, screenY));
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		if (true) {
			config.width = 1920;
			config.height = 1200;
			config.fullscreen = true;
		} else {
			config.width = 1280 * 3 / 4;
			config.height = 720 * 3 / 4;
		}
		config.useHDPI = true;
		new LwjglApplication(new StutterTest(), config);
	}
}

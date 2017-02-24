package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Iterator;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class FTSTest extends BaseScreen {
	private static final String TAG = FTSTest.class.getSimpleName();

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

	public FTSTest (GameReset game) {
		super(game);

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
	float clearTimer;
	boolean paused;
	int clearTimes;

	@Override public void render (float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			paused = !paused;
		}

		if (touched) {
			centre.set(tp);
		}

		float rdt = Gdx.graphics.getRawDeltaTime();
		if (rdt > MAX_DT) rdt = MAX_DT;

		if (!paused) {
			acc += rdt;
			int frames = 0;
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
//						Bullet bullet = bulletPool.obtain().init(centre.x + tmp.x, centre.y + tmp.y, .25f);
							Bullet bullet = bulletPool.obtain().init(centre.x, centre.y, .25f);
							bullet.texture(texture);
							bullet.update(0);
							bullet.vel.set(tmp).scl(16);
							bullets.add(bullet);
							angle += step;
						}
					}
				}

				for (Bullet bullet : bullets) {
					bullet.update(FIXED_DT);
				}
				acc -= FIXED_DT;
				frames++;
			}
			if (frames != 1) {
				Gdx.app.log(TAG, "f " + frames + " a " + acc/ FIXED_DT);
			}
		}
		float alpha = acc/ FIXED_DT;

		if (!paused) {
			clearTimer += rdt;
			if (clearTimer > .5f) {
				clearTimes++;
				Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				if (clearTimes > 1) {
					clearTimes = 0;
					clearTimer -= .5f;
				}
			}
		}
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
		LwjglApplicationConfiguration config = PlaygroundGame.config();
//		config.vSyncEnabled = false;
		if (false) {
			config.width = 1980;
			config.height = 1200;
			config.fullscreen = true;
		}
		PlaygroundGame.start(args, config, FTSTest.class);
	}
}

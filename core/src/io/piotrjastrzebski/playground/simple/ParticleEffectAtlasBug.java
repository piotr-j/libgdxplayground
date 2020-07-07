package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class ParticleEffectAtlasBug extends ApplicationAdapter {
	public final static float SCALE = 128f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;

	private OrthographicCamera camera;
	private ExtendViewport viewport;
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private TextureAtlas atlas;
	private ParticleEffect normal;
	private ParticleEffect stripped;
	@Override public void create () {
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);
		// TODO dispose stuff
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		atlas = new TextureAtlas(Gdx.files.internal("particles/particles.atlas"));

		normal = new ParticleEffect();
		normal.load(Gdx.files.internal("particles/fire-normal.p"), atlas);
		normal.setPosition(-2.5f, 0);
		stripped = new ParticleEffect();
		stripped.load(Gdx.files.internal("particles/fire-packed.p"), atlas);
		stripped.setPosition(2.5f, 0);
	}

	@Override public void render () {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		float delta = Gdx.graphics.getDeltaTime();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		normal.draw(batch, delta);
		// offset by 2,2, same as offset in atlas
		stripped.draw(batch, delta);
		batch.end();

		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		shapes.setColor(Color.CYAN);
		ParticleEmitter normalEmitter = normal.getEmitters().get(0);
		shapes.circle(normalEmitter.getX(), normalEmitter.getY(), .1f, 16);
		ParticleEmitter strippedEmitter = stripped.getEmitters().get(0);
		shapes.circle(strippedEmitter.getX(), strippedEmitter.getY(), .1f, 16);
		shapes.end();
	}

	@Override public void resize (int width, int height) {
		viewport.update(width, height, false);
	}

	public static void main (String[] arg) {
		PlaygroundGame.start(new ParticleEffectAtlasBug());
	}
}

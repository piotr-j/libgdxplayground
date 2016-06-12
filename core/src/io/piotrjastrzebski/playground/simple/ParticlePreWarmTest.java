package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ParticlePreWarmTest extends BaseScreen {
	private static final String TAG = ParticlePreWarmTest.class.getSimpleName();

	final ParticleEffect effect;
	public ParticlePreWarmTest (GameReset game) {
		super(game);

		effect = new ParticleEffect();
		effect.load(Gdx.files.internal("particles/test.p"), Gdx.files.internal("particles"));
		effect.getEmitters().removeRange(1, effect.getEmitters().size-1);
		effect.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		ParticleEmitter emitter = effect.getEmitters().first();
		float acc = 0;
		float duration = emitter.getDuration().newLowValue();
		while (acc < duration) {
			emitter.update(1/30f);
			acc += 1/30f;
		}
//		emitter.update(duration);
	}


	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.enableBlending();
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
		effect.update(delta);
		effect.draw(batch);
		batch.end();
	}


	@Override public void dispose () {
		super.dispose();
		effect.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		PlaygroundGame.start(args, ParticlePreWarmTest.class);
	}
}

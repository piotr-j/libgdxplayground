package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class ParticleTest extends BaseScreen {
	ParticleEffectPool pool;
	Array<ParticleEffectPool.PooledEffect> effects;
	ParticleEffectPool.PooledEffect current;
	public ParticleTest (PlaygroundGame game) {
		super(game);
		ParticleEffect effect = new ParticleEffect();
		effect.load(Gdx.files.internal("particles/test.p"), Gdx.files.internal("particles"));

		pool = new ParticleEffectPool(effect, 8, 16);
		effects = new Array<>();
	}

	@Override public void render (float delta) {
		super.render(delta);
		// use gui cam as the effect is not setup for scaled drawing
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
		Iterator<ParticleEffectPool.PooledEffect> iterator = effects.iterator();
		while (iterator.hasNext()) {
			ParticleEffectPool.PooledEffect next = iterator.next();
			next.draw(batch, delta);
			if (next.isComplete()) {
				pool.free(next);
				iterator.remove();
			}
		}
		batch.end();
	}

	private void createEffect (float x, float y) {
		ParticleEffectPool.PooledEffect effect = pool.obtain();
		effect.setPosition(x, y);
		current = effect;
		effects.add(effect);
	}

	private void moveEffect (float x, float y) {
		if (current != null) {
			current.setPosition(x, y);
		}
	}

	private void finishEffect (float x, float y) {
		if (current != null) {
			current.allowCompletion();
		}
	}

	Vector3 pos = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		guiCamera.unproject(pos.set(screenX, screenY, 0));
		createEffect(pos.x, pos.y);
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		guiCamera.unproject(pos.set(screenX, screenY, 0));
		moveEffect(pos.x, pos.y);
		return super.touchDragged(screenX, screenY, pointer);
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		guiCamera.unproject(pos.set(screenX, screenY, 0));
		finishEffect(pos.x, pos.y);
		return super.touchUp(screenX, screenY, pointer, button);
	}
}

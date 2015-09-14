package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

import java.util.Iterator;

public class ParticleGUITest extends BaseScreen {
	ParticleEffectPool pool;
	Array<ParticleEffectPool.PooledEffect> effects;
	public ParticleGUITest (GameReset game) {
		super(game);
		final ParticleEffect effect = new ParticleEffect();
		effect.load(Gdx.files.internal("particles/test.p"), Gdx.files.internal("particles"));
		effect.getEmitters().removeRange(1, effect.getEmitters().size-1);

		pool = new ParticleEffectPool(effect, 8, 16);
		effects = new Array<>();

		final Vector2 pos = new Vector2();

		VisTable cont = new VisTable(true);
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				final VisTextButton button = new VisTextButton("Btn " + x + " " + y);
				ClickListener clicked = new ClickListener(){
					@Override public void clicked (InputEvent event, float x, float y) {
						button.localToStageCoordinates(pos.set(x, y));
						createEffect(pos.x, pos.y);
					}
				};
				button.addListener(clicked);
				cont.add(button);
			}
			cont.row();
		}
		stage.addActor(cont);
		// center
		cont.setPosition(
			Gdx.graphics.getWidth()/2 - cont.getWidth()/2,
			Gdx.graphics.getHeight()/2 - cont.getHeight()/ 2);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
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
		// if needs to be rendered in world coordinates, x, y could be unprojected
//		gameCamera.unproject(vec3.set(x, y, 0));
		ParticleEffectPool.PooledEffect effect = pool.obtain();
		effect.setPosition(x, y);
		effects.add(effect);
		effect.allowCompletion();
	}
}

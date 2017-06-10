package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIToastTest extends BaseScreen {
	private final static String TAG = UIToastTest.class.getSimpleName();

	public UIToastTest (GameReset game) {
		super(game);
		Toasts.init(skin, stage);
		clear.set(.5f, .5f, .5f, 1);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			Toasts.show("Welp got a thing! " + MathUtils.random(123));
		}
	}

	@Override public void dispose () {
		super.dispose();
		Toasts.dispose();
	}

	private static class Toasts {
		static int max = 3;
		static Skin skin;
		static Stage stage;
		static Array<Label> active = new Array<>();

		public static void show (String text) {
			show(text, 1.5f);
		}

		public static void show (String text, float duration) {
			final Label label = new Label(text, skin);
			float x = (stage.getWidth() - label.getWidth()) / 2;
			label.getColor().a = 0;
			label.setPosition(x, 0);
			label.addAction(Actions.sequence(
				Actions.parallel(
					Actions.fadeIn(.2f, Interpolation.circleOut),
					Actions.moveBy(0, 50, .2f, Interpolation.circleOut)
				),
				Actions.delay(duration),
				Actions.parallel(
					Actions.fadeOut(.2f, Interpolation.circleIn),
					Actions.moveBy(0, 50, .2f, Interpolation.circleIn)
				),
				Actions.run(new Runnable() {
					@Override public void run () {
						active.removeValue(label, true);
					}
				}),
				Actions.removeActor()
			));
			for (int i = 0; i < active.size; i++) {
				Label visible = active.get(i);
				visible.addAction(
					Actions.sequence(
						Actions.delay(i * .05f),
						Actions.moveBy(0, visible.getHeight() * 1.2f, .1f, Interpolation.circleOut)
					));
			}
			active.add(label);
			stage.addActor(label);
			if (active.size > max) {
				Label evict = active.removeIndex(0);
				evict.clearActions();
				evict.addAction(Actions.sequence(
					Actions.parallel(
						Actions.fadeOut(.2f, Interpolation.circleIn),
						Actions.moveBy(0, 50, .2f, Interpolation.circleIn)
					),
					Actions.removeActor()
				));
			}
		}

		public static void init (Skin skin, Stage stage) {
			Toasts.skin = skin;
			Toasts.stage = stage;
		}

		public static void dispose () {
			for (Label label : active) {
				label.remove();
			}
			active.clear();
			stage = null;
			skin = null;
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIToastTest.class);
	}
}

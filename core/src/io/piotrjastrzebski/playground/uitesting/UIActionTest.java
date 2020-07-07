package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIActionTest extends BaseScreen {
	private final static String TAG = UIActionTest.class.getSimpleName();
	Label label;
	int frame = 0;
	public UIActionTest (GameReset game) {
		super(game);

		label = new Label("TEXT!!!", skin) {
			@Override public void setPosition (float x, float y) {
				super.setPosition(x, y);
				Gdx.app.log("Label", "setPosition " + x + ", " + y);
			}

			@Override public void setPosition (float x, float y, int alignment) {
				super.setPosition(x, y, alignment);
				Gdx.app.log("Label:" + frame, "setPosition " + x + ", " + y + ", " + alignment);
			}

			@Override public void moveBy (float x, float y) {
				super.moveBy(x, y);
				Gdx.app.log("Label:"+frame, "moveBy " + x + ", " + y);
			}
		};
		root.addActor(label);
	}

	void addAction() {
		frame = 0;
		float cx = stage.getWidth()/2;
		float cy = stage.getHeight()/2;
		label.clearActions();
		label.setPosition(cx - 250, cy, Align.center);
		float duration = 5;
		label.addAction(Actions.parallel(
			Actions.moveTo(cx + 250, cy, duration),
			Actions.sequence(
				Actions.action(MoveByAddAction.class).from(0f, 0f).to(0f, 50f).duration(duration/2),
				Actions.action(MoveByAddAction.class).from(0f, 50f).to(0f, 0f).duration(duration/2)
			)
		));
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
		frame++;
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			Gdx.app.log("W", "Add Action");
			addAction();
		}
	}

	public static class MoveByAddAction extends TemporalAction {
		private float fromX, fromY;
		private float toX, toY;

		public MoveByAddAction () {
			setInterpolation(Interpolation.linear);
		}

		@Override protected void update (float percent) {
			float dx = getInterpolation().apply(fromX, toX, percent);
			float dy = getInterpolation().apply(fromY, toY, percent);
			target.moveBy(dx, dy);
		}

		public MoveByAddAction to(float x, float y) {
			toX = x;
			toY = y;
			return this;
		}

		public MoveByAddAction from(float x, float y) {
			fromX = x;
			fromY = y;
			return this;
		}

		public MoveByAddAction duration(float duration) {
			setDuration(duration);
			return this;
		}

		public MoveByAddAction interpolation(Interpolation interpolation) {
			setInterpolation(interpolation);
			return this;
		}

		@Override public void reset () {
			super.reset();
			toX = 0;
			toY = 0;
			fromX = 0;
			fromY = 0;
		}
	}


	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowedMode(1280/2, 720/2);
		PlaygroundGame.start(args, config, UIActionTest.class);
	}
}

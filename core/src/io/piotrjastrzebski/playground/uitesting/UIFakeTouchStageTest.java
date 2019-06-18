package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIFakeTouchStageTest extends BaseScreen {
	FakeStage fakeStage;
	public UIFakeTouchStageTest (GameReset game) {
		super(game);

		fakeStage = new FakeStage();
		multiplexer.clear();
		multiplexer.addProcessor(this);
		multiplexer.addProcessor(fakeStage);
	}

	private static class FakeStage extends Stage {
		private Vector2 v2 = new Vector2();
		public void fakeTouchDown (int stageX, int stageY, int pointer, int button) {
			stageToScreenCoordinates(v2.set(stageX, stageY));
			touchDown((int)v2.x, (int)v2.y, pointer, button);
		}

		public void fakeTouchDragged (int stageX, int stageY, int pointer) {
			stageToScreenCoordinates(v2.set(stageX, stageY));
			touchDragged((int)v2.x, (int)v2.y, pointer);
		}

		public void fakeTouchUp (int stageX, int stageY, int pointer, int button) {
			stageToScreenCoordinates(v2.set(stageX, stageY));
			touchUp((int)v2.x, (int)v2.y, pointer, button);
		}

		@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
			PLog.log("fs::touchDown " + screenX + ", " + screenY);
			return super.touchDown(screenX, screenY, pointer, button);
		}

		@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
			PLog.log("fs::touchDragged " + screenX + ", " + screenY);
			return super.touchDragged(screenX, screenY, pointer);
		}

		@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
			PLog.log("fs::touchUp " + screenX + ", " + screenY);
			return super.touchUp(screenX, screenY, pointer, button);
		}
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fakeStage.act(delta);
		fakeStage.draw();
	}


	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		PLog.log("touchDown " + screenX + ", " + screenY);
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		PLog.log("touchDragged " + screenX + ", " + screenY);
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		PLog.log("touchUp " + screenX + ", " + screenY);
		return false;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIFakeTouchStageTest.class);
	}
}

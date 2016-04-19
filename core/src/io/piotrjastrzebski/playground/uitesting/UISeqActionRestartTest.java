package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class UISeqActionRestartTest extends BaseScreen {
	private static final String TAG = UISeqActionRestartTest.class.getSimpleName();
	SequenceAction action;

	public UISeqActionRestartTest (GameReset game) {
		super(game);
		final VisTextButton vtb = new VisTextButton("WELP!");
		vtb.setPosition(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2);
		stage.addActor(vtb);

//		action = Actions.sequence(Actions.moveBy(-50, 0, 1), Actions.moveBy(50, 0, 1));
		MoveByAction mb1 = new MoveByAction();
		mb1.setAmount(-50, 0);
		mb1.setDuration(1);
		MoveByAction mb2 = new MoveByAction();
		mb2.setAmount(50, 0);
		mb2.setDuration(1);
		action = new SequenceAction(mb1, mb2);
		vtb.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				vtb.clearActions();
				action.restart();
				vtb.addAction(action);
			}
		});
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UISeqActionRestartTest.class);
	}
}

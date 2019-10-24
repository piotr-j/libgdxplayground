package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIRotateLabelTest extends BaseScreen {
	private final static String TAG = UIRotateLabelTest.class.getSimpleName();

	public UIRotateLabelTest (GameReset game) {
		super(game);

		Label label = new Label("aaa", skin);
		Container<Label> container = new Container<>(label);
		container.setTransform(true);
		container.pack();
		container.setOrigin(Align.center);
		container.addAction(Actions.forever(
			Actions.rotateBy(360f, 5f)
		));
		container.setPosition(stage.getWidth()/2, stage.getHeight()/2, Align.center);
		container.debugAll();
		stage.addActor(container);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIRotateLabelTest.class);
	}
}

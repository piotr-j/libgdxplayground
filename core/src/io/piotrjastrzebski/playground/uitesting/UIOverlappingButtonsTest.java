package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIOverlappingButtonsTest extends BaseScreen {
	final static String TAG = UIOverlappingButtonsTest.class.getSimpleName();

	public UIOverlappingButtonsTest (GameReset game) {
		super(game);
		clear.set(.5f, .5f, .5f, 1);

		VisTextButton button1 = new VisTextButton("Button 1");
		button1.setPosition(100, 100);
		button1.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log(TAG, "Clicked button 1");
			}
		});
		VisTextButton button2 = new VisTextButton("Button 2");
		button2.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log(TAG, "Clicked button 2");
			}
		});
		button2.setPosition(125, 108);

		root.addActor(button1);
		root.addActor(button2);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowedMode(1280/2, 720/2);
		PlaygroundGame.start(args, config, UIOverlappingButtonsTest.class);
	}
}

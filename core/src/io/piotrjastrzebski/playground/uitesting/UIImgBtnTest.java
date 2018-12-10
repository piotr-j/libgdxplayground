package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIImgBtnTest extends BaseScreen {
	Texture texture;
	public UIImgBtnTest (GameReset game) {
		super(game);
		texture = new Texture("badlogic.jpg");
		ImageButton button = new ImageButton(new SpriteDrawable(new Sprite(texture)));
		stage.addActor(button);
		button.setSize(200, 200);
		button.setPosition(600, 400);
		button.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log("", "welp");
			}
		});
		button.debugAll();
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIImgBtnTest.class);
	}
}

package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIOverlayTest extends BaseScreen {
	Texture logo;
	public UIOverlayTest (GameReset game) {
		super(game);

		Table table = new Table();
		root.add(table);
		root.add(new VisTextButton("Button!"));

		logo = new Texture("badlogic.jpg");

		clear.set(Color.FOREST);
	}

	float alpha = 0;
	@Override public void render (float delta) {
		super.render(delta);
		alpha += delta;

		gameViewport.apply();
		batch.enableBlending();
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		batch.draw(logo, -2, -2, 4, 4);

		shapes.setColor(.5f, .5f, .5f, MathUtils.sin(alpha)/4 + .5f);
		shapes.filledRectangle(-5, -5, 10, 10);
		batch.end();

		guiViewport.apply();
		stage.act(delta);
		stage.draw();

	}


	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UIOverlayTest.class);
	}
}

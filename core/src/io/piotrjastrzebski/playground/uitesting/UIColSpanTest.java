package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIColSpanTest extends BaseScreen {
	Texture badlogic;

	public UIColSpanTest (GameReset game) {
		super(game);

		badlogic = new Texture("badlogic.jpg");

		Table uiTable = new Table();
		uiTable.setFillParent(true);
		stage.addActor(uiTable);
		uiTable.pad(5);
		uiTable.add(new Label("POINTS: 0", skin));
		uiTable.add().expandX();
		for (int i = 0; i < 3; i++) {
			Image image = new Image(new TextureRegionDrawable(new TextureRegion(badlogic)));
			uiTable.add(image).size(100);
		}
		uiTable.row();
		uiTable.add().height(150);
		uiTable.row();
		uiTable.add(new Label("WARNING!", skin)).colspan(5).expandY();

		uiTable.debugAll();
	}


	public void render(float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
		badlogic.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIColSpanTest.class);
	}
}

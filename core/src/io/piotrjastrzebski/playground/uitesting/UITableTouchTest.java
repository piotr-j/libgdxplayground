package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITableTouchTest extends BaseScreen {
	public UITableTouchTest (GameReset game) {
		super(game);
		VisTable table = new VisTable(true);
//		table.setSize(200, 200);
//		table.setPosition(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 -100);
		table.setFillParent(true);
		table.setBackground(VisUI.getSkin().getDrawable("window"));
		stage.addActor(table);
		table.setTouchable(Touchable.enabled);
		table.addListener(new InputListener(){
			@Override public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Gdx.app.log("table", "welp");
				return super.touchDown(event, x, y, pointer, button);
			}
		});
		Gdx.input.setInputProcessor(stage);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UITableTouchTest.class);
	}
}

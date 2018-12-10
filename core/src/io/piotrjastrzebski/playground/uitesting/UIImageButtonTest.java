package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIImageButtonTest extends BaseScreen {
	Texture texture;
	public UIImageButtonTest (GameReset game) {
		super(game);
		texture = new Texture("badlogic.jpg");
		TextureRegion region = new TextureRegion(texture);
		Table table = new Table();

		TextButton simple = new TextButton("some text", skin);
		table.add(simple).expand().fill();
		table.row();

		ImageButton.ImageButtonStyle style = skin.get("default", ImageButton.ImageButtonStyle.class);
		ImageButton.ImageButtonStyle newStyle = new ImageButton.ImageButtonStyle(style);
		newStyle.imageUp = new TextureRegionDrawable(region);

		ImageButton button = new ImageButton(newStyle);
		table.add(button).expand().fill();
		table.row();

		root.add(table).expand().fill().pad(100);
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
		PlaygroundGame.start(args, UIImageButtonTest.class);
	}
}

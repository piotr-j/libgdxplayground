package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTextField;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIOverlapTFTest extends BaseScreen {
	private final static String TAG = UIOverlapTFTest.class.getSimpleName();

	Texture texture;

	public UIOverlapTFTest (GameReset game) {
		super(game);

		texture = new Texture("badlogic.jpg");

		Table table = new Table();
		VisImage img = new VisImage(texture);
		table.add(img).prefSize(32);
		VisTextField vtf = new VisTextField("");
		table.add(vtf).padLeft(-32);
		img.setZIndex(1);
		root.add(table);
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
		PlaygroundGame.start(args, UIOverlapTFTest.class);
	}
}

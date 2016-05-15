package io.piotrjastrzebski.playground.uitesting;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIWrapTest extends BaseScreen {
	public UIWrapTest (GameReset game) {
		super(game);

		VisWindow window = new VisWindow("Window!");
		window.setResizable(true);
		window.setMovable(true);

		window.debugAll();
		window.defaults().pad(5);
		window.add(new VisLabel("Label1!"));
		window.add().expandX().fillX();
		window.add(new VisLabel("Label2!"));
		window.row();
		VisLabel longLabel = new VisLabel("ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!ELLIPSIS!");
		longLabel.setWrap(true);
		longLabel.setEllipsis(true);
		window.add(longLabel).expandX().fillX().colspan(3);
		window.row();
		VisLabel longLabel2 = new VisLabel("WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!WRAP!");
		longLabel2.setWrap(true);
		window.add(longLabel2).expandX().fillX().colspan(3);
		window.row();
		window.add().colspan(3).expand().fill();
		window.row();
		window.add(new VisLabel("Label3!"));
		window.add().expandX().fillX();
		window.add(new VisLabel("Label4!"));
		window.pack();

		stage.addActor(window);
		window.centerWindow();
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
		PlaygroundGame.start(args, UIWrapTest.class);
	}
}

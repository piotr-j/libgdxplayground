package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIListTest extends BaseScreen {
	private final static String TAG = UIListTest.class.getSimpleName();

	private Array<String> strings = new Array<>();
	VisList<String> list;
	private VisScrollPane pane;
	public UIListTest (GameReset game) {
		super(game);

		for (int i = 0; i < 10; i++) {
			strings.add("S="+strings.size+"   ;");
		}

		VisWindow welp = new VisWindow("welp");
		welp.centerWindow();
		stage.addActor(welp);
		list = new VisList<>();
		list.setItems(strings);
		pane = new VisScrollPane(list);
		welp.add(pane);
		welp.layout();
		pane.setScrollPercentY(100);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			strings.add("S="+strings.size+"   ;");
			list.setItems(strings);
			pane.layout();
			pane.setScrollPercentY(100);
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIListTest.class);
	}
}

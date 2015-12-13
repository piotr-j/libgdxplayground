package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISelectBoxTest extends BaseScreen {
	private final static String TAG = UISelectBoxTest.class.getSimpleName();

	public UISelectBoxTest (GameReset game) {
		super(game);

		SelectBox.SelectBoxStyle style = VisUI.getSkin().get(SelectBox.SelectBoxStyle.class);
		style.fontColor.set(Color.RED);
		VisSelectBox<String> selectBox = new VisSelectBox<>(style);
		selectBox.setItems(new Array<>(new String[] {"Value 1", "Value 2", "Value 3", "Value 4"}));
		root.add(selectBox);
		VisSelectBox<String> selectBox2 = new VisSelectBox<>(style);
		selectBox2.setItems(new Array<>(new String[] {"Value 1", "Value 2", "Value 3", "Value 4"}));
		root.add(selectBox2);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}
}

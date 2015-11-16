package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIReplaceFontTest extends BaseScreen {
	BitmapFont oldFont;
	public UIReplaceFontTest (GameReset game) {
		super(game);

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid-serif-bold.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 12;
		BitmapFont droidBold12 = generator.generateFont(parameter);
		generator.dispose();

		Label.LabelStyle labelStyle = VisUI.getSkin().get(Label.LabelStyle.class);
		oldFont = labelStyle.font;
		labelStyle.font = droidBold12;
		VisLabel label = new VisLabel("DroidBold12!?");
		root.add(label);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
		Label.LabelStyle labelStyle = VisUI.getSkin().get(Label.LabelStyle.class);
		labelStyle.font = oldFont;
	}
}

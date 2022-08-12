package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.TypingLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Valid for textra 0.6.3
 */
public class UITextraTest extends BaseScreen {
	static final String prefix = "textra/";
	public UITextraTest (GameReset game) {
		super(game);
		KnownFonts.setAssetPrefix(prefix);
		// NOTE requires knownFonts to be placed in textra/
		Font[] fonts = KnownFonts.getAllStandard();
		BitmapFont[] bitmapFonts = getFonts();
		Table labels = new Table();
		labels.defaults().pad(5);
		for (int i = 0; i < fonts.length; i++) {
			Font font = fonts[i];
			labels.add(new VisLabel(font.name)).left();
			TypingLabel label = new TypingLabel("Dummy Text 123", skin, font);
			labels.add(label).expandX().left();
			label.validate();

			BitmapFont bf = bitmapFonts[i];
			if (bf != null) {
				Label.LabelStyle style = new Label.LabelStyle();
				style.font = bf;
				style.fontColor = Color.WHITE;
				Label bmLabel = new Label("Dummy Text 123", style);
				bmLabel.validate();
				float scale = label.getPrefHeight()/bmLabel.getPrefHeight();
				PLog.log(font.name + ", " + label.getPrefHeight() + ", " + bmLabel.getPrefHeight() + ", " + scale);
				bmLabel.setFontScale(scale);
				labels.add(bmLabel).expandX().left();
			} else {
				labels.add(new VisLabel("MISSING!")).expandX().left();
			}
			labels.row();
		}

		root.add(labels);
		labels.debugAll();

	}

	private BitmapFont[] getFonts () {
		return new BitmapFont[] {
			getFont("AStarry"),
			getFont("Bitter"),
			getFont("Canada1500"),
			getFont("CascadiaMono"),
			getFont("Cozette"),
			getFont("Gentium"),
			getFont("Hanazono"),
			// cant load this one, wrong format
//			getFont("IBM-8x16"),
			null,
			getFont("Inconsolata-LGC-Custom"),
			getFont("Iosevka"),
			getFont("Iosevka-Slab"),
			getFont("KingthingsFoundation"),
			getFont("LibertinusSerif"),
			getFont("OpenSans"),
			getFont("Oxanium"),
			getFont("RobotoCondensed"),
			getFont("YanoneKaffeesatz")
		};
	}

	private BitmapFont getFont (String name) {
		BitmapFont bf = new BitmapFont(Gdx.files.internal(prefix + name + "-standard.fnt"));
		bf.setUseIntegerPositions(false);
		return bf;
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
		PlaygroundGame.start(args, UITextraTest.class);
	}
}

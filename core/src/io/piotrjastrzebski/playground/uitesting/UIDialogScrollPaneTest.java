package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDialogScrollPaneTest extends BaseScreen {

	public UIDialogScrollPaneTest (GameReset game) {
		super(game);
		VisDialog dialog = new VisDialog("Dialog with pane!");
		VisTable content = new VisTable();
		final VisScrollPane scrollPane = new VisScrollPane(content);
		for (int i = 1; i <= 500; i++) {
			content.add(new VisLabel("Label " + i)).expandX().fillX();
			content.add().pad(5).expandX().fillX();
			content.add(new VisTextButton("Button " + i)).row();
		}
		scrollPane.setScrollingDisabled(true, false);
		dialog.getContentTable().add(scrollPane).expandX().fillX().row();
		VisTextButton scrollToBottom = new VisTextButton("Bottom");
		scrollToBottom.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				scrollPane.setScrollPercentY(1);
//				float height = scrollPane.getMaxY();
//				int size = scrollPane.getChildren().size;
//				float childHeight = height/size;
//				scrollPane.setScrollY(childHeight * size);
			}
		});
		VisTextButton scrollToTop = new VisTextButton("Top");
		scrollToTop.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				scrollPane.setScrollPercentY(0);
			}
		});
		Table buttons = new Table();
		buttons.add(scrollToBottom).left();
		buttons.add(new VisTextButton("Close")).expandX();
		buttons.add(scrollToTop).right();
		dialog.getContentTable().add(buttons).expandX().fillX();
		stage.addActor(dialog.fadeIn());
		dialog.setResizable(true);
		dialog.centerWindow();
		dialog.debugAll();
//		dialog.pack();
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIDialogScrollPaneTest.class);
	}
}

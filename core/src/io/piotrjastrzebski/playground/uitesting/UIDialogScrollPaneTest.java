package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
		VisScrollPane scrollPane = new VisScrollPane(content);
		for (int i = 1; i <= 100; i++) {
			content.add(new VisLabel("Label " + i)).expandX().fillX();
			content.add(new VisTextButton("Button " + i)).row();
		}
		dialog.getContentTable().add(scrollPane).expandX().fillX().row();
		dialog.getContentTable().add(new VisTextButton("Close"));
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

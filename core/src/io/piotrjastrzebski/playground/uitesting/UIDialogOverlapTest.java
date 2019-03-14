package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDialogOverlapTest extends BaseScreen {
	private final static String TAG = UIDialogOverlapTest.class.getSimpleName();

	private TextureRegionDrawable drawable;
	private Table gameRoot;
	private Table guiRoot;

	public UIDialogOverlapTest (GameReset game) {
		super(game);

		drawable = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("badlogic.jpg"))));

		gameRoot = new Table();
		gameRoot.setFillParent(true);
		root.addActor(gameRoot);
		guiRoot = new Table();
		guiRoot.setFillParent(true);
		root.addActor(guiRoot);

		TextButton showDialog = new TextButton("dialog", skin);
		showDialog.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				final Dialog dialog = new Dialog("Modal dialog", skin, "dialog");
				dialog.setModal(true);
				dialog.getContentTable().add(new Label("Hello from dialog", skin)).row();
				TextButton closeDialog = new TextButton("close", skin);
				closeDialog.addListener(new ChangeListener() {
					@Override public void changed (ChangeEvent event, Actor actor) {
						dialog.hide();
					}
				});
				dialog.getContentTable().add(closeDialog);
				dialog.show(stage);
			}
		});
		guiRoot.add(showDialog);


	}

	float timer;
	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();

		timer += delta;
		if (timer > 1) {
			timer -= 1;
			for (int i = 0; i < 10; i++) {
				final Image image = new Image(drawable);
				image.setScale(MathUtils.random(.25f, .5f));
				image.setPosition(MathUtils.random(Gdx.graphics.getWidth()), MathUtils.random(Gdx.graphics.getHeight()));
				image.addListener(new ClickListener(){
					@Override public void clicked (InputEvent event, float x, float y) {
						image.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
					}
				});
				gameRoot.addActor(image);
			}
		}
	}


	public static void main (String[] args) {
		PlaygroundGame.start(args, UIDialogOverlapTest.class);
	}

}

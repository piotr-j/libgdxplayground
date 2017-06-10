package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIEditTest extends BaseScreen {
	private final static String TAG = UIEditTest.class.getSimpleName();
	Image white;
	VisWindow window;

	public UIEditTest (GameReset game) {
		super(game);
		// use movable, resizeable windows as containers for stuff?
		clear.set(.5f, .5f, .5f, 1);

		white = new Image(skin.getDrawable("white"));
		TextureRegionDrawable drawable = (TextureRegionDrawable)white.getDrawable();
		drawable.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		white.setSize(200, 400);
		white.setPosition(300, 250);
		white.setColor(Color.MAGENTA);
		root.addActor(white);

		window = new VisWindow("") {
			@Override protected void drawBackground (Batch batch, float parentAlpha, float x, float y) {
				setColor(1, 1, 1, .5f);
				super.drawBackground(batch, parentAlpha, x, y);
				setColor(Color.WHITE);
			}
		};
		window.setResizable(true);
		window.setMovable(true);
	}

	boolean edit = false;
	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			edit = !edit;
			if (edit) {
				startEdit();
			} else {
				endEdit();
			}
		}
//		Gdx.app.log(TAG, "Window = " + window.getWidth() + "x" + window.getHeight() + ", img = " + white.getWidth() + "x" + white.getHeight());
	}

	private Vector2 v2 = new Vector2();
	private void endEdit () {
		white.localToStageCoordinates(v2.set(0, 0));
		float actorWidth = white.getWidth();
		float actorHeight = white.getHeight();
		root.addActor(white);
		white.setPosition(v2.x, v2.y);
		white.setSize(actorWidth, actorHeight);
		window.clearChildren();
		window.remove();
	}

	private void startEdit () {
		// we assume that the actor is added to root directly as addActor
		float actorX = white.getX();
		float actorY = white.getY();
		float actorWidth = white.getWidth();
		float actorHeight = white.getHeight();
		window.add(white).expand().fill();
		// note this is skin dependant
		float gw = 9; // 9
		float gh = 56; // 56

		window.setSize(actorWidth + gw, actorHeight + gh);
		// note offsets skin dependant
		window.setPosition(actorX -3, actorY-2);
		root.addActor(window);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIEditTest.class);
	}
}

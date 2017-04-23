package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISplitViewportsTest extends BaseScreen {
	protected final static String TAG = UISplitViewportsTest.class.getSimpleName();

	private UIStacks3Test screenLeft;
	private UIStacks3Test screenRight;
	public UISplitViewportsTest (GameReset game) {
		super(game);
		clear.set(Color.WHITE);
		screenLeft = new UIStacks3Test(game);
		screenLeft.clear.set(Color.MAGENTA);
		screenRight = new UIStacks3Test(game);
		screenRight.clear.set(Color.CYAN);

		multiplexer.addProcessor(screenLeft.getInputProcessor());
		multiplexer.addProcessor(screenRight.getInputProcessor());
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override public void render (float delta) {
		super.render(delta);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		{
			ScreenViewport viewport = screenLeft.getViewport();
			viewport.apply();
			HdpiUtils.glScissor(viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
			screenLeft.render(delta);
		}
		{
			ScreenViewport viewport = screenRight.getViewport();
			viewport.apply();
			HdpiUtils.glScissor(viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
			screenRight.render(delta);
		}
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		screenLeft.resize(width/2, height);
		screenRight.resize(width/2, height);
		screenRight.getViewport().setScreenX(width/2);
	}

	@Override public void dispose () {
		super.dispose();
		screenLeft.dispose();
		screenRight.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UISplitViewportsTest.class);
	}

}

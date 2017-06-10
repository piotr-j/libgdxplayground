package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIStacks6Test extends BaseScreen {
	protected final static String TAG = UIStacks6Test.class.getSimpleName();

	TextureRegion region;
	public UIStacks6Test (GameReset game) {
		super(game);

		clear.set(Color.DARK_GRAY);

		// simple coin
		float scale = .5f;
		Pixmap pixmap = new Pixmap((int)(128 * scale), (int)(140 * scale), Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.LIGHT_GRAY);
		pixmap.fillCircle((int)(64 * scale), (int)(64 * scale), (int)(60 * scale));
		pixmap.setColor(Color.WHITE);
		pixmap.fillCircle((int)(64 * scale), (int)(74 * scale), (int)(60 * scale));
		pixmap.setColor(Color.LIGHT_GRAY);
		pixmap.fillCircle((int)(64 * scale), (int)(74 * scale), (int)(40 * scale));
		pixmap.setColor(Color.WHITE);
		pixmap.fillCircle((int)(64 * scale), (int)(70 * scale), (int)(36 * scale));
		region = new TextureRegion(new Texture(pixmap));
		region.flip(false, true);
		pixmap.dispose();

		rebuild();
	}

	protected Vector2 v2 = new Vector2();
	protected void rebuild() {
		root.clear();

		Table table = new Table();
		Table table1 = new Table();
		table.add(createImage(.75f, .75f, Color.CYAN)).expand();
		table.add(table1).expand();
		table.add(createImage(.75f, .75f, Color.CYAN)).expand();
		for (int i = 0; i < 3; i++) {
			table1.add(createImage(1, 1, Color.RED));
			table1.add(createImage(1, 1, Color.GREEN));
			table1.add(createImage(1, 1, Color.BLUE));
			table1.row();
		}
		root.add(table).colspan(3).row();
		for (int i = 0; i < 3; i++) {
			root.add(createImage(1.25f, 1.25f, Color.LIGHT_GRAY));
			root.add(createImage(1.25f, 1.25f, Color.GRAY));
			root.add(createImage(1.25f, 1.25f, Color.DARK_GRAY));
			root.row();
		}
		root.debugAll();
	}

	protected Image createImage(final float scaleX, final float scaleY, final Color color) {
		TextureRegionDrawable drawable = new TextureRegionDrawable(region);
		drawable.setMinWidth(region.getRegionWidth() * scaleX);
		drawable.setMinHeight(region.getRegionHeight() * scaleY);
		final Image image = new Image(drawable);
		image.setOrigin(Align.center);
		image.setColor(color);
		image.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				image.localToStageCoordinates(v2.set(image.getWidth()/2, image.getHeight()/2));
				Image img = createImage(scaleX * .5f, scaleY * .5f, Color.ORANGE);
				img.setPosition(v2.x, v2.y, Align.center);
				stage.addActor(img);
				img.clearListeners();
			}
		});
		return image;
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			rebuild();
		}
	}

	@Override public void dispose () {
		super.dispose();
		region.getTexture().dispose();
	}

	public ScreenViewport getViewport() {
		return guiViewport;
	}

	public InputProcessor getInputProcessor() {
		return multiplexer;
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width *= .5f;
		config.height *= .5f;
		PlaygroundGame.start(args, config, UIStacks6Test.class);
	}

}

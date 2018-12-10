package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIExtendTest extends BaseScreen {
	private final static String TAG = UIExtendTest.class.getSimpleName();
	private final static int TARGET_WIDTH = 450;
	private final static int TARGET_HEIGHT = 800;
	private OrthographicCamera extendCamera;
	private ExtendViewport extendGui;
	Array<VisTextButton> buttons = new Array<>();
	public UIExtendTest (GameReset game) {
		super(game);
		extendCamera = new OrthographicCamera();
		extendGui = new ExtendViewport(TARGET_WIDTH, TARGET_HEIGHT, extendCamera);

		stage = new Stage(extendGui, batch);
		stage.addActor(root);
		multiplexer.clear();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);

		clear.set(.5f, .5f, .5f, 1);

		for (int i = 0; i < 16; i++) {
			final VisTextButton button = new VisTextButton("WELP " + MathUtils.random(1234) + " ?!");
			buttons.add(button);
			button.setPosition(MathUtils.random(0, TARGET_WIDTH - 50), MathUtils.random(0, TARGET_HEIGHT - 50));
			button.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					button.setPosition(MathUtils.random(0, TARGET_WIDTH - 50), MathUtils.random(0, TARGET_HEIGHT - 50));
				}
			});
			root.addActor(button);
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
		enableBlending();
		renderer.setProjectionMatrix(extendCamera.combined);
		renderer.setColor(0, .5f, 0, .25f);
		renderer.begin(ShapeRenderer.ShapeType.Filled);

		renderer.end();
	}

	private Vector2 v2 = new Vector2();
	@Override public void resize (int width, int height) {
		super.resize(width, height);
		extendGui.update(width, height);
		Vector2 scaled = Scaling.fit.apply(TARGET_WIDTH, TARGET_HEIGHT, width, height);
		float scale = scaled.x/TARGET_WIDTH;
		Gdx.app.log(TAG, "New x scale " + scale);
		float invScale = MathUtils.clamp(1/scale, .25f, 1);
		Gdx.app.log(TAG, "New x invscale " + invScale);

		BitmapFont font = VisUI.getSkin().getFont("default-font");
		font.getData().setScale(invScale);
		// gotta force the labels to validate somehow...
		invalidate(root);
	}

	private void invalidate (Group group) {
		for (Actor actor : group.getChildren()) {
			if (actor instanceof Layout) {
				((Layout)actor).invalidate();
			}
			if (actor instanceof Group) {
				invalidate((Group)actor);
			}
		}
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		float scale = .5f;
		config.width = (int)(TARGET_WIDTH * scale);
		config.height = (int)(TARGET_HEIGHT * scale);
		config.useHDPI = true;
		PlaygroundGame.start(args, config, UIExtendTest.class);
	}
}

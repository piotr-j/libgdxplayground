package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.scenes.scene2d.ui.Table.Debug.actor;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIEdit2Test extends BaseScreen {
	private final static String TAG = UIEdit2Test.class.getSimpleName();
	TextureRegion region;
	Array<Actor> editables = new Array<>();
	Array<VisWindow> windows = new Array<>();
//	Image white;
	float lw;
	float rw;
	float th;
	float bh;
	public UIEdit2Test (GameReset game) {
		super(game);
		// use movable, resizeable windows as containers for stuff?
		clear.set(.5f, .5f, .5f, 1);

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
		Pixmap pixmap2 = new Pixmap((int)(128 * scale), (int)(140 * scale * .5f), Pixmap.Format.RGBA8888);
		pixmap2.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, pixmap.getWidth(), pixmap.getHeight()/2);
		region = new TextureRegion(new Texture(pixmap2));

		region.flip(false, true);
		pixmap.dispose();
		pixmap2.dispose();

		VisWindow window = createWindow();
		NinePatchDrawable np = (NinePatchDrawable)window.getBackground();
		lw = np.getLeftWidth();
		rw = np.getRightWidth();
		bh = np.getBottomHeight();
		th = np.getTopHeight();

		rebuild();
	}

	private VisWindow createWindow () {
		VisWindow window = new VisWindow("") {
			@Override protected void drawBackground (Batch batch, float parentAlpha, float x, float y) {
				setColor(1, 1, 1, .5f);
				super.drawBackground(batch, parentAlpha, x, y);
				setColor(Color.WHITE);
			}
		};
		window.setResizable(true);
		window.setMovable(true);
		return window;
	}

	private void rebuild () {
		endEdit();
		for (Actor editable : editables) {
			editable.remove();
		}
		editables.clear();

		TextureRegionDrawable white = (TextureRegionDrawable)skin.getDrawable("white");
		white.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		TextureRegionDrawable coin = new TextureRegionDrawable(region);

		for (int i = 0; i < 3; i++) {
			Actor actor = new PulsingLabel("", "", "ASDG!!!" + i * 3, skin, 1);
			editables.add(actor);
			actor.setPosition(MathUtils.random(0, Gdx.graphics.getWidth() - 100), MathUtils.random(0, Gdx.graphics.getHeight() - 100));
			root.addActor(actor);
		}
	}

	boolean edit = false;
	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
		renderer.setProjectionMatrix(guiCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		for (Actor editable : editables) {
			editable.localToStageCoordinates(v2.set(0, 0));
			float actorX = v2.x;
			float actorY = v2.y;
			float actorWidth = editable.getWidth();
			float actorHeight = editable.getHeight();
			if (editable instanceof Layout) {
				actorWidth = ((Layout)editable).getPrefWidth();
				actorHeight = ((Layout)editable).getPrefHeight();
			}
			renderer.setColor(Color.MAGENTA);
			renderer.rect(actorX, actorY, actorWidth, actorHeight);
		}
		renderer.end();

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			edit = !edit;
			if (edit) {
				startEdit();
			} else {
				endEdit();
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			rebuild();
		}
//		Gdx.app.log(TAG, "Window = " + window.getWidth() + "x" + window.getHeight() + ", img = " + white.getWidth() + "x" + white.getHeight());
	}

	private Vector2 v2 = new Vector2();
	private void endEdit () {
		if (actor == null) return;
		edit = false;

		for (int i = 0; i < editables.size; i++) {
			Actor actor = editables.get(i);
			VisWindow window = windows.get(i);
			actor.localToStageCoordinates(v2.set(0, 0));
			float actorWidth = actor.getWidth();
			float actorHeight = actor.getHeight();
			root.addActor(actor);
			actor.setPosition(v2.x, v2.y);
//		actor.setSize(actorWidth, actorHeight);
			window.clearChildren();
			window.remove();
		}
	}

	private void startEdit () {
		// we assume that the actor is added to root directly as addActor
//		if (actor instanceof Layout) {
//			((Layout)actor).invalidateHierarchy();
//			((Layout)actor).layout();
//		}
//		if (actor instanceof Container) {
//			Container container = (Container)actor;
//			container.setSize(container.getPrefWidth(), container.getPrefHeight());
//			container.setPosition(container.getX() - container.getWidth()/2, container.getY() - container.getHeight()/2);
//		}
		windows.clear();
		for (Actor actor : editables) {
			float actorX = actor.getX();
			float actorY = actor.getY();
			float actorWidth = actor.getWidth();
			float actorHeight = actor.getHeight();
			if (actor instanceof Layout) {
				actorWidth = ((Layout)actor).getPrefWidth();
				actorHeight = ((Layout)actor).getPrefHeight();
			}
			VisWindow window = createWindow();
			window.add(actor).expand().fill();
			// note this is skin dependant

			window.setSize(actorWidth + lw + rw, actorHeight + bh+ th );
			// note offsets skin dependant
			window.setPosition(actorX - lw, actorY-bh);
			window.setResizeBorder(16);
			windows.add(window);
			root.addActor(window);
		}

	}

	static class PulsingLabel extends Container {

		private String ownerTag;
		private Label label;
		private String tag;

		/**
		 * @param text
		 * @param skin
		 * @param fontScale
		 */
		public PulsingLabel (final String ownerTag, final String tag, String text, Skin skin, float fontScale) {
			super();
			this.ownerTag = ownerTag;
			this.tag = tag;
			label = new Label(text, skin, "default") {
				@Override public String toString () {
					return tag + "::" + super.toString();
				}
			};
			setText(text);
			label.setFontScale(fontScale);
			setActor(label);
			setTransform(false);
			setOrigin(0, 0);
			setPosition(100, 100);
			//setDebug(true, true);
		}

		/**
		 * need to call layout? set center?
		 *
		 * @param text
		 */
		public void setText (String text) {
			label.setText(text);
//			invalidateHierarchy();  // hierarchy because it affects width, etc.
//			layout();
			setSize(label.getPrefWidth(), label.getPrefHeight());
			setOrigin(Align.center);
			// layout or ???
		}

		public float getLabelWidth () {
			return label.getWidth();
		}

		public float getLabelHeight () {
			return label.getHeight();
		}

		@Override public void setColor (Color c) {
			label.setColor(c);
		}

		public void pulse (String s, float duration, float maxScale) {
			pulse(s, duration, maxScale, 0.15f);
		}

		/**
		 * Maybe change from maxScale to a proportion to existing scale? then can be
		 * negative for making smaller...
		 *
		 * @param s
		 * @param duration
		 * @param maxScale
		 */
		public void pulse (String s, float duration, float maxScale, float finalAlpha) {
			clearActions();

			setText(s);
			setTransform(true);
			setScale(1);
			addAction(sequence(scaleTo(maxScale, maxScale, duration * .2f, Interpolation.sineOut),
				scaleTo(1, 1, duration * .2f, Interpolation.sine), Actions.run(new Runnable() {
					@Override public void run () {
						setTransform(false);
					}
				})));
		}

		public void fadeOut (float delay, float duration) {
			addAction(sequence(delay(delay), Actions.fadeOut(duration)));
		}

		public void fadeIn (float delay, float duration) {
			addAction(sequence(delay(delay), Actions.fadeIn(duration)));
		}

		@Override public boolean remove () {
			return super.remove();
		}

		@Override public String toString () {
			return "PokerLabel{'" + tag + "', " + label.getText() + '}';
		}
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width /= 2;
		config.height /= 2;
		PlaygroundGame.start(args, config, UIEdit2Test.class);
	}
}

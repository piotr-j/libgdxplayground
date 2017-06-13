package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
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
	TextureRegion region;
	Actor actor;
//	Image white;
	VisWindow window;
	float lw;
	float rw;
	float th;
	float bh;
	public UIEditTest (GameReset game) {
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
		region = new TextureRegion(new Texture(pixmap));

		region.flip(false, true);
		pixmap.dispose();

		window = new VisWindow("") {
			@Override protected void drawBackground (Batch batch, float parentAlpha, float x, float y) {
				setColor(1, 1, 1, .5f);
				super.drawBackground(batch, parentAlpha, x, y);
				setColor(Color.WHITE);
			}
		};
		window.setResizable(true);
		window.setMovable(true);
		NinePatchDrawable np = (NinePatchDrawable)window.getBackground();
		lw = np.getLeftWidth();
		rw = np.getRightWidth();
		bh = np.getBottomHeight();
		th = np.getTopHeight();

		rebuild();
	}

	private void rebuild () {
		endEdit();
		if (actor != null)
			actor.remove();

		Table table = new Table();
		TextureRegionDrawable white = (TextureRegionDrawable)skin.getDrawable("white");
		white.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		TextureRegionDrawable coin = new TextureRegionDrawable(region);
		for (int i = 0; i < 5; i++) {
//			VertGroup group = new VertGroup();
//			group.debug();
//			group.reverse();
//			group.align(Align.bottom);
//			group.setActorCap(5);
//			group.setActorSize(coin.getRegion().getRegionWidth(), coin.getRegion().getRegionHeight());

			VertGroup2 group = new VertGroup2();
			group.align(Align.bottom);
			group.setActorCap(5);
			for (int j = 0, n = 5; j < n; j++) {
				final Image image = new Image(coin);
				image.setScaling(Scaling.fit);
				image.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), .5f);
//				group.space(-image.getWidth() * .8f);
//				group.addActor(image);
//				group.setActorSize(image.getPrefWidth(), image.getPrefHeight());
//				group.add(image).expand().fill().padTop(-image.getPrefHeight() * .8f).row();
				group.add(image).expand().fill().padTop(percentPrefHeight(-.8f, image)).row();
//				group.add(image).expand().fill().row();
			}

			table.add(group).expand(4, 1).fillX().bottom();
			if (i < 4)
				table.add().expand();
		}
		table.setSize(200, 400);
		table.setPosition(300, 250);
		table.pack();
		root.addActor(table);
		actor = table;
	}

	private Value percentPrefWidth (final float percent, final Layout layout) {
		return new Value() {
			public float get (Actor context) {
				return layout.getPrefWidth() * percent;
			}
		};
	}

	private Value percentPrefHeight (final float percent, final Layout layout) {
		return new Value() {
			public float get (Actor context) {
				return layout.getPrefHeight() * percent;
			}
		};
	}

	static class VertGroup2 extends Table {

		private int actorCap;
		private int actors;

		public void setActorCap (int actorCap) {
			this.actorCap = actorCap;
//			for (int i = 0; i < actorCap; i++) {
//				add().expand().fill().row();
//			}
		}

//		@Override public <T extends Actor> Cell<T> add (T actor) {
//			if (actor == null) return super.add(actor);
//			Array<Cell> cells = getCells();
//			return cells.get(actors++).setActor(actor);
//		}
	}

	static class VertGroup extends VerticalGroup {
		private int actorCap = 1;
		private float actorWidth;
		private float actorHeight;

		@Override public float getPrefHeight () {
			float ph = getPadBottom() + getPadTop();
			ph += getSpace() * (actorCap - 1);
			ph += actorHeight * actorCap;
			return ph;
		}

		@Override public float getMinWidth () {
			return 16;
		}

		@Override public float getMinHeight () {
			return 16;
		}

		public void setActorCap (int actorCap) {
			this.actorCap = actorCap;
		}

		public void setActorSize (float actorWidth, float actorHeight) {
			this.actorWidth = actorWidth;
			this.actorHeight = actorHeight;
		}
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
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			rebuild();
		}
//		Gdx.app.log(TAG, "Window = " + window.getWidth() + "x" + window.getHeight() + ", img = " + white.getWidth() + "x" + white.getHeight());
	}

	private Vector2 v2 = new Vector2();
	private void endEdit () {
		if (actor == null) return;
		edit = false;
		actor.localToStageCoordinates(v2.set(0, 0));
		float actorWidth = actor.getWidth();
		float actorHeight = actor.getHeight();
		root.addActor(actor);
		actor.setPosition(v2.x, v2.y);
		actor.setSize(actorWidth, actorHeight);
		window.clearChildren();
		window.remove();
	}

	private void startEdit () {
		// we assume that the actor is added to root directly as addActor
		float actorX = actor.getX();
		float actorY = actor.getY();
		float actorWidth = actor.getWidth();
		float actorHeight = actor.getHeight();
		window.add(actor).expand().fill();
		// note this is skin dependant

		window.setSize(actorWidth + lw + rw, actorHeight + bh+ th );
		// note offsets skin dependant
		window.setPosition(actorX - lw, actorY-bh);
		root.addActor(window);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIEditTest.class);
	}
}

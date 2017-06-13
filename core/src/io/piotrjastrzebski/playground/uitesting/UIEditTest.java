package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;
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
		Pixmap pixmap2 = new Pixmap((int)(128 * scale), (int)(140 * scale * .5f), Pixmap.Format.RGBA8888);
		pixmap2.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, pixmap.getWidth(), pixmap.getHeight()/2);
		region = new TextureRegion(new Texture(pixmap2));

		region.flip(false, true);
		pixmap.dispose();
		pixmap2.dispose();

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
		TextureRegionDrawable white = (TextureRegionDrawable)skin.getDrawable("white");
		white.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		TextureRegionDrawable coin = new TextureRegionDrawable(region);

		if (false) {
			Table table = new Table();

			for (int i = 0; i < 5; i++) {
				VertGroup group = new VertGroup();
				group.debug();
				group.reverse();
				group.align(Align.bottom);
				group.setActorCap(5);
				group.setActorSize(coin.getRegion().getRegionWidth(), coin.getRegion().getRegionHeight());

//			VertGroup2 group = new VertGroup2();
//			group.align(Align.bottom);
//			group.setActorCap(5);
//			for (int j = 0, n = 5; j < n; j++) {
				for (int j = 0, n = MathUtils.random(2, 4); j < n; j++) {
					final Image image = new Image(coin);
					image.setScaling(Scaling.fit);
					image.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), .5f);
//				group.space(-image.getWidth() * .8f);
					group.addActor(image);
//				group.setActorSize(image.getPrefWidth(), image.getPrefHeight());
//				group.add(image).expand().fill().padTop(-image.getPrefHeight() * .8f).row();
//				group.add(image).expand().fillY().padTop(percentPrefHeight(-.8f, image)).row();
//				group.add(image).expand().fillY().spaceTop(percentPrefHeight(-.8f, image)).row();
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
		if (false) {
			VertGroup group = new VertGroup();
			group.debug();
			group.reverse();
			group.align(Align.bottom);
//			group.setActorCap(8);
			float scale = MathUtils.random(.5f, 2f);
//			group.setActorSize(coin.getRegion().getRegionWidth() * scale, coin.getRegion().getRegionHeight() * scale);

//			VertGroup2 group = new VertGroup2();
//			group.align(Align.bottom);
//			group.setActorCap(5);
//			for (int j = 0, n = 5; j < n; j++) {
			for (int j = 0, n = MathUtils.random(3, 8); j < n; j++) {
				final Img image = new Img(coin);
//				image.setScaling(Scaling.fit);
				image.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), .5f);
//				image.setScale(scale);
//				group.space(-image.getWidth() * .8f);
				group.add(image);
//				group.setActorSize(image.getPrefWidth(), image.getPrefHeight());
//				group.add(image).expand().fill().padTop(-image.getPrefHeight() * .8f).row();
//				group.add(image).expand().fillY().padTop(percentPrefHeight(-.8f, image)).row();
//				group.add(image).expand().fillY().spaceTop(percentPrefHeight(-.8f, image)).row();
//				group.add(image).expand().fill().row();
			}
			group.setSize(200, 400);
			group.setPosition(100, 50);
			group.pack();
			root.addActor(group);
			actor = group;
		}

		if (true) {
			Table table = new Table();
//			for (int j = 0, n = MathUtils.random(3, 8); j < n; j++) {
			for (int j = 0, n = 8; j < n; j++) {
//				final Img2 image = new Img2(coin);
				final Img2 image = new Img2(new TextureRegionDrawable(coin){
					@Override public float getMinHeight () {
						return super.getMinHeight() * .25f;
					}

					@Override public void draw (Batch batch, float x, float y, float width, float height) {
						super.draw(batch, x, y, width, height * 4);
					}
				});
				image.setScaling(Scaling.fillY);
				image.debug();
//				image.setAlign(Align.bottom);
				image.layout();
//				table.add(image).expand().fill().padTop(-10).row();
				Cell<Img2> cell = table.add(image).expand().fill();
				if (j < n-1) {
//					cell.padBottom(new Value() {
//						@Override public float get (Actor context) {
//							return -image.getImageHeight() * .75f;
//						}
//					});
					cell.row();
				}
			}
			SnapshotArray<Actor> children = table.getChildren();
			for (int i = 0; i < children.size; i++) {
				children.get(0).setZIndex(children.size - 1 - i);
			}

			table.setSize(200, 400);
			table.setPosition(100, 250);
			table.pack();
			root.addActor(table);
			actor = table;
		}

		if (false) {
			Table outer = new Table();
			for (int i = 0; i < 4; i++) {

				Table table = new Table();
//			for (int j = 0, n = MathUtils.random(3, 8); j < n; j++) {
				for (int j = 0, n = 8; j < n; j++) {
					final Img2 image = new Img2(coin);
					image.setScaling(Scaling.fillY);
					image.debug();
//				image.setAlign(Align.bottom);
					image.layout();
//				table.add(image).expand().fill().padTop(-10).row();
					Cell<Img2> cell = table.add(image).expand().fill();
					if (j < n - 1) {
						cell.padBottom(new Value() {
							@Override public float get (Actor context) {
								return -image.getImageHeight() * .75f;
							}
						});
						cell.row();
					}
				}
				SnapshotArray<Actor> children = table.getChildren();
				for (int k = 0; k < children.size; k++) {
					children.get(0).setZIndex(children.size - 1 - k);
				}
				outer.add(table).expand().fill();
			}

			outer.setSize(200, 400);
			outer.setPosition(100, 250);
			outer.pack();
			root.addActor(outer);
			actor = outer;
		}
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

	static class VertGroup extends VerticalGroup {
		private int actorCap = 1;
		private float baseActorWidth;
		private float baseActorHeight;
		private float actorWidth;
		private float actorHeight;
		private Array<Img> imgs = new Array<>();

//		@Override public float getPrefHeight () {
//			float ph = getPadBottom() + getPadTop();
//			ph += getSpace() * (actorCap - 1);
//			ph += actorHeight * actorCap;
//			return ph;
//		}

		@Override public void layout () {
			super.layout();
//			Gdx.app.log(TAG, getWidth() + " x " + getHeight());
//			Gdx.app.log(TAG, getPrefWidth() + " x " + getPrefHeight());
//			Vector2 apply = Scaling.fit.apply(getPrefWidth(), getPrefHeight(), getWidth(), getHeight());
//			float scale = actorWidth/apply.x;
//			for (Img img : imgs) {
//				img.setScale(scale);
//			}
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
			baseActorWidth = actorWidth;
			baseActorHeight = actorHeight;
		}

		public void add (Img img) {
			imgs.add(img);
			addActor(img);
		}
	}

	static class Img extends Image {
		private TextureRegionDrawable drawable;
		private float scaleX;
		private float scaleY;

		public Img (TextureRegionDrawable drawable) {
			super(new TextureRegionDrawable(drawable));
			this.drawable = (TextureRegionDrawable)getDrawable();
		}

		@Override public void layout () {
			super.layout();

		}

		@Override public void setBounds (float x, float y, float width, float height) {
			super.setBounds(x, y, width, height);
//			Gdx.app.log(TAG, getWidth() + " x " + getHeight());
//			Gdx.app.log(TAG, getPrefWidth() + " x " + getPrefHeight());
		}

		@Override
		public void setScale(float scaleXY) {
			setScale(scaleXY, scaleXY);
		}

		@Override
		public void setScale(float scaleX, float scaleY) {
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			// instead of relaying on normal scaling, we scale the drawable
			// we do that so layouts work as expected, as they dont take scale into account
			drawable.setMinWidth(drawable.getRegion().getRegionWidth() * scaleX);
			drawable.setMinHeight(drawable.getRegion().getRegionHeight() * scaleY);
			setSize(drawable.getMinWidth(), drawable.getMinHeight());
			setOrigin(Align.center);
			invalidateHierarchy();
		}

		@Override
		public float getScaleX() {
			return scaleX;
		}

		@Override
		public float getScaleY() {
			return scaleY;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			// we need to reset the scale before drawing or it will be applied twice
			float sx = scaleX;
			float sy = scaleY;
			scaleX = scaleY = 1;
			super.draw(batch, parentAlpha);
			scaleX = sx;
			scaleY = sy;
		}

		private Color color = new Color();
		@Override
		protected void drawDebugBounds(ShapeRenderer shapes) {
			// we need to reset the scale before drawing or it will be applied twice
			float sx = scaleX;
			float sy = scaleY;
			scaleX = scaleY = 1;
			Color debugColor = getStage().getDebugColor();
			color.set(debugColor);
			debugColor.set(Color.RED);
			super.drawDebugBounds(shapes);
			debugColor.set(color);
			scaleX = sx;
			scaleY = sy;
		}
	}

	static class Img2 extends Widget {
		private Scaling scaling;
		private int align = Align.center;
		private float imageX, imageY, imageWidth, imageHeight;
		private Drawable drawable;

		/** Creates an image with no region or patch, stretched, and aligned center. */
		public Img2 () {
			this((Drawable)null);
		}

		/** Creates an image stretched, and aligned center.
		 * @param patch May be null. */
		public Img2 (NinePatch patch) {
			this(new NinePatchDrawable(patch), Scaling.stretch, Align.center);
		}

		/** Creates an image stretched, and aligned center.
		 * @param region May be null. */
		public Img2 (TextureRegion region) {
			this(new TextureRegionDrawable(region), Scaling.stretch, Align.center);
		}

		/** Creates an image stretched, and aligned center. */
		public Img2 (Texture texture) {
			this(new TextureRegionDrawable(new TextureRegion(texture)));
		}

		/** Creates an image stretched, and aligned center. */
		public Img2 (Skin skin, String drawableName) {
			this(skin.getDrawable(drawableName), Scaling.stretch, Align.center);
		}

		/** Creates an image stretched, and aligned center.
		 * @param drawable May be null. */
		public Img2 (Drawable drawable) {
			this(drawable, Scaling.stretch, Align.center);
		}

		/** Creates an image aligned center.
		 * @param drawable May be null. */
		public Img2 (Drawable drawable, Scaling scaling) {
			this(drawable, scaling, Align.center);
		}

		/** @param drawable May be null. */
		public Img2 (Drawable drawable, Scaling scaling, int align) {
			setDrawable(drawable);
			this.scaling = scaling;
			this.align = align;
			setSize(getPrefWidth(), getPrefHeight());
		}

		public void layout () {
			if (drawable == null) return;

			float regionWidth = drawable.getMinWidth();
			float regionHeight = drawable.getMinHeight();
			float width = getWidth();
			float height = getHeight();

			Vector2 size = scaling.apply(regionWidth, regionHeight, width, height);
			imageWidth = size.x;
			imageHeight = size.y;

			if ((align & Align.left) != 0)
				imageX = 0;
			else if ((align & Align.right) != 0)
				imageX = (int)(width - imageWidth);
			else
				imageX = (int)(width / 2 - imageWidth / 2);

			if ((align & Align.top) != 0)
				imageY = (int)(height - imageHeight);
			else if ((align & Align.bottom) != 0)
				imageY = 0;
			else
				imageY = (int)(height / 2 - imageHeight / 2);
		}

		public void draw (Batch batch, float parentAlpha) {
			validate();

			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

			float x = getX();
			float y = getY();
			float scaleX = getScaleX();
			float scaleY = getScaleY();

			if (drawable instanceof TransformDrawable) {
				float rotation = getRotation();
				if (scaleX != 1 || scaleY != 1 || rotation != 0) {
					((TransformDrawable)drawable).draw(batch, x + imageX, y + imageY, getOriginX() - imageX, getOriginY() - imageY,
						imageWidth, imageHeight, scaleX, scaleY, rotation);
					return;
				}
			}
			if (drawable != null) drawable.draw(batch, x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
		}

		public void setDrawable (Skin skin, String drawableName) {
			setDrawable(skin.getDrawable(drawableName));
		}

		/** @param drawable May be null. */
		public void setDrawable (Drawable drawable) {
			if (this.drawable == drawable) return;
			if (drawable != null) {
				if (getPrefWidth() != drawable.getMinWidth() || getPrefHeight() != drawable.getMinHeight()) invalidateHierarchy();
			} else
				invalidateHierarchy();
			this.drawable = drawable;
		}

		/** @return May be null. */
		public Drawable getDrawable () {
			return drawable;
		}

		public void setScaling (Scaling scaling) {
			if (scaling == null) throw new IllegalArgumentException("scaling cannot be null.");
			this.scaling = scaling;
			invalidate();
		}

		public void setAlign (int align) {
			this.align = align;
			invalidate();
		}

		public float getMinWidth () {
			return 0;
		}

		public float getMinHeight () {
			return 0;
		}

		public float getPrefWidth () {
			if (drawable != null) return drawable.getMinWidth();
			return 0;
		}

		public float getPrefHeight () {
			if (drawable != null) return drawable.getMinHeight();
			return 0;
		}

		public float getImageX () {
			return imageX;
		}

		public float getImageY () {
			return imageY;
		}

		public float getImageWidth () {
			return imageWidth;
		}

		public float getImageHeight () {
			return imageHeight;
		}
	}


	boolean edit = false;
	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
		renderer.setProjectionMatrix(guiCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		actor.localToStageCoordinates(v2.set(0, 0));
//		float actorX = actor.getX();
		float actorX = v2.x;
//		float actorY = actor.getY();
		float actorY = v2.y;
		float actorWidth = actor.getWidth();
		float actorHeight = actor.getHeight();
//		if (actor instanceof Layout) {
//			actorWidth = ((Layout)actor).getPrefWidth();
//			actorHeight = ((Layout)actor).getPrefHeight();
//		}
		renderer.setColor(Color.MAGENTA);
		renderer.rect(actorX, actorY, actorWidth, actorHeight);
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
//		if (actor instanceof Layout) {
//			actorWidth = ((Layout)actor).getPrefWidth();
//			actorHeight = ((Layout)actor).getPrefHeight();
//		}
		window.add(actor).expand().fill();
		// note this is skin dependant

		window.setSize(actorWidth + lw + rw, actorHeight + bh+ th );
		// note offsets skin dependant
		window.setPosition(actorX - lw, actorY-bh);
		window.setResizeBorder(16);
		root.addActor(window);
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width /= 2;
		config.height /= 2;
		PlaygroundGame.start(args, config, UIEditTest.class);
	}
}

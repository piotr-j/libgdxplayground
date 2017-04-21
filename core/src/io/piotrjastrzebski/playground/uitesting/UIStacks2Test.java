package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIStacks2Test extends BaseScreen {
	protected final static String TAG = UIStacks2Test.class.getSimpleName();

	TextureRegion region;
	public UIStacks2Test (GameReset game) {
		super(game);
		// TODO we want to figure out a way to position stacks of images, say coins or whatever
		// we want multiple columns of stacks of coins
		// we want an ability to scale them, without using transforms.
		// we can scale the images, but padding between things should take that into account

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

		final float group1Scale = .75f;
		final float group2Scale = 1.5f;
		final VerticalGroup group1 = new VerticalGroup();
		group1.reverse();
		group1.setTouchable(Touchable.enabled);
		group1.debug();
//		group1.align(Align.center);
//		group1.columnAlign(Align.topRight);
		root.add(group1).expand().bottom().right().pad(50);
		root.add().width(200);
		for (int i = 0; i < 5; i++) {
			Actor image = createImage(group1Scale, group1Scale, Color.RED);
			group1.addActor(image);
		}

		final VerticalGroup group2 = new VerticalGroup();
		group2.reverse();
		group2.setTouchable(Touchable.enabled);
		group2.debug();
		root.add(group2).expand().bottom().left().pad(50);
		for (int i = 0; i < 5; i++) {
			Actor image = createImage(group2Scale, group2Scale, Color.BLUE);
			group2.addActor(image);
		}

		group1.addListener(new ActorGestureListener(){
			@Override public void tap (InputEvent event, float x, float y, int count, int button) {
				float duration = 1;
				Actor hit = group1.hit(x, y, true);
				if (hit != null) {
					hit.addAction(rotateBy(360, duration));
				}
			}

			@Override public void fling (InputEvent event, float velocityX, float velocityY, int button) {
				SnapshotArray<Actor> children = group1.getChildren();
				if (children.size > 0) {
					moveChild(children.get(children.size -1), group1, group2, Color.BLUE, group2Scale);
				}
			}
		});

		group2.addListener(new ActorGestureListener(){
			@Override public void tap (InputEvent event, float x, float y, int count, int button) {
				float duration = 1;
				Actor hit = group2.hit(x, y, true);
				if (hit != null) {
					hit.addAction(rotateBy(360, duration));
				}
			}

			@Override public void fling (InputEvent event, float velocityX, float velocityY, int button) {
				SnapshotArray<Actor> children = group2.getChildren();
				if (children.size > 0) {
					moveChild(children.get(children.size -1), group2, group1, Color.RED, group1Scale);
				}
			}
		});

	}

	void moveChild (final Actor actor, final VerticalGroup from, final VerticalGroup to, Color targetColor, float scale) {
		from.localToStageCoordinates(v2.set(actor.getX(), actor.getY()));
		stage.addActor(actor);
		actor.setPosition(v2.x, v2.y);
		to.localToStageCoordinates(v2.set(0, to.getHeight()));
//		actor.setOrigin(Align.center);
		float duration = 3;
		actor.addAction(
			sequence(
				parallel(
					moveTo(v2.x, v2.y, duration),
					color(targetColor, duration),
					rotateBy(360, duration),
					scaleTo(scale, scale, duration)
				),
				run(new Runnable() {
					@Override public void run () {
//						actor.setOrigin(Align.bottomLeft);
						to.addActor(actor);
					}
				})
			)
		);
	}

	protected Actor createImage(final float scaleX, float scaleY, Color color) {
		final MyImage image = new MyImage(region);
		image.setScale(scaleX, scaleY );

		image.setDebug(true);
		image.setColor(color);
		image.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {

			}
		});
		return image;
	}

	static class MyImage extends Image {

		public MyImage (TextureRegion region) {
			super(region);
//			setAlign(Align.bottomLeft);
//			setScaling(Scaling.fit);
			setOrigin(Align.center);
//			setOrigin(getPrefWidth()/2, getPrefHeight()/2);

		}

//		@Override public void draw (Batch batch, float parentAlpha) {
//			float sx = getScaleX();
//			float sy = getScaleY();
//			if (getParent() != getStage().getRoot()) {
//				setScale(1);
//			}
//			super.draw(batch, parentAlpha);
//			setScale(sx, sy);
//		}
//
//		@Override protected void drawDebugBounds (ShapeRenderer shapes) {
//			float sx = getScaleX();
//			float sy = getScaleY();
//			if (getParent() != getStage().getRoot()) {
//				setScale(1);
//			}
//			super.drawDebugBounds(shapes);
//			setScale(sx, sy);
//		}
//
//		@Override public float getPrefWidth () {
//			return super.getPrefWidth() * getScaleX();
//		}
//
//		@Override public float getPrefHeight () {
//			return super.getPrefHeight() * getScaleY();
//		}
	}

	static class VerticalGroup extends WidgetGroup {
		private float prefWidth, prefHeight, lastPrefWidth;
		private boolean sizeInvalid = true;
		private FloatArray columnSizes; // column height, column width, ...

		private int align = Align.top, columnAlign;
		private boolean reverse, round = true, wrap, expand;
		private float space, wrapSpace, fill, padTop, padLeft, padBottom, padRight;

		public VerticalGroup () {
			setTouchable(Touchable.childrenOnly);
			setTransform(false);
		}

		public void invalidate () {
			super.invalidate();
			sizeInvalid = true;
		}

		private void computeSize () {
			sizeInvalid = false;
			SnapshotArray<Actor> children = getChildren();
			int n = children.size;
			prefWidth = 0;
			if (wrap) {
				prefHeight = 0;
				if (columnSizes == null)
					columnSizes = new FloatArray();
				else
					columnSizes.clear();
				FloatArray columnSizes = this.columnSizes;
				float space = this.space, wrapSpace = this.wrapSpace;
				float pad = padTop + padBottom, groupHeight = getHeight() - pad, x = 0, y = 0, columnWidth = 0;
				int i = 0, incr = 1;
				if (reverse) {
					i = n - 1;
					n = -1;
					incr = -1;
				}
				for (; i != n; i += incr) {
					Actor child = children.get(i);
					float scaleX = child.getScaleX();
					float scaleY = child.getScaleY();

					float width, height;
					if (child instanceof Layout) {
						Layout layout = (Layout)child;
						width = layout.getPrefWidth();
						height = layout.getPrefHeight();
					} else {
						width = child.getWidth();
						height = child.getHeight();
					}

					width *= scaleX;
					height *= scaleY;

					float incrY = height + (y > 0 ? space : 0);
					if (y + incrY > groupHeight && y > 0) {
						columnSizes.add(y);
						columnSizes.add(columnWidth);
						prefHeight = Math.max(prefHeight, y + pad);
						if (x > 0) x += wrapSpace;
						x += columnWidth;
						columnWidth = 0;
						y = 0;
						incrY = height;
					}
					y += incrY;
					columnWidth = Math.max(columnWidth, width);
				}
				columnSizes.add(y);
				columnSizes.add(columnWidth);
				prefHeight = Math.max(prefHeight, y + pad);
				if (x > 0) x += wrapSpace;
				prefWidth = Math.max(prefWidth, x + columnWidth);
			} else {
				prefHeight = padTop + padBottom + space * (n - 1);
				for (int i = 0; i < n; i++) {
					Actor child = children.get(i);
					float scaleX = child.getScaleX();
					float scaleY = child.getScaleY();
					if (child instanceof Layout) {
						Layout layout = (Layout)child;
						prefWidth = Math.max(prefWidth, layout.getPrefWidth() * scaleX);
						prefHeight += layout.getPrefHeight() * scaleY;
					} else {
						prefWidth = Math.max(prefWidth, child.getWidth() * scaleX);
						prefHeight += child.getHeight() * scaleY;
					}
				}
			}
			prefWidth += padLeft + padRight;
			if (round) {
				prefWidth = Math.round(prefWidth);
				prefHeight = Math.round(prefHeight);
			}
		}

		public void layout () {
			if (sizeInvalid) computeSize();

			if (wrap) {
				layoutWrapped();
				return;
			}

			boolean round = this.round;
			int align = this.align;
			float space = this.space, padLeft = this.padLeft, fill = this.fill;
			float columnWidth = (expand ? getWidth() : prefWidth) - padLeft - padRight, y = prefHeight - padTop + space;

			if ((align & Align.top) != 0)
				y += getHeight() - prefHeight;
			else if ((align & Align.bottom) == 0) // center
				y += (getHeight() - prefHeight) / 2;

			float startX;
			if ((align & Align.left) != 0)
				startX = padLeft;
			else if ((align & Align.right) != 0)
				startX = getWidth() - padRight - columnWidth;
			else
				startX = padLeft + (getWidth() - padLeft - padRight - columnWidth) / 2;

			align = columnAlign;

			SnapshotArray<Actor> children = getChildren();
			int i = 0, n = children.size, incr = 1;
			if (reverse) {
				i = n - 1;
				n = -1;
				incr = -1;
			}
			for (int r = 0; i != n; i += incr) {
				Actor child = children.get(i);
				float scaleX = child.getScaleX();
				float scaleY = child.getScaleY();

				float width, height;
				Layout layout = null;
				if (child instanceof Layout) {
					layout = (Layout)child;
					width = layout.getPrefWidth();
					height = layout.getPrefHeight();
				} else {
					width = child.getWidth();
					height = child.getHeight();
				}

				width *= scaleX;
				height *= scaleY;

				if (fill > 0) width = columnWidth * fill;

				if (layout != null) {
					width = Math.max(width, layout.getMinWidth() * scaleX);
					float maxWidth = layout.getMaxWidth() * scaleY;
					if (maxWidth > 0 && width > maxWidth) width = maxWidth;
				}

				float x = startX;
				if ((align & Align.right) != 0)
					x += columnWidth - width;
				else if ((align & Align.left) == 0) // center
					x += (columnWidth - width) / 2;

				y -= height + space;

				x += -8;
				y += 0;
				// it would be suboptimal if scale was 0
				if (round)
					child.setBounds(Math.round(x), Math.round(y), Math.round(width/scaleX), Math.round(height/scaleY));
				else
					child.setBounds(x, y, width, height);
//				child.setOrigin(Align.bottomLeft);

				if (layout != null) layout.validate();
			}
		}

		private void layoutWrapped () {
			float prefWidth = getPrefWidth();
			if (prefWidth != lastPrefWidth) {
				lastPrefWidth = prefWidth;
				invalidateHierarchy();
			}

			int align = this.align;
			boolean round = this.round;
			float space = this.space, padLeft = this.padLeft, fill = this.fill, wrapSpace = this.wrapSpace;
			float maxHeight = prefHeight - padTop - padBottom;
			float columnX = padLeft, groupHeight = getHeight();
			float yStart = prefHeight - padTop + space, y = 0, columnWidth = 0;

			if ((align & Align.right) != 0)
				columnX += getWidth() - prefWidth;
			else if ((align & Align.left) == 0) // center
				columnX += (getWidth() - prefWidth) / 2;

			if ((align & Align.top) != 0)
				yStart += groupHeight - prefHeight;
			else if ((align & Align.bottom) == 0) // center
				yStart += (groupHeight - prefHeight) / 2;

			groupHeight -= padTop;
			align = columnAlign;

			FloatArray columnSizes = this.columnSizes;
			SnapshotArray<Actor> children = getChildren();
			int i = 0, n = children.size, incr = 1;
			if (reverse) {
				i = n - 1;
				n = -1;
				incr = -1;
			}
			for (int r = 0; i != n; i += incr) {
				Actor child = children.get(i);

				float width, height;
				Layout layout = null;
				if (child instanceof Layout) {
					layout = (Layout)child;
					width = layout.getPrefWidth();
					height = layout.getPrefHeight();
				} else {
					width = child.getWidth();
					height = child.getHeight();
				}

				if (y - height - space < padBottom || r == 0) {
					y = yStart;
					if ((align & Align.bottom) != 0)
						y -= maxHeight - columnSizes.get(r);
					else if ((align & Align.top) == 0) // center
						y -= (maxHeight - columnSizes.get(r)) / 2;
					if (r > 0) {
						columnX += wrapSpace;
						columnX += columnWidth;
					}
					columnWidth = columnSizes.get(r + 1);
					r += 2;
				}

				if (fill > 0) width = columnWidth * fill;

				if (layout != null) {
					width = Math.max(width, layout.getMinWidth());
					float maxWidth = layout.getMaxWidth();
					if (maxWidth > 0 && width > maxWidth) width = maxWidth;
				}

				float x = columnX;
				if ((align & Align.right) != 0)
					x += columnWidth - width;
				else if ((align & Align.left) == 0) // center
					x += (columnWidth - width) / 2;

				y -= height + space;
				if (round)
					child.setBounds(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
				else
					child.setBounds(x, y, width, height);

				if (layout != null) layout.validate();
			}
		}

		public float getPrefWidth () {
			if (sizeInvalid) computeSize();
			return prefWidth;
		}

		public float getPrefHeight () {
			if (wrap) return 0;
			if (sizeInvalid) computeSize();
			return prefHeight;
		}

		/** If true (the default), positions and sizes are rounded to integers. */
		public void setRound (boolean round) {
			this.round = round;
		}

		/** The children will be displayed last to first. */
		public VerticalGroup reverse () {
			this.reverse = true;
			return this;
		}

		/** If true, the children will be displayed last to first. */
		public VerticalGroup reverse (boolean reverse) {
			this.reverse = reverse;
			return this;
		}

		public boolean getReverse () {
			return reverse;
		}

		/** Sets the vertical space between children. */
		public VerticalGroup space (float space) {
			this.space = space;
			return this;
		}

		public float getSpace () {
			return space;
		}

		/** Sets the horizontal space between columns when wrap is enabled. */
		public VerticalGroup wrapSpace (float wrapSpace) {
			this.wrapSpace = wrapSpace;
			return this;
		}

		public float getWrapSpace () {
			return wrapSpace;
		}

		/** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
		public VerticalGroup pad (float pad) {
			padTop = pad;
			padLeft = pad;
			padBottom = pad;
			padRight = pad;
			return this;
		}

		public VerticalGroup pad (float top, float left, float bottom, float right) {
			padTop = top;
			padLeft = left;
			padBottom = bottom;
			padRight = right;
			return this;
		}

		public VerticalGroup padTop (float padTop) {
			this.padTop = padTop;
			return this;
		}

		public VerticalGroup padLeft (float padLeft) {
			this.padLeft = padLeft;
			return this;
		}

		public VerticalGroup padBottom (float padBottom) {
			this.padBottom = padBottom;
			return this;
		}

		public VerticalGroup padRight (float padRight) {
			this.padRight = padRight;
			return this;
		}

		public float getPadTop () {
			return padTop;
		}

		public float getPadLeft () {
			return padLeft;
		}

		public float getPadBottom () {
			return padBottom;
		}

		public float getPadRight () {
			return padRight;
		}

		/** Sets the alignment of all widgets within the vertical group. Set to {@link Align#center}, {@link Align#top},
		 * {@link Align#bottom}, {@link Align#left}, {@link Align#right}, or any combination of those. */
		public VerticalGroup align (int align) {
			this.align = align;
			return this;
		}

		/** Sets the alignment of all widgets within the vertical group to {@link Align#center}. This clears any other alignment. */
		public VerticalGroup center () {
			align = Align.center;
			return this;
		}

		/** Sets {@link Align#top} and clears {@link Align#bottom} for the alignment of all widgets within the vertical group. */
		public VerticalGroup top () {
			align |= Align.top;
			align &= ~Align.bottom;
			return this;
		}

		/** Adds {@link Align#left} and clears {@link Align#right} for the alignment of all widgets within the vertical group. */
		public VerticalGroup left () {
			align |= Align.left;
			align &= ~Align.right;
			return this;
		}

		/** Sets {@link Align#bottom} and clears {@link Align#top} for the alignment of all widgets within the vertical group. */
		public VerticalGroup bottom () {
			align |= Align.bottom;
			align &= ~Align.top;
			return this;
		}

		/** Adds {@link Align#right} and clears {@link Align#left} for the alignment of all widgets within the vertical group. */
		public VerticalGroup right () {
			align |= Align.right;
			align &= ~Align.left;
			return this;
		}

		public int getAlign () {
			return align;
		}

		public VerticalGroup fill () {
			fill = 1f;
			return this;
		}

		/** @param fill 0 will use preferred height. */
		public VerticalGroup fill (float fill) {
			this.fill = fill;
			return this;
		}

		public float getFill () {
			return fill;
		}

		public VerticalGroup expand () {
			expand = true;
			return this;
		}

		/** When true and wrap is false, the columns will take up the entire vertical group width. */
		public VerticalGroup expand (boolean expand) {
			this.expand = expand;
			return this;
		}

		public boolean getExpand () {
			return expand;
		}

		/** Sets fill to 1 and expand to true. */
		public VerticalGroup grow () {
			expand = true;
			fill = 1;
			return this;
		}

		/** If false, the widgets are arranged in a single column and the preferred height is the widget heights plus spacing. If true,
		 * the widgets will wrap using the height of the vertical group. The preferred height of the group will be 0 as it is expected
		 * that something external will set the height of the group. Default is false.
		 * <p>
		 * When wrap is enabled, the group's preferred width depends on the height of the group. In some cases the parent of the group
		 * will need to layout twice: once to set the height of the group and a second time to adjust to the group's new preferred
		 * width. */
		public VerticalGroup wrap () {
			wrap = true;
			return this;
		}

		public VerticalGroup wrap (boolean wrap) {
			this.wrap = wrap;
			return this;
		}

		public boolean getWrap () {
			return wrap;
		}

		/** Sets the alignment of widgets within each column of the vertical group. Set to {@link Align#center}, {@link Align#left}, or
		 * {@link Align#right}. */
		public VerticalGroup columnAlign (int columnAlign) {
			this.columnAlign = columnAlign;
			return this;
		}

		/** Sets the alignment of widgets within each column to {@link Align#center}. This clears any other alignment. */
		public VerticalGroup columnCenter () {
			columnAlign = Align.center;
			return this;
		}

		/** Adds {@link Align#left} and clears {@link Align#right} for the alignment of widgets within each column. */
		public VerticalGroup columnLeft () {
			columnAlign |= Align.left;
			columnAlign &= ~Align.right;
			return this;
		}

		/** Adds {@link Align#right} and clears {@link Align#left} for the alignment of widgets within each column. */
		public VerticalGroup columnRight () {
			columnAlign |= Align.right;
			columnAlign &= ~Align.left;
			return this;
		}

		protected void drawDebugBounds (ShapeRenderer shapes) {
			super.drawDebugBounds(shapes);
			if (!getDebug()) return;
			shapes.set(ShapeRenderer.ShapeType.Line);
			shapes.setColor(getStage().getDebugColor());
			shapes.rect(getX() + padLeft, getY() + padBottom, getOriginX(), getOriginY(), getWidth() - padLeft - padRight,
				getHeight() - padBottom - padTop, getScaleX(), getScaleY(), getRotation());

			shapes.setColor(Color.CYAN);

			for (Actor actor : getChildren()) {
				shapes.circle(getX() + actor.getX(), getY() + actor.getY(), 6, 16);
			}
		}
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

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIStacks2Test.class);
	}

}

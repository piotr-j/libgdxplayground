package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIStacksTest extends BaseScreen {
	protected final static String TAG = UIStacksTest.class.getSimpleName();

	TextureRegion region;
	public UIStacksTest (GameReset game) {
		super(game);
		// TODO we want to figure out a way to position stacks of images, say coins or whatever
		// we want multiple columns of stacks of coins
		// we want an ability to scale them, without using transforms.
		// we can scale the images, but padding between things should take that into account

		clear.set(Color.CYAN);

		// simple coin
		Pixmap pixmap = new Pixmap(128, 140, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.LIGHT_GRAY);
		pixmap.fillCircle(64, 64, 60);
		pixmap.setColor(Color.WHITE);
		pixmap.fillCircle(64, 74, 60);
		pixmap.setColor(Color.LIGHT_GRAY);
		pixmap.fillCircle(64, 74, 40);
		pixmap.setColor(Color.WHITE);
		pixmap.fillCircle(64, 70, 36);
		region = new TextureRegion(new Texture(pixmap));
		region.flip(false, true);
		pixmap.dispose();

		rebuild();
	}

	protected void rebuild() {
		root.clear();

		{
			Table stacks = buildStack(6, 0, 1);
			root.add(stacks).height(300).row();
		}
		{
			Table stacks = buildStack(4, 4, 1);
			root.add(stacks).height(200).pad(25).row();
		}
		{
			Table stacks = buildStack(3, 6, 1.25f);
			root.add(stacks).height(400).row();
		}
	}

	protected Color ghost = new Color(1, 1, 1, .33f);
	protected Array<Actor> ghosts = new Array<>();
	protected Actor lastActor;
	protected Color lastColor = new Color();
	private Table buildStack(final int stackCount, final int coinCount, final float scale) {
		final Color[] colors = {Color.RED, Color.MAGENTA, Color.BLUE, Color.BROWN, Color.WHITE};
		Table stacks = new Table();
		stacks.defaults().pad(8 * scale);
		final float scaleX = .75f * scale;
		final float scaleY = .5f * scale;
		final Array<MyVerticalGroup> groups = new Array<>();
		final Vector2 v2 = new Vector2();
		for (int i = 0; i < stackCount; i++) {
			final int stackId = i;
			final Color color = colors[stackId % colors.length];
			final MyVerticalGroup group = new MyVerticalGroup();
			// not needed
			group.setTransform(false);
			group.debugAll();
			stacks.add(group).expandY().bottom();
			groups.add(group);
			// overlap
			group.space(-region.getRegionHeight() * scaleY * .85f);
			// draw from bottom to top
			group.reverse();
			group.setTouchable(Touchable.enabled);
			group.addListener(new ActorGestureListener(){
				Vector2 v2 = new Vector2();
				@Override public void fling (InputEvent event, float velocityX, float velocityY, int button) {
					if (!group.hasChildren()) return;
					int sourceId = groups.indexOf(group, true);
					int targetId = -1;
					boolean merge = false;
					v2.set(velocityX, velocityY);
					if (velocityX > 0) {
						if (sourceId < groups.size -1) {
							targetId = sourceId + 1;
							merge = true;
						}
					} else {
						if (sourceId > 0) {
							targetId = sourceId -1;
							merge = false;
						}
					}
					float duration = 2.25f;
					if (targetId >= 0 && targetId < groups.size) {
						final SnapshotArray<Actor> sourceChildren = group.getChildren();
						final Color targetColor = colors[targetId % colors.length];
						final MyVerticalGroup targetGroup = groups.get(targetId);
						final SnapshotArray<Actor> targetChildren = targetGroup.getChildren();
						if (merge) {
							// merge 2
							if (sourceChildren.size >= 2) {
								// second first so the draw order is correct
								final Actor sourceChild = sourceChildren.get(sourceChildren.size - 2);
								final Actor sourceChild2 = sourceChildren.get(sourceChildren.size - 1);
								group.localToStageCoordinates(v2.set(sourceChild.getX(), sourceChild.getY()));
								stage.addActor(sourceChild);
								sourceChild.setPosition(v2.x, v2.y);
								group.localToStageCoordinates(v2.set(sourceChild2.getX(), sourceChild2.getY()));
								stage.addActor(sourceChild2);
								sourceChild2.setPosition(v2.x, v2.y);
								Actor targetChild = targetChildren.get(targetChildren.size - 1);
								if (ghosts.contains(targetChild, true)) {
									targetGroup.localToStageCoordinates(
										v2.set(targetChild.getX(), targetChild.getY()));
								} else {
									targetGroup.localToStageCoordinates(
										v2.set(targetChild.getX(), targetChild.getY() + targetChild.getHeight() + targetGroup.getSpace()));
								}
								final float targetX = v2.x;
								final float targetY = v2.y;

								if (group.getChildren().size == 0) {
									Actor ghostImage = createImage(scaleX, scaleY, ghost);
									group.addActor(ghostImage);
									ghosts.add(ghostImage);
								}

								sourceChild.addAction(
									sequence(
										parallel(
											moveTo(targetX, targetY, duration),
											color(targetColor, duration),
											sequence(
												scaleBy(.25f, .25f, duration/2),
												scaleBy(-.25f, -.25f, duration/2)
											)
										),
										run(new Runnable() {
											@Override public void run () {
												if (targetChildren.size == 1) {
													Actor actor = targetChildren.get(0);
													if (ghosts.removeValue(actor, true)) {
														targetGroup.removeActor(actor);
													}
												}
												targetGroup.addActor(sourceChild);
											}
										})));
								sourceChild2.addAction(sequence(
									parallel(
										moveTo(targetX, targetY, duration),
										color(targetColor, duration),
										sequence(
											scaleBy(.25f, .25f, duration/2),
											scaleBy(-.25f, -.25f, duration/2)
										)
									), removeActor()));
							}
						} else {
							final Actor sourceChild = sourceChildren.get(sourceChildren.size - 1);
							// cant move ghosts
							if (ghosts.contains(sourceChild, true)) return;
							final Actor clonedChild = createImage(scaleX, scaleY, sourceChild.getColor());
							group.localToStageCoordinates(v2.set(sourceChild.getX(), sourceChild.getY()));
							stage.addActor(sourceChild);
							sourceChild.setPosition(v2.x, v2.y);
							stage.addActor(clonedChild);
							clonedChild.setPosition(v2.x, v2.y);
							Actor targetChild = targetChildren.get(targetChildren.size - 1);
							boolean targetIsGhost = ghosts.contains(targetChild, true);
							if (targetIsGhost) {
								targetGroup.localToStageCoordinates(
									v2.set(targetChild.getX(), targetChild.getY()));
							} else {
								targetGroup.localToStageCoordinates(
									v2.set(targetChild.getX(), targetChild.getY() + targetChild.getHeight() + targetGroup.getSpace()));
							}
							final float targetX = v2.x;
							final float targetY = v2.y;
							if (targetIsGhost) {
								targetGroup.localToStageCoordinates(
									v2.set(targetChild.getX(), targetChild.getY() + targetChild.getHeight() + targetGroup.getSpace()));
							} else {
								targetGroup.localToStageCoordinates(
									v2.set(targetChild.getX(), targetChild.getY() + (targetChild.getHeight() + targetGroup.getSpace()) * 2));
							}
							final float cloneTargetX = v2.x;
							final float cloneTargetY = v2.y;

							if (group.getChildren().size == 0) {
								Actor ghostImage = createImage(scaleX, scaleY, ghost);
								group.addActor(ghostImage);
								ghosts.add(ghostImage);
							}

							sourceChild.addAction(
								sequence(
									parallel(
										moveTo(targetX, targetY, duration),
										color(targetColor, duration),
										sequence(
											scaleBy(.25f, .25f, duration/2),
											scaleBy(-.25f, -.25f, duration/2)
										)
									),
									run(new Runnable() {
										@Override public void run () {
											if (targetChildren.size == 1) {
												Actor actor = targetChildren.get(0);
												if (ghosts.removeValue(actor, true)) {
													targetGroup.removeActor(actor);
												}
											}
											targetGroup.addActor(sourceChild);
										}
									})));
							clonedChild.addAction(
								sequence(
									parallel(
										moveTo(cloneTargetX, cloneTargetY, duration),
										color(targetColor, duration),
										sequence(
											scaleBy(.25f, .25f, duration/2),
											scaleBy(-.25f, -.25f, duration/2)
										)
									),
									run(new Runnable() {
										@Override public void run () {
											if (targetChildren.size == 1) {
												Actor actor = targetChildren.get(0);
												if (ghosts.removeValue(actor, true)) {
													targetGroup.removeActor(actor);
												}
											}
											targetGroup.addActor(clonedChild);
										}
									})));
						}
					}
				}
			});
			if (coinCount == 0) {
				Actor image = createImage(scaleX, scaleY, ghost);
				ghosts.add(image);
				group.addActor(image);
			} else {
				for (int j = 0; j < coinCount; j++) {
					group.addActor(createImage(scaleX, scaleY, color));
				}
			}
		}
		return stacks;
	}

	protected Actor createImage(final float scaleX, float scaleY, Color color) {
//		final Table table = new Table() {
//			@Override public void draw (Batch batch, float parentAlpha) {
//				for (Actor actor : getChildren()) {
//					actor.setColor(getColor());
//				}
//				super.draw(batch, parentAlpha);
//			}
//		};
//		TextureRegionDrawable drawable = new TextureRegionDrawable(region);
//		float offsetX = width * MathUtils.random(0, .1f);
//		float offsetY = width * MathUtils.random(0, .1f);
//		drawable.setMinWidth(width);
//		drawable.setMinHeight(height);
		final Image image = new Image(region) {
			// we need to take scale into account so layout works as expected
			@Override public float getPrefWidth () {
				return super.getPrefWidth() * getScaleX();
			}
			@Override public float getPrefHeight () {
				return super.getPrefHeight() * getScaleY();
			}
		};
		// all 3 are important to achieve the result we want
		image.setScale(scaleX, scaleY);
		image.setAlign(Align.bottomLeft);
		image.setScaling(Scaling.none);

		image.setColor(color);
		image.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				if (lastActor != null) {
					lastActor.setColor(lastColor);
				}
//				lastActor = table;
//				lastColor.set(table.getColor());
//				table.setColor(Color.CYAN);
				lastActor = image;
				lastColor.set(image.getColor());
				image.setColor(Color.CYAN);
			}
		});
//		table.add(image).padLeft(offsetX).padBottom(offsetY);
//		table.add(image);
//		table.setColor(color);
//		return table;
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

	/** A group that lays out its children top to bottom vertically, with optional wrapping. This can be easier than using
	 * {@link Table} when actors need to be inserted into or removed from the middle of the group.
	 * <p>
	 * The preferred width is the largest preferred width of any child. The preferred height is the sum of the children's preferred
	 * heights plus spacing. The preferred size is slightly different when {@link #wrap() wrap} is enabled. The min size is the
	 * preferred size and the max size is 0.
	 * <p>
	 * Widgets are sized using their {@link Layout#getPrefWidth() preferred height}, so widgets which return 0 as their preferred
	 * height will be given a height of 0.
	 * @author Nathan Sweet */
	static class MyVerticalGroup extends WidgetGroup {
		private float prefWidth, prefHeight, lastPrefWidth;
		private boolean sizeInvalid = true;
		private FloatArray columnSizes; // column height, column width, ...

		private int align = Align.top, columnAlign;
		private boolean reverse, round = true, wrap, expand;
		private float space, wrapSpace, fill, padTop, padLeft, padBottom, padRight;

		public MyVerticalGroup () {
			setTouchable(Touchable.childrenOnly);
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

					float width, height;
					if (child instanceof Layout) {
						Layout layout = (Layout)child;
						width = layout.getPrefWidth();
						height = layout.getPrefHeight();
					} else {
						width = child.getWidth();
						height = child.getHeight();
					}

//					width *= child.getScaleX();
//					height *= child.getScaleY();

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
					if (child instanceof Layout) {
						Layout layout = (Layout)child;
//						prefWidth = Math.max(prefWidth, layout.getPrefWidth() * child.getScaleX());
//						prefHeight += layout.getPrefHeight() * child.getScaleY();
						prefWidth = Math.max(prefWidth, layout.getPrefWidth());
						prefHeight += layout.getPrefHeight();
					} else {
//						prefWidth = Math.max(prefWidth, child.getWidth() * child.getScaleX());
//						prefHeight += child.getHeight() * child.getScaleY();
						prefWidth = Math.max(prefWidth, child.getWidth());
						prefHeight += child.getHeight();
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

//				width *= child.getScaleX();
//				height *= child.getScaleY();

				if (fill > 0) width = columnWidth * fill;

				if (layout != null) {
//					width = Math.max(width, layout.getMinWidth() * child.getScaleX());
//					float maxWidth = layout.getMaxWidth() * child.getScaleX();
					width = Math.max(width, layout.getMinWidth());
					float maxWidth = layout.getMaxWidth();
					if (maxWidth > 0 && width > maxWidth) width = maxWidth;
				}

				float x = startX;
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

//				width *= child.getScaleX();
//				height *= child.getScaleY();

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
//					width = Math.max(width, layout.getMinWidth() * child.getScaleX());
//					float maxWidth = layout.getMaxWidth() * child.getScaleX();
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
		public MyVerticalGroup reverse () {
			this.reverse = true;
			return this;
		}

		/** If true, the children will be displayed last to first. */
		public MyVerticalGroup reverse (boolean reverse) {
			this.reverse = reverse;
			return this;
		}

		public boolean getReverse () {
			return reverse;
		}

		/** Sets the vertical space between children. */
		public MyVerticalGroup space (float space) {
			this.space = space;
			return this;
		}

		public float getSpace () {
			return space;
		}

		/** Sets the horizontal space between columns when wrap is enabled. */
		public MyVerticalGroup wrapSpace (float wrapSpace) {
			this.wrapSpace = wrapSpace;
			return this;
		}

		public float getWrapSpace () {
			return wrapSpace;
		}

		/** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
		public MyVerticalGroup pad (float pad) {
			padTop = pad;
			padLeft = pad;
			padBottom = pad;
			padRight = pad;
			return this;
		}

		public MyVerticalGroup pad (float top, float left, float bottom, float right) {
			padTop = top;
			padLeft = left;
			padBottom = bottom;
			padRight = right;
			return this;
		}

		public MyVerticalGroup padTop (float padTop) {
			this.padTop = padTop;
			return this;
		}

		public MyVerticalGroup padLeft (float padLeft) {
			this.padLeft = padLeft;
			return this;
		}

		public MyVerticalGroup padBottom (float padBottom) {
			this.padBottom = padBottom;
			return this;
		}

		public MyVerticalGroup padRight (float padRight) {
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
		public MyVerticalGroup align (int align) {
			this.align = align;
			return this;
		}

		/** Sets the alignment of all widgets within the vertical group to {@link Align#center}. This clears any other alignment. */
		public MyVerticalGroup center () {
			align = Align.center;
			return this;
		}

		/** Sets {@link Align#top} and clears {@link Align#bottom} for the alignment of all widgets within the vertical group. */
		public MyVerticalGroup top () {
			align |= Align.top;
			align &= ~Align.bottom;
			return this;
		}

		/** Adds {@link Align#left} and clears {@link Align#right} for the alignment of all widgets within the vertical group. */
		public MyVerticalGroup left () {
			align |= Align.left;
			align &= ~Align.right;
			return this;
		}

		/** Sets {@link Align#bottom} and clears {@link Align#top} for the alignment of all widgets within the vertical group. */
		public MyVerticalGroup bottom () {
			align |= Align.bottom;
			align &= ~Align.top;
			return this;
		}

		/** Adds {@link Align#right} and clears {@link Align#left} for the alignment of all widgets within the vertical group. */
		public MyVerticalGroup right () {
			align |= Align.right;
			align &= ~Align.left;
			return this;
		}

		public int getAlign () {
			return align;
		}

		public MyVerticalGroup fill () {
			fill = 1f;
			return this;
		}

		/** @param fill 0 will use preferred height. */
		public MyVerticalGroup fill (float fill) {
			this.fill = fill;
			return this;
		}

		public float getFill () {
			return fill;
		}

		public MyVerticalGroup expand () {
			expand = true;
			return this;
		}

		/** When true and wrap is false, the columns will take up the entire vertical group width. */
		public MyVerticalGroup expand (boolean expand) {
			this.expand = expand;
			return this;
		}

		public boolean getExpand () {
			return expand;
		}

		/** Sets fill to 1 and expand to true. */
		public MyVerticalGroup grow () {
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
		public MyVerticalGroup wrap () {
			wrap = true;
			return this;
		}

		public MyVerticalGroup wrap (boolean wrap) {
			this.wrap = wrap;
			return this;
		}

		public boolean getWrap () {
			return wrap;
		}

		/** Sets the alignment of widgets within each column of the vertical group. Set to {@link Align#center}, {@link Align#left}, or
		 * {@link Align#right}. */
		public MyVerticalGroup columnAlign (int columnAlign) {
			this.columnAlign = columnAlign;
			return this;
		}

		/** Sets the alignment of widgets within each column to {@link Align#center}. This clears any other alignment. */
		public MyVerticalGroup columnCenter () {
			columnAlign = Align.center;
			return this;
		}

		/** Adds {@link Align#left} and clears {@link Align#right} for the alignment of widgets within each column. */
		public MyVerticalGroup columnLeft () {
			columnAlign |= Align.left;
			columnAlign &= ~Align.right;
			return this;
		}

		/** Adds {@link Align#right} and clears {@link Align#left} for the alignment of widgets within each column. */
		public MyVerticalGroup columnRight () {
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
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIStacksTest.class);
	}

}

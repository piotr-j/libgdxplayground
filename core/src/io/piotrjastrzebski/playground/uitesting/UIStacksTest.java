package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
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

		Table stacks = buildStack(6, 0, 1);
		root.add(stacks).height(300).row();
		stacks = buildStack(6, 10, .5f);
		root.add(stacks).height(200).row();
		stacks = buildStack(3, 6, 1.5f);
		root.add(stacks).height(400).row();
	}

	protected Color ghost = new Color(1, 1, 1, .33f);
	protected Array<Actor> ghosts = new Array<>();
	protected Actor lastActor;
	protected Color lastColor = new Color();
	private Table buildStack(final int stackCount, final int coinCount, final float scale) {
		final Color[] colors = {Color.RED, Color.MAGENTA, Color.BLUE, Color.BROWN, Color.WHITE};
		Table stacks = new Table();
		stacks.defaults().pad(8 * scale);
		final float imageWidth = region.getRegionWidth() * 1.f * scale;
		final float imageHeight = region.getRegionHeight() * .8f * scale;
		final Array<VerticalGroup> groups = new Array<>();
		final Vector2 v2 = new Vector2();
		for (int i = 0; i < stackCount; i++) {
			final int stackId = i;
			final Color color = colors[stackId % colors.length];
			final VerticalGroup group = new VerticalGroup();
			stacks.add(group).expandY().bottom();
			groups.add(group);
			// overlap
			group.space(-imageHeight * .85f);
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
						final VerticalGroup targetGroup = groups.get(targetId);
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
									Actor ghostImage = createImage(imageWidth, imageHeight, ghost);
									group.addActor(ghostImage);
									ghosts.add(ghostImage);
								}

								sourceChild.addAction(
									sequence(parallel(moveTo(targetX, targetY, duration), color(targetColor, duration)),
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
								sourceChild2.addAction(sequence(parallel(
									moveTo(targetX, targetY, duration), color(targetColor, duration)), removeActor()));
							}
						} else {
							final Actor sourceChild = sourceChildren.get(sourceChildren.size - 1);
							// cant move ghosts
							if (ghosts.contains(sourceChild, true)) return;
							final Actor clonedChild = createImage(imageWidth, imageHeight, sourceChild.getColor());
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
								Actor ghostImage = createImage(imageWidth, imageHeight, ghost);
								group.addActor(ghostImage);
								ghosts.add(ghostImage);
							}

							sourceChild.addAction(
								sequence(parallel(moveTo(targetX, targetY, duration), color(targetColor, duration)),
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
							clonedChild.addAction(sequence(parallel(
									moveTo(cloneTargetX, cloneTargetY, duration), color(targetColor, duration)),
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
				Actor image = createImage(imageWidth, imageHeight, ghost);
				ghosts.add(image);
				group.addActor(image);
			} else {
				for (int j = 0; j < coinCount; j++) {
					group.addActor(createImage(imageWidth, imageHeight, color));
				}
			}
		}
		return stacks;
	}

	protected Actor createImage(float width, float height, Color color) {
//		final Table table = new Table() {
//			@Override public void draw (Batch batch, float parentAlpha) {
//				for (Actor actor : getChildren()) {
//					actor.setColor(getColor());
//				}
//				super.draw(batch, parentAlpha);
//			}
//		};
		TextureRegionDrawable drawable = new TextureRegionDrawable(region);
//		float offsetX = width * MathUtils.random(0, .1f);
//		float offsetY = width * MathUtils.random(0, .1f);
		drawable.setMinWidth(width);
		drawable.setMinHeight(height);
		final Image image = new Image(drawable);
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
		image.setColor(color);
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

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIStacksTest.class);
	}

}

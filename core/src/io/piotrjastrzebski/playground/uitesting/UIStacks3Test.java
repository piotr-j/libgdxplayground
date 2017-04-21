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
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIStacks3Test extends BaseScreen {
	protected final static String TAG = UIStacks3Test.class.getSimpleName();

	TextureRegion region;
	public UIStacks3Test (GameReset game) {
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
		group1.space(-region.getRegionHeight() * group1Scale * .8f);
		group1.setTouchable(Touchable.enabled);
		root.add(group1).expand().bottom().right().pad(50);
		root.add().width(200);
		for (int i = 0; i < 5; i++) {
			Image image = createImage(group1Scale, group1Scale, Color.RED);
			group1.addActor(image);
		}

		final VerticalGroup group2 = new VerticalGroup();
		group2.reverse();
		group2.space(-region.getRegionHeight() * group2Scale * .8f);
		group2.setTouchable(Touchable.enabled);
		root.add(group2).expand().bottom().left().pad(50);
		for (int i = 0; i < 5; i++) {
			Image image = createImage(group2Scale, group2Scale, Color.BLUE);
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
					moveChild((Image)children.get(children.size -1), group1, group2, Color.BLUE, group2Scale);
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
					moveChild((Image)children.get(children.size -1), group2, group1, Color.RED, group1Scale);
				}
			}
		});
	}

	void moveChild (final Image actor, final VerticalGroup from, final VerticalGroup to, Color targetColor, float scale) {
		from.localToStageCoordinates(v2.set(actor.getX(), actor.getY()));
		stage.addActor(actor);
		actor.setPosition(v2.x, v2.y);

		to.localToStageCoordinates(v2.set(0, to.getHeight() -region.getRegionHeight() * scale * .8f));
		float duration = 3;
		actor.addAction(
			sequence(
				parallel(
					moveTo(v2.x, v2.y, duration),
					color(targetColor, duration),
					rotateBy(360, duration),
					new ImageScaleToAction().scaleXY(scale).duration(duration)
				),
				run(new Runnable() {
					@Override public void run () {
						to.addActor(actor);
					}
				})
			)
		);
	}

	protected Image createImage(final float scaleX, float scaleY, Color color) {
		TextureRegionDrawable drawable = new TextureRegionDrawable(region);
		drawable.setMinWidth(region.getRegionWidth() * scaleX);
		drawable.setMinHeight(region.getRegionHeight() * scaleY);
		final Image image = new Image(drawable);
		image.setColor(color);
		image.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
			}
		});
		return image;
	}

	public class ImageScaleToAction extends TemporalAction {
		private float startWidth, startHeight;
		private float endWidth, endHeight;
		private float sizeScaleX;
		private float sizeScaleY;
		private boolean imageTarget;
		private TextureRegionDrawable drawable;

		protected void begin () {
			imageTarget = (target instanceof Image);
			if (imageTarget) {
				Image image = (Image)target;
				drawable = (TextureRegionDrawable)image.getDrawable();
				startWidth = drawable.getMinWidth();
				startHeight = drawable.getMinHeight();
				endWidth = drawable.getRegion().getRegionWidth() * sizeScaleX;
				endHeight = drawable.getRegion().getRegionHeight() * sizeScaleY;
			} else {
				startWidth = target.getWidth();
				startHeight = target.getHeight();
			}
		}

		protected void update (float percent) {
			if (imageTarget) {
				drawable.setMinWidth(startWidth + (endWidth - startWidth) * percent);
				drawable.setMinHeight(startHeight + (endHeight - startHeight) * percent);
				target.setSize(drawable.getMinWidth(), drawable.getMinHeight());
				target.setOrigin(Align.center);
			} else {
				target.setSize(startWidth + (endWidth - startWidth) * percent, startHeight + (endHeight - startHeight) * percent);
			}
		}

		public ImageScaleToAction scaleX (float sizeScaleX) {
			this.sizeScaleX = sizeScaleX;
			return this;
		}

		public ImageScaleToAction scaleY (float sizeScaleY) {
			this.sizeScaleY = sizeScaleY;
			return this;
		}

		public ImageScaleToAction scaleXY (float sizeScaleXY) {
			this.sizeScaleX = sizeScaleXY;
			this.sizeScaleY = sizeScaleXY;
			return this;
		}

		public ImageScaleToAction duration (float duration) {
			setDuration(duration);
			return this;
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
		PlaygroundGame.start(args, UIStacks3Test.class);
	}

}

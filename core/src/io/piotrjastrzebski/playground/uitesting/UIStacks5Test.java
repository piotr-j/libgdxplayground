package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
public class UIStacks5Test extends BaseScreen {
	protected final static String TAG = UIStacks5Test.class.getSimpleName();

	TextureRegion region;
	public UIStacks5Test (GameReset game) {
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
		final float group2Scale = .75f;
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
		group2.align(Align.bottom);
//		root.add(group2).expandX().fillY().left().height(Value.percentHeight(.25f, root)).pad(50);
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
		root.row();
		root.debugAll();
	}

	void moveChild (final Image actor, final VerticalGroup from, final VerticalGroup to, Color targetColor, float scale) {
		from.localToStageCoordinates(v2.set(actor.getX(), actor.getY()));
		stage.addActor(actor);
		float sx = v2.x;
		float sy = v2.y;
		actor.setPosition(sx, sy);

		to.localToStageCoordinates(v2.set(0, to.getHeight() -region.getRegionHeight() * scale * .8f));
		float duration = 3;
		Bezier<Vector2> path = new Bezier<>();
		path.set(v2s(
			sx, sy,
			(sx + v2.x)/2, (sy + v2.y)/2 + 250,
			v2.x, v2.y));
		this.path = path;
		actor.addAction(
			sequence(
				parallel(
					new MoveToPath(path, duration),
					color(targetColor, duration),
					rotateBy(360, duration),
					new ImageScaleToAction().scaleXY(scale).duration(duration)),
				run(new Runnable() {
					@Override public void run () {
						to.addActor(actor);
					}
				})
			)
		);
	}

	private Vector2[] v2s (float x1, float y1, float x2, float y2) {
		return new Vector2[]{
			new Vector2(x1, y1),
			new Vector2(x2, y2)
		};
	}

	private Vector2[] v2s (float x1, float y1, float x2, float y2, float x3, float y3) {
		return new Vector2[]{
			new Vector2(x1, y1),
			new Vector2(x2, y2),
			new Vector2(x3, y3)
		};
	}

	private Vector2[] v2s (float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		return new Vector2[]{
			new Vector2(x1, y1),
			new Vector2(x2, y2),
			new Vector2(x3, y3),
			new Vector2(x4, y4)
		};
	}

	protected Image createImage(final float scaleX, float scaleY, Color color) {
		TextureRegionDrawable drawable = new TextureRegionDrawable(region);
		drawable.setMinWidth(region.getRegionWidth() * scaleX);
		drawable.setMinHeight(region.getRegionHeight() * scaleY);
		final Image image = new Image(drawable);
		image.setOrigin(Align.center);
		image.setColor(color);
		image.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
			}
		});
		return image;
	}

	static class MoveToPath extends TemporalAction {
		private final Path<Vector2> path;

		public MoveToPath (Path<Vector2> path, float duration) {
			this(path, duration, null);
		}

		public MoveToPath (Path<Vector2> path, float duration, Interpolation interpolation) {
			super(duration, interpolation);
			if (path == null) throw new AssertionError("path cannot be null");
			this.path = path;
		}
		private Vector2 out = new Vector2();
		@Override protected void update (float percent) {
			path.valueAt(out, percent);
			target.setPosition(out.x, out.y);
		}
	}

	static class ImageScaleToAction extends TemporalAction {
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

	int SAMPLE_POINTS = 100;
	float SAMPLE_POINT_DISTANCE = 1f / SAMPLE_POINTS;
	final Vector2 tmpV = new Vector2();
	final Vector2 tmpV2 = new Vector2();
	private Path<Vector2> path;
	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();

		if (path != null) {
			renderer.setProjectionMatrix(guiCamera.combined);
			renderer.begin(ShapeRenderer.ShapeType.Line);
			if (path instanceof Bezier) {
				renderer.setColor(0f, 1f, 1f, 1f);
				Bezier<Vector2> bezier = (Bezier<Vector2>)path;
				for (Vector2 point : bezier.points) {
					renderer.circle(point.x, point.y, .2f, 8);
				}
			} else if (path instanceof CatmullRomSpline) {
				CatmullRomSpline<Vector2> catmullRomSpline = (CatmullRomSpline<Vector2>)path;
				renderer.setColor(1f, 1f, 0, 1f);
				for (Vector2 cp : catmullRomSpline.controlPoints) {
					renderer.circle(cp.x, cp.y, 8f, 16);
				}
			}
			float val = 0f;
			renderer.setColor(0f, 1f, 1f, 1f);
			while (val <= 1f - SAMPLE_POINT_DISTANCE) {
				path.valueAt(/* out: */tmpV, val);
				path.valueAt(/* out: */tmpV2, val + SAMPLE_POINT_DISTANCE);
				renderer.line(tmpV.x, tmpV.y, tmpV2.x, tmpV2.y);
				val += SAMPLE_POINT_DISTANCE;
			}
			renderer.end();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			rebuild();
		}
	}

	@Override public void dispose () {
		super.dispose();
		region.getTexture().dispose();
	}

	@Override public void resize (int width, int height) {
//		float maxWidth = 1280;
//		float minWidth = 1280/4;

//		float scale = MathUtils.clamp((width - minWidth)/(maxWidth - minWidth), .25f, 1.25f);
//		guiViewport.setUnitsPerPixel(1/scale);
//		for (int i = 1280/4 - 300; i <= 1280 + 300; i+= 100) {
//			scale = MathUtils.clamp((i - minWidth)/(maxWidth - minWidth), .25f, 1.25f);
//			Gdx.app.log("", "welp " + i + " " + scale);
//		}
		super.resize(width, height);
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
		PlaygroundGame.start(args, config, UIStacks5Test.class);
	}

}

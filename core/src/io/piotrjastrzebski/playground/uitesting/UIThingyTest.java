package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 * Created by PiotrJ on 20/06/15.
 */
public class UIThingyTest extends BaseScreen {
	static final int WIDTH = 300;
	static final int HEIGHT = 500;
	public UIThingyTest (GameReset game) {
		super(game);
		for (Texture texture : skin.getAtlas().getTextures()) {
			texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		}

		final Area area = new Area(skin);

		final Table container = new Table();
		container.addActor(area);
		// transform instead of clip so behaviour is the same, but we can draw outside
		container.setTransform(true);
//		container.setClip(true);

		{
			Image image = new Image(skin, "white");
			image.setColor(1, .5f, 1f, .5f);
			root.add(image).grow().colspan(3).row();
		}
		{
			Image image = new Image(skin, "white");
			image.setColor(1, .5f, 1, .5f);
			root.add(image).growX().height(HEIGHT);
		}
		root.add(container).size(WIDTH, HEIGHT);
		{
			Image image = new Image(skin, "white");
			image.setColor(1, .5f, 1, .5f);
			root.add(image).growX().height(HEIGHT).row();
		}
		{
			Image image = new Image(skin, "white");
			image.setColor(1, .5f, 1, .5f);
			root.add(image).grow().colspan(3);
		}
		container.setZIndex(0);

		clear.set(Color.GRAY);

		{
			final Dude dude = new Dude(skin, true);
			container.setTouchable(Touchable.enabled);
			container.addListener(new ActorGestureListener(){
				Vector2 v2 = new Vector2();
				@Override public void tap (InputEvent event, float x, float y, int count, int button) {
					container.localToDescendantCoordinates(area, v2.set(x, y));
					dude.move(v2.x, v2.y);
				}
			});
			area.addDude(dude);
		}

		area.addDude(new Dude(skin, false));
		area.addDude(new Dude(skin, false));
	}

	static class Area extends Group {
		Rectangle viewBounds = new Rectangle();
		Rectangle bounds = new Rectangle();
		Drawable bg;
		int targetSize = 1;
		float currentSize = 1;
		boolean resizing;
		float resizeTime;

		public Area (Skin skin) {
			bounds.set(0, 0, WIDTH, HEIGHT);
			viewBounds.set(0, 0, WIDTH, HEIGHT);
			setSize(WIDTH, HEIGHT);
			bg = skin.getDrawable("white");
			debugAll();
		}

		void expandSize () {
			if (targetSize >= 4) return;
			targetSize++;
			resizing = true;
			resizeTime = 0;
		}

		void contractSize () {
			if (targetSize <= 1) return;
			targetSize--;
			resizing = true;
			resizeTime = 0;
		}

		Dude main;
		Vector2 v2 = new Vector2();
		@Override public void act (float delta) {
			super.act(delta);
			if (resizing) {
				if (currentSize > targetSize) {
					currentSize -= delta * 5;
					if (currentSize < targetSize) {
						currentSize = targetSize;
						resizing = false;
					}
				} else {
					currentSize += delta * 5;
					if (currentSize > targetSize) {
						currentSize = targetSize;
						resizing = false;
					}
				}
				bounds.set(-(WIDTH * (currentSize-1)) * .5f, 0, WIDTH * currentSize, HEIGHT);
			}

			if (main != null) {
				Group parent = getParent();
				main.localToAscendantCoordinates(parent, v2.set(main.getWidth()/2, 0));
				float dx = v2.x;
				parentToLocalCoordinates(v2.set(0, 0));
				float lx = getX() + v2.x + WIDTH * .2f;
				float rx = getX() + v2.x + WIDTH * .8f;
				if (dx < lx) {
					float offset = lx - dx;
					setX(getX() + offset);
				} else if (dx > rx) {
					float offset = dx - rx;
					setX(getX() - offset);
				}
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
				setX(getX() - 10);
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
				setX(getX() + 10);
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
				expandSize();
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
				contractSize();
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
				setX(0);
			}
		}

		@Override public void draw (Batch batch, float parentAlpha) {
			batch.setColor(Color.LIME);
			float bx = getX();
			float by = getY();
			bg.draw(batch, bx + bounds.x, by + bounds.y, bounds.width, bounds.height);
			batch.setColor(Color.FOREST);
			int size = 10;
			for (int x = 0; x < bounds.width/size; x++) {
				for (int y = 0; y < bounds.height/size; y++) {
					if (x % 2 == 0 && y % 2 == 1) {
						bg.draw(batch, bx + bounds.x + x * size, by + bounds.y + y * size, size, size);
					} else if (x % 2 == 1 && y % 2 == 0) {
						bg.draw(batch, bx + bounds.x + x * size, by + bounds.y + y * size, size, size);
					}
				}
			}

			batch.setColor(Color.WHITE);

			super.draw(batch, parentAlpha);
		}

		@Override public void drawDebug (ShapeRenderer shapes) {
			super.drawDebug(shapes);
			if (!getDebug()) return;
			shapes.setColor(Color.MAGENTA);
			float bx = getX();
			float by = getY();
			float cx = bounds.x + bounds.width/2;
			float lx = cx - WIDTH * .3f;
			float rx = cx + WIDTH * .3f;

			parentToLocalCoordinates(v2.set(0, 0));
			float plx = bx + v2.x + WIDTH * .2f;
			float prx = bx + v2.x + WIDTH * .8f;
			shapes.line(plx, by, plx, by + HEIGHT);
			shapes.line(prx, by, prx, by + HEIGHT);
		}

		@Override public Actor hit (float x, float y, boolean touchable) {
			return super.hit(x, y, touchable);
		}

		public void addDude (final Dude dude) {
			addActor(dude);
			dude.setPosition(
				MathUtils.random(WIDTH * .3f, WIDTH * .7f),
				MathUtils.random(HEIGHT * .3f, HEIGHT * .7f)
			);
			if (dude.main) {
				main = dude;
			}
		}
	}

	static class Dude extends Image {
		static int ids = 1;
		int id = ids++;
		final boolean main;

		public Dude (Skin skin, boolean main) {
			super(skin, "white");
			this.main = main;
			setSize(33, 33);
			setTouchable(Touchable.enabled);
			if (main) {
				setColor(Color.YELLOW);
			} else {
				setColor(Color.ORANGE);
				moveRandomly();
			}
			addListener(new ClickListener(){
				@Override public void clicked (InputEvent event, float x, float y) {
					PLog.log("Clicked dude " + id);
				}
			});
		}

		@Override public void act (float delta) {
			super.act(delta);
			Area parent = (Area)getParent();
			float x = getX(Align.center);
			if (x > parent.bounds.x + parent.bounds.width) {
				setX(parent.bounds.x + parent.bounds.width, Align.center);
				if (!main) {
					clearActions();
					moveRandomly();
				}
			} else if (x < parent.bounds.x){
				setX(parent.bounds.x, Align.center);
				if (!main) {
					clearActions();
					moveRandomly();
				}
			}
		}

		private void moveRandomly () {
			addAction(Actions.sequence(
				Actions.delay(MathUtils.random(1f, 5f)),
				Actions.run(new Runnable() {
					@Override public void run () {
						Area parent = (Area)getParent();
						float tx = parent.bounds.x + MathUtils.random(parent.bounds.width);
						float ty = parent.bounds.y + MathUtils.random(parent.bounds.height);
						move(tx, ty);
						moveRandomly();
					}
				})
			));
		}

		static Vector2 v2 = new Vector2();
		public void move (float x, float y) {
			clearActions();
			float dst = v2.set(getX(Align.center), getY(Align.center)).dst(x, y);
			addAction(Actions.moveToAligned(x, y, Align.center, dst/300f));
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}


	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setWindowedMode(1280/2, 720/2);
		PlaygroundGame.start(args, config, UIThingyTest.class);
	}
}

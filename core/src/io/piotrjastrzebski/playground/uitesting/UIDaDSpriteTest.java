package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.ecs.assettest.SpriteRenderable;

/**
 * We want to figure out a way to drag and drop stuff from one tree to another, to proper places
 * Also change level of a node in a tree and order at same depth
 * Largish actors in nodes
 *
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/DragAndDropTest.java
 * Created by PiotrJ on 20/06/15.
 */
public class UIDaDSpriteTest extends BaseScreen {
	Label source1;
	Label source2;
	Label target;
	DragAndDrop dad;
	Texture texture;
	public UIDaDSpriteTest (GameReset game) {
		super(game);


		texture = new Texture("tiled/brick.png");
		root.add(source1 = new Label("Sprite Source", skin)).pad(100);
		root.add(source2 = new Label("Image Source", skin)).pad(100);
		root.add(target = new Label("Target", skin)).pad(100);

		dad = new DragAndDrop();

		dad.addSource(new Source(source1) {
			@Override public Payload dragStart (InputEvent event, float x, float y, int pointer) {
				Payload payload = new Payload();
				payload.setDragActor(new LabelWithSprite("Drag", new Sprite(texture), skin));
				return payload;
			}
		});

		dad.addSource(new Source(source2) {
			@Override public Payload dragStart (InputEvent event, float x, float y, int pointer) {
				Payload payload = new Payload();
				Table table = new Table();
				Image image = new Image(texture);
				image.setColor(Color.RED);
				table.add(image);
				table.add(new Label("Drag", skin));
				table.pack();
				payload.setDragActor(table);
				return payload;
			}
		});

		dad.addTarget(new Target(target) {
			@Override public boolean drag (Source source, Payload payload, float x, float y, int pointer) {
				return true;
			}

			@Override public void drop (Source source, Payload payload, float x, float y, int pointer) {

			}
		});

	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	public static class LabelWithSprite extends Label {
		Sprite sprite;

		public LabelWithSprite (CharSequence text, Sprite sprite, Skin skin) {
			super(text, skin);
			this.sprite = sprite;
			sprite.setColor(Color.RED);
		}

		Vector2 pos = new Vector2();
		@Override public void draw (Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			// this doesnt work as the parent is already the size of entire stage
//			localToStageCoordinates(pos.set(getX(), getY()));
//			sprite.setPosition(pos.x - 30, pos.y);
			sprite.setPosition(getX() - 30, getY());
			sprite.draw(batch);
		}
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}
}

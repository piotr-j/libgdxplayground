package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISortTest extends BaseScreen {
	Actor lastFocus = null;
	VisList<Row> rowList;
	Array<Row> rows;
 	public UISortTest (GameReset game) {
		super(game);
//		VisTable data = new VisTable();
//		for (int i = 1; i <= 30; i++) {
//			final VisTextButton button;
//			data.add(button = new VisTextButton("Some data " + i));
//			data.row();
//			button.addListener(new ClickListener() {
//				@Override public void clicked (InputEvent event, float x, float y) {
//					Gdx.app.log("", "Clicked " + button);
//				}
//			});
//		}


		rows = new Array<>();
		for (int i = 0; i < 20; i++) {
			Row row = new Row("Row " + i, MathUtils.random(40));
			rows.add(row);
		}

		rowList = new VisList<>();
		rowList.setItems(rows);

		VisWindow window = new VisWindow("Pane");
		VisTextButton sortBtn = new VisTextButton("Sort");
		sortBtn.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				rows.sort();
				rowList.setItems(rows);
			}
		});
		window.add(sortBtn).row();
		final VisScrollPane pane;
		window.add(pane = new VisScrollPane(rowList));
		root.add(window).pad(200);
		pane.addListener(new InputListener() {
			@Override public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				lastFocus = stage.getScrollFocus();
				stage.setScrollFocus(pane);
			}

			@Override public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				stage.setScrollFocus(lastFocus);
			}
		});
		pane.addListener(new ActorGestureListener() {
			@Override public void fling (InputEvent event, float velocityX, float velocityY, int button) {
				Gdx.app.log("", "fling...");
			}
		});
		pane.setCancelTouchFocus(false);


	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	public static class Row implements Comparable<Row>{
		VisTextButton button;
		int sort;
		String name;
		public Row(String name, int sort) {
			this.sort = sort;
			this.name = name + ", s=" + sort;
			button = new VisTextButton(name);
			button.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					Gdx.app.log("", "Clicked " + button);
				}
			});
		}

		@Override public int compareTo (Row o) {
			return sort - o.sort;
		}

		@Override public String toString () {
			return name;
		}
	}
}

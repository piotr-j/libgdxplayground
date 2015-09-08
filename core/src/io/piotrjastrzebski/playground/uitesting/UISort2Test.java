package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISort2Test extends BaseScreen {
	Actor lastFocus = null;
	Array<Row> rows;
	VisTable rowsTable;
	VisWindow window;
 	public UISort2Test (GameReset game) {
		super(game);
		rowsTable = new VisTable();
//		for (int i = 1; i <= 30; i++) {
//			final VisTextButton button;
//			rowsTable.add(button = new VisTextButton("Some data " + i));
//			rowsTable.row();
//			button.addListener(new ClickListener() {
//				@Override public void clicked (InputEvent event, float x, float y) {
//					Gdx.app.log("", "Clicked " + button);
//				}
//			});
//		}


		rows = new Array<>();
		for (int i = 0; i < 20; i++) {
			Row row = new Row("Row " + i, MathUtils.random(40));
			rowsTable.add(row).row();
			rows.add(row);
		}

		window = new VisWindow("Pane");
		window.setResizable(true);
		window.setResizeBorder(16);
		VisTextButton sortBtn = new VisTextButton("Sort");
		sortBtn.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				sort();
			}
		});
		window.add(sortBtn).row();
		final VisScrollPane pane;
		window.add(pane = new VisScrollPane(rowsTable));
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
		pane.setCancelTouchFocus(false);

		sort();
	}

	private void sort() {
//		rowsTable.invalidate();
//		rows.sort();
//		SnapshotArray<Actor> children = rowsTable.getChildren();
//		children.sort();

//		Array<Cell> cells = rowsTable.getCells();
//		cells.sort(new Comparator<Cell>() {
//			@Override public int compare (Cell o1, Cell o2) {
//				Row a1 = (Row)o1.getActor();
//				Row a2 = (Row)o2.getActor();
//				return a1.compareTo(a2);
//			}
//		});

//		rowsTable.invalidate();
//		rowsTable.invalidateHierarchy();
//		rowsTable.pack();

		rows.sort();
		rowsTable.clear();
		for (Row row : rows) {
			rowsTable.add(row).expandX().fill().pad(5).row();
//			row.addTo(rowsTable);
			row.sortId(MathUtils.random(40));
		}

//		window.pack();
	}

	float timer;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
//		timer += delta;
//		if (timer > 0.25f) {
//			timer = 0;
//			sort();
//		}
	}

	public static class Row extends Table implements Comparable<Row>{
		VisTextButton button;
		VisLabel sortLabel;
		int sort;
		String rawName;
		String name;
		public Row(String name, int sort) {
			rawName = name;
			this.name = name;
			this.sort = sort;

			sortLabel = new VisLabel(Integer.toString(sort));
//			this.name = name + ", s=" + sort;
			button = new VisTextButton(name, "toggle");
			button.setChecked(true);
			button.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					Gdx.app.log("", "Clicked " + button);
				}
			});
			add(button).padRight(10).expandX().fill();
			add(sortLabel);
		}

		@Override public int compareTo (Row o) {
			return sort - o.sort;
		}

//		public void addTo (VisTable table) {
//			table.add(this).pad(10).row();
//			table.add(button).pad(10);
//			table.add(sortLabel).row();
//		}

		public void sortId (int id) {
			sort = id;
			sortLabel.setText(Integer.toString(sort));
//			this.name = rawName + ", s=" + sort;
		}
	}
}

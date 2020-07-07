package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISplitPane3Test extends BaseScreen {
	public UISplitPane3Test (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
//		stage.setDebugAll(true);

//		Table splitTable = new Table();
//		splitTable.defaults().pad(16).grow();
//
//		splitTable.add(new VisTextButton("Button!"));
//		splitTable.add(new VisTextButton("Button!")).colspan(2);
//		splitTable.row();
//		splitTable.add(new VisTextButton("Button!")).colspan(2);
//		splitTable.add(new VisTextButton("Button!"));
//		splitTable.row();
//		splitTable.add(new VisTextButton("Button!"));
//		splitTable.add(new VisTextButton("Button!"));
//		splitTable.add(new VisTextButton("Button!"));
//		splitTable.row();
//		splitTable.add(new VisTextButton("Button!")).colspan(3);
//
//		root.add(splitTable).pad(32).grow();
//		// separator actor + resize cells
//
//		splitTable.addListener(new InputListener() {
//			@Override
//			public boolean mouseMoved (InputEvent event, float x, float y) {
////				Actor hit = splitTable.hit(x, y, false);
//				Cell cell = splitTable.cellAt(x, y);
//				PLog.log("over " + cell);
//				return super.mouseMoved(event, x, y);
//			}
//		});

		Table table = new Table();
		VisTextButton left = new VisTextButton("  LEFT  ");
		VisTextButton center = new VisTextButton("  CENTER  ");
		VisTextButton right = new VisTextButton("  RIGHT  ");
//
		SplitSeparator leftCenter = new SplitSeparator(table, left, center, true);
		SplitSeparator centerRight = new SplitSeparator(table, center, right, true);
//
		table.add(left).pad(16).growY().prefWidth(Value.percentWidth(.3f, table)).minWidth(Value.percentWidth(.05f, table));
		table.add(leftCenter).grow().padTop(16).padBottom(16);
		table.add(center).pad(16).growY().prefWidth(Value.percentWidth(.3f, table)).minWidth(Value.percentWidth(.05f, table));
		table.add(centerRight).grow().padTop(16).padBottom(16);
		table.add(right).pad(16).growY().prefWidth(Value.percentWidth(.3f, table)).minWidth(Value.percentWidth(.05f, table));

		table.debug();
		root.add(table).pad(32).grow();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		root.pack();
	}

	protected static class SplitSeparator extends Separator {
		private final Table parent;
		private final Actor left;
		private final Actor right;

		ClickListener clickListener;

		public SplitSeparator (Table parent, Actor first, Actor second, boolean vertical) {
			this.parent = parent;
			this.left = first;
			this.right = second;

			setTouchable(Touchable.enabled);
			addListener(clickListener = new ClickListener());
			addListener(new ActorGestureListener() {
				boolean moved;
				@Override
				public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
					super.pan(event, x, y, deltaX, deltaY);
					Cell<Actor> cellFirst = parent.getCell(first);
					Cell<Actor> cellSecond = parent.getCell(second);
					if (vertical) {
						float firstWidth = deltaX + cellFirst.getPrefWidthValue().get(first);
						float secondWidth = -deltaX + cellSecond.getPrefWidthValue().get(second);
						// not great, seem to be extra step around boundary
						// result width ends up higher and we go lower again?
						// using absolute values is kinda janky anyway...
						// but its quite tricky to use % based when we smoothly move it
						// figure it our in touch up?
						if (firstWidth >= cellFirst.getMinWidth() && secondWidth >= cellSecond.getMinWidth()) {
							moved = true;
							cellFirst.prefWidth(firstWidth);
							cellSecond.prefWidth(secondWidth);
							parent.invalidateHierarchy();
						}
					} else {

					}
				}

				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					super.touchUp(event, x, y, pointer, button);
					if (!moved) return;
					moved = false;
//					Cell<Actor> cellFirst = parent.getCell(first);
//					Cell<Actor> cellSecond = parent.getCell(second);
//					float firstWidth = cellFirst.getPrefWidthValue().get(first);
//					float secondWidth = cellSecond.getPrefWidthValue().get(second);
//
//					float firstPercent = firstWidth/parent.getWidth();
//					float secondPercent = secondWidth/parent.getWidth();
//					PLog.log("1 % " + firstPercent + ", 2 %" + secondPercent);
//
//					cellFirst.width(Value.percentWidth(firstPercent, parent));
//					cellSecond.width(Value.percentWidth(secondPercent, parent));
//					parent.invalidateHierarchy();

//
//					cellFirst.setActorWidth();

				}
			});
		}

		@Override
		public void draw (Batch batch, float parentAlpha) {
			if (clickListener.isOver()) {
				setColor(Color.RED);
			} else {
				setColor(Color.WHITE);
			}
			super.draw(batch, parentAlpha);
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}


	public static void main (String[] args) {
		PlaygroundGame.start(args, UISplitPane3Test.class);
	}
}

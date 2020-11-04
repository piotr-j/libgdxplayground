package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.widget.VisTextArea;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITextAreaScrollPaneTest extends BaseScreen {
	public UITextAreaScrollPaneTest (GameReset game) {
		super(game);

		// we want text area to work in a scroll pane, somehow
		StringBuilder dummyText = new StringBuilder();
		for (int i = 0; i < 512; i++) {
			char c = (char)MathUtils.random('a', 'z');
			dummyText.append(c);
			if (MathUtils.random() > .95f) {
				dummyText.append("\n");
			}
		}
		{
			dummyText.setLength(0);
			for (int i = 0; i < 55; i++) {
				dummyText.append("ASDF QWER 1234 #").append(i).append("\n");
			}
			dummyText.setLength(dummyText.length() - 1);
		}
		{
			dummyText.setLength(0);
			dummyText.append("|jg Text Area\nEssentially, a text field\nwith\nmultiple\nlines.\n");
			// we need a bunch of lines to demonstrate that prefHeight is way too large
			for (int i = 0; i < 30; i++) {
//				dummyText.append("It can even handle very loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong lines.\n");
				dummyText.append("It cant even handle very loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong lines.\n");
			}
		}
		if (false) {
			Table table = new Table();
//			root.add(table).size(300, 200).pad(100);
			TextArea textArea = new TextArea(dummyText.toString(), skin);
			table.add(textArea).grow();

			root.add(table).growY().width(300).pad(100);
		}
		if (false) {

			Table table = new Table();
			table.debugAll();

//			Image image = new Image(new Texture(Gdx.files.internal("badlogic.jpg")));

			ScrollPane pane = new ScrollPane(table, skin);
			pane.setScrollingDisabled(true, false);
//			pane.setScrollbarsVisible(true);
//			pane.setFadeScrollBars(false);
			// we probably want to scroll via text area? :<
			pane.setFlickScroll(false);
//			pane.setCancelTouchFocus(false);

//			root.add(table).size(300, 200).pad(100);
			TextArea textArea = new TextArea(dummyText.toString(), skin) {
				@Override
				protected void calculateOffsets () {
					int lines = getLines();
					super.calculateOffsets();
					int newLines = getLines();
					if (lines != newLines) {
						PLog.log("New offsets? " + lines + " -> " + newLines);
						setPrefRows(newLines);
						invalidateHierarchy();
					}
				}
			};
			table.add(textArea).grow();
//			table.add(image).grow();

			root.add(pane).size(300, 200).pad(100);
//			pane.debugAll();
		}
		if (false) {

			Table table = new Table();

			ScrollPane pane = new ScrollPane(table, skin);
			pane.setScrollingDisabled(true, false);
			pane.setScrollbarsVisible(true);
			pane.setFadeScrollBars(false);
//			pane.setFlickScroll(false);
//			pane.setCancelTouchFocus(false);

			TextArea textArea = new TextArea(dummyText.toString(), skin);
			table.add(textArea).grow();

			root.add(pane).size(300, 200).pad(100);
		}
		if (false) {

//			Table table = new Table();
			TextArea textArea = new TextArea(dummyText.toString(), skin);
//			table.add(textArea).grow();

			ScrollPane pane = new ScrollPane(textArea, skin);
			pane.setScrollingDisabled(true, false);
			pane.setScrollbarsVisible(true);
			pane.setFadeScrollBars(false);
//			pane.setFlickScroll(false);
//			pane.setCancelTouchFocus(false);


			root.add(pane).size(300, 200).pad(100);
		}
		if (false){
			TextField textField = new TextField("|ASDFIJjnjóćżęŻŹÓł", skin);
//			textField.setDebug(true);
			root.add(textField).pad(10).row();
		}
		if (false){

			TextField textField = new TextField("|ASDFIJjnjóćżęŻŹÓł", skin) {
				@Override
				public void setStyle (TextFieldStyle style) {
					super.setStyle(style);
					textHeight = style.font.getLineHeight();
					invalidateHierarchy();
				}
			};
//			textField.setDebug(true);
			root.add(textField).pad(10).row();
		}
		if (true) {
			// dummyText 55 lines of "ASDF QWER 1234 #line"
			// a bunch of enters and its fucked :<

//			Image knobStart = new Image(skin, "default-slider-knob");
//			Image knobEnd = new Image(skin, "default-slider-knob");
			Image knobStart = new Image(skin, "default-round-down");
			Image knobEnd = new Image(skin, "default-round-down");
			// we depend on my TextArea pr for this to look ok
			PTextArea textArea = new PTextArea(dummyText.toString(), skin)  {
				float prefRows;
				@Override
				protected void calculateOffsets () {
					int lines = getLines();
					super.calculateOffsets();
					int newLines = getLines();
					// if we set prefRows to number of rows area will be bigger thus enabling scrolling
					// otherwise the text area will be size of the pane and draw visible lines
					if (lines != newLines) {
						setPrefRows(newLines);
						prefRows = newLines;
						invalidateHierarchy();
					}
				}

				@Override
				public void draw (Batch batch, float parentAlpha) {
					super.draw(batch, parentAlpha);

					// cursor y is from the top, not bottom like we would expect
					float cursorY = getHeight() + getCursorY();
					Drawable drawable = getBackgroundDrawable();
					if (drawable != null) {
						cursorY -= drawable.getTopHeight();
					}

					float x = getX();
					float y = getY();
//					shapes.setColor(Color.MAGENTA);
//					shapes.setColor(Color.ORANGE);
//					shapes.line(x, y + cursorY + lineHeight, x + getWidth(), y + cursorY + lineHeight);
//
//					shapes.setColor(Color.FIREBRICK);
//					shapes.line(x, y + cursorY, x + getWidth(), y + cursorY);



//					ScrollPane pane = (ScrollPane)getParent();
					// this is like a char id in
					if (!hasSelection || getSelection().length() == 0) {
						knobStart.setVisible(false);
						knobEnd.setVisible(false);
						return;
					}
					knobStart.setVisible(true);
					knobEnd.setVisible(true);
					// we want to draw a knob or some such at start and end of the selection
					// a square will do for now
					Vector2 start = getSelectionStart(new Vector2());

					shapes.setColor(Color.YELLOW);
//					shapes.filledRectangle(x + start.x-5, y + start.y, 10, 10);
					knobStart.setPosition(start.x-5, start.y);
					Vector2 end = getSelectionEnd(new Vector2());
//					shapes.filledRectangle(x + getCursorX(), y + cursorY, 10, 10);
//					knobEnd.setPosition(end.x - 5, end.y);
					knobEnd.setPosition(end.x - 5, end.y);

//					PLog.log("Knob start = " + start + ", end = " + end);

				}

				// we want to prevent selection by dragging
				@Override
				protected InputListener createInputListener () {
					return new TextAreaListener() {
						boolean dragging;
						@Override
						public void touchDragged (InputEvent event, float x, float y, int pointer) {
							dragging = true;
							super.touchDragged(event, x, y, pointer);
							dragging = false;
						}

						@Override
						protected void setCursorPosition (float x, float y) {
							if (dragging) return;
							super.setCursorPosition(x, y);
						}
					};
				}
			};

//			knobStart.addListener(new InputListener(){
//				@Override
//				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
//					return true;
//				}
//
//				@Override
//				public void touchDragged (InputEvent event, float x, float y, int pointer) {
//					PLog.log("Drag start");
//				}
//			});

			Table table = new Table();
			table.add(textArea).grow();
			table.addActor(knobStart);
			table.addActor(knobEnd);
			ScrollPane pane = new ScrollPane(table, skin);
			// vertical scroll only
			pane.setScrollingDisabled(true, false);
			pane.setScrollbarsVisible(true);
			pane.setFadeScrollBars(false);
			// if enabled (default) we cant easily select text as pane moved when we try it
			// if disabled, we can select but scroll pane doesnt react, need to scroll based on cursor position or some such
//			pane.setFlickScroll(false);
			pane.setCancelTouchFocus(false);


			// 0 delay for pan
			knobStart.addListener(new ActorGestureListener(0, 0.4f, 1.1f, Integer.MAX_VALUE) {
				@Override
				public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
					// probably not needed
					knobEnd.setTouchable(Touchable.disabled);
					stage.cancelTouchFocus(pane);
					pane.setFlickScroll(false);
					float sx = knobStart.getX() + x;
					float sy = knobStart.getY() + y;
					float ex = knobEnd.getX();
					float ey = knobEnd.getY();
					Vector2 target = knobStart.localToActorCoordinates(textArea, new Vector2(x, y));
					if (sy > ey) {
//						PLog.log("Start > end");
						textArea.setSelectionStart(target.x, target.y);
					} else if (MathUtils.isEqual(sy, ey) && sx <= ex) {
//						PLog.log("Start > end");
						textArea.setSelectionStart(target.x, target.y);
					} else {
//						PLog.log("Start < end");
						textArea.setSelectionEnd(target.x, target.y);
					}

					PLog.log("s " + sx + ", " + sy + ", e " + ex + ", " + ey);
//					PLog.log("knobStart: pan " + target);
//					textArea.setSelection(textArea.getSelectionStart() - 1, textArea.getSelectionStart() + textArea.getSelection().length());
				}

				@Override
				public void panStop (InputEvent event, float x, float y, int pointer, int button) {
					super.panStop(event, x, y, pointer, button);
					pane.setFlickScroll(true);
					knobEnd.setTouchable(Touchable.enabled);
				}
			});
			// 0 delay for pan
			knobEnd.addListener(new ActorGestureListener(0, 0.4f, 1.1f, Integer.MAX_VALUE) {
				@Override
				public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
					knobStart.setTouchable(Touchable.disabled);
					stage.cancelTouchFocus(pane);
					pane.setFlickScroll(false);

					float sx = knobStart.getX();
					float sy = knobStart.getY();
					float ex = knobEnd.getX() + x;
					float ey = knobEnd.getY() + y;
//					textArea.setSelection(textArea.getSelectionStart(), textArea.getSelectionStart() + textArea.getSelection().length() + 1);
					// we assume that parent is same size as text area
					Vector2 target = knobEnd.localToActorCoordinates(textArea, new Vector2(x, y));
//					textArea.setSelectionEnd(target.x, target.y);
					if (sy > ey) {
//						PLog.log("Start > end");
						textArea.setSelectionEnd(target.x, target.y);
					} else if (MathUtils.isEqual(sy, ey) && sx <= ex) {
//						PLog.log("Start > end");
						textArea.setSelectionEnd(target.x, target.y);
					} else {
//						PLog.log("Start < end");
						textArea.setSelectionStart(target.x, target.y);
					}
//					PLog.log("knobEnd: pan " + target);
				}

				@Override
				public void panStop (InputEvent event, float x, float y, int pointer, int button) {
					super.panStop(event, x, y, pointer, button);
					pane.setFlickScroll(true);
					knobStart.setTouchable(Touchable.enabled);
				}
			});

			// size smaller than desired so there is something to scroll
			root.add(pane).size(300, 200).pad(100);
		}
		if (false) {

			Table table = new Table();
			table.debugAll();

//			Image image = new Image(new Texture(Gdx.files.internal("badlogic.jpg")));

			ScrollPane pane = new ScrollPane(table, skin);
			pane.setScrollingDisabled(true, false);
			pane.setScrollbarsVisible(true);
			pane.setFadeScrollBars(false);
			// we probably want to scroll via text area? :<
			pane.setFlickScroll(false);
//			pane.setCancelTouchFocus(false);

//			root.add(table).size(300, 200).pad(100);
			VisTextArea textArea = new VisTextArea(dummyText.toString()) {
				@Override
				protected void calculateOffsets () {
					int lines = getLines();
					super.calculateOffsets();
					int newLines = getLines();
//					if (lines != newLines) {
//						PLog.log("New offsets? " + lines + " -> " + newLines);
//						setPrefRows(newLines);
//						invalidateHierarchy();
//					}
				}
			};
			table.add(textArea).grow();
//			table.add(image).grow();

			root.add(pane).size(300, 200).pad(100);
//			pane.debugAll();
		}
		if (false){
			// ... easier to add a scroll bar to the area perhaps?

			class TA extends Table {
				TextArea textArea;
				Slider scrollBar;
				boolean programmaticChange = false;
				public TA (String text, Skin skin) {
					// we will scroll per line i suppose?
					scrollBar = new Slider(0, 10, 1, true, skin);
					Table table = new Table();
					table.setFillParent(true);
					table.add(scrollBar).expand().fillY().right();
					textArea = new TextArea(text, skin) {
						int lastLineShowing = -1;
						@Override
						protected void calculateOffsets () {
							int lines = getLines();
							super.calculateOffsets();
							int newLines = getLines();
							int firstLineShowing = getFirstLineShowing();

							float pages = newLines/(float)getLinesShowing();
							PLog.log("Pages = " + pages);

							if (lines != newLines) {
								PLog.log("New offsets? " + lines + " -> " + newLines);
								setPrefRows(newLines);

								programmaticChange = true;
								scrollBar.setRange(getLinesShowing() - 1, newLines);
//								scrollBar.setStepSize(1);
								programmaticChange = false;
								invalidateHierarchy();
							}

							if (firstLineShowing != lastLineShowing) {
								PLog.log("first showing " + firstLineShowing);
//								PLog.log("New first? " + lastLineShowing + " -> " + firstLineShowing);
								float v = (pages) / (firstLineShowing + 1.0f);
//								PLog.log("new val = " + v);
								programmaticChange = true;
//								scrollBar.setValue(v);
								programmaticChange = false;
								lastLineShowing = firstLineShowing;
							}
						}
					};
					add(textArea).size(300, 200).fill();
					addActor(table);

					scrollBar.addListener(new ChangeListener() {
						@Override
						public void changed (ChangeEvent event, Actor actor) {
							if (programmaticChange) return;
							PLog.log("scroll -> " + scrollBar.getValue());
//							textArea.moveCursorLine(textArea.getLines() - (int)scrollBar.getValue() -1);
						}
					});

					debug();
				}
			}
//			root.add(table).size(300, 200).pad(100);
			Table textAreaWrapper = new TA(dummyText.toString(), skin);
//			table.add(image).grow();

			root.add(textAreaWrapper).size(300, 200).pad(100);
//			pane.debugAll();
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UITextAreaScrollPaneTest.class);
	}
}

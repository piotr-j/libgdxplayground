package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.Focusable;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Simple traversal for focusable vis widgets with tab
 *
 * Created by PiotrJ on 20/06/15.
 */
public class UITabTraversalTest extends BaseScreen {
    private final static String TAG = UITabTraversalTest.class.getSimpleName();

    public UITabTraversalTest (GameReset game) {
        super(game);

        VisWindow widgets = new VisWindow("Widgets");
        widgets.defaults().pad(5).expandX().fillX();

        Array<Focusable> focusables = new Array<>();
        VisTextField textField = new VisTextField() {
            public void next (boolean up) {
                // do nothing, we will handle it ourselves
            }
        };
        textField.addListener(new FocusableListener(textField, focusables));
        focusables.add(textField);
        widgets.add(textField).row();

        VisTextArea textArea = new VisTextArea() {
            public void next (boolean up) {
                // do nothing, we will handle it ourselves
            }
        };
        textArea.addListener(new FocusableListener(textArea, focusables));
        focusables.add(textArea);
        widgets.add(textArea).row();

        VisTextButton button = new VisTextButton("Do the thing");
        button.addListener(new FocusableListener(button, focusables));
        // got to use change events, we use check for this...
        button.addListener(new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {
                PLog.log("do the thing!");
            }
        });
        focusables.add(button);
        widgets.add(button).row();

        final VisCheckBox checkBox = new VisCheckBox("Check the thing");
        checkBox.addListener(new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {
                PLog.log("check the thing! " + checkBox.isChecked());
            }
        });
        focusables.add(checkBox);
        checkBox.addListener(new FocusableListener(checkBox, focusables));
        widgets.add(checkBox).row();

        widgets.pack();
        widgets.centerWindow();
        root.addActor(widgets);
    }

    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();
    }

    private static class FocusableListener extends ClickListener {
        private final Focusable owner;
        private final Array<Focusable> focusables;

        public FocusableListener (Focusable owner, Array<Focusable> focusables) {
            this.owner = owner;
            this.focusables = focusables;
        }

        @Override public void clicked (InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            event.getStage().setKeyboardFocus((Actor)owner);
        }

        @Override public boolean keyTyped (InputEvent event, char character) {
            if (character == '\t') {
                int index = focusables.indexOf(owner, true);
                if (index == -1) return false;
                Stage stage = event.getStage();
                if (UIUtils.shift()) { // uo
                    index -= 1;
                    if (index < 0) index += focusables.size;
                } else { // down
                    index += 1;
                }
                index %= focusables.size;
                Focusable target = focusables.get(index);
                FocusManager.switchFocus(stage, target);
                stage.setKeyboardFocus((Actor)target);
                return true;
            } else if (character == '\n' || character == '\r' || character == ' ') {
                // if we have focus and enter is pressed, do type specific action
                if (owner instanceof Button) {
                    // got to use checked, also handles checkbox
                    Button btn = (Button)owner;
                    btn.setChecked(!btn.isChecked());
                    return true;
                }
            }
            return super.keyTyped(event, character);
        }
    }
    public static void main (String[] args) {
        PlaygroundGame.start(args, UITabTraversalTest.class);
    }

}

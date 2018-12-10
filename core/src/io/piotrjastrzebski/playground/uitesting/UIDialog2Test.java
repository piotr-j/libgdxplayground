package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDialog2Test extends BaseScreen {
    private final static String TAG = UIDialog2Test.class.getSimpleName();

    public UIDialog2Test (GameReset game) {
        super(game);

        {
            final VisTextButton button = new VisTextButton("Show VIS dialog");
            button.addListener(new ClickListener() {
                @Override public void clicked (InputEvent event, float x, float y) {
                    showVisDialog();
                    button.addAction(Actions.sequence(Actions.moveBy(0, 10, .1f, Interpolation.bounce),
                        Actions.moveBy(0, -10, .1f, Interpolation.bounce)));
                }
            });
            root.add(button).pad(20).row();
        }
        {
            final VisTextButton button = new VisTextButton("Show dialog");
            button.addListener(new ClickListener() {
                @Override public void clicked (InputEvent event, float x, float y) {
                    showDialog();
                    button.addAction(Actions.sequence(Actions.moveBy(0, 10, .1f, Interpolation.bounce),
                        Actions.moveBy(0, -10, .1f, Interpolation.bounce)));
                }
            });
            root.add(button).pad(20).row();
        }
    }

    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();
    }

    enum Result {YES, NO}

    private void showVisDialog () {
        final VisDialog dialog = new VisDialog("Yes or no?!") {
            @Override protected void result (Object object) {
                if (object.equals(Result.YES)) {
                    Gdx.app.log(TAG, "YES");
                } else {
                    Gdx.app.log(TAG, "NO");
                }
            }

            @Override public void hide () {
                Gdx.app.log(TAG, "Hide");
                super.hide(sequence(Actions.fadeOut(3, Interpolation.fade),
                    Actions.removeActor()));
            }
        };
        dialog.addCloseButton();
        dialog.key(Input.Keys.Y, Result.YES);
        dialog.key(Input.Keys.N, Result.NO);
        dialog.button("yes! (Y)", Result.YES);
        dialog.button("no! (N)", Result.NO);
        dialog.show(stage);
        dialog.addListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (x < 0 || x > dialog.getWidth() || y < 0 || y > dialog.getHeight()){
                    dialog.hide();
                    event.cancel();
                    return true;
                }
                return false;
            }
        });
    }

    private void showDialog () {
        final Dialog dialog = new Dialog("Yes or no?!", skin) {
            @Override protected void result (Object object) {
                if (object.equals(Result.YES)) {
                    Gdx.app.log(TAG, "YES");
                } else {
                    Gdx.app.log(TAG, "NO");
                }
            }

            @Override public void hide () {
                Gdx.app.log(TAG, "Hide");
                super.hide(sequence(Actions.fadeOut(3, Interpolation.fade),
                    Actions.removeActor()));
            }
        };
        TextButton closeButton = new TextButton("X", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });
        // whats the purpose of this? doesnt seem to do anything...
        closeButton.addListener(new ClickListener() {
            @Override public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });

        dialog.getTitleTable().add(closeButton);

        dialog.key(Input.Keys.Y, Result.YES);
        dialog.key(Input.Keys.N, Result.NO);
        dialog.button("yes! (Y)", Result.YES);
        dialog.button("no! (N)", Result.NO);
        dialog.show(stage);
        dialog.addListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (x < 0 || x > dialog.getWidth() || y < 0 || y > dialog.getHeight()){
                    dialog.hide();
                    event.cancel();
                    return true;
                }
                return false;
            }
        });
    }

    public static void main (String[] args) {
        PlaygroundGame.start(args, UIDialog2Test.class);
    }

}

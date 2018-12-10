package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIPopupTest extends BaseScreen {
    protected static final String TAG = UIPopupTest.class.getSimpleName();
    public UIPopupTest (GameReset game) {
        super(game);

        final VisTextButton button = new VisTextButton("Show!");
        root.add(button);//.expand().fill().center();
        button.addListener(new ClickListener() {
            @Override public void clicked (InputEvent event, float x, float y) {
                Gdx.app.log(TAG, "Clicked!");
                showPopup(button);
            }
        });
    }

    private void showPopup (Actor actor) {
        float sx = actor.getX(Align.center);
        float sy = actor.getY(Align.center);

        VisDialog dialog = new VisDialog("Welp");
        dialog.closeOnEscape();
        dialog.addCloseButton();
        dialog.text("WTF!!! WTF!!! WTF!!! WTF!!! ");
        dialog.show(stage,
            sequence(
                Actions.alpha(0),
                Actions.scaleTo(.1f, .1f),
                Actions.parallel(
                    Actions.fadeIn(0.3f, Interpolation.fade),
                    Actions.scaleTo(1, 1, .3f, Interpolation.exp5Out)
                )
                ));
        dialog.setPosition(sx, sy, Align.topRight);
        dialog.setOrigin(Align.topRight);
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
        PlaygroundGame.start(args, UIPopupTest.class);
    }
}

package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISlideTest extends BaseScreen {
    private final static String TAG = UISlideTest.class.getSimpleName();
    VisWindow table;
    public UISlideTest (GameReset game) {
        super(game);
        clear.set(.5f, .5f, .5f, 1);
        table = new VisWindow("Sliding window");
        table.setMovable(false);
        for (int i = 0; i < 5; i++) {
            table.add(new VisLabel("Dummy content " + i)).row();
        }
        root.add(table).row();
        VisTextButton slide = new VisTextButton("Slide");
        slide.addListener(new ChangeListener() {
            int slide = 0;
            @Override public void changed (ChangeEvent event, Actor actor) {
                if (slide == 0) {
                    slide = 1;
                    table.addAction(Actions.sequence(
                        Actions.moveBy(-200, 0),
                        Actions.moveBy(200, 0, 1, Interpolation.sine),
                        Actions.run(new Runnable() {
                            @Override public void run () {
                                slide = 2;
                            }
                        })
                    ));
                } else if (slide == 2) {
                    slide = 1;
                    table.addAction(Actions.sequence(
                        Actions.moveBy(200, 0),
                        Actions.moveBy(-200, 0, 1, Interpolation.sine),
                        Actions.run(new Runnable() {
                            @Override public void run () {
                                slide = 0;
                            }
                        })
                    ));
                }
            }
        });
        root.add(slide);
    }

    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

        }
    }

    @Override public void dispose () {
        super.dispose();
    }

    public static void main (String[] args) {
		PlaygroundGame.start(args, UISlideTest.class);
	}
}

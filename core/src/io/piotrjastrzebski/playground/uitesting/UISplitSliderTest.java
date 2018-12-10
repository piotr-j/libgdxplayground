package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISplitSliderTest extends BaseScreen {
    protected static final String TAG = UISplitSliderTest.class.getSimpleName();

    TextureRegion region;
    public UISplitSliderTest (GameReset game) {
        super(game);
        Pixmap pixmap = new Pixmap(12, 12, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle(6, 6, 4);
        region = new TextureRegion(new Texture(pixmap));

        // TODO we want a progress bar with 2 distinct parts
        // TODO we need control over the precise position of transition
        // |----===|=====|

        final TimeBar progressBar = new TimeBar(0, 100, 1, false, skin);
        ProgressBar.ProgressBarStyle style = progressBar.getStyle();
        // stretches between start/end and the knob
//        style.knobBefore = new TextureRegionDrawable(region);
//        style.knobAfter = new TextureRegionDrawable(region);
        CompositeDrawable drawable = new CompositeDrawable(new NinePatchDrawable((NinePatchDrawable)style.background), new NinePatchDrawable((NinePatchDrawable)style.background));
        style.background = drawable;
        progressBar.setValue(25);
        progressBar.addAction(Actions.forever(
            Actions.sequence(new TemporalAction(2.5f) {
            @Override protected void update (float percent) {
                progressBar.setValue(percent * 100);
            }
        }, Actions.delay(.5f), new TemporalAction(2.5f) {
                @Override protected void update (float percent) {
                    progressBar.setValue((1-percent) * 100);
                }
            }, Actions.delay(.5f))));
        root.add(progressBar);

        clear.set(Color.LIGHT_GRAY);
    }

    @Override public void render (float delta) {
        super.render(delta);

        stage.act(delta);
        stage.draw();
    }

    static class CompositeDrawable extends BaseDrawable {
        NinePatchDrawable left;
        NinePatchDrawable right;
        float offset = .25f;

        public CompositeDrawable (NinePatchDrawable left, NinePatchDrawable right) {
            super(left);
            this.left = left;
            this.right = right;
        }

        @Override public void draw (Batch batch, float x, float y, float width, float height) {
            batch.setColor(Color.RED);
            left.draw(batch, x, y, width * offset, height);
            batch.setColor(Color.GREEN);
            right.draw(batch, x + width * offset, y, width * (1-offset), height);
            batch.setColor(Color.WHITE);
        }
    }

    static class TimeBar extends ProgressBar {
        public TimeBar (float min, float max, float stepSize, boolean vertical, Skin skin) {
            super(min, max, stepSize, vertical, skin);
        }
    }


    public static void main (String[] args) {
        PlaygroundGame.start(args, UISplitSliderTest.class);
    }
}

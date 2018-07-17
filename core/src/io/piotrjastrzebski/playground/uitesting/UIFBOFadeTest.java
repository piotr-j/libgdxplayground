package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIFBOFadeTest extends BaseScreen {
    protected static final String TAG = UIFBOFadeTest.class.getSimpleName();

    protected FadeDialog dialog;
    private FrameBuffer fbo;
    public UIFBOFadeTest (GameReset game) {
        super(game);
        clear.set(Color.DARK_GRAY);
        FadeRenderer.init();


        VisWindow.FADE_TIME = 2.0f;

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 2048, 2048, false);
        VisTextButton button = new VisTextButton("Dialog?");
        root.add(button);
        button.addListener(new ClickListener() {
            @Override public void clicked (InputEvent event, float x, float y) {
                dialog.pack();
                dialog.setTransform(true);
                dialog.setOrigin(Align.center);
                dialog.clearActions();
                // @off
                dialog.show(stage,
                    sequence(Actions.alpha(0),
                        Actions.scaleTo(0, 0),
                        Actions.parallel(
                            Actions.scaleTo(1, 1, VisWindow.FADE_TIME, Interpolation.fade),
                            Actions.fadeIn(VisWindow.FADE_TIME, Interpolation.fade))
                        )
                );

//                dialog.show(stage, sequence(Actions.alpha(0), Actions.fadeIn(VisWindow.FADE_TIME, Interpolation.fade)));
//                dialog.show(stage, sequence(Actions.alpha(.5f)));
                // @off
                dialog.setPosition(Math.round((stage.getWidth() - dialog.getWidth()) / 2), Math.round((stage.getHeight() - dialog.getHeight()) / 2));
            }
        });
        dialog = new FadeDialog("Hello!");
        dialog.addCloseButton();

        for (int i = 0; i < 20 + MathUtils.random(10); i++) {
            if (MathUtils.randomBoolean()) {
                dialog.getContentTable().add(new VisLabel("Dummy text " + MathUtils.random(0, 200)));
            } else {
                dialog.getContentTable().add(new VisSlider(0, 5 + MathUtils.random(5.f), 1, false));
            }
            if (i > 0 && i % 2 == 0) {
                dialog.getContentTable().row();
            }
        }
        VisTextButton ok = new VisTextButton("OK?");
        ok.addListener(new ClickListener(){
            @Override public void clicked (InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        VisTextButton cancel = new VisTextButton("Cancel!");
        cancel.addListener(new ClickListener(){
            @Override public void clicked (InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getButtonsTable().add(ok);
        dialog.getButtonsTable().add(cancel);

        dialog.pack();
        dialog.setTransform(true);
//        dialog.setColor(1, 1, 1, 0);
//        dialog.show(stage, forever(
//            sequence(Actions.fadeIn(2),
//            sequence(Actions.fadeOut(2)))
//        ));
    }

    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();
    }

    @Override public void dispose () {
        super.dispose();
        fbo.dispose();
        FadeRenderer.dispose();
    }

    static class FadeRenderer {
        protected static final String TAG = FadeRenderer.class.getSimpleName();
        static FrameBuffer fbo;
        static TextureRegion region;
        static OrthographicCamera camera;
        static Matrix4 projection;
        static final int size = 2048;

        public static void init () {
            projection = new Matrix4();

            resize(size, size);
        }

        static void resize (int width, int height) {
            if (fbo != null) fbo.dispose();
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            camera = new OrthographicCamera(width, height);
            region = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, width, height);
            region.flip(false, true);
        }

        public static void draw (Actor actor, Batch batch, float parentAlpha) {
            if (!(actor instanceof FadeActor)) {
                Gdx.app.log(TAG, "Invalid actor " + actor);
                return;
            }
            FadeActor fa = (FadeActor)actor;

            Color color = actor.getColor();
            // we need to use custom path only when semi transparent
            if (!MathUtils.isEqual(color.a * parentAlpha, 1.0f)) {
                // we probably cant do a whole lot about ending the batch, we need to do it so stuff in fbo is what we expect
                // cant defer either, as we need this to be in order in case stuff overlaps
                batch.end();

                final int margin = 2;
                final int width = MathUtils.round(actor.getWidth()) + margin * 2;
                final int height = MathUtils.round(actor.getHeight()) + margin * 2;
                camera.setToOrtho(false, width, height);
                camera.position.x = width/2;
                camera.position.y = height/2;
                camera.update();

                fbo.begin();
                // with some luck this will make it faster to clear, as we need just the region
                HdpiUtils.glViewport(0, 0, width, height);
                Gdx.gl.glClearColor(1, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                projection.set(batch.getProjectionMatrix());

                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                float a = color.a;
                color.a = 1;
                float x = actor.getX();
                float y = actor.getY();
                actor.setPosition(margin, margin);
                fa.fadeDraw(batch, 1);
                actor.setPosition(x, y);
                batch.end();
                color.a = a;
                // reset the color just in case
                batch.setColor(Color.WHITE);

                fbo.end();
                // flipped
                region.setRegion(0, height, width, -height);

                batch.setProjectionMatrix(projection);
                batch.begin();
                batch.setColor(1, 1, 1, a * parentAlpha);
                batch.draw(region, x - margin, y - margin, width, height);
                batch.setColor(Color.WHITE);
            } else {
                fa.fadeDraw(batch, parentAlpha);
            }
        }

        public static void dispose () {
            fbo.dispose();
        }
    }

    interface FadeActor {
        void fadeDraw(Batch batch, float parentAlpha);
    }

    static class FadeDialog extends VisDialog implements FadeActor {
        public FadeDialog (String title) {
            super(title);
        }

        @Override public void draw (Batch batch, float parentAlpha) {
            // this is annoying
            FadeRenderer.draw(this, batch, parentAlpha);
        }

        @Override public void fadeDraw (Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
        }
    }

    public static void main (String[] args) {
        PlaygroundGame.start(args, UIFBOFadeTest.class);
    }
}

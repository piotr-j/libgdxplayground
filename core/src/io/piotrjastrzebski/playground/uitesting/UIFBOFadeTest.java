package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
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

    public final static float SCALE = 2f;
    public final static float INV_SCALE = 1.f/SCALE;
    public final static float VP_WIDTH = 1280 * INV_SCALE;
    public final static float VP_HEIGHT = 720 * INV_SCALE;

    protected OrthographicCamera camera;
    protected ExtendViewport viewport;

    protected FadeDialog dialog;
    private FrameBuffer fbo;
    public UIFBOFadeTest (GameReset game) {
        super(game);
        clear.set(Color.DARK_GRAY);
        FadeRenderer.init();

        viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera = new OrthographicCamera());

//        VisWindow.FADE_TIME = 2.0f;

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
        FadeLabel dummyText = new FadeLabel("Dummy text");
        dummyText.setAlignment(Align.bottomLeft);
        dialog.getContentTable().add(dummyText).size(200).pad(20);
//        for (int i = 0; i < 20 + MathUtils.random(10); i++) {
//            if (MathUtils.randomBoolean()) {
//                dialog.getContentTable().add(new FadeLabel("Dummy text " + MathUtils.random(0, 200)));
//            } else {
//                dialog.getContentTable().add(new VisSlider(0, 5 + MathUtils.random(5.f), 1, false));
//            }
//            if (i > 0 && i % 2 == 0) {
//                dialog.getContentTable().row();
//            }
//        }
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
        stage.setViewport(viewport);
//        dialog.setColor(1, 1, 1, 0);
//        dialog.show(stage, forever(
//            sequence(Actions.fadeIn(2),
//            sequence(Actions.fadeOut(2)))
//        ));
    }

    @Override public void resize (int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
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
        static ScreenViewport viewport;
        static OrthographicCamera camera;
        static TextureRegion region;
        static Matrix4 projection;
        static ShapeRenderer shapeRenderer;
        static final int size = 2048;
        static Vector2 v2 = new Vector2();

        public static void init () {
            projection = new Matrix4();
            viewport = new ScreenViewport(camera = new OrthographicCamera());
            shapeRenderer = new ShapeRenderer();
            resize(size, size);
        }

        static void resize (int width, int height) {
            if (fbo != null) fbo.dispose();
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            region = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, width, height);
            region.flip(false, true);
        }

        static boolean inFbo = false;
        public static void draw (Actor actor, Batch batch, float parentAlpha) {
            if (!(actor instanceof FadeActor)) {
                Gdx.app.log(TAG, "Invalid actor " + actor);
                return;
            }
            FadeActor fa = (FadeActor)actor;

            Color color = actor.getColor();
            // we need to use custom path only when semi transparent
            // stuff gets kinda broken when this is nested, so lets not do that
            if (!inFbo && !MathUtils.isEqual(color.a * parentAlpha, 1.0f)) {
                // we probably cant do a whole lot about ending the batch, we need to do it so stuff in fbo is what we expect
                // cant defer either, as we need this to be in order in case stuff overlaps
                batch.end();

                final int margin = 2;
                final int width = MathUtils.round(actor.getWidth()) + margin * 2;
                final int height = MathUtils.round(actor.getHeight()) + margin * 2;

                inFbo = true;
                fbo.begin();
                viewport.update(width, height, true);

                float x = actor.getX();
                float y = actor.getY();


                Viewport prevViewport = actor.getStage().getViewport();
                actor.getStage().setViewport(viewport);

                actor.localToStageCoordinates(v2.set(0, 0));

                // should be 0, 0, 0, 0 for non test
                Gdx.gl.glClearColor(.5f, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                if (false) {
                    shapeRenderer.setProjectionMatrix(camera.combined);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    {
                        shapeRenderer.setColor(Color.CYAN);
                        if (false) {
                            int size = 16;
                            for (int gx = -100; gx < 100; gx++) {
                                for (int gy = -100; gy < 100; gy++) {
                                    if (gx % 2 == 0 && gy % 2 != 0)
                                        continue;
                                    if (gx % 2 != 0 && gy % 2 == 0)
                                        continue;
                                    shapeRenderer.rect(gx * size, gy * size, size, size);
                                }
                            }
                        }
                        shapeRenderer.setColor(Color.YELLOW);
                        shapeRenderer.rect(0, 0, 16, 16);
                    }
                    shapeRenderer.end();
                }

                projection.set(batch.getProjectionMatrix());
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                float a = color.a;
                color.a = 1;

                actor.setPosition(margin - v2.x + x,  margin - v2.y + y);
                fa.fadeDraw(batch, 1);

                actor.setPosition(x, y);
                actor.getStage().setViewport(prevViewport);

                batch.end();
                color.a = a;
                // reset the color just in case
                batch.setColor(Color.WHITE);

                fbo.end();
                inFbo = false;
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
            setClip(false);
        }

        @Override public void draw (Batch batch, float parentAlpha) {
            // this is annoying
            FadeRenderer.draw(this, batch, parentAlpha);
        }

        @Override public void fadeDraw (Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
        }
    }

    static class FadeLabel extends VisLabel implements FadeActor {
        public FadeLabel (String title) {
            super(title);
            getColor().a = .5f;
            ;
            LabelStyle labelStyle = new LabelStyle(getStyle());
            labelStyle.background = VisUI.getSkin().getDrawable("textfield");
            setStyle(labelStyle);
            setFontScale(1.5f);
            addAction(Actions.forever(
                Actions.sequence(
                    Actions.fadeOut(2f),
                    Actions.fadeIn(2f),
                    Actions.delay(1)
                )
            ));
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

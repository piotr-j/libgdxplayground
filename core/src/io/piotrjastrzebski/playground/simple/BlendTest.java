package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class BlendTest extends BaseScreen {
    private static final String TAG = BlendTest.class.getSimpleName();

    private TextureRegion ball;
    private TextureRegion bg;
    private FrameBuffer fbo;
    private TextureRegion fboReg;
    private static Array<Blend> blends;

    public BlendTest (GameReset game) {
        super(game);
        ball = new TextureRegion(new Texture("blend/crystalball.png"));
        bg = new TextureRegion(new Texture("blend/flamingobg.jpg"));
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, bg.getRegionWidth(), bg.getRegionHeight(), false);
        fboReg = new TextureRegion(fbo.getColorBufferTexture());
        fboReg.flip(false, true);

        blendedBg = new Blended(bg);
        blendedBall = new Blended(ball);

        blends = new Array<>();
        blends.add(new Blend(GL20.GL_ONE, "GL_ONE"));
        blends.add(new Blend(GL20.GL_ZERO, "GL_ZERO"));
        blends.add(new Blend(GL20.GL_SRC_COLOR, "GL_SRC_COLOR"));
        blends.add(new Blend(GL20.GL_ONE_MINUS_SRC_COLOR, "GL_ONE_MINUS_SRC_COLOR"));
        blends.add(new Blend(GL20.GL_SRC_ALPHA, "GL_SRC_ALPHA"));
        blends.add(new Blend(GL20.GL_ONE_MINUS_SRC_ALPHA, "GL_ONE_MINUS_SRC_ALPHA"));
        blends.add(new Blend(GL20.GL_DST_ALPHA, "GL_DST_ALPHA"));
        blends.add(new Blend(GL20.GL_ONE_MINUS_DST_ALPHA, "GL_ONE_MINUS_DST_ALPHA"));
        blends.add(new Blend(GL20.GL_DST_COLOR, "GL_DST_COLOR"));
        blends.add(new Blend(GL20.GL_ONE_MINUS_DST_COLOR, "GL_ONE_MINUS_DST_COLOR"));

        VisWindow window = new VisWindow("Settings");
        window.setPosition(0, 0);
        {
            final VisCheckBox checkBox = new VisCheckBox("Bg first", bgFirst);
            checkBox.addListener(new ChangeListener() {
                @Override public void changed (ChangeEvent event, Actor actor) {
                    bgFirst = checkBox.isChecked();
                }
            });
            window.add(checkBox).row();
        }

        window.add(createGUI("Background", blendedBg)).row();
        window.add(createGUI("Ball", blendedBall)).row();
        window.pack();
        root.addActor(window);
    }

    private Actor createGUI (String name, final Blended blended) {
        VisTable content = new VisTable(true);
        content.add(new VisLabel(name)).colspan(2).row();
        {
            content.add(new VisLabel("SrcFunc", "small")).right();
            final VisSelectBox<Blend> sb = new VisSelectBox<>();
            sb.setItems(blends);
            sb.setSelected(blend(blended.srcFunc));
            sb.addListener(new ChangeListener() {
                @Override public void changed (ChangeEvent event, Actor actor) {
                    blended.srcFunc = sb.getSelected().func;
                }
            });
            content.add(sb).row();
        }
        {
            content.add(new VisLabel("DstFunc", "small")).right();
            final VisSelectBox<Blend> sb = new VisSelectBox<>();
            sb.setItems(blends);
            sb.setSelected(blend(blended.dstFunc));
            sb.addListener(new ChangeListener() {
                @Override public void changed (ChangeEvent event, Actor actor) {
                    blended.dstFunc = sb.getSelected().func;
                }
            });
            content.add(sb).row();
        }
        final VisSelectBox<Blend> sbSrcAlpha = new VisSelectBox<>();
        sbSrcAlpha.setDisabled(!blended.separate);
        final VisSelectBox<Blend> sbDstAlpha = new VisSelectBox<>();
        sbDstAlpha.setDisabled(!blended.separate);
        final VisCheckBox separateCB = new VisCheckBox("Separate?", blended.separate);
        content.add(separateCB).colspan(2).row();
        separateCB.addListener(new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {
                blended.separate = separateCB.isChecked();
                sbSrcAlpha.setDisabled(!blended.separate);
                sbDstAlpha.setDisabled(!blended.separate);
            }
        });
        {
            content.add(new VisLabel("SrcAlphaFunc", "small"));
            sbSrcAlpha.setItems(blends);
            sbSrcAlpha.setSelected(blend(blended.srcFuncAlpha));
            sbSrcAlpha.addListener(new ChangeListener() {
                @Override public void changed (ChangeEvent event, Actor actor) {
                    blended.srcFuncAlpha = sbSrcAlpha.getSelected().func;
                }
            });
            content.add(sbSrcAlpha).row();
        }
        {
            content.add(new VisLabel("DstAlphaFunc", "small"));
            sbDstAlpha.setItems(blends);
            sbDstAlpha.setSelected(blend(blended.dstFuncAlpha));
            sbDstAlpha.addListener(new ChangeListener() {
                @Override public void changed (ChangeEvent event, Actor actor) {
                    blended.dstFuncAlpha = sbDstAlpha.getSelected().func;
                }
            });
            content.add(sbDstAlpha).row();
        }
        return content;
    }

    private Blend blend (int func) {
        for (Blend blend : blends) {
            if (blend.func == func) return blend;
        }
        return null;
    }

    boolean bgFirst = true;
    Blended blendedBg;
    Blended blendedBall;

    @Override public void render (float delta) {
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());


        if (bgFirst) {
            blendedBg.draw(batch);
            blendedBall.draw(batch);
        } else {
            blendedBall.draw(batch);
            blendedBg.draw(batch);
        }
        fbo.end();

        batch.setProjectionMatrix(guiCamera.combined);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(guiCamera.combined);
        batch.begin();
        batch.draw(fboReg, (Gdx.graphics.getWidth() - fboReg.getRegionWidth()) * .5f,
            (Gdx.graphics.getHeight() - fboReg.getRegionHeight()) * .5f);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private static class Blended {
        boolean separate = false;
        int srcFunc = GL20.GL_SRC_ALPHA;
        int dstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
        int srcFuncAlpha = GL20.GL_SRC_ALPHA;
        int dstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
        float alpha = 1;
        private TextureRegion region;

        public Blended (TextureRegion region) {
            this.region = region;
        }

        public void draw (SpriteBatch batch) {
            if (separate) {
                batch.setBlendFunctionSeparate(srcFunc, dstFunc, srcFuncAlpha, dstFuncAlpha);
            } else {
                batch.setBlendFunction(srcFunc, dstFunc);
            }
            batch.begin();
            batch.setColor(1, 1, 1, alpha);
            batch.draw(region, 0, 0);
            batch.end();
        }
    }

    private static class Blend {
        final int func;
        final String name;

        public Blend (int func, String name) {
            this.func = func;
            this.name = name;
        }

        @Override public String toString () {
            return name;
        }

        @Override public boolean equals (Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Blend blend = (Blend)o;

            return func == blend.func;
        }

        @Override public int hashCode () {
            return func;
        }
    }

    @Override public void dispose () {
        super.dispose();
        ball.getTexture().dispose();
        bg.getTexture().dispose();
        fbo.dispose();
    }

    public static void main (String[] args) {
        PlaygroundGame.start(args, BlendTest.class);
    }
}

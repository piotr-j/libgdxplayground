package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Dim screen excluding certain rects
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class Dimmer2Test extends BaseScreen {
    private static final String TAG = Dimmer2Test.class.getSimpleName();
    Texture rawDim;
    NinePatch inner;
    TextureRegion white;
    SnapshotArray<Image> images = new SnapshotArray<>();
    Rectangle target = new Rectangle();
    FrameBuffer fbo;
    TextureRegion fboRegion;
    Color fboColor = new Color(1, 1, 1, 0);
    public Dimmer2Test (GameReset game) {
        super(game);
        // cba to pack this...
        rawDim = new Texture("dim.9.png");
        inner = new NinePatch(
            new TextureRegion(rawDim, 2, 2, 58, 58), // BOTTOM_LEFT
            new TextureRegion(rawDim, 60, 2, 8, 58), // BOTTOM_CENTER
            new TextureRegion(rawDim, 68, 2, 58, 58), // BOTTOM_RIGHT
            new TextureRegion(rawDim, 2, 60, 58, 8), // MIDDLE_LEFT
            new TextureRegion(rawDim, 60, 60, 8, 8), // MIDDLE_CENTER
            new TextureRegion(rawDim, 68, 60, 58, 8), // MIDDLE_RIGHT
            new TextureRegion(rawDim, 2, 68, 58, 58), // TOP_LEFT
            new TextureRegion(rawDim, 60, 68, 8, 58), // TOP_CENTER
            new TextureRegion(rawDim, 68, 68, 58, 58) // TOP_RIGHT
            );
        white = new TextureRegion(new Texture("white.png"));
        clear.set(.25f, .25f, .25f, 1);

        // TODO handle resize etc
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);

        {
            Table buttons = new Table();
            for (int i = 0; i < 10; i++) {
                buttons.add(new VisTextButton("Button " + i)).pad(10);
                if (i > 0 && i % 2 == 1) {
                    buttons.row();
                }
            }
            buttons.row();
            VisTextArea textArea = new VisTextArea();
            textArea.setText("ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF! ASDF!");
            textArea.setSelection(0, 100);
            buttons.add(textArea).colspan(2).expand().fill().height(200);
            root.add(buttons).expand();
        }
    }

    float timer = 0;
    float dimAlpha = 0;
    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();
        fboColor.r = 0f;
        fboColor.g = 0f;
        fboColor.b = 0f;
        fboColor.a = .5f;

        if (images.size > 0) {
            timer += delta * 90;
            dimAlpha += delta * 4;
            if (dimAlpha > 1) dimAlpha = 1;
            batch.enableBlending();
            // we need special drawing for these, so we wont add them to the stage
            fbo.begin();
            Gdx.gl.glClearColor(0, 0, 0, dimAlpha);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            batch.setBlendFunction(GL20.GL_ZERO , GL20.GL_SRC_ALPHA);
            batch.begin();
            Object[] begin = images.begin();
            for (Object o : begin) {
                Image patch = (Image)o;
                if (patch == null)
                    continue;
                patch.act(delta);
                patch.draw(batch, 1);
            }
            images.end();
            batch.end();
            fbo.end();
            fboColor.a = .5f * dimAlpha;
            batch.setBlendFunction(GL20.GL_DST_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.begin();
            batch.setColor(fboColor);
            batch.draw(fboRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            if (dimAlpha > 0) {
                batch.enableBlending();
                dimAlpha -= delta * 4;
                if (dimAlpha < 0) dimAlpha = 0;
                fboColor.a = .5f * dimAlpha;
                batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
                batch.begin();
                batch.setColor(fboColor);
                batch.draw(fboRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                batch.end();
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            images.clear();
        }

    }

    @Override public void dispose () {
        super.dispose();
        rawDim.dispose();
        white.getTexture().dispose();
    }

    Vector2 v2 = new Vector2();
    @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        guiViewport.unproject(v2.set(screenX, screenY));
        final Image patch = new Image(inner);
        patch.setSize(128, 128);
        patch.setPosition(v2.x, v2.y, Align.center);
        patch.setAlign(Align.center);
        if (button == Input.Buttons.LEFT) {
            patch.addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.sizeTo(256, 256, 2, Interpolation.fade),
                        Actions.moveToAligned(v2.x, v2.y, Align.center, 2, Interpolation.linear)),
                    Actions.delay(2),
                    Actions.parallel(
                        Actions.sizeTo(128, 128, 2, Interpolation.fade),
                        Actions.moveToAligned(v2.x, v2.y, Align.center, 2, Interpolation.linear)),
                    Actions.run(new Runnable() {
                        @Override public void run () {
                            images.removeValue(patch, true);
                        }
                    })));
        } else {
            patch.setSize(196, 196);
        }
        images.add(patch);
        return super.touchDown(screenX, screenY, pointer, button);
    }
    public static void main (String[] args) {
		PlaygroundGame.start(args, Dimmer2Test.class);
	}
}

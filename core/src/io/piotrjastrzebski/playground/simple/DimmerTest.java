package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Dim screen excluding certain rect
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class DimmerTest extends BaseScreen {
    private static final String TAG = DimmerTest.class.getSimpleName();
    Texture rawDim;
    NinePatch inner;
    Image centerImage;
    TextureRegion white;
    Rectangle target = new Rectangle();
    public DimmerTest (GameReset game) {
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
        centerImage = new Image(inner) {
            @Override public void draw (Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                float bx = getX();
                float by = getY();
                float tx = getX() + getWidth();
                float ty = getY() + getHeight();
                Group parent = getParent();
                float w = parent.getWidth();
                float h = parent.getHeight();
                // TOP
                if (ty < h)
                    batch.draw(white, 0, ty, w, h-ty);
                // BOTTOM
                if (by > 0)
                    batch.draw(white, 0, 0, w, by);
                // LEFT
                if (bx > 0)
                    batch.draw(white, 0, by, bx, ty-by);
                // RIGHT
                if (tx < w)
                    batch.draw(white, tx, by, w - tx, ty-by);
            }
        };
        centerImage.setColor(.25f, .25f, .25f, .75f);
        white = new TextureRegion(new Texture("white.png"));
        clear.set(.5f, 0, .5f, 1);
        target.set(100, 100, 200, 200);
        centerImage.setPosition(target.x, target.y);
        centerImage.setSize(target.width, target.height);
        root.addActor(centerImage);
    }

    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.A)) {
            float w = MathUtils.clamp(centerImage.getWidth() - 25, 100, 500);
            centerImage.clearActions();
            centerImage.addAction(Actions.sizeTo(w, centerImage.getHeight(), .5f, Interpolation.sine));
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D)) {
            float w = MathUtils.clamp(centerImage.getWidth() + 25, 100, 500);
            centerImage.clearActions();
            centerImage.addAction(Actions.sizeTo(w, centerImage.getHeight(), .5f, Interpolation.sine));
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.S)) {
            float h = MathUtils.clamp(centerImage.getHeight() - 25, 100, 500);
            centerImage.clearActions();
            centerImage.addAction(Actions.sizeTo(centerImage.getWidth(), h, .5f, Interpolation.sine));
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W)) {
            float h = MathUtils.clamp(centerImage.getHeight() + 25, 100, 500);
            centerImage.clearActions();
            centerImage.addAction(Actions.sizeTo(centerImage.getWidth(), h, .5f, Interpolation.sine));
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.Q)) {
            centerImage.addAction(Actions.color(new Color(.25f, .25f, .25f, 0), .5f, Interpolation.fade));
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.E)) {
            centerImage.addAction(Actions.color(new Color(.25f, .25f, .25f, .75f), .5f, Interpolation.fade));
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
        centerImage.clearActions();
        centerImage.addAction(Actions.moveToAligned(v2.x, v2.y, Align.center, 1f, Interpolation.sine));
        return super.touchDown(screenX, screenY, pointer, button);
    }

    public static void main (String[] args) {
		PlaygroundGame.start(args, DimmerTest.class);
	}
}

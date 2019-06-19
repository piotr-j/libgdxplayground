package io.piotrjastrzebski.playground.masking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 */
public class MaskTest3 extends BaseScreen {
    Texture patternA;
    Texture patternB;
    Texture patternC;
    Texture patternD;
    Texture patternE;

    Texture bg;
    Sprite bgSprite;

    public MaskTest3 (GameReset game) {
        super(game);
        bg = new Texture("masking/bg.png");
        bgSprite = new Sprite(bg);
        bgSprite.setBounds(-bg.getWidth() * .1f * .5f, -bg.getHeight() * .1f * .5f, bg.getWidth() * .1f, bg.getHeight() * .1f);
        bgSprite.setOriginCenter();
        bgSprite.setColor(Color.ORANGE);
        patternA = new Texture("masking/pattern-a.png");
        patternA.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        patternB = new Texture("masking/pattern-b.png");
        patternB.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        patternC = new Texture("masking/pattern-c.png");
        patternC.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        patternD = new Texture("masking/pattern-d.png");
        patternD.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        patternE = new Texture("masking/pattern-e.png");
        patternE.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pattern = new TextureRegion(patternA);

        clear.set(Color.GRAY);

    }

    float rotation = 0;
    float scale = 1;
    float patternRot = 0;
    float scaleDir = 1;
    TextureRegion pattern;
    public void render (float delta) {
        clear.set(Color.MAGENTA);
        super.render(delta);

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        {

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                pattern = new TextureRegion(patternA);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                pattern = new TextureRegion(patternB);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                pattern = new TextureRegion(patternC);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
                pattern = new TextureRegion(patternD);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
                pattern = new TextureRegion(patternE);
            }
            bgSprite.draw(batch);


            rotation += 15 * delta;
            // draw this to fbo with mask shader and we are golden :o
            drawTiled(batch, pattern, 0, 0, 0, 0, .2f, .2f, rotation);

        }
        batch.end();
    }

    Matrix4 oldTransform = new Matrix4();
    private void drawTiled (SpriteBatch batch, TextureRegion region, float x, float y, float ox, float oy, float sx, float sy,
        float rot) {
        oldTransform.set(batch.getTransformMatrix());

        // not cool
        Affine2 worldTransform = new Affine2();
        worldTransform.setToTrnRotScl(x + ox, y + oy, rot, sx, sy);
        if (ox != 0 || oy != 0) worldTransform.translate(-ox, -oy);
        batch.setTransformMatrix(batch.getTransformMatrix().set(worldTransform));

        float w = region.getRegionWidth() * .1f;
        float h = region.getRegionHeight() * .1f;
        for (int tx = 0, nx = 20; tx < nx; tx++) {
            for (int ty = 0, ny = 20; ty < ny; ty++) {
                batch.draw(region,
                    tx * w + x - w * nx * .5f,
                    ty * h + y - h * ny * .5f,
                    0, 0,
                    w, h,
                    1, 1,
                    0);
            }
        }

        batch.getTransformMatrix().set(oldTransform);
    }

    @Override
    public void dispose () {
        super.dispose();
        bg.dispose();
        patternA.dispose();
        patternB.dispose();
        patternC.dispose();
        patternD.dispose();
    }

    public static void main (String[] args) {
        PlaygroundGame.start(args, MaskTest3.class);
    }

}

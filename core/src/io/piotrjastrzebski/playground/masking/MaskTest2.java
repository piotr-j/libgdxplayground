package io.piotrjastrzebski.playground.masking;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Based on https://github.com/mattdesl/lwjgl-basics/wiki/LibGDX-Masking
 *
 * https://gist.github.com/mattdesl/6076846
 */
public class MaskTest2 extends BaseScreen {
    Texture bg, sprite, alphaMask;

    public MaskTest2 (GameReset game) {
        super(game);
        bg = new Texture("badlogic.jpg");
        sprite = new Texture("masking/grass.png");
        alphaMask = new Texture("masking/mask.png");
        alphaMask.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Gdx.input.setInputProcessor(this);
    }

    private void drawBackground(SpriteBatch batch) {
        //regular blending mode
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


        //... draw background entities/tiles here ...

//        batch.setColor(Color.GRAY);
        float size = 5f;
        for (int x = -3; x < 3; x++) {
            for (int y = -2; y < 2; y++) {
//                batch.draw(bg, x * size, y * size, size, size);
            }
        }


        //flush the batch to the GPU
        batch.flush();
    }

    private void drawAlphaMask(SpriteBatch batch, float x, float y, float width, float height) {
        //disable RGB color, only enable ALPHA to the frame buffer
        Gdx.gl.glColorMask(false, false, false, true);

        //change the blending function for our alpha map
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);

        //draw alpha mask sprite(s)
        batch.draw(alphaMask, x, y, width, height);

        //flush the batch to the GPU
        batch.flush();
    }

    Vector3 vp = new Vector3();
    private void drawForeground(SpriteBatch batch, float x, float y, float width, float height) {
        //now that the buffer has our alpha, we simply draw the sprite with the mask applied
        Gdx.gl.glColorMask(true, true, true, true);
        batch.setBlendFunction(GL20.GL_DST_ALPHA, GL20.GL_ONE_MINUS_DST_ALPHA);

        //The scissor test is optional, but it depends
        // on what exactly?
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        gameViewport.project(vp.set(x, y, 0));
        int clipX = MathUtils.floor(vp.x);
        int clipY = MathUtils.floor(vp.y);
        gameViewport.project(vp.set(x + width, y + height, 0));
        int clipWidth = MathUtils.ceil(vp.x) - clipX;
        int clipHeight = MathUtils.ceil(vp.y) - clipY;
        Gdx.gl.glScissor(clipX, clipY, clipWidth, clipHeight);

        //draw our sprite to be masked
        batch.draw(sprite, x, y, width, height);

        //remember to flush before changing GL states again
        batch.flush();

        //disable scissor before continuing
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }


    FPSLogger fpsLogger = new FPSLogger();
    public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();


        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();

        //draw background
//        drawBackground(batch);


        float size = 5;
        drawAlphaMask(batch, cs.x - size/2, cs.y - size/2, size, size);
        drawForeground(batch, cs.x - size/2, cs.y - size/2, size, size);
//        batch.draw(sprite, cs.x - size/2, cs.y - size/2, size, size);

        batch.end();

        fpsLogger.log();
    }

    @Override
    public void dispose () {
        super.dispose();
        alphaMask.dispose();
        sprite.dispose();
        bg.dispose();
    }

    public static void main (String[] args) {
        PlaygroundGame.start(args, MaskTest2.class);
    }

}

package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class LineTest extends BaseScreen {
    FrameBuffer fbo;
    TextureRegion fboRegion;
    Array<Sprite> sprites = new Array<>();
    public LineTest (GameReset game) {
        super(game);
        clear.set(Color.WHITE);
        {
            Pixmap pixmap = new Pixmap(1, 100, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.CLEAR);
            pixmap.fill();
            pixmap.setColor(Color.BLACK);
            pixmap.fillRectangle(0, 0, 1, 100);
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Sprite sprite = new Sprite(texture);
            sprite.setOriginBasedPosition(-3.5f, 0);
            sprite.setScale(INV_SCALE * 2);
            sprite.setOriginCenter();
            pixmap.dispose();
            sprites.add(sprite);
        }
        {
            Pixmap pixmap = new Pixmap(3, 102, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.CLEAR);
            pixmap.fill();
            pixmap.setColor(Color.BLACK);
            pixmap.fillRectangle(1, 1, 1, 100);
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Sprite sprite = new Sprite(texture);
            sprite.setOriginBasedPosition(3.5f, 0);
            sprite.setScale(INV_SCALE * 2);
            pixmap.dispose();
            sprites.add(sprite);
        }
        resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }


    boolean rotate = false;
    boolean useFbo = false;
    float rotation = 22.5f;
    float timer = 0;
    @Override public void render (float delta) {
        super.render(delta);
        enableBlending();
        timer += delta;

        // 2x scale seems to work the best
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            fboScale(1);
            PLog.log("fbo scale = " + fboScale);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            fboScale(2);
            PLog.log("fbo scale = " + fboScale);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            fboScale(4);
            PLog.log("fbo scale = " + fboScale);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            fboScale(8);
            PLog.log("fbo scale = " + fboScale);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            fboScale(16);
            PLog.log("fbo scale = " + fboScale);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            rotate = !rotate;
        }


        if (rotate) {
            rotation += delta * 45f;
        }
        if (useFbo) {
//            gameViewport.update(fbo.getWidth(), fbo.getHeight(), false);
            fbo.begin();
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            renderer.setProjectionMatrix(gameCamera.combined);
            batch.setProjectionMatrix(gameCamera.combined);
            draw();
            fbo.end();

//            gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
            batch.setProjectionMatrix(gameCamera.combined);
            batch.setColor(1, 1, 1, 1);
            batch.begin();
            batch.draw(fboRegion,
                gameCamera.position.x - gameCamera.viewportWidth/2 * gameCamera.zoom,
                gameCamera.position.y - gameCamera.viewportHeight/2 * gameCamera.zoom,
                gameCamera.viewportWidth * gameCamera.zoom, gameCamera.viewportHeight * gameCamera.zoom);
            batch.end();
        } else {
            renderer.setProjectionMatrix(gameCamera.combined);
            batch.setProjectionMatrix(gameCamera.combined);
            draw();
        }

    }

    private void fboScale (int scale) {
        if (fboScale != scale) {
            fboScale = scale;
            useFbo = true;
        } else {
            useFbo = !useFbo;
        }
    }

    private void draw () {
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.MAGENTA);
        renderer.circle(-3.5f, 0, .1f, 16);
        renderer.circle(3.5f, 0, .1f, 16);
        renderer.rect(-.5f, -.5f, .5f, .5f, 1, 1, 1, 1, 45);
        renderer.rect(-.5f, -2.5f, .5f, .5f, 1, 1, 1, 1, 22.5f);
        renderer.rect(-.5f, 1.5f, .5f, .5f, 1, 1, 1, 1, 67.5f);
        renderer.end();

        batch.enableBlending();
        batch.begin();
        for (Sprite sprite : sprites) {
            sprite.setRotation(rotation);
            sprite.draw(batch);
        }
        batch.end();
    }

    @Override public void resize (int width, int height) {
        super.resize(width, height);
        resizeFBO(width, height);
    }

    int fboScale = 1;
    private void resizeFBO (int width, int height) {
        // screen size fbo
        if (fbo == null || (fbo.getWidth() != width * fboScale && fbo.getHeight() != height * fboScale)) {
            if (fbo != null) {
                fbo.dispose();
            }
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width * fboScale, height * fboScale, false);
            fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            fboRegion = new TextureRegion(fbo.getColorBufferTexture());
            fboRegion.flip(false, true);
        }
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        PlaygroundGame.start(args, LineTest.class);
    }
}

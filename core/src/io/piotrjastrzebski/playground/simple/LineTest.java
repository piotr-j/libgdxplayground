package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
            sprite.setOriginBasedPosition(-4.5f, -4);
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
            sprite.setOriginBasedPosition(4.5f, -4);
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
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            fboScale(2);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            fboScale(4);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            fboScale(8);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            fboScale(16);
            resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            rotate = !rotate;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + .1f, .5f, 2f);
            gameCamera.update();
            PLog.log("zoom " + gameCamera.zoom);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            gameCamera.zoom = MathUtils.clamp(gameCamera.zoom - .1f, .5f, 2f);
            gameCamera.update();
            PLog.log("zoom " + gameCamera.zoom);
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
        PLog.log("fbo scale = " + fboScale + ", use fbo " + useFbo);
    }

    Vector2 v2 = new Vector2();
    private void draw () {
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.MAGENTA);
        renderer.circle(-3.5f, 0, .1f, 16);
        renderer.circle(3.5f, 0, .1f, 16);
        renderer.rect(-.5f, -.5f, .5f, .5f, 1, 1, 1, 1, 45);
        renderer.rect(-.5f, -2.5f, .5f, .5f, 1, 1, 1, 1, 22.5f);
        renderer.rect(-.5f, 1.5f, .5f, .5f, 1, 1, 1, 1, 67.5f);
        renderer.setColor(Color.BLACK);
        v2.set(0, 3.33f).rotateDeg(rotation);
        renderer.rectLine(-4.5f - v2.x, 4 - v2.y, -4.5f + v2.x, 4 + v2.y, .075f);
        renderer.end();
        Gdx.gl.glLineWidth(4 * (useFbo?fboScale:1));
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.line(4.5f - v2.x, 4 - v2.y, 4.5f + v2.x, 4 + v2.y);
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
        Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
//        config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 8);
        PlaygroundGame.start(args, config, LineTest.class);
    }
}

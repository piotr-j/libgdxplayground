package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class SubpixelTest extends BaseScreen {
    private static final String TAG = SubpixelTest.class.getSimpleName();

    private Texture texture;
    private TextureRegion region;
    private Texture texture2;
    private TextureRegion region2;

    public SubpixelTest (GameReset game) {
        super(game);
        position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

        int scale = 4;
        Pixmap pixmap = new Pixmap(64 * scale, 64 * scale, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        // transparent border is essential for sub pixel rendering to work
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fillRectangle(1 * scale, 1 * scale, 62 * scale, 62 * scale);
        texture = new Texture(pixmap, true);
        // linear filtering is essential for sub pixel rendering to work
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        region = new TextureRegion(texture);
        region.flip(false, true);

        pixmap = new Pixmap(64 * scale, 64 * scale, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(Color.CHARTREUSE);
        pixmap.fillCircle(32 * scale, 32 * scale, 30 * scale);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fillCircle(32 * scale, 32 * scale, 15 * scale);

        texture2 = new Texture(pixmap, true);
        // linear filtering is essential for sub pixel rendering to work
        texture2.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        region2 = new TextureRegion(texture2);
        region2.flip(false, true);
    }

    private Vector2 position = new Vector2();
    private float moveSpeed = .05f;

    @Override public void render (float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        processInput(delta);
        renderer.setProjectionMatrix(gameCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.DARK_GRAY);
        for (int x = 0; x < VP_WIDTH; x++) {
            renderer.line(x, 0, x, VP_HEIGHT);
        }
        for (int y = 0; y < VP_HEIGHT; y++) {
            renderer.line(0, y, VP_WIDTH, y);
        }
        renderer.end();

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        // seems that if stuff ends up larger then source, it breaks, weird
        // top rect, texture based
        batch.draw(region, position.x, position.y + .5f, 1, 1, 2, 2, 1, 1, 45);
        // top circle, texture based
        batch.draw(region2, position.x, position.y + 4.5f, 2, 2);
        batch.end();

        // bottom rect, shape based
        renderer.setProjectionMatrix(gameCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.WHITE);
        renderer.rect(position.x, position.y - 3.5f, 1, 1, 2, 2, 1, 1, 45);
        renderer.end();
    }

    private void processInput (float delta) {
        float scale = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            scale *= 5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            position.y += moveSpeed * delta * scale;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            position.y -= moveSpeed * delta * scale;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            position.x -= moveSpeed * delta * scale;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            position.x += moveSpeed * delta * scale;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Gdx.app.log(TAG, "Reset");
            position.set(VP_WIDTH / 2, VP_HEIGHT / 2);
        }
    }

    @Override public void resize (int width, int height) {
        super.resize(width, height);
        gameViewport.update(width, height, true);
        position.set(VP_WIDTH / 2, VP_HEIGHT / 2);
    }

    @Override public void dispose () {
        super.dispose();
        texture.dispose();
        texture2.dispose();
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1280/2;
        config.height = 720/2;
        config.useHDPI = true;
        config.stencil = 8;
        // for mesh based sub pixel rendering multi sampling is required or post process aa
        config.samples = 4;
        PlaygroundGame.start(args, config, SubpixelTest.class);
    }
}

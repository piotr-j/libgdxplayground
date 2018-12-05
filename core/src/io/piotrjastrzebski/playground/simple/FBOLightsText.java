package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class FBOLightsText extends BaseScreen {
    public final static float SCALE = 32f;
    public final static float INV_SCALE = 1.f/SCALE;
    public final static float VP_WIDTH = 1280 * INV_SCALE;
    public final static float VP_HEIGHT = 720 * INV_SCALE;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Array<Vector2> lights = new Array<>();

    private FrameBuffer fbo;
    private TextureRegion fbRegion;
    public FBOLightsText (GameReset game) {
        super(game);
        gameCamera = new OrthographicCamera();
        gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);

        TiledMap map = new TmxMapLoader().load("tiled/simple.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, INV_SCALE, batch);
        TiledMapTileLayer mapLayer = (TiledMapTileLayer)map.getLayers().get(0);
        float w = mapLayer.getWidth();
        float h = mapLayer.getHeight();
        gameCamera.position.set(w/2f, h/2f, 0);
        gameCamera.update();

        for (int i = 0; i < 100; i++) {
            lights.add(new Vector2(MathUtils.random(w), MathUtils.random(h)));
        }
        resizeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private float camMoveSpeed = 5;
    @Override public void render (float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        processInput(delta);

        fbo.begin();
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        renderer.setProjectionMatrix(gameCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Vector2 light : lights) {
            renderer.circle(light.x, light.y, 1.5f, 16);
            renderer.setColor(Color.WHITE);
        }
        renderer.end();
        fbo.end();

        mapRenderer.setView(gameCamera);
        mapRenderer.render();
        renderer.setProjectionMatrix(gameCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.YELLOW);
        for (Vector2 light : lights) {
            renderer.circle(light.x, light.y, .5f, 16);
        }
        renderer.end();

        batch.enableBlending();
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        batch.setColor(1, 1, 1, .5f);
        batch.draw(fbRegion,
            gameCamera.position.x - gameCamera.viewportWidth/2 * gameCamera.zoom,
            gameCamera.position.y - gameCamera.viewportHeight/2 * gameCamera.zoom,
            gameCamera.viewportWidth * gameCamera.zoom, gameCamera.viewportHeight * gameCamera.zoom);
        batch.end();
    }

    @Override public void resize (int width, int height) {
        super.resize(width, height);
        gameViewport.update(width, height, false);
        resizeFBO(width, height);
    }

    private void resizeFBO (int width, int height) {
        if (fbo == null || (fbo.getWidth() != width && fbo.getHeight() != height)) {
            if (fbo != null) {
                fbo.dispose();
            }
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            fbRegion = new TextureRegion(fbo.getColorBufferTexture());
            fbRegion.flip(false, true);
        }
    }

    private void processInput (float delta) {
        float scale = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            scale = 5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            gameCamera.position.y += camMoveSpeed * delta * scale;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            gameCamera.position.y -= camMoveSpeed * delta * scale;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            gameCamera.position.x -= camMoveSpeed * delta * scale;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            gameCamera.position.x += camMoveSpeed * delta * scale;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom*0.01f, 0.1f, 3f);
        } else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            gameCamera.zoom = MathUtils.clamp(gameCamera.zoom - gameCamera.zoom*0.01f, 0.1f, 3f);
        }
        gameCamera.update();
    }

    @Override public boolean scrolled (int amount) {
        gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom*amount*0.1f, 0.1f, 3f);
        return true;
    }

    @Override public void dispose () {
        super.dispose();
        if (fbo != null) {
            fbo.dispose();
        }
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        PlaygroundGame.start(args, FBOLightsText.class);
    }
}

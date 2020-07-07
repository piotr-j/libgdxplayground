package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Shader based masking
 *
 * Created by PiotrJ on 20/06/15.
 */
public class UIShineTest extends BaseScreen {
    protected static final String TAG = UIShineTest.class.getSimpleName();

    TextureAtlas atlas;
    TextureRegion cardRegion;
    TextureRegion shineRegion;
    ShaderProgram shader;
    int count = 6;

    public UIShineTest (GameReset game) {
        super(game);

        clear.set(Color.LIGHT_GRAY);

        PixmapPacker packer = new PixmapPacker(512, 1024, Pixmap.Format.RGBA8888, 2, true);
        {
            Texture cardTexture = new Texture("shine/card.png");
            cardTexture.getTextureData().prepare();
            Pixmap cardPixmap = cardTexture.getTextureData().consumePixmap();

            Texture shineTexture = new Texture("shine/shine.png");
            shineTexture.getTextureData().prepare();
            Pixmap shinePixmap = shineTexture.getTextureData().consumePixmap();

            // pack multiples so we can pick random one, to make sure offsets are correct regardless of position
            for (int i = 0; i <= count; i++) {
                packer.pack("shine" + i, shinePixmap);
                packer.pack("card" + i, cardPixmap);
            }

            cardTexture.dispose();
            shineTexture.dispose();
        }

        atlas = packer.generateTextureAtlas(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
        packer.dispose();
//        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(Gdx.files.internal("shine/shine.vert"), Gdx.files.internal("shine/shine.frag"));
        if (!shader.isCompiled()) {
            PLog.log(shader.getLog());
        }

        init();
    }

    private void init () {
        clear.set(Color.ORANGE);

        cardRegion = atlas.findRegion("card" + MathUtils.random(0, count));
        shineRegion = atlas.findRegion("shine" + MathUtils.random(0, count));


        ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shine/shine.vert"), Gdx.files.internal("shine/shine.frag"));
        if (!shader.isCompiled()) {
            PLog.log(shader.getLog());
        } else {
            this.shader = shader;
        }

        root.clear();

        Table table = new Table();
        table.setTransform(true);
        table.addAction(Actions.forever(
            Actions.parallel(
                Actions.sequence(
                    Actions.scaleTo(1.5f, 1.5f, 1.5f, Interpolation.sine),
                    Actions.scaleTo(.75f, .75f, 1.5f, Interpolation.sine)
                ),
                Actions.sequence(
                    Actions.moveBy(-100, 0, 1, Interpolation.sine),
                    Actions.moveBy(200, 0, 1, Interpolation.sine),
                    Actions.moveBy(-100, 0, 1, Interpolation.sine)
                ),
                Actions.rotateBy(90, 3)
            )
        ));
        Image card = new Image(cardRegion);
        card.setOrigin(Align.center);
        card.setTouchable(Touchable.disabled);
        table.add(card);
        table.pack();
        table.setOrigin(Align.center);

        ShineImage shine = new ShineImage(shineRegion, shader, cardRegion);
        shine.setOrigin(Align.center);
        shine.setTouchable(Touchable.disabled);
        shine.setPosition(0, -shine.getHeight());
        table.addActor(shine);
        shine.addAction(Actions.forever(
            Actions.sequence(
                Actions.moveTo(0, card.getHeight(), 1f, Interpolation.linear),
                Actions.delay(.5f),
                Actions.moveTo(0, -shine.getHeight(), 1f, Interpolation.linear),
                Actions.delay(.5f)
            )
        ));

        root.add(table);
    }

    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();

        float scale = .5f;
        batch.begin();
        // should be one
        Texture texture = atlas.getTextures().first();
        batch.draw(texture, 0, 0, texture.getWidth() * scale, texture.getHeight() * scale);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        renderer.setProjectionMatrix(guiCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        float th = texture.getHeight() * scale;
        renderer.setColor(0, 1, 1, .5f);
        renderer.rect(cardRegion.getRegionX() * scale, th-cardRegion.getRegionY() * scale, cardRegion.getRegionWidth() * scale, cardRegion.getRegionHeight() * -scale);
        renderer.rect(shineRegion.getRegionX() * scale, th-shineRegion.getRegionY() * scale, shineRegion.getRegionWidth() * scale, shineRegion.getRegionHeight() * -scale);
        renderer.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            init();
        }
    }

    private static class ShineImage extends Image {
        private final TextureRegion shineRegion;
        private final ShaderProgram shader;
        private final TextureRegion cardRegion;

        public ShineImage (TextureRegion region, ShaderProgram shader, TextureRegion cardRegion) {
            super(region);
            this.shineRegion = region;
            this.shader = shader;
            this.cardRegion = cardRegion;
        }

        boolean act = true;
        @Override public void act (float delta) {
            if (act) {
                super.act(delta);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                act = !act;
            }
        }

        @Override public void draw (Batch batch, float parentAlpha) {
            batch.setShader(shader);
            // we want to figure out offset from our uv to mask uv
            // this is base offset from out top left to mask top left
            float ox = cardRegion.getU() - shineRegion.getU();
            float oy = cardRegion.getV() - shineRegion.getV();

            // bounds of the mask in unv coords, so we can clip outside
            shader.setUniformf("u_bounds", cardRegion.getU(), cardRegion.getV(), cardRegion.getU2(), cardRegion.getV2());

            float cv = cardRegion.getV2() - cardRegion.getV();
            float sv = shineRegion.getV2() - shineRegion.getV();

            // we assume that we are in same coordinates as the thing we are masking
            float y = getY();
            // total movement in y
            // we assume that actors are same size as regions
            float scy = map(-shineRegion.getRegionHeight(), cardRegion.getRegionHeight(), y,  cv, -sv);

            // amount we need to scroll the mask
            shader.setUniformf("u_scroll", ox, oy + scy);

            super.draw(batch, parentAlpha);
            batch.setShader(null);
//            debug();
        }
    }

    static float map(float is, float ie, float input, float os, float oe) {
        float slope = (oe - os) / (ie - is);
        return os + slope * (input - is);
    }

    boolean dragging;
    Vector2 pos = new Vector2();
    @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            dragging = true;
            guiViewport.unproject(pos.set(screenX, screenY));
        }
        return true;
    }

    @Override public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (dragging) {
            guiViewport.unproject(pos.set(screenX, screenY));
        }
        return true;
    }

    @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            dragging = false;
            guiViewport.unproject(pos.set(screenX, screenY));
        }
        return true;
    }

    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
        config.setWindowedMode(1280, 720 + 200);
        PlaygroundGame.start(args, config, UIShineTest.class);
    }
}

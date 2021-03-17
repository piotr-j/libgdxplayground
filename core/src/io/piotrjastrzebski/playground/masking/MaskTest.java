package io.piotrjastrzebski.playground.masking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 */
public class MaskTest extends BaseScreen {
    Texture bg;
    Texture patternA;
    Texture patternB;
    Texture patternC;
    Texture patternD;
    Texture patternE;
    ShaderProgram shaderDefault;
    ShaderProgram shaderMask;

    Sprite bgSprite;

    public MaskTest (GameReset game) {
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
        pattern = patternA;

        clear.set(Color.GRAY);

        shaderDefault = new ShaderProgram(Gdx.files.internal("masking/default.vert"), Gdx.files.internal("masking/default.frag"));
        reloadMaskShader();
    }

    private void reloadMaskShader () {
        ShaderProgram.pedantic = false;
        ShaderProgram program = new ShaderProgram(Gdx.files.internal("masking/mask.vert"), Gdx.files.internal("masking/mask.frag"));
        if (program.isCompiled()) {
            if (shaderMask != null) shaderMask.dispose();
            shaderMask = program;
            shaderMask.bind();
            PLog.log("Shader reloaded " + shaderMask.getLog());
        } else {
            PLog.log("Shader failed to compile " + program.getLog());
        }
    }

    float rotation = 0;
    float scale = 1;
    float patternRot = 0;
    float scaleDir = 1;
    Texture pattern;
    public void render (float delta) {
        clear.set(Color.MAGENTA);
        super.render(delta);

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        {

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                pattern = patternA;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                pattern = patternB;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                pattern = patternC;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
                pattern = patternD;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
                pattern = patternE;
            }
            batch.setShader(shaderMask);

            rotation -= 45 * delta;
            patternRot += 15 * delta;

            patternRot = 0;

            scale += scaleDir * delta * .5f;
            if (scale > 1.5f) {
                scaleDir *= -1;
                scale = 1.5f;
            } else if (scale < .75f) {
                scaleDir *= -1;
                scale = .75f;
            }

            if (shaderMask != null) {
                try {
                    shaderMask.setUniformi("u_texture", 0);
                    shaderMask.setUniformi("u_pattern", 1);

                    float bgw = bgSprite.getTexture().getWidth();
                    float bgh = bgSprite.getTexture().getHeight();

                    float pw = pattern.getWidth();
                    float ph = pattern.getHeight();
//                    shaderMask.setUniformf("u_resolution", pw, ph);
                    shaderMask.setUniformf("u_resolution", 1f, 1.f);

                    float patternScale = 1f;
                    float patternAspect = pw / ph;
                    float bgAspect = bgw / bgh;
                    // we need to scale pattern uvs to match src texture, or it will be distorted
                    // we wrap, so we dont need to offset in x,y
//                    float sox = bgSprite.getTexture().getWidth() / (float)pattern.getWidth() * 1/patternScale * 1/patternAspect;
//                    float soy = bgSprite.getTexture().getHeight() / (float)pattern.getHeight() * 1/patternScale;
                    float invPatternScale = 1/patternScale;
                    // with u_aspect = patternAspect
//                    float sox = (bgw / pw) * (ph / pw) * invPatternScale;
//                    float soy = bgh / ph * invPatternScale;

//                    float sox = (bgw / pw) * (ph / pw) * invPatternScale;
                    float sox = (bgw / pw) * invPatternScale;
                    float soy = (bgh / ph) * invPatternScale;
//                    sox = .5f;
//                    soy = 1;
                    shaderMask.setUniformf("u_scale_offset", sox, soy);
//                    shaderMask.setUniformf("u_aspect", 1.0f);

                    float pox = 1f;
                    float poy = 1f;
                    shaderMask.setUniformf("u_pos_offset", pox, poy);
//                    shaderMask.setUniformf("u_angle", patternRot * MathUtils.degreesToRadians);
//                    shaderMask.setUniformf("u_angle", 45 * MathUtils.degreesToRadians);
//                    shaderMask.setUniformf("u_angle", 90 * MathUtils.degreesToRadians);
//                    shaderMask.setUniformf("u_angle", 0 * MathUtils.degreesToRadians);
                    shaderMask.setUniformf("u_angle", patternRot * MathUtils.degreesToRadians);

                } catch (Exception ex) {
                    PLog.error("Error: " + ex.getMessage());
                    PLog.error("Fix and reload shader!");
                    shaderMask.dispose();
                    shaderMask = null;
                }

//            bgSprite.setRotation(rotation);
                bgSprite.setX(-15);
                bgSprite.setRotation(0);
                bgSprite.setScale(2);
                pattern.bind(1);
                // need to rebind it
                bgSprite.getTexture().bind(0);
                bgSprite.draw(batch);
            }
            batch.flush();

            if (shaderMask != null) {
                try {
                    shaderMask.setUniformi("u_texture", 0);
                    shaderMask.setUniformi("u_pattern", 1);

                    float bgw = bgSprite.getTexture().getWidth();
                    float bgh = bgSprite.getTexture().getHeight();

                    float pw = pattern.getWidth();
                    float ph = pattern.getHeight();
//                    shaderMask.setUniformf("u_resolution", pw, ph);
                    shaderMask.setUniformf("u_resolution", 1f, 1.f);

                    float patternScale = 1f;
                    float patternAspect = pw / ph;
                    float bgAspect = bgw / bgh;
                    // we need to scale pattern uvs to match src texture, or it will be distorted
                    // we wrap, so we dont need to offset in x,y
//                    float sox = bgSprite.getTexture().getWidth() / (float)pattern.getWidth() * 1/patternScale * 1/patternAspect;
//                    float soy = bgSprite.getTexture().getHeight() / (float)pattern.getHeight() * 1/patternScale;
                    float invPatternScale = 1/patternScale;
                    // with u_aspect = patternAspect
//                    float sox = (bgw / pw) * (ph / pw) * invPatternScale;
//                    float soy = bgh / ph * invPatternScale;

//                    float sox = (bgw / pw) * (ph / pw) * invPatternScale;
                    float sox = (bgw / pw) * invPatternScale;
                    float soy = (bgh / ph) * invPatternScale;
//                    sox = .5f;
//                    soy = 1;
                    shaderMask.setUniformf("u_scale_offset", sox, soy);
//                    shaderMask.setUniformf("u_aspect", 1.0f);

                    float pox = 1f;
                    float poy = 1f;
                    shaderMask.setUniformf("u_pos_offset", pox, poy);
//                    shaderMask.setUniformf("u_angle", patternRot * MathUtils.degreesToRadians);
//                    shaderMask.setUniformf("u_angle", 45 * MathUtils.degreesToRadians);
//                    shaderMask.setUniformf("u_angle", 90 * MathUtils.degreesToRadians);
//                    shaderMask.setUniformf("u_angle", 0 * MathUtils.degreesToRadians);
                    shaderMask.setUniformf("u_angle", (patternRot + 90) * MathUtils.degreesToRadians);

                } catch (Exception ex) {
                    PLog.error("Error: " + ex.getMessage());
                    PLog.error("Fix and reload shader!");
                    shaderMask.dispose();
                    shaderMask = null;
                }

                /*
                j0oU Clyd V0xT Jg

                */
                bgSprite.setX(5);
//            bgSprite.setRotation(rotation);
                bgSprite.setRotation(0);
                bgSprite.setScale(2);
                pattern.bind(1);
                // need to rebind it
                bgSprite.getTexture().bind(0);
                bgSprite.draw(batch);
                batch.setShader(null);
            }
        }
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            bgSprite.setColor(Color.ORANGE);
            reloadMaskShader();
        }
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
        PlaygroundGame.start(args, MaskTest.class);
    }

}

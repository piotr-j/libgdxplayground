package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIScaledImage2Test extends BaseScreen {
    protected static final String TAG = UIScaledImage2Test.class.getSimpleName();

    TextureAtlas.AtlasRegion region;
    TextureAtlas.AtlasSprite atlasSprite;

    public UIScaledImage2Test (GameReset game) {
        super(game);

        clear.set(Color.DARK_GRAY);

        // no atlas handy
        region = new TextureAtlas.AtlasRegion(new Texture("badlogic.jpg"), 0, 0, 256, 256);
        region.offsetX = 64;
        region.offsetY = 16;
        region.originalWidth = 256 + 64 + 64;
        region.originalHeight = 256 + 16 + 16;

        atlasSprite = new TextureAtlas.AtlasSprite(region);
        atlasSprite.setPosition(50, 500);

        rebuild();
    }

    @Override public void render (float delta) {
        super.render(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            rebuild();
        }
        stage.act(delta);
        stage.draw();

//        batch.begin();
//        batch.draw(region, 50, 50, region.getRegionWidth(), region.getRegionHeight());
//        batch.draw(region, 512 + region.offsetX, 50 + region.offsetY, region.getRegionWidth(), region.getRegionHeight());
//        atlasSprite.draw(batch);
//        batch.end();

    }

    private void rebuild () {
        Gdx.app.log(TAG, "Rebuild");
        root.clear();
        {
            final Table table = new Table();
            root.add(table).pad(10);
            table.debugAll();

            {
                Drawable drawable = new TextureRegionDrawable(region);
                Image image = new Image(drawable);
                image.setScaling(Scaling.fit);
                table.add(image).size(region.originalWidth, region.originalHeight).pad(32);
            }
            {
                Drawable drawable = new SpriteDrawable(new TextureAtlas.AtlasSprite(region));
                Image image = new Image(drawable);
                image.setScaling(Scaling.fit);
                table.add(image).size(region.originalWidth, region.originalHeight).pad(32);
            }
        }
        root.row();
        {
            final Table table = new Table();
            root.add(table).pad(10);
            table.debugAll();
            {
                Drawable drawable = new SpriteDrawable(new TextureAtlas.AtlasSprite(region));
                Image image = new Image(drawable);
                image.setScaling(Scaling.fit);
                table.add(image).pad(32);
            }
            {
                TextureAtlas.AtlasSprite sprite = new TextureAtlas.AtlasSprite(region);
                Drawable drawable = new SpriteDrawable(sprite);
                drawable.setMinWidth(sprite.getWidth() * .75f);
                drawable.setMinHeight(sprite.getHeight() * .75f);
                Image image = new Image(drawable);
                image.setScaling(Scaling.fit);
                table.add(image).pad(32);
            }
            {
                TextureAtlas.AtlasSprite sprite = new TextureAtlas.AtlasSprite(region);
                Drawable drawable = new SpriteDrawable(sprite);
                drawable.setMinWidth(sprite.getWidth() * 1.5f);
                drawable.setMinHeight(sprite.getHeight() * 1.5f);
                Image image = new Image(drawable);
                image.setScaling(Scaling.fit);
                table.add(image).pad(32);
            }
        }

        // TODO

    }

    private static class SpriteImage extends Image {

    }


    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
        config.setWindowedMode(1280, 720 + 200);
        PlaygroundGame.start(args, config, UIScaledImage2Test.class);
    }
}

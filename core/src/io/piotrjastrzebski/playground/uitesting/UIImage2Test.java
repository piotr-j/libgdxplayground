package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIImage2Test extends BaseScreen {
    protected static final String TAG = UIImage2Test.class.getSimpleName();

    TextureRegion region;
    public UIImage2Test (GameReset game) {
        super(game);
        Pixmap pixmap = new Pixmap(128, 256, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        pixmap.setColor(Color.GRAY);
        pixmap.fillRectangle(6, 6, 128 - 12, 256 - 12);
        pixmap.setColor(Color.YELLOW);
        pixmap.fillCircle(64, 128 + 24, 32);
        pixmap.setColor(Color.CYAN);
        pixmap.fillCircle(64, 128 - 24, 32);
        pixmap.setColor(Color.RED);
        pixmap.fillCircle(20, 20, 8);
        pixmap.setColor(Color.GREEN);
        pixmap.fillCircle(20, 256 - 20, 8);
        pixmap.setColor(Color.BLUE);
        pixmap.fillCircle(128 - 20, 20, 8);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(128 - 20, 256 - 20, 8);
        pixmap.setColor(Color.MAGENTA);
        pixmap.fillRectangle(0, 125, 128, 2);
        pixmap.fillRectangle(64, 0, 2, 256);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        region = new TextureRegion(texture);

        clear.set(Color.DARK_GRAY);

        init();
    }
    CardGroup topGroup;
    CardGroup botGroup;
    private void init () {
        int pad = 64;
        // we to have h
        topGroup = new CardGroup(1.25f, 16);
        root.add(topGroup).pad(pad).row();
        botGroup = new CardGroup(.75f, 16);
        root.add(botGroup).pad(pad).row();

        int align[] = { Align.center, Align.bottom, Align.left, Align.top, Align.right};
        for (int i = 0; i < 5; i++) {
//            topGroup.addActor(newCard(1.0f, align[i]));
            final Image topCard = newCard(1.25f, Align.center);
            final Image botCard = newCard(.75f, Align.center);
            topGroup.addActor(topCard);
            botGroup.addActor(botCard);
            topCard.addListener(new ClickListener(Input.Buttons.MIDDLE){
                @Override public void clicked (InputEvent event, float x, float y) {
                    moveCard(topCard, botCard);
                }
            });
            botCard.addListener(new ClickListener(Input.Buttons.MIDDLE){
                @Override public void clicked (InputEvent event, float x, float y) {
                    moveCard(botCard, topCard);
                }
            });
        }
        root.debugAll();
    }

    Vector2 v2 = new Vector2();
    Vector2 fromV2 = new Vector2();
    Vector2 toV2 = new Vector2();
    private void moveCard (Image from, Image to) {
        float duration = 2;
        Image image = newCard(from.getScaleX(), Align.center);
        image.setColor(Color.GREEN);
        root.addActor(image);
        from.localToAscendantCoordinates(root, v2.set(from.getImageWidth()/2, from.getImageHeight()/2));
        fromV2.set(v2);
        image.setPosition(v2.x, v2.y, Align.center);
        to.localToAscendantCoordinates(root, v2.set(to.getImageWidth()/2, to.getImageHeight()/2));
        toV2.set(v2);
        image.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveToAligned(v2.x, v2.y, Align.center, duration, Interpolation.sine),
                Actions.sequence(
                    Actions.scaleTo(1.5f, 1.5f, duration/2, Interpolation.sine),
                    Actions.scaleTo(to.getScaleX(), to.getScaleY(), duration/2, Interpolation.sine)
                ),
                Actions.rotateBy(360, duration, Interpolation.sine)
            ),
            Actions.removeActor()
        ));
    }

    private Image newCard (float scale, int align) {
        final Image image = new Image(region);
        image.setOrigin(align);
        image.setScale(scale);
        image.setTouchable(Touchable.enabled);
//        image.setColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
        image.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override public void clicked (InputEvent event, float x, float y) {
                float s = image.getScaleX();
                image.clearActions();
                // @off
                image.addAction(Actions.sequence(
                    Actions.scaleTo(s * 1.25f, s * 1.25f, 1),
                    Actions.scaleTo(s, s, 1)
                ));
                // @on
            }
        });
        image.addListener(new ClickListener(Input.Buttons.RIGHT) {
            @Override public void clicked (InputEvent event, float x, float y) {
                float s = image.getScaleX();
                image.clearActions();
                // @off
                image.addAction(Actions.sequence(
                    Actions.scaleTo(s * .75f, s * .75f, 1),
                    Actions.scaleTo(s, s, 1)
                ));
                // @on
            }
        });
        return image;
    }

    private static class CardGroup extends HorizontalGroup {
        float scale = 1;

        public CardGroup (float scale, float space) {
            super();
            this.scale = scale;
//            space(space);
//            setTransform(false);
            space(-128 * (1-scale));
//            setScale(scale);
        }

        @Override public void addActor (Actor actor) {
            super.addActor(actor);
            actor.setScale(scale);
        }

        @Override public float getPrefWidth () {
            // this already is scaled due to negative space
            // but its missing the edges, tho we cant seem to do a whole lot about that
            return super.getPrefWidth();
        }

        @Override public float getPrefHeight () {
            return super.getPrefHeight() * scale;
        }
    }

    @Override public void render (float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();


        if (true) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Color.CYAN);
            renderer.line(fromV2, toV2);
            renderer.end();
        }
    }

    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
        config.setWindowedMode(1280, 720 + 200);
        PlaygroundGame.start(args, config, UIImage2Test.class);
    }
}

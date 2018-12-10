package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntMap;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.utils.Align.*;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIImageTest extends BaseScreen {
    protected static final String TAG = UIImageTest.class.getSimpleName();

    TextureRegion region;
    public UIImageTest (GameReset game) {
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
        rebuild();

        clear.set(Color.DARK_GRAY);
    }

    @Override public void render (float delta) {
        super.render(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            rebuild();
        }
        stage.act(delta);
        stage.draw();

        if (true) return;
        renderer.setProjectionMatrix(guiCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        {
            renderer.setColor(Color.CYAN);
            float cardWidth = 128;
            float cardHeight = 256;
            float pad = 48;
            float cx = guiViewport.getWorldWidth() / 2;
            float cy = guiViewport.getWorldHeight() / 2;
            float w = 5 * (cardWidth + pad * 2);
            float h = 2 * (cardHeight + pad * 2);
            float bx = cx - w / 2;
            float by = cy - h / 2;
            for (int i = 0; i < 5; i++) {
                float x = bx + pad + i * (cardWidth + pad * 2);
                float y = by + pad;
                renderer.rect(x, y, 128, 256);
                y += cardHeight + pad * 2;
                renderer.rect(x, y, 128, 256);
            }
        }

        {

            float scale = .5f;
            renderer.setColor(Color.MAGENTA);
            float cardWidth = 128;
            float cardHeight = 256;
            float pad = 48;
            float cx = guiViewport.getWorldWidth()/2;
            float cy = guiViewport.getWorldHeight()/2;
            float w = 5 * (cardWidth + pad * 2);
            float h = 2 * (cardHeight + pad * 2);
            float bx = cx - w/2;
            float by = cy - h/2;
            for (int i = 0; i < 5; i++) {
                int origin = 0;
                float ox = 0;
                float oy = 0;
                switch (i) {
                case 0: {
                    origin = Align.bottom;
                } break;
                case 1: {
                    origin = Align.bottomLeft;
                } break;
                case 2: {
                    origin = Align.bottomRight;
                } break;
                case 3: {
                    origin = Align.topLeft;
                } break;
                case 4: {
                    origin = Align.topRight;
                } break;
                }

                if ((origin & left) != 0) {
                    ox = 0;
                } else if ((origin & right) != 0) {
                    ox = cardWidth * (1 - scale) * 2;
                } else {
                    ox = cardWidth * (1 - scale);
                }

                if ((origin & bottom) != 0) {
                    oy = 0;
                } else if ((origin & top) != 0) {
                    oy = cardHeight * (1 - scale) * 2;
                } else {
                    oy = cardHeight * (1 - scale);
                }

                float x = bx + pad + i * (cardWidth + pad * 2);
                float y = by + pad;
                renderer.rect(x, y, ox, oy, 128, 256, scale, scale, 0);
                y += cardHeight + pad * 2;
                renderer.rect(x, y, ox, oy, 128, 256, scale, scale, 0);
            }
        }

        renderer.end();
    }

    private void rebuild () {

        Gdx.app.log(TAG, "Rebuild");
        root.clear();
        final Table table = new Table();
        root.add(table);
        int pad = 48;
        float scale = .5f;
        float duration = 4;
        for (int i = 0; i < 5; i++) {
            Image image = new Image(new TextureRegionDrawable(region));
//            image.debug();
//            image.setSize(128, 256);
            image.setScale(scale);
            switch (i) {
            case 0: {
                image.setOrigin(Align.center);
            } break;
            case 1: {
                image.setOrigin(Align.bottomLeft);
            } break;
            case 2: {
                image.setOrigin(Align.bottomRight);
            } break;
            case 3: {
                image.setOrigin(Align.topLeft);
            } break;
            case 4: {
                image.setOrigin(Align.topRight);
            } break;
            }
//            image.setOrigin(Align.center);
            table.add(image).pad(pad);
//            image.addAction(Actions.forever(Actions.rotateBy(360, 8, Interpolation.sine)));
            image.addAction(Actions.forever(
                Actions.sequence(
                    Actions.scaleTo(1.25f * scale, 1.25f * scale, duration, Interpolation.sine),
                    Actions.scaleTo(.75f * scale, .75f * scale, duration, Interpolation.sine)
                )
            ));

        }
        table.row();
        for (int i = 0; i < 5; i++) {
            ScaledImage image = new ScaledImage(new TextureRegionDrawable(region));
            image.setScale(scale);
//            image.debug();
//            image.setSize(128, 256);
            switch (i) {
            case 0: {
                image.setOrigin(Align.center);
            } break;
            case 1: {
                image.setOrigin(Align.bottomLeft);
            } break;
            case 2: {
                image.setOrigin(Align.bottomRight);
            } break;
            case 3: {
                image.setOrigin(Align.topLeft);
            } break;
            case 4: {
                image.setOrigin(Align.topRight);
            } break;
            }
//            image.setOrigin(Align.center);
//            image.setBaseScale(1f, 1f);
            table.add(image).pad(pad);
//            image.addAction(Actions.forever(Actions.rotateBy(360, 8, Interpolation.sine)));
            image.addAction(Actions.forever(
                Actions.sequence(
                    Actions.scaleTo(1.25f * scale, 1.25f * scale, duration, Interpolation.sine),
//                    Actions.scaleTo(.75f * scale, .75f * scale, duration, Interpolation.sine)
                    Actions.scaleTo(scale, scale, duration, Interpolation.sine)
                )
            ));
        }
        table.pack();
        if (false) {
            table.addAction(Actions.forever(Actions.run(new Runnable() {
                @Override public void run () {
                    table.invalidateHierarchy();
                    table.layout();
                    table.pack();
                }
            })));
        }
        table.debugAll();
    }

    @Override public boolean keyDown (int keycode) {

        return super.keyDown(keycode);
    }

    class ScaledImage extends Image {
        protected TextureRegionDrawable drawable;
        protected float scaleX = 1;
        protected float scaleY = 1;
        protected int originAlign = Align.center;
        protected float baseWidth;
        protected float baseHeight;

        public ScaledImage() {
            super();
            setTouchable(Touchable.disabled);
        }

        public ScaledImage(TextureRegionDrawable drawable) {
            super(drawable);
            setTouchable(Touchable.disabled);
        }

        @Override
        public void setDrawable(Drawable drawable) {
            // gotta set it to null first so it gets updated
            super.setDrawable(null);
            if (drawable == null) {
                return;
            }
            if (!(drawable instanceof TextureRegionDrawable)) {
                throw new AssertionError("Only TextureRegionDrawable is supported, got " + drawable.getClass());
            }
            // we make a copy as we need to modify the drawable for scaling
            this.drawable = new TextureRegionDrawable((TextureRegionDrawable)drawable);
            baseWidth = region.getRegionWidth();
            baseHeight = region.getRegionHeight();
            // make sure new drawable has correct size
            if (scaleX == 0) scaleX = 1;
            if (scaleY == 0) scaleY = 1;
            setScale(scaleX, scaleY);
            super.setDrawable(this.drawable);
        }

        @Override public void setScaleX (float scaleX) {
            setScale(scaleX, scaleY);
        }

        @Override public void setScaleY (float scaleY) {
            setScale(scaleX, scaleY);
        }

        @Override
        public void setScale(float scaleXY) {
            setScale(scaleXY, scaleXY);
        }

        @Override
        public void setScale(float scaleX, float scaleY) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            // instead of relaying on normal scaling, we scale the drawable
            // we do that so layouts work as expected, as they dont take scale into account
            final TextureRegion region = drawable.getRegion();
            drawable.setMinWidth(region.getRegionWidth() * scaleX);
            drawable.setMinHeight(region.getRegionHeight() * scaleY);
            setSize(drawable.getMinWidth(), drawable.getMinHeight());
            setOrigin(originAlign);
        }

        @Override
        public float getScaleX() {
            return scaleX;
        }

        @Override
        public float getScaleY() {
            return scaleY;
        }

        @Override public void scaleBy (float scaleX, float scaleY) {
            setScale(this.scaleX + scaleX, this.scaleY + scaleY);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // we need to reset the scale before drawing or it will be applied twice
            float sx = scaleX;
            float sy = scaleY;
            float ox = 0;
            float oy = 0;
            float w = getWidth();
            float h = getHeight();

            if ((originAlign & left) != 0) {
                ox = 0;
            } else if ((originAlign & right) != 0) {
//                ox = baseWidth * (1 - scaleX) * 2;
                ox = (baseWidth/2 - w);
            } else {
                // aka center
//                ox = baseWidth * (1 - scaleX);
//                ox = -(baseWidth - w)/2;
//                ox = (baseWidth - w/2);
//                ox = (-w/2);
//                ox = -(baseWidth/2 - w); // bottom right?

                ox = 0;
            }

            if ((originAlign & bottom) != 0) {
                oy = 0;
            } else if ((originAlign & top) != 0) {
                oy = baseHeight * (1 - scaleY) * 2;
            } else {
                // aka center
//                oy = baseHeight * (1 - scaleY);
            }

            scaleX = scaleY = 1;
            float bx = getX();
            float by = getY();
//            setPosition(bx - ox, by - oy);
            setPosition(bx + ox, by + oy);
            super.draw(batch, parentAlpha);
            setPosition(bx, by);
            scaleX = sx;
            scaleY = sy;
        }

        private Color color = new Color();

        @Override
        protected void drawDebugBounds(ShapeRenderer shapes) {
            // we need to reset the scale before drawing or it will be applied twice
            float sx = scaleX;
            float sy = scaleY;


            scaleX = scaleY = 1;
            Color debugColor = getStage().getDebugColor();
            color.set(debugColor);
            debugColor.set(Color.RED);
            super.drawDebugBounds(shapes);
            debugColor.set(color);
            scaleX = sx;
            scaleY = sy;
        }

        @Override
        public void setOrigin(int alignment) {
            originAlign = alignment;
            super.setOrigin(alignment);
        }
    }

    public static void main (String[] args) {
        LwjglApplicationConfiguration config = PlaygroundGame.config();
        config.height *= 1.25f;
        PlaygroundGame.start(args, config, UIImageTest.class);
    }
}

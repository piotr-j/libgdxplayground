package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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


        region = new TextureRegion(new Texture(pixmap));
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
    }

    private void rebuild () {

        Gdx.app.log(TAG, "Rebuild");
        root.clear();
        Table table = new Table();
        root.add(table);
        for (int i = 0; i < 5; i++) {
            Image image = new Image(new TextureRegionDrawable(region));
            image.debug();
//            image.setSize(128, 256);
            image.setOrigin(Align.center);
            table.add(image).pad(16);
//            image.addAction(Actions.forever(Actions.rotateBy(360, 8, Interpolation.sine)));
            image.addAction(Actions.forever(
                Actions.sequence(
                    Actions.scaleTo(1.25f, 1.25f, 1, Interpolation.sine),
                    Actions.scaleTo(.75f, .75f, 1, Interpolation.sine)
                )
            ));

        }
        table.row();
        for (int i = 0; i < 5; i++) {
            ScaledImage image = new ScaledImage(new TextureRegionDrawable(region));
            image.debug();
//            image.setSize(128, 256);
            image.setOrigin(Align.center);
            image.setBaseScale(1f, 1f);
            table.add(image).pad(16);
//            image.addAction(Actions.forever(Actions.rotateBy(360, 8, Interpolation.sine)));
            image.addAction(Actions.forever(
                Actions.sequence(
                    Actions.scaleTo(1.25f, 1.25f, 1, Interpolation.sine),
                    Actions.scaleTo(.75f, .75f, 1, Interpolation.sine)
                )
            ));

        }
    }

    @Override public boolean keyDown (int keycode) {

        return super.keyDown(keycode);
    }

    class ScaledImage extends Image {
        protected TextureRegionDrawable drawable;
        protected float scaleX = 1;
        protected float scaleY = 1;
        protected float baseScaleX = 1;
        protected float baseScaleY = 1;
        protected int originAlign = Align.center;
        private Color color = new Color();

        public ScaledImage() {
            super();
        }

        public ScaledImage(TextureRegionDrawable drawable) {
            super(drawable);
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
            // this is called from super, before field are initialized...
            if (this.drawable == null) {
                this.drawable = new TextureRegionDrawable();
            }
            // we make a copy as we need to modify the drawable for scaling
            TextureRegionDrawable other = (TextureRegionDrawable) drawable;
            this.drawable.setRegion(other.getRegion());
            // make sure new drawable has correct size
            setScale(scaleX, scaleY);
            super.setDrawable(this.drawable);
        }

        @Override
        public void setScale(float scaleXY) {
            setScale(scaleXY, scaleXY);
        }

        @Override
        public void setScale(float scaleX, float scaleY) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
//            super.setScale(scaleX, scaleY);
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

        @Override public void setScaleX (float scaleX) {
            setScale(scaleX, scaleY);
        }

        @Override
        public float getScaleY() {
            return scaleY;
        }

        @Override public void setScaleY (float scaleY) {
            setScale(scaleX, scaleY);
        }

        @Override public void scaleBy (float scaleX, float scaleY) {
            setScale(this.scaleX + scaleX, this.scaleY + scaleY);
        }

        @Override public void layout () {
            super.layout();
            setOrigin(originAlign);
//            Gdx.app.log(TAG, "layout");

        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // we need to reset the scale before drawing or it will be applied twice
            float sx = scaleX;
            float sy = scaleY;
            scaleX = scaleY = 1;
            super.draw(batch, parentAlpha);
            scaleX = sx;
            scaleY = sy;
        }

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

        public void setBaseScale (float baseScaleX, float baseScaleY) {
            this.baseScaleX = baseScaleX;
            this.baseScaleY = baseScaleY;
            final TextureRegion region = drawable.getRegion();
            drawable.setMinWidth(region.getRegionWidth() * baseScaleY);
            drawable.setMinHeight(region.getRegionHeight() * baseScaleY);
            setSize(drawable.getMinWidth(), drawable.getMinHeight());
            setOrigin(originAlign);
        }
    }

    public static void main (String[] args) {
        PlaygroundGame.start(args, UIImageTest.class);
    }
}

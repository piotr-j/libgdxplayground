package io.piotrjastrzebski.playground.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.esotericsoftware.spine.utils.SkeletonActor;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Fit spine skeleton to actor
 * <p>
 * Created by EvilEntity on 25/01/2016.
 */
public class ActorFitTest extends BaseScreen {
    private static final String TAG = ActorFitTest.class.getSimpleName();
    SkeletonRenderer renderer;
    SkeletonRendererDebug debugRenderer;

    TextureAtlas atlas;
    Skeleton skeleton;
    AnimationState state;
    SkeletonBounds bounds;
    PolygonSpriteBatch polyBatch;
    VisWindow window;

    public ActorFitTest (GameReset game) {
        super(game);
        clear.set(0.5f, 0.5f, 0.5f, 1);
        polyBatch = new PolygonSpriteBatch();
        renderer = new SkeletonRenderer();
        debugRenderer = new SkeletonRendererDebug();
        debugRenderer.setBoundingBoxes(true);
        debugRenderer.setRegionAttachments(false);
//        debugRenderer.setScale(INV_SCALE);

        atlas = new TextureAtlas(Gdx.files.internal("spine/tree.atlas"));
        SkeletonJson json = new SkeletonJson(atlas);
//        json.setScale(INV_SCALE);
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("spine/tree.json"));

        skeleton = new Skeleton(skeletonData);
//        skeleton.setPosition(0, -5);

        bounds = new SkeletonBounds();

        AnimationStateData stateData = new AnimationStateData(skeletonData);

        state = new AnimationState(stateData);
//        state.setTimeScale(0.5f);

        state.setAnimation(0, "idle", true);


        stage = new Stage(guiViewport, polyBatch);
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        window = new VisWindow("Resizeable skeleton");
        window.setResizable(true);
        root.addActor(window);

        SpineActor spineActor = new SpineActor(renderer, skeleton, state);
        spineActor.debug();
        window.add(spineActor).expand().fill();
        window.setSize(400, 400);
        window.centerWindow();

        multiplexer.removeProcessor(0);
        multiplexer.addProcessor(0, stage);
    }

    @Override public void render (float delta) {
        super.render(delta);
        enableBlending();

        state.update(delta);

        state.apply(skeleton);
        skeleton.updateWorldTransform();
        bounds.update(skeleton, false);

        debugRenderer.getShapeRenderer().setProjectionMatrix(guiCamera.combined);

        polyBatch.setProjectionMatrix(guiCamera.combined);
//        polyBatch.begin();
//        renderer.draw(polyBatch, skeleton);
//        polyBatch.end();


        stage.act(delta);
        stage.draw();

        debugRenderer.draw(skeleton);
    }

    private Vector2 tp = new Vector2();

    @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        gameViewport.unproject(tp.set(screenX, screenY));

        return true;
    }

    @Override public void dispose () {
        super.dispose();
        polyBatch.dispose();
    }

    private static class SpineActor extends Table {
        private SkeletonRenderer renderer;
        private Skeleton skeleton;
        AnimationState state;
        private SkeletonActor actor;
        private Rectangle size = new Rectangle();

        /** Creates an uninitialized SkeletonActor. The renderer, skeleton, and animation state must be set before use. */
        public SpineActor () {
        }

        public SpineActor (SkeletonRenderer renderer, Skeleton skeleton, AnimationState state) {
            super();
            this.renderer = renderer;
            this.skeleton = skeleton;
            this.state = state;
            // on the root bone we have a bounding box, that is our target size
            BoundingBoxAttachment attachment = (BoundingBoxAttachment)skeleton.getAttachment("bounds", "bounds");
            float[] vertices = attachment.getVertices();
            for (int i = 0, n = vertices.length; i < n; i+=2) {
                size.merge(vertices[i], vertices[i + 1]);
            }
            actor = new SkeletonActor(renderer, skeleton, state);
            add(actor).expand().bottom();
        }

        @Override public void draw (Batch batch, float parentAlpha) {
            float width = getWidth();
            float height = getHeight();
            if (width != 0 && height != 0) {
                Vector2 scaling = Scaling.fit.apply(size.width, size.height, width, height);
                float scale = scaling.x/size.width;
                skeleton.getRootBone().setScale(scale);
            }
            super.draw(batch, parentAlpha);

        }

        public SkeletonRenderer getRenderer () {
            return renderer;
        }

        public void setRenderer (SkeletonRenderer renderer) {
            this.renderer = renderer;
        }

        public Skeleton getSkeleton () {
            return skeleton;
        }

        public void setSkeleton (Skeleton skeleton) {
            this.skeleton = skeleton;
        }

        public AnimationState getAnimationState () {
            return state;
        }

        public void setAnimationState (AnimationState state) {
            this.state = state;
        }

        @Override public void setSize (float width, float height) {
            super.setSize(width, height);

        }
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
        config.setWindowedMode(1280/2, 720/2);
        PlaygroundGame.start(args, config, ActorFitTest.class);
    }
}

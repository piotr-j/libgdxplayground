package spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class ReorderTest extends BaseScreen {
	private static final String TAG = ReorderTest.class.getSimpleName();
    SkeletonRenderer renderer;
    SkeletonRendererDebug debugRenderer;

    TextureAtlas atlas;
    Skeleton skeleton;
    AnimationState state;
    SkeletonBounds bounds;
    PolygonSpriteBatch polyBatch;
    int[] drawOrders;

	public ReorderTest(GameReset game) {
		super(game);
		clear.set(0.5f, 0.5f, 0.5f, 1);
        polyBatch = new PolygonSpriteBatch();
        renderer = new SkeletonRenderer();
        debugRenderer = new SkeletonRendererDebug();
//        debugRenderer.setBoundingBoxes(false);
        debugRenderer.setRegionAttachments(false);
        debugRenderer.setScale(INV_SCALE);

        atlas = new TextureAtlas(Gdx.files.internal("spine/test_reorder.atlas"));
        SkeletonJson json = new SkeletonJson(atlas);
        json.setScale(INV_SCALE);
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("spine/test_reorder.json"));

        skeleton = new Skeleton(skeletonData);
        skeleton.setPosition(0,-5);

        bounds = new SkeletonBounds();

        AnimationStateData stateData = new AnimationStateData(skeletonData);

        state = new AnimationState(stateData);
        state.setTimeScale(0.5f);

        state.setAnimation(0, "idle", true);


        initDrawOrders();
        state.setAnimation(1, "draw_order", false);

	}

    private void initDrawOrders() {
        Animation setupAnchors = state.getData().getSkeletonData().findAnimation("draw_order");
        Array<Animation.Timeline> timeLines = setupAnchors.getTimelines();
        // find draw order data so each category doesnt have to look for it
        int[] drawOrders = null;
        Animation.DrawOrderTimeline drawOrder = null;
        for (Animation.Timeline timeline : timeLines) {
            if (timeline instanceof Animation.DrawOrderTimeline) {
                // there should be only one
                drawOrder = (Animation.DrawOrderTimeline) timeline;
                // default order is on first frame
                // it will be missing, unless it is different from the default
                drawOrders = drawOrder.getDrawOrders()[0];
                break;
            }
        }
        if (drawOrders != null) {
            this.drawOrders = drawOrders;
        } else {
            // otherwise, draw order is in natural order
            drawOrders = new int[skeleton.getSlots().size];
            for (int i = 0; i < drawOrders.length; i++) {
                drawOrders[i] = i;
            }
            if (drawOrder != null) {
                drawOrder.getDrawOrders()[0] = drawOrders;
            }
            this.drawOrders = drawOrders;
        }

        printDrawOrder("Start", this.drawOrders);
    }

    private void printDrawOrder(String prefix, int[] drawOrders) {
        if (drawOrders == null) {
            Gdx.app.log(TAG, prefix + " DrawOrders=[]");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(" DrawOrders=[");
        Array<Slot> slots = skeleton.getSlots();
        for (int i = 0; i < drawOrders.length; i++) {
            Slot slot = slots.get(drawOrders[i]);
            sb.append(slot.getData().getName());
            if (i < drawOrders.length -1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        Gdx.app.log(TAG, sb.toString());
    }

    @Override public void render (float delta) {
		super.render(delta);
		enableBlending();

        state.update(delta);

        state.apply(skeleton);
        skeleton.updateWorldTransform();
        bounds.update(skeleton, false);


        debugRenderer.getShapeRenderer().setProjectionMatrix(gameCamera.combined);

        polyBatch.setProjectionMatrix(gameCamera.combined);
        polyBatch.begin();
        renderer.draw(polyBatch, skeleton);
        polyBatch.end();

//        debugRenderer.draw(skeleton);

	}

	private Vector2 tp = new Vector2();
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
	    gameViewport.unproject(tp.set(screenX, screenY));
        BoundingBoxAttachment attachment = bounds.containsPoint(tp.x, tp.y);
        if (attachment != null) {
            Gdx.app.log(TAG, "att " + attachment);
            printDrawOrder("Old", drawOrders);
            // seems that SkeletonBounds doesnt care about this order, so lets skip that
//            bringToBack(attachment.getName());
            bringToBack(attachment.getName().replace("bb", "badlogic"));
            bringToBack(attachment.getName().replace("bb", "clip"));
            printDrawOrder("New", drawOrders);
        }
        return true;
    }

    private void bringToBack(String slotName) {
        Slot slot = skeleton.findSlot(slotName);
        if (slot == null) return;

        int index = slot.getData().getIndex();
        int[] drawOrders = this.drawOrders;
        for (int i = 0; i < drawOrders.length; i++) {
            if (drawOrders[i] == index) {
                System.arraycopy(drawOrders, 0, drawOrders, 1, i);
                break;
            }
        }
        drawOrders[0] = index;

        state.setAnimation(1, "draw_order", false);
    }

    @Override
    public void dispose() {
        super.dispose();
        polyBatch.dispose();
    }

    // allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width /= 2;
		config.height /= 2;
		PlaygroundGame.start(args, config, ReorderTest.class);
	}
}

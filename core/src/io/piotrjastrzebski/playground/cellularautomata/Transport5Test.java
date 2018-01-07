package io.piotrjastrzebski.playground.cellularautomata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Iterator;

/**
 * We want to implement transport belts or something
 * each belt hast 2 lanes items can move on, with at most 2 items per lanes, for 4 total
 * items should travel along the belts directiona
 * we want different types of belts, straight, elbows,
 * Created by EvilEntity on 25/01/2016.
 */
@SuppressWarnings("Duplicates") public class Transport5Test extends BaseScreen {
    BitmapFont font;
    GlyphLayout layout;

    public final static int WIDTH = (int)VP_WIDTH;
    public final static int HEIGHT = (int)VP_HEIGHT;

    private boolean simEnabled = true;
    private Array<Item> items = new Array<>();

    public Transport5Test (GameReset game) {
        super(game);
        gameViewport.setMinWorldWidth(VP_WIDTH / 2);
        gameViewport.setMinWorldHeight(VP_HEIGHT / 2);
        BitmapFont visFont = VisUI.getSkin().getFont("small-font");
        font = new BitmapFont(new BitmapFont.BitmapFontData(visFont.getData().fontFile, false), visFont.getRegions(), false);
        font.setUseIntegerPositions(false);
        font.getData().setScale(INV_SCALE);
        layout = new GlyphLayout();
        clear.set(Color.GRAY);

    }

    private Belt getBelt (int x, int y) {
        int index = x + y * WIDTH;
        if (index < 0 || index > belts.length)
            return null;
        return belts[index];
    }

    private Belt setBelt (int x, int y, int type) {
        System.out.println("Belt " + x + ", " + y + ", " + type);
        Belt belt = getBelt(x, y);
        if (belt == null) {
            belt = new Belt(x, y, type);
            belts[belt.index] = belt;
        } else {
            belt.setType(type);
        }
        return belt;
    }

    private void setBelt (int x, int y, int type, int direction) {
        Belt belt = setBelt(x, y, type);
        belt.rotate(direction);
    }

    private void clearBelt (int x, int y) {
        System.out.println("Clear " + x + ", " + y);
        int index = x + y * WIDTH;
        belts[index] = null;
    }

    private void spawnItem(float x, float y) {
        Item item = new Item(x, y);
        items.add(item);

        Belt belt = getBelt((int)x, (int)y);
        if (belt != null) {
            belt.enqueue(item);
            item.despawn = -1;
        }
    }

    Belt[] belts = new Belt[WIDTH * HEIGHT];
    int direction = Belt.DIR_EAST;
    float tickTimer = 0;
    @Override public void render (float delta) {
        super.render(delta);
        // handle input
        int mx = (int)cs.x;
        int my = (int)cs.y;
        if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                setBelt(mx, my, Belt.TYPE_STRAIGHT, direction);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                setBelt(mx, my, Belt.TYPE_CURVE, direction);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                setBelt(mx, my, Belt.TYPE_CURVE_REV, direction);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    direction--;
                    if (direction < Belt.DIR_NORTH) {
                        direction = Belt.DIR_WEST;
                    }
                } else {
                    direction++;
                    if (direction > Belt.DIR_WEST) {
                        direction = Belt.DIR_NORTH;
                    }
                }
                Gdx.app.log("ROT", "" + direction);
                Belt belt = getBelt(mx, my);
                if (belt != null) {
                    belt.rotate(direction);
                }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//                setBelt(mx, my, 0);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
//                setBelt(mx, my, 0);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                clearBelt(mx, my);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                spawnItem(cs.x, cs.y);
            }
        }
         if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            simEnabled = !simEnabled;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
//
        }
        {
            tickTimer += delta;
            final float tickPeriod = .5f;
            if (tickTimer >= tickPeriod) {
                tickTimer -= tickPeriod;
                for (Belt belt : belts) {
                    if (belt != null) {
                        belt.tick();
                    }
                }
            }
        }
        {
            Iterator<Item> it = items.iterator();
            while (it.hasNext()) {
                Item item = it.next();
                item.update(delta);
                if (item.done) {
                    it.remove();
                }
            }
        }

        enableBlending();
        renderer.setProjectionMatrix(gameCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.BLACK);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                renderer.rect(x, y, 1, 1);
            }
        }
        renderer.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        enableBlending();
        for (Belt belt : belts) {
            if (belt != null) {
                belt.render(renderer);
            }
        }
        for (Item item : items) {
            item.render(renderer);
        }

        renderer.end();
    }

    protected static float map (float value, float start1, float end1, float start2, float end2) {
        return start2 + ((end2 - start2) / (end1 - start1)) * (value - start1);
    }

    protected static Vector2[] v2s (float x1, float y1, float x2, float y2, float x3, float y3) {
        return new Vector2[] {new Vector2(x1, y1), new Vector2(x2, y2), new Vector2(x3, y3)};
    }

    protected static Vector2 v2 (float x, float y) {
        return new Vector2(x, y);
    }

    protected static class Item {
        public final static int ITEM_SIZE = 3;
        protected float x;
        protected float y;
        protected float despawn = 2;
        protected boolean done;
        // size of this array is the size of the item on the belt
        Array<Belt.Slot> slots = new Array<>(ITEM_SIZE);

        public Item (float x, float y) {
            this.x = x;
            this.y = y;
            // so set() works
            slots.size = ITEM_SIZE;
        }

        public void update (float delta) {
            if (despawn > 0) {
                despawn -= delta;
                done = despawn <= 0;
            }
            Belt.Slot slot = slots.get(ITEM_SIZE / 2);
            if (slot != null) {
                x = slot.tx;
                y = slot.ty;
            }
        }

        public void render (ShapeRenderer renderer) {
            renderer.setColor(Color.MAGENTA);
            renderer.circle(x, y, .15f, 8);
            renderer.setColor(Color.CYAN);
            for (Belt.Slot slot : slots) {
                if (slot != null) {
                    renderer.rectLine(x, y, slot.tx, slot.ty, .05f);
                }
            }

        }
    }

    protected static class Belt {
        public final static int BELT_STRAIGHT_SIZE = 9;
        public final static int BELT_CURVE_SIZE = 6;

        public static final int DIR_NORTH = 1;
        public static final int DIR_EAST = 2;
        public static final int DIR_SOUTH = 3;
        public static final int DIR_WEST = 4;
        public static final int DIR_ROT_LEFT = -1;
        public static final int DIR_ROT_RIGHT = 1;

        public static final int TYPE_STRAIGHT = 1;
        public static final int TYPE_CURVE = 2;
        public static final int TYPE_CURVE_REV = 3;
        private Array<Item> queue = new Array<>();
        // single item takes multiple slots
        Slot[] slots = new Slot[BELT_STRAIGHT_SIZE];
        int speed = 1;
        int x;
        int y;
        int type;
        int direction = DIR_NORTH;
        int index;

        public Belt (int x, int y, int type) {
            this.x = x;
            this.y = y;
            index = x + y * WIDTH;
            setType(type);
        }

        public void setType (int type) {
            this.type = type;
            switch (type) {
            case TYPE_STRAIGHT: {
                slots = new Slot[BELT_STRAIGHT_SIZE];
                for (int i = 0; i < BELT_STRAIGHT_SIZE; i++) {
                    float a = (i + .5f) / BELT_STRAIGHT_SIZE;
                    slots[i] = new Slot(this, .5f - a, 0);
                }
            }
            break;
            case TYPE_CURVE: {
                slots = new Slot[BELT_CURVE_SIZE];
                Vector2 tmp = new Vector2();
                for (int i = 0; i < BELT_CURVE_SIZE; i++) {
                    float a = (i + .5f) / BELT_CURVE_SIZE * 90;
                    tmp.set(0, .5f).rotate(a);
                    slots[i] = new Slot(this, tmp.x + .5f, tmp.y - .5f);
                }
            }
            break;
            case TYPE_CURVE_REV: {
                slots = new Slot[BELT_CURVE_SIZE];
                Vector2 tmp = new Vector2();
                for (int i = 0; i < BELT_CURVE_SIZE; i++) {
//                        float a = 90 - (i + .5f)/BELT_CURVE_SIZE * 90;
                    float a = -(i + .5f) / BELT_CURVE_SIZE * 90;
                    tmp.set(0, -.5f).rotate(a);
                    slots[i] = new Slot(this, tmp.x + .5f, tmp.y + .5f);
                }
            }
            break;
            }
        }

        public void render (ShapeRenderer renderer) {
            switch (type) {
            case TYPE_STRAIGHT: {
                renderer.setColor(Color.GOLDENROD);
                renderer.rect(x, y, 1, 1);
            }
            break;
            case TYPE_CURVE: {
                renderer.setColor(Color.ORANGE);
                renderer.rect(x, y, 1, 1);
            }
            break;
            case TYPE_CURVE_REV: {
                renderer.setColor(Color.ORANGE);
                renderer.rect(x, y, 1, 1);
            }
            break;
            }
            for (int i = 0, n = slots.length; i < n; i++) {
                Slot slot = slots[i];
                float a = .1f + i / (float)n * .8f;
                float r = .05f;
                if (slot.item != null) {
                    r = .075f;
                }
//                renderer.setColor(a, 0, 1-a, 1);
                renderer.setColor(a, a, a, 1);
                float sx = slot.tx;
                float sy = slot.ty;
                renderer.circle(sx, sy, r, 6);
            }

        }

        public void tick () {
            // TODO advance items by #speed slots
            if (queue.size > 0) {
                Item item = queue.peek();
                if (put(item)) {
                    queue.pop();
                }
            }
            for (int i = slots.length -2; i >= 0; i--) {
                // need a way to get next slot easily
                Slot next = slots[i + 1];
                Slot slot = slots[i];
                Item item = slot.item;
                if (next.item != null || item == null) continue;
                next.item = item;
                slot.item = null;
                int indexOf = item.slots.indexOf(slot, true);
                if (indexOf == -1) {
                    throw new AssertionError("Welp");
                }
                item.slots.set(indexOf, next);
            }
        }

        private boolean put (Item item) {
            // to put an item, we need x consecutive empty slots
            final int size = Item.ITEM_SIZE;
            // lets do this dumb way for now
            final Slot[] slots = this.slots;
            for (int i = 0, n = slots.length - (size -1); i < n; i++) {
                Slot slotA = slots[i];
                Slot slotB = slots[i + 1];
                Slot slotC = slots[i + 2];
                if (slotA.item == null && slotB.item == null && slotC.item == null) {
                    // middle slot probably makes sense
                    item.x = slotB.tx;
                    item.y = slotB.ty;
                    slotA.item = item;
                    slotB.item = item;
                    slotC.item = item;
                    item.slots.set(0, slotA);
                    item.slots.set(1, slotB);
                    item.slots.set(2, slotC);
                    return true;
                }
            }
            return false;
        }

        public void rotate (int direction) {
            this.direction = direction;
            for (Slot slot : slots) {
                slot.rotate(direction);
            }

        }

        public void enqueue (Item item) {
            queue.add(item);
        }

        protected static class Slot {
            private final Belt parent;
            public Item item;
            // relative to center of the belt
            public final float x;
            public final float y;
            public float tx, ty;

            public Slot (Belt parent, float x, float y) {
                this.parent = parent;
                this.x = x;
                this.y = y;
            }

            private static Vector2 v2 = new Vector2();

            public void rotate (int direction) {
                v2.set(x, y);
                switch (direction) {
                case DIR_NORTH: {
                    v2.rotate(90);
                }
                break;
                case DIR_EAST: {
                    // the default
                }
                break;
                case DIR_SOUTH: {
                    v2.rotate(270);
                }
                break;
                case DIR_WEST: {
                    v2.rotate(180);
                }
                break;
                }
                tx = parent.x + v2.x + .5f;
                ty = parent.y + v2.y + .5f;
            }
        }
    }

    @Override public boolean keyDown (int keycode) {
        switch (keycode) {
        case Input.Keys.SPACE: {
            simEnabled = !simEnabled;
        }
        break;
        }
        return true;
    }

    @Override public void resize (int width, int height) {
        gameViewport.update(width, height, true);
        guiViewport.update(width, height, true);
    }

    @Override public void dispose () {
        super.dispose();
    }

    public static void main (String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1280;
        config.height = 720;
        config.useHDPI = true;
        config.stencil = 8;
        // for mesh based sub pixel rendering multi sampling is required or post process aa
        config.samples = 4;
        PlaygroundGame.start(args, config, Transport5Test.class);
    }
}

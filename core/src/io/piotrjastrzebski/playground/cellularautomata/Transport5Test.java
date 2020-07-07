package io.piotrjastrzebski.playground.cellularautomata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
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

import java.util.Arrays;
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
    private GameMap map;

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

        map = new GameMap();

        if (true) {
            createLoop(0, 0);
            createLoopReverse(4, 0);
        }
    }

    private void createLoop (int x, int y) {
        map.setBelt(x + 0, y + 0, 3, 2);
        map.setBelt(x + 1, y + 0, 1, 2);
        map.setBelt(x + 2, y + 0, 3, 1);
        map.setBelt(x + 0, y + 1, 1, 3);
        map.setBelt(x + 2, y + 1, 1, 1);
        map.setBelt(x + 0, y + 2, 3, 3);
        map.setBelt(x + 1, y + 2, 1, 4);
        map.setBelt(x + 2, y + 2, 3, 4);
    }

    private void createLoopReverse (int x, int y) {
        map.setBelt(x + 0, y + 0, 2, 1);
        map.setBelt(x + 1, y + 0, 1, 4);
        map.setBelt(x + 2, y + 0, 2, 4);
        map.setBelt(x + 0, y + 1, 1, 1);
        map.setBelt(x + 2, y + 1, 1, 3);
        map.setBelt(x + 0, y + 2, 2, 2);
        map.setBelt(x + 1, y + 2, 1, 2);
        map.setBelt(x + 2, y + 2, 2, 3);
    }

    private void spawnItem(float x, float y) {
        Item item = new Item(x, y);
        items.add(item);

        Belt belt = map.getBelt((int)x, (int)y);
        if (belt != null) {
            belt.enqueue(item);
            item.despawn = -1;
        }
    }

    int direction = Belt.DIR_EAST;
    float tickTimer = 0;
    @Override public void render (float delta) {
        super.render(delta);
        // handle input
        int mx = (int)cs.x;
        int my = (int)cs.y;
        if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                map.setBelt(mx, my, Belt.TYPE_STRAIGHT, direction);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                map.setBelt(mx, my, Belt.TYPE_CURVE_CW, direction);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                map.setBelt(mx, my, Belt.TYPE_CURVE_CCW, direction);
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
                Belt belt = map.getBelt(mx, my);
                if (belt != null) {
                    belt.rotate(direction);
                }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//                setBelt(mx, my, 0);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
//                setBelt(mx, my, 0);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                map.clearBelt(mx, my);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                spawnItem(cs.x, cs.y);
            }
        }
         if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            simEnabled = !simEnabled;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            map.print();
        }
        {
            tickTimer += delta;
            final float tickPeriod = .5f;
            if (tickTimer >= tickPeriod) {
                tickTimer -= tickPeriod;
                map.tick();
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
        map.render(renderer);

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

    protected static class GameMap {
        Belt[] belts = new Belt[WIDTH * HEIGHT];

        public Belt getBelt (int x, int y) {
            int index = x + y * WIDTH;
            if (index < 0 || index > belts.length)
                return null;
            return belts[index];
        }

        public Belt setBelt (int x, int y, int type) {
//        System.out.println("Belt " + x + ", " + y + ", " + type);
            Belt belt = getBelt(x, y);
            if (belt == null) {
                belt = new Belt(this, x, y, type);
                belts[belt.index] = belt;
            } else {
                belt.setType(type);
            }
            return belt;
        }

        public void setBelt (int x, int y, int type, int direction) {
            Belt belt = setBelt(x, y, type);
            belt.rotate(direction);
        }

        public void clearBelt (int x, int y) {
//        System.out.println("Clear " + x + ", " + y);
            int index = x + y * WIDTH;
            belts[index] = null;
        }

        int tick = 0;
        public void tick () {
            for (Belt belt : belts) {
                if (belt != null) {
                    belt.tick(tick);
                }
            }
            for (Belt belt : belts) {
                if (belt != null) {
                    belt.swap();
                }
            }
            tick++;
        }

        public void render (ShapeRenderer renderer) {
            for (Belt belt : belts) {
                if (belt != null) {
                    belt.render(renderer);
                }
            }
        }

        public Belt getNext (Belt belt) {
            int tx = belt.x, ty = belt.y;
            switch (belt.direction) {
            case Belt.DIR_NORTH: {
                tx += 0;
                ty += 1;
            } break;
            case Belt.DIR_EAST: {
                tx += 1;
                ty += 0;
            } break;
            case Belt.DIR_SOUTH: {
                tx += 0;
                ty += -1;
            } break;
            case Belt.DIR_WEST: {
                tx += -1;
                ty += 0;
            } break;
            }
            if (tx < 0 || tx >= WIDTH) return null;
            if (ty < 0 || ty >= HEIGHT) return null;
            // check if we can connect to it
            Belt next = getBelt(tx, ty);
            if (next == null) return null;
            if (next.type == Belt.TYPE_STRAIGHT && belt.direction == next.direction) {
                return next;
            }
            if (next.type == Belt.TYPE_CURVE_CW && belt.direction == ccw(next.direction)) {
                return next;
            }
            if (next.type == Belt.TYPE_CURVE_CCW && belt.direction == cw(next.direction)) {
                return next;
            }
            return null;
        }

        private int cw (int direction) {
            switch (direction) {
            case Belt.DIR_NORTH: return Belt.DIR_EAST;
            case Belt.DIR_EAST: return Belt.DIR_SOUTH;
            case Belt.DIR_SOUTH: return Belt.DIR_WEST;
            case Belt.DIR_WEST: return Belt.DIR_NORTH;
            }
            return 0;
        }

        private int ccw (int direction) {
            switch (direction) {
            case Belt.DIR_NORTH: return Belt.DIR_WEST;
            case Belt.DIR_EAST: return Belt.DIR_NORTH;
            case Belt.DIR_SOUTH: return Belt.DIR_EAST;
            case Belt.DIR_WEST: return Belt.DIR_SOUTH;
            }
            return 0;
        }

        public void print () {
            for (Belt belt : belts) {
                if (belt != null) {
                    System.out.println("setBelt("+belt.x + ", " + belt.y + ", " + belt.type + ", " + belt.direction+");");
                }
            }
        }
    }

    protected static class Item {
        public final static int ITEM_SIZE = 3;
        static int ids = 0;
        protected int id = ++ids;
        protected float x;
        protected float y;
        protected float despawn = 2;
        protected boolean done;
        // size of this array is the size of the item on the belt
        Array<Belt.Slot> slots = new Array<>(ITEM_SIZE);

        public Item (float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void update (float delta) {
            if (despawn > 0) {
                despawn -= delta;
                done = despawn <= 0;
            }
            if (slots.size > 0) {
                x = 0;
                y = 0;
                for (Belt.Slot slot : slots) {
                    x += slot.tx;
                    y += slot.ty;
                }
                x /= slots.size;
                y /= slots.size;
            }
        }

        public void render (ShapeRenderer renderer) {
            renderer.setColor(Color.GOLD);
            renderer.circle(x, y, .15f, 8);
            float c = 0;
            for (Belt.Slot slot : slots) {
                renderer.setColor(c, 0, 1-c, 1);
                if (slot != null) {
//                    renderer.circle(slot.tx, slot.ty, .075f, 6);
//                    renderer.circle(slot.tx, slot.ty, .1f, 6);
//                    renderer.rectLine(slot.tx, slot.ty-.2f, slot.tx, slot.ty+.2f, .05f);
                }
                c += 1f/ITEM_SIZE;
            }

        }

        @Override public String toString () {
            return "Item{" + id + '}';
        }

        public void next (Belt.Slot slot) {
            slots.add(slot);
            while (slots.size > ITEM_SIZE) {
                slots.removeIndex(0);
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
        public static final int TYPE_CURVE_CW = 2;
        public static final int TYPE_CURVE_CCW = 3;
        private Array<Item> queue = new Array<>();
        // single item takes multiple slots
        Slot[] slots = new Slot[BELT_STRAIGHT_SIZE];
        int speed = 1;
        final GameMap map;
        int x;
        int y;
        int type;
        int direction = DIR_NORTH;
        int index;
        Belt next;

        public Belt (GameMap map, int x, int y, int type) {
            this.map = map;
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
                    slots[i] = new Slot(this, i, a - .5f, 0);
                }
            }
            break;
            case TYPE_CURVE_CW: {
                slots = new Slot[BELT_CURVE_SIZE];
                Vector2 tmp = new Vector2();
                for (int i = 0; i < BELT_CURVE_SIZE; i++) {
                    float a = 90 - (i + .5f) / BELT_CURVE_SIZE * 90;
                    tmp.set(0, .5f).rotate(a);
                    slots[i] = new Slot(this, i, tmp.x + .5f, tmp.y - .5f);
                }
            }
            break;
            case TYPE_CURVE_CCW: {
                slots = new Slot[BELT_CURVE_SIZE];
                Vector2 tmp = new Vector2();
                for (int i = 0; i < BELT_CURVE_SIZE; i++) {
                    float a = (i + .5f) / BELT_CURVE_SIZE * 90 - 90;
                    tmp.set(0, -.5f).rotate(a);
                    slots[i] = new Slot(this, i, tmp.x + .5f, tmp.y + .5f);
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
            case TYPE_CURVE_CW: {
                renderer.setColor(Color.ORANGE);
                renderer.rect(x, y, 1, 1);
            }
            break;
            case TYPE_CURVE_CCW: {
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

            if (next != null) {
//                renderer.setColor(Color.MAGENTA);
//                renderer.rectLine(x + .5f, y + .5f, next.x + .5f, next.y + .5f, .1f);
            }

        }

        public void swap() {
            for (Slot slot : slots) {
                slot.swap();
            }
        }

        public void tick (int tick) {
            // TODO advance items by #speed slots
            next = map.getNext(this);
            if (queue.size > 0) {
                Item item = queue.peek();
                if (put(item)) {
                    queue.pop();
                }
            }
            for (int s = 0; s < speed; s++) {
                Slot to = null;
                if (next != null) {
                    to = next.slots[0];
                }
                int slotId = slots.length - 1;
                do {
                    Slot from = slots[slotId--];
                    Item item = from.item;
                    if (to != null && (to.item == null || to.item == item) && item != null) {
                        to.itemNext = item;
                        item.next(to);
                    }
                    to = from;
                } while (slotId >= 0);
            }
        }

        private boolean put (Item item) {
            // to put an item, we need x consecutive empty slots
            final int size = Item.ITEM_SIZE;
            // lets do this dumb way for now
            final Slot[] slots = this.slots;
            for (int i = 0, n = slots.length - (size -1); i < n; i++) {
                boolean hasSpace = true;
                for (int j = 0; j < size; j++) {
                    Slot slot = slots[i + j];
                    if (slot.item != null) {
                        hasSpace = false;
                        break;
                    }
                }
                if (hasSpace) {
                    for (int j = size -1; j >= 0; j--) {
                        Slot slot = slots[i + j];
                        slot.item = item;
                        item.slots.add(slot);
                    }
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

        @Override public String toString () {
            return "Belt{" + "slots=" + Arrays.toString(slots) + ", x=" + x + ", y=" + y + ", type=" + type + ", direction="
                + direction + '}';
        }

        protected static class Slot {
            private final Belt parent;
            public Item item;
            public Item itemNext;
            private int id;
            // relative to center of the belt
            public final float x;
            public final float y;
            public float tx, ty;

            public Slot (Belt parent, int id, float x, float y) {
                this.parent = parent;
                this.id = id;
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

            @Override public String toString () {
                return id + ":"+ (item!=null?item.id:"_");
            }

            public void swap () {
                item = itemNext;
                itemNext = null;
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
        Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
        config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 4);
        PlaygroundGame.start(args, config, Transport5Test.class);
    }
}

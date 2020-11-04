package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 */
public class PolygonOverlapTest extends BaseScreen {
    private static final String TAG = PolygonOverlapTest.class.getSimpleName();

    Array<PolygonActor> polygonActors = new Array<>();

    public PolygonOverlapTest (GameReset game) {
        super(game);
        clear.set(.5f, .5f, .5f, 1);
        // probably should be counter clockwise
        float[] triangle = new float[] {
            -1, -.5f,
            1, -.5f,
            0, .5f
        };
        float[] rectangle = new float[] {
            -1, -1,
            1, -1,
            1, 1,
            -1, 1,
        };
        {
            // triangle
            PolygonActor pa = new PolygonActor();
            pa.color.set(1, 0, 1, .7f);
            pa.polygon.setVertices(triangle);
            pa.polygon.setRotation(MathUtils.random(360));
            pa.polygon.scale(MathUtils.random(3f, 4f));
            pa.polygon.setPosition(-3, 0);
            polygonActors.add(pa);
        }
        {
            // rectangle
            PolygonActor pa = new PolygonActor();
            pa.color.set(0, 1, 1, .7f);
            pa.polygon.setVertices(rectangle);
            pa.polygon.setRotation(MathUtils.random(360));
            pa.polygon.scale(MathUtils.random(3f, 4f));
            pa.polygon.setPosition(3, 0);
            polygonActors.add(pa);
        }
    }

    PolygonActor selected = null;

    Polygon translated = new Polygon();
    Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
    @Override public void render (float delta) {
        super.render(delta);
        enableBlending();
        renderer.setProjectionMatrix(gameCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        for (PolygonActor actor : polygonActors) {
            actor.render(renderer);
        }
        if (selected != null) {
            renderer.setColor(selected.color.r, selected.color.g, selected.color.b, 1);
            final Polygon sp = selected.polygon;
            renderer.polygon(sp.getTransformedVertices());

            for (PolygonActor pa : polygonActors) {
                if (pa == selected) continue;
                Rectangle sbb = sp.getBoundingRectangle();
                Rectangle obb = pa.polygon.getBoundingRectangle();
                if (!sbb.overlaps(obb)) continue;
                mtv.depth = 0;
                mtv.normal.set(0, 0);
                if (Intersector.overlapConvexPolygons(sp, pa.polygon, mtv)) {
                    translated.setVertices(sp.getVertices());
                    translated.setRotation(sp.getRotation());
                    translated.setOrigin(sp.getOriginX(), sp.getOriginY());
                    translated.setScale(sp.getScaleX(), sp.getScaleY());
                    float ox = mtv.normal.x * mtv.depth;
                    float oy = mtv.normal.y * mtv.depth;

                    translated.setPosition(sp.getX() + ox, sp.getY() + oy);
                    renderer.setColor(1, 0, 0, .5f);
                    renderer.polygon(translated.getTransformedVertices());

                    translated.setPosition(sp.getX() - ox, sp.getY() - oy);
                    renderer.setColor(0, 0, 1, .5f);
                    renderer.polygon(translated.getTransformedVertices());

                    renderer.setColor(Color.YELLOW);
                    renderer.line(sp.getX(), sp.getY(), sp.getX() + ox, sp.getY() + oy);
                }
            }

        }

        renderer.end();
    }

    private static class PolygonActor {
        Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
        Polygon polygon = new Polygon();

        public void render (ShapeRenderer renderer) {

            float[] vertices = polygon.getTransformedVertices();
            renderer.setColor(color);
            renderer.polygon(vertices);
        }
    }

    private Vector2 cp = new Vector2();
    private Vector2 selectedOffset = new Vector2();
    @Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        gameViewport.unproject(cp.set(screenX, screenY));
        selected = null;
        for (PolygonActor pa : polygonActors) {
            if (pa.polygon.contains(cp.x, cp.y)){
                selected = pa;
                selectedOffset.set(pa.polygon.getX() - cp.x, pa.polygon.getY() - cp.y);
                break;
            }
        }
        return true;
    }

    @Override public boolean touchDragged (int screenX, int screenY, int pointer) {
        gameViewport.unproject(cp.set(screenX, screenY));
        if (selected != null) {
            selected.polygon.setPosition(
                cp.x + selectedOffset.x,
                cp.y + selectedOffset.y
            );
        }
        return true;
    }

    @Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        gameViewport.unproject(cp.set(screenX, screenY));
        return true;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        if (selected != null) {
            selected.polygon.rotate(amountX * 5);
        }
        return super.scrolled(amountX, amountY);
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        PlaygroundGame.start(args, PolygonOverlapTest.class);
    }
}

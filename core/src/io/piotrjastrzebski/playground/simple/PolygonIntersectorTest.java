package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Arrays;

/**
 */
public class PolygonIntersectorTest extends BaseScreen {
    private static final String TAG = PolygonIntersectorTest.class.getSimpleName();

    public PolygonIntersectorTest (GameReset game) {
        super(game);

        float[] vertices = new float[] {1, 1, 2, 2, 3, 1 };
        Polygon p1 = new Polygon(vertices);
        Polygon p2 = new Polygon(vertices);
        Polygon overlap = new Polygon();

        boolean intersects = Intersector.intersectPolygons(p1, p2, overlap);
        System.out.println("same triangles intersect: " + intersects + ", overlap = " + Arrays.toString(overlap.getVertices()));

        float[] vertices2 = new float[] {1, 1, 2, 3, 3, 1 };
        Polygon p3 = new Polygon(vertices2);
        intersects = Intersector.intersectPolygons(p1, p3, overlap);
        System.out.println("diff triangles intersect: " + intersects + ", overlap = " + Arrays.toString(overlap.getVertices()));
    }


    @Override public void render (float delta) {
        super.render(delta);
        enableBlending();
    }

    // allow us to start this test directly
    public static void main (String[] args) {
        PlaygroundGame.start(args, PolygonIntersectorTest.class);
    }
}

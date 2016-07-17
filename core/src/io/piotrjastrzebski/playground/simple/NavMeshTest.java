package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.poly2tri.CDT;
import io.piotrjastrzebski.playground.poly2tri.Poly2Tri;
import io.piotrjastrzebski.playground.poly2tri.Shapes;
import io.piotrjastrzebski.playground.poly2tri.Shapes.Triangle;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class NavMeshTest extends BaseScreen {
	private static final String TAG = NavMeshTest.class.getSimpleName();

	Array<Obstacle> obstacles = new Array<>();
	NavMesh navMesh;
	CDT cdt;
	public NavMeshTest (GameReset game) {
		super(game);
		navMesh = new NavMesh();
		Collection<Shapes.Point> polyline = new ArrayList<>();
		// -VP_WIDTH/2 + .5f, -VP_HEIGHT/2 + .5f, VP_WIDTH/2-.5f, VP_HEIGHT/2-.5f
		polyline.add(new Shapes.Point(-VP_WIDTH / 2 + .5f, -VP_HEIGHT / 2));
		polyline.add(new Shapes.Point(-VP_WIDTH / 2 + .5f, VP_HEIGHT / 2));
		polyline.add(new Shapes.Point(VP_WIDTH / 2 - .5f, VP_HEIGHT / 2 - .5f));
		polyline.add(new Shapes.Point(VP_WIDTH / 2 + .5f, -VP_HEIGHT / 2));
		navMesh.build(-VP_WIDTH / 2 + .5f, -VP_HEIGHT / 2 + .5f, VP_WIDTH / 2 - .5f, VP_HEIGHT / 2 - .5f, obstacles);

//		ArrayList<Triangle> triangles = new ArrayList<>();
//		float[] bounds = new float[] {};
//		float[] points = new float[] {};
//		float[] hole = new float[] {};
//		Poly2Tri.triangulate(triangles, bounds, points, hole);
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			if (cs.x >= -VP_WIDTH/2 + 1 && cs.x <= VP_WIDTH/2 -2 && cs.y >= -VP_HEIGHT/2 + 1 && cs.y <= VP_HEIGHT/2 -2) {
				Obstacle obstacle = new Obstacle();
				obstacle.shape.set(cs.x - 1, cs.y - 1, 2, 2);
				obstacles.add(obstacle);

				navMesh.build(-VP_WIDTH/2 + .5f, -VP_HEIGHT/2 + .5f, VP_WIDTH/2-.5f, VP_HEIGHT/2-.5f, obstacles);
			}
		}

		enableBlending();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.DARK_GRAY);
		for (Obstacle obstacle : obstacles) {
			Rectangle rect = obstacle.shape;
			renderer.rect(rect.x, rect.y, rect.width, rect.height);
		}
		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Line);
		navMesh.draw(renderer);
		renderer.end();
	}

	private static class NavMesh {
		CDT cdt;
		Collection<Triangle> triangles;
		public void build (float x1, float y1, float x2, float y2, Array<Obstacle> obstacles) {
			Collection<Shapes.Point> polyline = new ArrayList<>();
			// -VP_WIDTH/2 + .5f, -VP_HEIGHT/2 + .5f, VP_WIDTH/2-.5f, VP_HEIGHT/2-.5f
			polyline.add(new Shapes.Point(x1, y1));
			polyline.add(new Shapes.Point(x1, y2));
			polyline.add(new Shapes.Point(x2, y2));
			polyline.add(new Shapes.Point(x2, y1));
			cdt = new CDT(polyline);
			cdt.triangulate();
			triangles = cdt.getTriangles();
		}

		public void draw(ShapeRenderer renderer) {
			renderer.setColor(Color.BLACK);
//			renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
			if (triangles != null) {
				for (Triangle triangle : triangles) {

				}
			}
		}
	}

	private static class Obstacle {
		public Rectangle shape = new Rectangle();

	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.useHDPI = true;
		config.stencil = 8;
		// for mesh based sub pixel rendering multi sampling is required or post process aa
		config.samples = 4;
		PlaygroundGame.start(args, config, NavMeshTest.class);
	}
}

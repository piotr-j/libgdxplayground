package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Simple trail behind touch drag
 * A lot of stuff left for optimizations
 */
public class SplineTrailTest extends BaseScreen {
	private final static int NUM_POINTS = 16;
	private Array<Vector2> points = new Array<>();
	private Array<Vector2> smoothed = new Array<>();
	private Mesh rect;
	private ShaderProgram program;
	public SplineTrailTest (GameReset game) {
		super(game);
		rect = new Mesh(true, 4, 6, new VertexAttribute(VertexAttributes.Usage.Position, 2,
			ShaderProgram.POSITION_ATTRIBUTE));
		rect.setVertices(new float[]{-.5f, -.5f, .5f, -.5f, .5f, .5f, -.5f, .5f, });
		rect.setIndices(new short[]{0, 1, 2, 2, 3, 0});
		// we need to use custom shader cus we dont use colors or tex coords
		program = new ShaderProgram(Gdx.files.internal("shaders/simple.vert"), Gdx.files.internal("shaders/simple.frag"));
		if (!program.isCompiled()) {
			Gdx.app.error("", "shader failed");
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.setProjectionMatrix(gameCamera.combined);
		program.begin();
		program.setUniformMatrix("u_projTrans", gameCamera.combined);
		rect.render(program, GL20.GL_TRIANGLES);
		if (mesh != null) {
//			mesh.setAutoBind(false);
			mesh.render(program, GL20.GL_TRIANGLE_STRIP);
		} else {
			smoothed.clear();
			smoothed.add(new Vector2(0, 0));
			smoothed.add(new Vector2(1, 0));
			smoothed.add(new Vector2(2, 0));
			smoothed.add(new Vector2(3, 0));
			createTriangleStrip(smoothed);
		}
		program.end();
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(Color.RED);
		for (int i = 0; i < smoothed.size - 1; i++) {
			Vector2 p1 = smoothed.get(i);
			Vector2 p2 = smoothed.get(i + 1);
			renderer.rectLine(p1, p2, 0.1f);
		}
		renderer.setColor(Color.GREEN);
		renderer.getColor().a = .75f;
		for (int i = 0; i < points.size - 1; i++) {
			Vector2 p1 = points.get(i);
			Vector2 p2 = points.get(i + 1);
			renderer.rectLine(p1, p2, 0.05f);
		}
		renderer.end();
	}

	private void addPoint(float x, float y) {
		points.insert(0, new Vector2(x, y));
		points.truncate(NUM_POINTS);
		resolve(points, smoothed);
	}

	private void simplify(Array<Vector2> points, float sqTolerance, Array<Vector2> out) {
		int len = points.size;

		Vector2 prevPoint = points.get(0);
		Vector2 tmp = new Vector2();
		out.clear();
		out.add(prevPoint);

		for (int i = 1; i < len; i++) {
			tmp = points.get(i);
			if (tmp.dst2(prevPoint) > sqTolerance) {
				out.add(tmp);
				prevPoint = tmp;
			}
		}
		if (!prevPoint.equals(tmp)) {
			out.add(tmp);
		}
	}

	private void smooth(Array<Vector2> input, Array<Vector2> output) {
		//expected size
		output.clear();
		output.ensureCapacity(input.size*2);

		//first element
		output.add(input.get(0));
		//average elements
		for (int i=0; i<input.size-1; i++) {
			Vector2 p0 = input.get(i);
			Vector2 p1 = input.get(i+1);

			Vector2 Q = new Vector2(0.75f * p0.x + 0.25f * p1.x, 0.75f * p0.y + 0.25f * p1.y);
			Vector2 R = new Vector2(0.25f * p0.x + 0.75f * p1.x, 0.25f * p0.y + 0.75f * p1.y);
			output.add(Q);
			output.add(R);
		}

		//last element
		output.add(input.get(input.size-1));
	}

	public static int iterations = 2;
	public static float simplifyTolerance = .5f;
	private Array<Vector2> tmp = new Array<>();

	public void resolve(Array<Vector2> input, Array<Vector2> output) {
		output.clear();
		if (input.size<=2) { //simple copy
			output.addAll(input);
			return;
		}

		//simplify with squared tolerance
		if (simplifyTolerance>0 && input.size>3) {
			simplify(input, simplifyTolerance * simplifyTolerance, tmp);
			input = tmp;
		}

		//perform smooth operations
		if (iterations<=0) { //no smooth, just copy input to output
			output.addAll(input);
		} else if (iterations==1) { //1 iteration, smooth to output
			smooth(input, output);
		} else { //multiple iterations.. ping-pong between arrays
			int iters = iterations;
			//subsequent iterations
			do {
				smooth(input, output);
				tmp.clear();
				tmp.addAll(output);
				Array<Vector2> old = output;
				input = tmp;
				output = old;
			} while (--iters > 0);
		}
		createTriangleStrip(smoothed);
	}
	private Vector2 dir = new Vector2();
	private Vector2 perp = new Vector2();
	private Vector2 tmp1 = new Vector2();
	private Vector2 tmp2 = new Vector2();
	private float thickness = .5f;
	private Mesh mesh;
	private void createTriangleStrip (Array<Vector2> smoothed) {
		if (mesh != null) mesh.dispose();
		// 2 for first point
		// 2 for last point
		// 2x2 * (size -2)  for mid points
		// (smoothed.size -1) * 4 = (smoothed.size -2) * 4 + 2 + 2
		float[] vertices = new float[(smoothed.size -1) * 4];
		short[] indices = new short[(smoothed.size) * 2 - 2];
		// this is obviously bad, reusing the mesh is optimal
		mesh = new Mesh(true, vertices.length, indices.length, new VertexAttribute(VertexAttributes.Usage.Position, 2,
			ShaderProgram.POSITION_ATTRIBUTE));
		Vector2 p1;
		Vector2 p2;
		p1 = smoothed.get(0);
		vertices[0] = p1.x;
		vertices[1] = p1.y;
		for (int i = 1; i < smoothed.size -1; i++) {
			p1 = smoothed.get(i - 1);
			p2 = smoothed.get(i);
			dir.set(p2).sub(p1).nor();
			float t = thickness - thickness * (i/(float)smoothed.size);
			perp.set(-dir.y, dir.x).scl(t/2);
			tmp1.set(p2).sub(perp);
			tmp2.set(p2).add(perp);
			vertices[i * 4 -2] = tmp1.x ;
			vertices[i * 4 + 1 -2] = tmp1.y ;
			vertices[i * 4 + 2 -2] = tmp2.x;
			vertices[i * 4 + 3 -2] = tmp2.y;
		}
		p1 = smoothed.get(smoothed.size -1);
		vertices[vertices.length -2] = p1.x;
		vertices[vertices.length -1] = p1.y;
		// we use strip, so this is simple
		for (int i = 0; i < indices.length; i++) {
			indices[i] = (short)i;
		}

		mesh.setVertices(vertices);
		mesh.setIndices(indices);
	}

	Vector3 tp = new Vector3();
	boolean dragging;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = true;
		points.clear();
		addPoint(tp.x, tp.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		if (!dragging) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		addPoint(tp.x, tp.y);
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		dragging = false;
		addPoint(tp.x, tp.y);
		return true;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, SplineTrailTest.class);
	}
}

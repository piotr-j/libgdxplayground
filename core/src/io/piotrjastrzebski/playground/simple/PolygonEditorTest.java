package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class PolygonEditorTest extends BaseScreen {
	private static final String TAG = PolygonEditorTest.class.getSimpleName();
	private Texture image;
	private PolygonSpriteBatch polyBatch;
	private PolygonEditor editor;

	public PolygonEditorTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
		polyBatch = new PolygonSpriteBatch();
		image = new Texture("badlogic.jpg");
		editor = new PolygonEditor();
		editor.init(-8, -8, 16, 16);
	}

	@Override public void render (float delta) {
		super.render(delta);
		polyBatch.enableBlending();
		polyBatch.setProjectionMatrix(gameCamera.combined);
		polyBatch.begin();
		polyBatch.setColor(Color.DARK_GRAY);
		polyBatch.draw(image, -8, -8, 16, 16);
		polyBatch.setColor(Color.WHITE);

		polyBatch.end();

		editor.update(delta);

		renderer.setProjectionMatrix(gameCamera.combined);
		enableBlending();
		renderer.begin(ShapeRenderer.ShapeType.Line);
		editor.drawLine(renderer);
		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		editor.drawFilled(renderer);
		renderer.end();
	}


	protected static class PolygonEditor {
		private Array<Vertex> vertices = new Array<>();

		public void init (float x, float y, float width, float height){
			// 0,1,2, 0,3,2
			// build a basic rect
			vertex(x, y);
			vertex(x + width, y);
			vertex(x + width, y + height);
			vertex(x, y + height);

			connect(0, 1);
			connect(0, 2);
			connect(1, 2);
			connect(0, 3);
			connect(3, 2);
		}

		private void vertex(float x, float y) {
			vertices.add(new Vertex(x, y, vertices.size));
		}

		private void connect (int v1, int v2) {
			connect(vertices.get(v1), vertices.get(v2));
		}

		private void connect (Vertex vertex1, Vertex vertex2) {
			vertex1.connections.add(vertex2);
			vertex2.connections.add(vertex1);
		}

		private void disconnect (Vertex vertex) {
			for (Vertex other : vertex.connections) {
				other.connections.removeValue(vertex, true);
			}
		}

		private void disconnect (Vertex vertex1, Vertex vertex2) {
			vertex1.connections.removeValue(vertex2, true);
			vertex2.connections.removeValue(vertex1, true);
		}

		private void remove(Vertex vertex) {
			vertices.removeValue(vertex, true);
			disconnect(vertex);
			for (int i = 0; i < vertices.size; i++) {
				vertices.get(i).id = i;
			}
		}

		public void drawFilled(ShapeRenderer renderer) {
			if (selected != null && dragged == null) {
				renderer.setColor(Color.ORANGE);
				renderer.circle(selected.pos.x, selected.pos.y, .25f, 16);
			}
			for (Vertex vertex : vertices) {
				renderer.setColor(Color.GREEN);
				renderer.circle(vertex.pos.x, vertex.pos.y, .15f, 8);
			}
		}

		public void drawLine(ShapeRenderer renderer) {
			for (Vertex vertex : vertices) {
				renderer.setColor(0, 1, 1, .5f);
				for (Vertex other : vertex.connections) {
					renderer.line(vertex.pos, other.pos);
				}
			}

		}

		protected Vertex selected;
		protected Vertex dragged;
		public void touchDown (float x, float y) {
			if (doubleClick > 0) {
				Vertex at = null;
				for (Vertex vertex : vertices) {
					if (vertex.pos.epsilonEquals(x, y, .25f)) {
						at = vertex;
						break;
					}
				}
				if (at != null) {
					remove(at);
				} else {
					vertex(x, y);
				}
			} else {
				doubleClick = .25f;
				boolean link = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && selected != null;
				if (!link) selected = null;
				for (Vertex vertex : vertices) {
					if (vertex.pos.epsilonEquals(x, y, .25f)) {
						if (link) {
							if (selected.connections.contains(vertex, true)) {
								disconnect(selected, vertex);
							} else {
								connect(selected, vertex);
							}
						} else {
							dragged = vertex;
							selected = vertex;
						}
						break;
					}
				}
			}
		}

		public void touchDragged (float x, float y) {
			if (dragged != null) {
				dragged.pos.set(x, y);
			}
		}

		public void touchUp (float x, float y) {
			if (dragged != null) {
				dragged = null;
			}
		}

		private float doubleClick;
		public void update (float delta) {
			if (doubleClick > 0) doubleClick -= delta;
		}

		protected static class Vertex {
			public int id;
			public Vector2 pos = new Vector2();
			public Array<Vertex> connections = new Array<>();

			public Vertex (float x, float y, int id) {
				pos.set(x, y);
				this.id = id;
			}
		}
	}


	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		editor.touchDown(cs.x, cs.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		editor.touchDragged(cs.x, cs.y);
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		editor.touchUp(cs.x, cs.y);
		return true;
	}

	@Override public void dispose () {
		super.dispose();
		polyBatch.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, PolygonEditorTest.class);
	}
}

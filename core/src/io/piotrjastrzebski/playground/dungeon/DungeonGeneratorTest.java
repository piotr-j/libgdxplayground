package io.piotrjastrzebski.playground.dungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

import java.util.Comparator;
import java.util.Random;

/**
 * Created by PiotrJ on 02/09/15.
 */
public class DungeonGeneratorTest extends BaseScreen {
	private int roomID;
	Array<Room> rooms = new Array<>();
	private float gridSize;
	private float ellipseWidth, ellipseHeight;
	private float roomWidth, roomHeight;
	private float mainRoomScale;
	private float reconnectChance;

	World b2d;
	Box2DDebugRenderer b2dd;
	Vector2 tmp = new Vector2();
	Grid grid;
	boolean drawBodies;
	public DungeonGeneratorTest (GameReset game) {
		super(game);
		b2d = new World(new Vector2(), true);
		b2dd = new Box2DDebugRenderer();

		gridSize = .25f;
		ellipseWidth = 20*gridSize;
		ellipseHeight = 10*gridSize;
		roomWidth = 4*gridSize;
		roomHeight = 4*gridSize;
		mainRoomScale = 1.3f;
		reconnectChance = 0.2f;
		grid = new Grid();
		grid.setSize(gridSize);
		restart();
	}

	private void restart () {
		graph.clear();
		mainRooms.clear();
		if (rooms.size > 0) {
			for (Room room : rooms) {
				b2d.destroyBody(room.body);
			}
		}
		rooms.clear();

		for (int i = 0; i < 100; i++) {
			Room room = new Room(roomID++, gridSize);
			float w = Utils.roundedRngFloat(roomWidth, gridSize);
			if (w < 0) w = -w;
			float h = Utils.roundedRngFloat(roomHeight, gridSize);
			if (h < 0) h = -h;
			if (w < gridSize || h < gridSize) continue;
			Utils.roundedPointInEllipse(ellipseWidth, ellipseHeight, gridSize, tmp);
			room.set(tmp.x, tmp.y, w, h);
			createBody(room);
			rooms.add(room);
		}
	}

	private void createBody (Room room) {
		Rectangle b = room.bounds;
		if (b.width < 0.1f || b.height < 0.1f) {
			return;
		}
		BodyDef bodyDef = new BodyDef();
		bodyDef.fixedRotation = true;
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(b.x, b.y);
		Body body = b2d.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(b.width / 2, b.height / 2);

		FixtureDef fd = new FixtureDef();
		fd.restitution = 0;
		fd.friction = 0.5f;
		fd.density = 1;
		fd.shape = shape;

		body.createFixture(fd);

		shape.dispose();

		room.body = body;
	}

	int pIters = 1000;
	@Override public void render (float delta) {
		super.render(delta);
		boolean settled = true;
		for (int i = 0; i < pIters; i++) {
			b2d.step(0.1f, 12, 8);
			for (Room room : rooms) {
				if (!room.isSleeping()) {
					settled = false;
					break;
				}
			}
			if (settled) break;
		}

		if (drawBodies){
			b2dd.render(b2d, gameCamera.combined);
		}
		Gdx.gl.glEnable(GL20.GL_BLEND);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);

		grid.draw(renderer);
		for (Room room : rooms) {
			room.update();
			room.draw(renderer);
		}
		if (settled && mainRooms.size == 0) {
			float mw = roomWidth * mainRoomScale;
			float mh = roomHeight * mainRoomScale;
			for (Room room : rooms) {
				if (room.bounds.width >= mw && room.bounds.height >= mh) {
					room.isMain = true;
					mainRooms.add(room);
				}
			}
			// sort so main rooms are drawn lsat
			rooms.sort(new Comparator<Room>() {
				@Override public int compare (Room o1, Room o2) {
					return Boolean.compare(o1.isMain, o2.isMain);
				}
			});
			triangulate();
		}
		graph.render(renderer);
		renderer.end();
	}

	Array<Room> mainRooms = new Array<>();
	RoomGraph graph = new RoomGraph();
	private void triangulate () {
		DelaunayTriangulator triangulator = new DelaunayTriangulator();

		float[] points = new float[mainRooms.size * 2];
		for (int i = 0; i < points.length; i+=2) {
			Room room = mainRooms.get(i / 2);
			points[i] = room.cx();
			points[i+1] = room.cy();
		}

		ShortArray indicies = triangulator.computeTriangles(points, 0, points.length, false);

		graph.clear();

		for (int i = 0; i < indicies.size; i += 3) {
			int p1 = indicies.get(i) * 2;
			int p2 = indicies.get(i + 1) * 2;
			int p3 = indicies.get(i + 2) * 2;
			// this is pretty dumb...
			Room roomA = getRoom(points[p1], points[p1 + 1]);
			Room roomB = getRoom(points[p2], points[p2 + 1]);
			Room roomC = getRoom(points[p3], points[p3 + 1]);
			graph.add(roomA, roomB);
			graph.add(roomA, roomC);
			graph.add(roomB, roomC);
		}

		createMST();
	}

	private void createMST () {
		/*
		kruskal's algorithm, we dont need anything fancy
		 */
		Array<RoomEdge> edges = graph.getEdges();
		edges.sort(new Comparator<RoomEdge>() {
			@Override public int compare (RoomEdge o1, RoomEdge o2) {
				return Float.compare(o1.len, o2.len);
			}
		});
		RoomGraph mstGraph = new RoomGraph();
		for (RoomEdge edge : edges) {
			if (!mstGraph.isConnected(edge)) {
				mstGraph.add(edge);
				edge.mst = true;
			}
		}

		for (RoomEdge edge : edges) {
			if (!edge.mst && MathUtils.random() < reconnectChance) {
				edge.mst = true;
				edge.recon = true;
				Gdx.app.log("", "recon " + edge);
			}
		}
	}

	private Room getRoom (float cx, float cy) {
		for (Room room : mainRooms) {
			if (MathUtils.isEqual(cx, room.cx()) && MathUtils.isEqual(cy, room.cy())) {
				return room;
			}
		}
		return null;
	}

	Color gridColor = new Color(0.25f, 0.25f, 0.25f, 0.25f);
	private void drawGrid (float size, float w, float h) {
		renderer.setColor(gridColor);
		int hSegments = (int)(w / size);
		for (int i = 0; i < hSegments/2; i++) {
			float x = i * size;
			renderer.line(x, -h / 2, x, h / 2);
			renderer.line(-x, -h / 2, -x, h / 2);
		}
		int vSegments = (int)(h / size);
		for (int i = 0; i < vSegments/2; i++) {
			float y = i * size;
			renderer.line(-w / 2, y, w / 2, y);
			renderer.line(-w / 2, -y, w / 2, -y);
		}
	}


	@Override public void dispose () {
		super.dispose();
		b2d.dispose();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		if (grid != null)
			grid.setViewPort(width, height);
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.SPACE:
			restart();
			break;
		case Input.Keys.B:
			drawBodies = !drawBodies;
			break;
		case Input.Keys.Q:
			if (pIters == 1) {
				pIters = 1000;
			} else {
				pIters = 1;
			}
			break;
		}
		return super.keyDown(keycode);
	}

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.useHDPI = true;
		new LwjglApplication(new Game() {
			@Override public void create () {
				setScreen(new DungeonGeneratorTest(new Reset()));
			}
		}, config);
	}

	public static class Reset implements GameReset {
		@Override public void reset () {
			Gdx.app.exit();
		}
	}
}

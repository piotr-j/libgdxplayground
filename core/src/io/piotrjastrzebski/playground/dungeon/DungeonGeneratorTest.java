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
import com.badlogic.gdx.utils.ObjectMap;
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
			float w = roundToSize(rngFloat(roomWidth), gridSize);
			if (w < 0) w = -w;
			float h = roundToSize(rngFloat(roomHeight), gridSize);
			if (h < 0) h = -h;
			if (w < gridSize || h < gridSize) continue;
			pointInEllipse(ellipseWidth, ellipseHeight, gridSize, tmp);
			room.set(roundToSize(tmp.x, gridSize), roundToSize(tmp.y, gridSize), w, h);
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

		drawGrid(gridSize, gameViewport.getWorldWidth(), gameViewport.getWorldHeight());
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

	public static class Room {
		public int id;
		public Rectangle bounds = new Rectangle();
		public Body body;
		public float gridSize;
		public boolean isMain;

		public Room (int id, float gridSize) {
			this.id = id;
			this.gridSize = gridSize;
		}

		public void update () {
			if (body == null) return;
			Vector2 pos = body.getPosition();
			bounds.setPosition(roundToSize(pos.x - bounds.width / 2, gridSize), roundToSize(pos.y - bounds.height / 2, gridSize));
		}

		public boolean isSleeping () {
			if (body == null) return false;
			return !body.isAwake();
		}

		public void draw (ShapeRenderer renderer) {
			if (body == null) {
				renderer.setColor(Color.BLUE);
			} else if (isMain) {
				renderer.setColor(Color.RED);
			} else if (isSleeping()){
				renderer.setColor(Color.GRAY);
			} else {
				renderer.setColor(Color.CYAN);
			}
			renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		public void set (float x, float y, float w, float h) {
			if (w < 0) w = -w;
			if (h < 0) h = -h;
			bounds.set(x, y, w, h);
		}

		public float cx () {
			return bounds.x + bounds.width / 2;
		}

		public float cy () {
			return bounds.y + bounds.height / 2;
		}

		@Override public String toString () {
			return "Room{" +
				"id=" + id +
				'}';
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Room room = (Room)o;

			if (id != room.id)
				return false;
			if (Float.compare(room.gridSize, gridSize) != 0)
				return false;
			if (isMain != room.isMain)
				return false;
			if (bounds != null ? !bounds.equals(room.bounds) : room.bounds != null)
				return false;
			return !(body != null ? !body.equals(room.body) : room.body != null);

		}

		@Override public int hashCode () {
			int result = id;
			result = 31 * result + (bounds != null ? bounds.hashCode() : 0);
			result = 31 * result + (body != null ? body.hashCode() : 0);
			result = 31 * result + (gridSize != +0.0f ? Float.floatToIntBits(gridSize) : 0);
			result = 31 * result + (isMain ? 1 : 0);
			return result;
		}
	}

	public static class RoomGraph {
		Array<RoomEdge> edges = new Array<>();
		ObjectMap<Room, RoomNode> roomToNode = new ObjectMap<>();
		Array<RoomNode> nodes = new Array<>();


		public RoomGraph () {}

		public void add (RoomEdge edge) {
			add(edge.roomA, edge.roomB);
		}

		public void add (Room roomA, Room roomB) {
			RoomEdge edge = new RoomEdge();
			edge.set(roomA, roomB);
			if (!edges.contains(edge, false)) {
				edges.add(edge);
			}
			addNode(roomA, roomB);
			addNode(roomB, roomA);
		}

		private void addNode(Room roomA, Room roomB) {
			RoomNode nodeA = roomToNode.get(roomA);
			if (nodeA == null) {
				nodeA = new RoomNode();
				nodeA.room = roomA;
				roomToNode.put(roomA, nodeA);
				nodes.add(nodeA);
			}
			nodeA.add(roomB);
		}

		public void render(ShapeRenderer renderer) {
			if (edges.size == 0) return;
			for (RoomEdge e : edges) {
				if (e.recon) {
					renderer.setColor(Color.CYAN);
				} else if (e.mst) {
					renderer.setColor(Color.YELLOW);
				} else {
//					renderer.setColor(Color.GREEN);
					renderer.setColor(Color.CLEAR);
				}
				renderer.line(e.ax(), e.ay(), e.bx(), e.by());
			}
		}

		public void clear () {
			edges.clear();
			roomToNode.clear();
			nodes.clear();
		}

		public Array<RoomEdge> getEdges () {
			return edges;
		}

		Array<RoomNode> open = new Array<>();
		Array<RoomNode> closed = new Array<>();
		public boolean isConnected (RoomEdge edge) {
			if (edges.size == 0) return false;
			// find if there is an existing path between edge.roomA and edge.roomB
			Room start = edge.roomA;
			Room target = edge.roomB;
			RoomNode sNode = roomToNode.get(start);
			// start not yet in graph
			if (sNode == null) return false;

			closed.clear();
			open.clear();
			open.add(sNode);
			while (open.size > 0) {
				RoomNode first = open.get(0);
				closed.add(first);
				open.removeIndex(0);
				for(RoomEdge e : first.edges) {
					RoomNode node = roomToNode.get(e.roomB);
					if (node.room == target) {
						return true;
					}
					if (!closed.contains(node, true)) {
						open.add(node);
					}
				}
			}
			return false;
		}
	}

	public static class RoomNode {
		public Room room;
		public Array<RoomEdge> edges = new Array<>();

		public void add (Room add) {
			RoomEdge edge = new RoomEdge();
			edge.roomA = room;
			edge.roomB = add;
			edges.add(edge);
		}

		public void add(RoomEdge add) {
			RoomEdge edge = new RoomEdge();
			if (add.roomA == room) {
				edge.roomA = add.roomA;
				edge.roomB = add.roomB;
			} else {
				edge.roomA = add.roomB;
				edge.roomB = add.roomA;
			}
			edges.add(edge);
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			RoomNode roomNode = (RoomNode)o;

			if (room != null ? !room.equals(roomNode.room) : roomNode.room != null)
				return false;
			return !(edges != null ? !edges.equals(roomNode.edges) : roomNode.edges != null);

		}

		@Override public int hashCode () {
			int result = room != null ? room.hashCode() : 0;
			result = 31 * result + (edges != null ? edges.hashCode() : 0);
			return result;
		}

		@Override public String toString () {
			return "RoomNode{" +
				"room=" + room +
				'}';
		}
	}

	public static class RoomEdge {
		private static Vector2 tmp = new Vector2();
		public Room roomA;
		public Room roomB;
		public float len;
		public boolean mst;
		public boolean recon;

		public void set(Room roomA, Room roomB) {
			this.roomA = roomA;
			this.roomB = roomB;
			len = tmp.set(roomA.cx(), roomA.cy()).sub(roomB.cx(), roomB.cy()).len();
		}

		public float ax () {
			return roomA.cx();
		}

		public float ay () {
			return roomA.cy();
		}

		public float by () {
			return roomB.cy();
		}

		public float bx () {
			return roomB.cx();
		}

		@Override public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			RoomEdge edge = (RoomEdge)o;
			if (edge.roomA == roomA && edge.roomB == roomB) return true;
			if (edge.roomA == roomB && edge.roomB == roomA) return true;
			return false;
		}

		@Override public int hashCode () {
			int result = roomA != null ? roomA.hashCode() : 0;
			result = 31 * result + (roomB != null ? roomB.hashCode() : 0);
			result = 31 * result + (len != +0.0f ? Float.floatToIntBits(len) : 0);
			return result;
		}

		@Override public String toString () {
			return "RoomEdge{" +
				"roomA=" + roomA +
				", roomB=" + roomB +
				", mst=" + mst +
				'}';
		}
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

	public static Vector2 pointInCircle(float radius, float size, Vector2 out) {
		return pointInEllipse(radius * 2, radius * 2, size, out);
	}

	public static Vector2 pointInEllipse (float width, float height, float size, Vector2 out) {
		float t = (float)(MathUtils.random() * Math.PI * 2);
		float u = MathUtils.random() + MathUtils.random();
		float r = (u > 1)?(2 - u):u;
		out.set(
			width * r * MathUtils.cos(t) / 2,
			height * r * MathUtils.sin(t) / 2
		);
		return out;
	}

	public static float roundToSize(float value, float size) {
		return MathUtils.floor(((value + size - 1)/size))*size;
	}

	private static Random rng = new Random(TimeUtils.millis());
	public static float rngFloat() {
		return (float)(rng.nextGaussian());
	}

	public static float rngFloat(float max) {
		return rngFloat() * max;
	}

	public static float rngFloat(float min ,float max) {
		return min + rngFloat() * (max - min);
	}

	@Override public void dispose () {
		super.dispose();
		b2d.dispose();
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

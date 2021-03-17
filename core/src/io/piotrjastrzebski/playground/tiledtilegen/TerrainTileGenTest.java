package io.piotrjastrzebski.playground.tiledtilegen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Created by PiotrJ on 20/06/15.
 */
@SuppressWarnings("Duplicates")
public class TerrainTileGenTest extends BaseScreen {
	Texture raw;
	TextureRegion base;
	PolygonSpriteBatch polyBatch;
	Array<TiledRegion> tiledRegions;
	PolyOrthoTiledMapRenderer mapRenderer;

	public TerrainTileGenTest (GameReset game) {
		super(game);
		VisTextButton reload = new VisTextButton("Reload");
		root.add(reload).expand().left().top().pad(10);
		reload.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				generateTiles();
			}
		});

		raw = new Texture(Gdx.files.internal("tiled/terrain.png"));
		// cant have this for packing/save
//		raw.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		polyBatch = new PolygonSpriteBatch();
		base = new TextureRegion(raw);

		tiledRegions = new Array<>();
		generateTiles();
		packTiles("template/", 32, Gdx.files.external(".atlas/atlas2"));

		TiledMap map = new TmxMapLoader().load("tiled/simple.tmx");
		mapRenderer = new PolyOrthoTiledMapRenderer(map, INV_SCALE, polyBatch);

		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
		int width = layer.getWidth();
		int height = layer.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				TiledMapTileLayer.Cell cell = layer.getCell(x, y);
				if (cell == null)
					continue;
				TiledMapTile tile = cell.getTile();
				if (tile == null)
					continue;
				PolyOrthoTiledMapRenderer.PolygonTiledMapTile mapTile;
				switch (tile.getId()) {
				case 1:
					mapTile = new PolyOrthoTiledMapRenderer.PolygonTiledMapTile(tiledRegions.first());
					mapTile.setId(tile.getId());
					cell.setTile(mapTile);
					break;
				case 2:
//					mapTile = new PolyOrthoTiledMapRenderer.PolygonTiledMapTile(tiledRegions.get(15));
//					mapTile.setId(tile.getId());
//					cell.setTile(mapTile);
					break;
				default:
//				mapTile = new PolyOrthoTiledMapRenderer.PolygonTiledMapTile(tiledRegions.get(tiledRegions.size-1));
				}
			}
		}
	}

	private void packTiles (String name, int pixelSize, FileHandle saveFile) {
		IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
		Gdx.gl20.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, intBuffer);
		// we probably dont need full size map, would be nice to pick semi optimal size
		int size = Math.min(intBuffer.get(), 256 + 64);
		FrameBuffer tileFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		PixmapPacker packer = new PixmapPacker(size, size, Pixmap.Format.RGBA8888, 2, true);

		// blending multiple times will break stuff
		polyBatch.disableBlending();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		for (TiledRegion region : tiledRegions) {
			TiledType type = region.type;
			tileFBO.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			polyBatch.begin();
			polyBatch.draw(region, 0, 0, pixelSize, pixelSize);
			polyBatch.end();
			Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, 32, 32);
			tileFBO.end();
			flipPM(pm);
			packer.pack(name + type.name(), pm);
		}

		PixmapPackerIO packerIO = new PixmapPackerIO();
		PixmapPackerIO.SaveParameters params = new PixmapPackerIO.SaveParameters();
		try {
			packerIO.save(saveFile, packer, params);
		} catch (IOException e) {
			e.printStackTrace();
		}

		packer.dispose();
		tileFBO.dispose();
		polyBatch.enableBlending();
	}

	private void flipPM (Pixmap pm) {
		int w = pm.getWidth();
		int h = pm.getHeight();
		final ByteBuffer pixels = pm.getPixels();
		final int numBytes = w * h * 4;
		byte[] lines = new byte[numBytes];
		final int numBytesPerLine = w * 4;
		for (int i = 0; i < h; i++) {
			pixels.position((h - i - 1) * numBytesPerLine);
			pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
		}
		pixels.clear();
		pixels.put(lines);
	}

	private void generateTiles () {
		tiledRegions.clear();
		for (TiledType tiledType : TiledType.values()) {
			tiledRegions.add(new TiledRegion(new TextureRegion(base), tiledType, 32));
		}
	}

	public void render (float delta) {
		// nice smooth background color
		float L = 255 / 255f;
		Gdx.gl.glClearColor(L, L, L, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float scale = (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) ? 25 : 1;
		scale *= delta * 0.1f;
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			gameCamera.position.x -= scale;
		} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			gameCamera.position.x += scale;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			gameCamera.position.y += scale;
		} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			gameCamera.position.y -= scale;
		}
		gameCamera.update();

//		mapRenderer.setView(gameCamera);
//		mapRenderer.render();

		polyBatch.setProjectionMatrix(gameCamera.combined);
		polyBatch.begin();
		polyBatch.draw(raw, -19, -2, 10, 6);

		for (int i = 0; i < tiledRegions.size; i++) {
			TiledRegion tiledRegion = tiledRegions.get(i);
			polyBatch.draw(tiledRegion, (i / 9) * 2.5f, 9 - (i % 9) * 2.5f, 2, 2);
		}

		polyBatch.end();

		stage.act(delta);
		stage.draw();
	}

	public static class TiledRegion extends PolygonRegion {
		public TiledType type;

		public TiledRegion (TextureRegion region, TiledType type, int size) {
			// we need copy, as we will modify it later
			super(region, type.getVerticesCopy(), type.triangles);
			this.type = type;

			float u = region.getU(), v = region.getV();
			float uvWidth = region.getU2() - u;
			float uvHeight = region.getV2() - v;

			// crap do we need to inset the regions a bit, for bleeding? we dont have a margin
			float[] vertices = getVertices();
			float[] textureCoords = getTextureCoords();
			for (int i = 0, n = vertices.length; i < n; i++) {
				textureCoords[i] = u + uvWidth * vertices[i];
				i++;
				textureCoords[i] = v + uvHeight * (1 - vertices[i]);
			}

			// expand packed vertices to they match region size
			// region must be square and the base region is 2 tiles high
			System.arraycopy(type.packedVertices, 0, vertices, 0, vertices.length);
			getRegion().setRegion(0, 0, size, size);
		}
	}

	/**
	 * Tiled stuff:
	 * targets:
	 * AB
	 * CD
	 */
	public static class TiledDataBuilder {
		// dimensions of tiled asset in pixels
		public static float width = 128 + 32;
		public static float height = 96;
		public static final int MAX_QUADS = 4;
		private float[] vertices;
		// vertices for rendering
		private float[] packedVertices;
		private int numVertices;
		// indices for triangles from vertices
		private short[] triangles;
		private int numTriangles;
		private short quads;
		private short[] indices = new short[] {0, 1, 2, 0, 3, 2};
		private TiledData data = new TiledData();

		public TiledDataBuilder () {
			vertices = new float[8 * MAX_QUADS];
			packedVertices = new float[8 * MAX_QUADS];
			triangles = new short[6 * MAX_QUADS];
		}

		// fix for bleeding issues
		private float fix = 0.0f;

		public TiledDataBuilder quad (int sx, int sy, int sw, int sh, int tx, int ty, int tw, int th) {
			if (quads >= MAX_QUADS)
				throw new IllegalStateException("Can only add " + MAX_QUADS + " quads!");

			int vo = numVertices;
			float nsx = sx / width;
			float nsy = sy / height;
			vertices[vo++] = nsx + fix;
			vertices[vo++] = nsy + fix;

			vertices[vo++] = nsx + sw / width - fix;
			vertices[vo++] = nsy + fix;

			vertices[vo++] = nsx + sw / width - fix;
			vertices[vo++] = nsy + sh / height - fix;

			vertices[vo++] = nsx + fix;
			vertices[vo++] = nsy + sh / height - fix;

			vo = numVertices;
			packedVertices[vo++] = tx;
			packedVertices[vo++] = ty;

			packedVertices[vo++] = tx + tw;
			packedVertices[vo++] = ty;

			packedVertices[vo++] = tx + tw;
			packedVertices[vo++] = ty + th;

			packedVertices[vo++] = tx;
			packedVertices[vo++] = ty + th;

			int to = numTriangles;
			for (int i = 0; i < indices.length; i++) {
				triangles[i + to] = (short)(indices[i] + numVertices / 2);
			}
			numTriangles += 6;
			numVertices += 8;
			quads++;
			return this;
		}

		public TiledDataBuilder toA (int x, int y, int width, int height) {
			return quad(x, y, width, height, 0, 16, 16, 16);
		}

		public TiledDataBuilder toB (int x, int y, int width, int height) {
			return quad(x, y, width, height, 16, 16, 16, 16);
		}

		public TiledDataBuilder toC (int x, int y, int width, int height) {
			return quad(x, y, width, height, 0, 0, 16, 16);
		}

		public TiledDataBuilder toD (int x, int y, int width, int height) {
			return quad(x, y, width, height, 16, 0, 16, 16);
		}

		public TiledDataBuilder toAB (int x, int y, int width, int height) {
			return quad(x, y, width, height, 0, 16, 32, 16);
		}

		public TiledDataBuilder toBD (int x, int y, int width, int height) {
			return quad(x, y, width, height, 16, 0, 16, 32);
		}

		public TiledDataBuilder toCD (int x, int y, int width, int height) {
			return quad(x, y, width, height, 0, 0, 32, 16);
		}

		public TiledDataBuilder toAC (int x, int y, int width, int height) {
			return quad(x, y, width, height, 0, 0, 16, 32);
		}

		public TiledDataBuilder toFull (int x, int y, int width, int height) {
			return quad(x, y, width, height, 0, 0, 32, 32);
		}

		public TiledDataBuilder fullABCD () {
			return quad(96, 32, 32, 32, 0, 0, 32, 32);
		}

		public TiledDataBuilder innerA () {
			return toA(0, 80, 16, 16);
		}

		public TiledDataBuilder innerAB () {
			return toB(16, 80, 16, 16);
		}

		public TiledDataBuilder innerAC () {
			return toC(0, 64, 16, 16);
		}

		public TiledDataBuilder innerB () {
			return toB(48, 80, 16, 16);
		}

		public TiledDataBuilder innerBA () {
			return toA(32, 80, 16, 16);
		}

		public TiledDataBuilder innerBD () {
			return toD(48, 64, 16, 16);
		}

		public TiledDataBuilder innerC () {
			return toC(0, 32, 16, 16);
		}

		public TiledDataBuilder innerCA () {
			return toA(0, 48, 16, 16);
		}

		public TiledDataBuilder innerCD () {
			return toD(16, 32, 16, 16);
		}

		public TiledDataBuilder innerD () {
			return toD(48, 32, 16, 16);
		}

		public TiledDataBuilder innerDC () {
			return toC(32, 32, 16, 16);
		}

		public TiledDataBuilder innerDB () {
			return toB(48, 48, 16, 16);
		}

		// NW
		public TiledDataBuilder cornerA () {
			return toA(128, 16, 16, 16);
		}

		// NE
		public TiledDataBuilder cornerB () {
			return toB(80, 16, 16, 16);
		}

		// SW
		public TiledDataBuilder cornerC () {
			return toC(128, 64, 16, 16);
		}

		// SE
		public TiledDataBuilder cornerD () {
			return toD(80, 64, 16, 16);
		}

		public TiledDataBuilder sideAB () {
			return toCD(96, 64, 32, 16);
		}

		public TiledDataBuilder sideCD () {
			return toAB(96, 16, 32, 16);
		}

		public TiledDataBuilder sideAC () {
			return toBD(80, 32, 16, 32);
		}

		public TiledDataBuilder sideBD () {
			return toAC(128, 32, 16, 32);
		}

		public TiledData build () {
			data.init(vertices, packedVertices, numVertices, triangles, numTriangles);
			numVertices = 0;
			numTriangles = 0;
			quads = 0;
			return data;
		}
		public static class TiledData {
			// vertices to build uvs from
			public float[] vertices;
			// vertices for rendering
			public float[] packedVertices;
			// indices for triangles from vertices
			public short[] triangles;

			protected TiledData () {
			}

			public TiledData init (float[] vertices, float[] packedVertices, int numVertices, short[] triangles, int numTriangles) {
				// need to make a copy at some point
				this.vertices = Arrays.copyOf(vertices, numVertices);
				this.packedVertices = Arrays.copyOf(packedVertices, numVertices);
				this.triangles = Arrays.copyOf(triangles, numTriangles);
				return this;
			}

			public float[] getVertices () {
				return vertices;
			}

			public float[] getPackedVertices () {
				return packedVertices;
			}

			public short[] getTriangles () {
				return triangles;
			}
		}
	}

	private static final TiledDataBuilder builder = new TiledDataBuilder();

	public enum TiledType {
		FULL(builder.fullABCD().build()),
		SINGLE(builder.toFull(32, 0, 32, 32).build()),

		CORNER_FULL(builder.cornerA().cornerB().cornerC().cornerD().build()),
		CORNER_ACD(builder.cornerA().cornerC().cornerD().build()),
		CORNER_ABC(builder.cornerA().cornerB().cornerC().build()),
		CORNER_ABD(builder.cornerA().cornerB().cornerD().build()),
		CORNER_BCD(builder.cornerB().cornerC().cornerD().build()),
		CORNER_BC(builder.cornerB().cornerC().build()),
		CORNER_AD(builder.cornerA().cornerD().build()),
		CORNER_CD(builder.cornerC().cornerD().build()),
		CORNER_AC(builder.cornerA().cornerC().build()),
		CORNER_AB(builder.cornerA().cornerB().build()),
		CORNER_BD(builder.cornerB().cornerD().build()),
		CORNER_D(builder.cornerD().build()),
		CORNER_C(builder.cornerC().build()),
		CORNER_A(builder.cornerA().build()),
		CORNER_B(builder.cornerB().build()),

		INNER_FULL(builder.innerA().innerB().innerC().innerD().build()),

		INNER_CD(builder.innerC().innerCA().innerD().innerDB().build()),
		INNER_AC(builder.innerA().innerAB().innerC().innerCD().build()),
		INNER_BD(builder.innerB().innerBA().innerD().innerDC().build()),
		INNER_AB(builder.innerA().innerAC().innerB().innerBD().build()),
		INNER_D_A(builder.innerD().innerDB().innerDC().cornerA().build()),
		INNER_C_B(builder.innerC().innerCA().innerCD().cornerB().build()),
		INNER_A_D(builder.innerA().innerAB().innerAC().cornerD().build()),
		INNER_B_C(builder.innerB().innerBA().innerBD().cornerC().build()),
		INNER_D(builder.innerD().innerDB().innerDC().build()),
		INNER_C(builder.innerC().innerCA().innerCD().build()),
		INNER_A(builder.innerA().innerAB().innerAC().build()),
		INNER_B(builder.innerB().innerBA().innerBD().build()),
		// NOTE side names are fucked :(
		SIDE_AC_BD(builder.sideAC().sideBD().build()),
		SIDE_AB_CD(builder.sideAB().sideCD().build()),

		SIDE_BD_A(builder.sideBD().cornerB().build()),
		SIDE_CD_B(builder.sideCD().cornerD().build()),
		SIDE_AC_D(builder.sideAC().cornerC().build()),
		SIDE_AB_C(builder.sideAB().cornerA().build()),

		SIDE_BD_C(builder.sideBD().cornerD().build()),
		SIDE_CD_A(builder.sideCD().cornerC().build()),
		SIDE_AC_B(builder.sideAC().cornerA().build()),
		SIDE_AB_D(builder.sideAB().cornerB().build()),

		SIDE_BD_A_C(builder.sideBD().cornerB().cornerD().build()),
		SIDE_CD_A_B(builder.sideCD().cornerC().cornerD().build()),
		SIDE_AC_B_D(builder.sideAC().cornerA().cornerC().build()),
		SIDE_AB_C_D(builder.sideAB().cornerA().cornerB().build()),

		SIDE_BD(builder.sideBD().build()),
		SIDE_CD(builder.sideCD().build()),
		SIDE_AC(builder.sideAC().build()),
		SIDE_AB(builder.sideAB().build()),
		;
		// vertices to build uvs from
		public final float[] vertices;
		// vertices for rendering
		public final float[] packedVertices;
		// indices for triangles from vertices
		public final short[] triangles;

		TiledType (TiledDataBuilder.TiledData build) {
			vertices = build.getVertices();
			packedVertices = build.getPackedVertices();
			triangles = build.getTriangles();
		}

		public float[] getVerticesCopy () {
			return Arrays.copyOf(vertices, vertices.length);
		}
	}

	@Override public void dispose () {
		super.dispose();
		raw.dispose();
		polyBatch.dispose();
		mapRenderer.getMap().dispose();
	}

	Vector3 temp = new Vector3();

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		// fairly dumb
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		gameCamera.position.set(temp.x, temp.y, 0);
		gameCamera.update();
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		// dumb, dont do this!
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		temp.sub(gameCamera.position).scl(0.1f);
		gameCamera.position.add(temp.x, temp.y, 0);
		gameCamera.update();
		return true;
	}

	@Override public boolean scrolled (float amountX, float amountY) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom * amountX * 0.1f, 0.1f, 3f);
		gameCamera.update();
		return true;
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, TerrainTileGenTest.class);
	}
}

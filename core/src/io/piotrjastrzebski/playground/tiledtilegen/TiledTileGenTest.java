package io.piotrjastrzebski.playground.tiledtilegen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class TiledTileGenTest extends BaseScreen {
	Texture raw;
	TextureRegion base;
	PolygonSpriteBatch polyBatch;
	Array<TiledRegion> tiledRegions;
	PolyOrthoTiledMapRenderer mapRenderer;
	public TiledTileGenTest (GameReset game) {
		super(game);
		VisTextButton reload = new VisTextButton("Reload");
		root.add(reload).expand().left().top().pad(10);
		reload.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				generateTiles();
			}
		});

		raw = new Texture(Gdx.files.internal("tiled/templatev2.png"));
		// cant have this for packing/save
//		raw.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		polyBatch = new PolygonSpriteBatch();
		base = new TextureRegion(raw);

		tiledRegions = new Array<>();
		generateTiles();
		packTiles("template/", 32, Gdx.files.external(".atlas/atlas"));


		TiledMap map = new TmxMapLoader().load("tiled/simple.tmx");
		mapRenderer = new PolyOrthoTiledMapRenderer(map, INV_SCALE, polyBatch);

		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
		int width = layer.getWidth();
		int height = layer.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				TiledMapTileLayer.Cell cell = layer.getCell(x, y);
				if (cell == null) continue;
				TiledMapTile tile = cell.getTile();
				if (tile == null) continue;
				PolyOrthoTiledMapRenderer.PolygonTiledMapTile mapTile;
				switch (tile.getId()) {
				case 1:
					mapTile = new PolyOrthoTiledMapRenderer.PolygonTiledMapTile(tiledRegions.first());
					mapTile.setId(tile.getId());
					cell.setTile(mapTile);
					break;
				case 2:
					mapTile = new PolyOrthoTiledMapRenderer.PolygonTiledMapTile(tiledRegions.get(15));
					mapTile.setId(tile.getId());
					cell.setTile(mapTile);
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
		int size = Math.min(intBuffer.get(), 2048);
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
			Pixmap pm = ScreenUtils.getFrameBufferPixmap(0, 0, pixelSize, pixelSize);
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

	private void flipPM(Pixmap pm) {
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
			tiledRegions.add(new TiledRegion(new TextureRegion(base), tiledType));
		}
	}

	public void render(float delta) {
		// nice smooth background color
		float L = 150 / 255f;
		Gdx.gl.glClearColor(L, L, L, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float scale = (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))?25:1;
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

		mapRenderer.setView(gameCamera);
		mapRenderer.render();

		polyBatch.setProjectionMatrix(gameCamera.combined);
		polyBatch.begin();
		polyBatch.draw(raw, -19, -2, 6, 4);

		for (int i = 0; i < tiledRegions.size; i++) {
			TiledRegion tiledRegion = tiledRegions.get(i);
			polyBatch.draw(tiledRegion, (i/9)*2.5f, 9 - (i%9) * 2.5f, 2, 2);
		}

		polyBatch.end();

		stage.act(delta);
		stage.draw();
	}

	public static class TiledRegion extends PolygonRegion {
		public TiledType type;
		public TiledRegion (TextureRegion region, TiledType type) {
			// we need copy, as we will modify it later
			super(region, type.getVerticesCopy(), type.triangles);
			this.type = type;

			float u = region.getU(), v = region.getV();
			float uvWidth = region.getU2()- u;
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
			// region dimensions will always be square, half of the original height
			// its the way the tiled region works
			int size = region.getRegionHeight()/2;
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
		public static float width = 96;
		public static float height = 64;
		public static final int MAX_QUADS = 4;
		private float[] vertices;
		// vertices for rendering
		private float[] packedVertices;
		private int numVertices;
		// indices for triangles from vertices
		private short[] triangles;
		private int numTriangles;
		private short quads;
		private short[] indices = new short[]{0,1,2,0,3,2};
		private TiledData data = new TiledData();

		public TiledDataBuilder () {
			vertices = new float[8 * MAX_QUADS];
			packedVertices = new float[8 * MAX_QUADS];
			triangles = new short[6 * MAX_QUADS];
		}

		// fix for bleeding issues
		private float fix = 0.0f;
		public TiledDataBuilder quad (
			int sx, int sy, int sw, int sh,
			int tx, int ty, int tw, int th) {
			if (quads >= MAX_QUADS) throw new IllegalStateException("Can only add "+MAX_QUADS+" quads!");

			int vo = numVertices;
			float nsx = sx/width;
			float nsy = sy/height;
			vertices[vo++] = nsx + fix;
			vertices[vo++] = nsy + fix;

			vertices[vo++] = nsx + sw/width - fix;
			vertices[vo++] = nsy + fix;

			vertices[vo++] = nsx + sw/width - fix;
			vertices[vo++] = nsy + sh/height - fix;

			vertices[vo++] = nsx + fix;
			vertices[vo++] = nsy + sh/height - fix;

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
				triangles[i + to] = (short)(indices[i] + numVertices/2);
			}
			numTriangles+=6;
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
		public TiledDataBuilder fullA() {
			return toA(64, 16, 16, 16);
		}
		public TiledDataBuilder fullB() {
			return toB(48, 16, 16, 16);
		}
		public TiledDataBuilder fullC() {
			return toC(64, 32, 16, 16);
		}
		public TiledDataBuilder fullD() {
			return toD(48, 32, 16, 16);
		}
		public TiledDataBuilder innerA() {
			return toA(0, 16, 16, 16);
		}
		public TiledDataBuilder innerB() {
			return toB(16, 16, 16, 16);
		}
		public TiledDataBuilder innerC() {
			return toC(0, 0, 16, 16);
		}
		public TiledDataBuilder innerD() {
			return toD(16, 0, 16, 16);
		}
		// NW
		public TiledDataBuilder cornerA() {
			return toA(32, 48, 16, 16);
		}
		// NE
		public TiledDataBuilder cornerB() {
			return toB(80, 48, 16, 16);
		}
		// SW
		public TiledDataBuilder cornerC() {
			return toC(32, 0, 16, 16);
		}
		// SE
		public TiledDataBuilder cornerD() {
			return toD(80, 0, 16, 16);
		}
		public TiledDataBuilder northA() {
			return toA(64, 48, 16, 16);
		}
		public TiledDataBuilder northB() {
			return toB(48, 48, 16, 16);
		}
		public TiledDataBuilder eastA() {
			return toB(80, 16, 16, 16);
		}
		public TiledDataBuilder eastB() {
			return toD(80, 32, 16, 16);
		}
		public TiledDataBuilder southA() {
			return toC(64, 0, 16, 16);
		}
		public TiledDataBuilder southB() {
			return toD(48, 0, 16, 16);
		}
		public TiledDataBuilder westA() {
			return toA(32, 16, 16, 16);
		}
		public TiledDataBuilder westB() {
			return toC(32, 32, 16, 16);
		}

		public TiledData build() {
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

			protected TiledData () {}

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
		FULL(builder.fullA().fullB().fullC().fullD().build()),

		INNER_A(builder.innerA().fullB().fullC().fullD().build()),
		INNER_B(builder.innerB().fullA().fullC().fullD().build()),
		INNER_C(builder.innerC().fullA().fullB().fullD().build()),
		INNER_D(builder.innerD().fullA().fullB().fullC().build()),

		INNER_AB(builder.innerA().innerB().fullC().fullD().build()),
		INNER_BD(builder.innerB().innerD().fullA().fullC().build()),
		INNER_CD(builder.innerC().innerD().fullA().fullB().build()),
		INNER_AC(builder.innerA().innerC().fullB().fullD().build()),

		INNER_AD(builder.innerA().innerD().fullB().fullC().build()),
		INNER_BC(builder.innerB().innerC().fullA().fullD().build()),

		INNER_ABD(builder.innerA().innerB().innerD().fullC().build()),
		INNER_ABC(builder.innerA().innerB().innerC().fullD().build()),
		INNER_ACD(builder.innerA().innerC().innerD().fullB().build()),
		INNER_BCD(builder.innerB().innerC().innerD().fullA().build()),

		INNER_FULL(builder.quad(0, 0, 32 ,32, 0, 0, 32, 32).build()),

		SIDE_AB(builder.northA().northB().fullC().fullD().build()),
		SIDE_CD(builder.southA().southB().fullA().fullB().build()),
		SIDE_BD(builder.eastA().eastB().fullA().fullC().build()),
		SIDE_AC(builder.westA().westB().fullB().fullD().build()),

		SIDE_AB_CD(builder.eastA().eastB().westA().westB().build()),
		SIDE_AC_BD(builder.northA().northB().southA().southB().build()),

		SIDE_C_AB(builder.cornerA().cornerB().southA().southB().build()),
		SIDE_C_CD(builder.northA().northB().cornerC().cornerD().build()),
		SIDE_C_AC(builder.eastA().eastB().cornerA().cornerC().build()),
		SIDE_C_BD(builder.cornerB().cornerD().westA().westB().build()),

		SIDE_AB_I_C(builder.northA().northB().innerC().fullD().build()),
		SIDE_AB_I_D(builder.northA().northB().fullC().innerD().build()),
		SIDE_AB_I_CD(builder.northA().northB().innerC().innerD().build()),
		SIDE_CD_I_A(builder.southA().southB().innerA().fullB().build()),
		SIDE_CD_I_B(builder.southA().southB().fullA().innerB().build()),
		SIDE_CD_I_AB(builder.southA().southB().innerA().innerB().build()),
		SIDE_BD_I_A(builder.eastA().eastB().innerA().fullC().build()),
		SIDE_BD_I_C(builder.eastA().eastB().fullA().innerC().build()),
		SIDE_BD_I_AC(builder.eastA().eastB().innerA().innerC().build()),
		SIDE_AC_I_B(builder.westA().westB().innerB().fullD().build()),
		SIDE_AC_I_D(builder.westA().westB().fullB().innerD().build()),
		SIDE_AC_I_BD(builder.westA().westB().innerB().innerD().build()),

		CORNER_A(builder.cornerA().fullD().northB().westB().build()),
		CORNER_A_I(builder.cornerA().innerD().northB().westB().build()),

		CORNER_B(builder.cornerB().fullC().northA().eastB().build()),
		CORNER_B_I(builder.cornerB().innerC().northA().eastB().build()),

		CORNER_C(builder.cornerC().fullB().southB().westA().build()),
		CORNER_C_I(builder.cornerC().innerB().southB().westA().build()),

		CORNER_D(builder.cornerD().fullA().southA().eastA().build()),
		CORNER_D_I(builder.cornerD().innerA().southA().eastA().build()),
//		INNER_FULL_TEST(builder.innerA().innerB().innerC().innerD().build()),
//		CORNER_FULL_TEST(builder.cornerA().cornerB().cornerC().cornerD().build()),
		SINGLE(builder.quad(0, 32, 32 ,32, 0, 0, 32, 32).build()),
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

	@Override public boolean scrolled (int amount) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom * amount * 0.1f, 0.1f, 3f);
		gameCamera.update();
		return true;
	}
}

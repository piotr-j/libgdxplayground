package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.graphics.g2d.Batch.*;

/**
 * Simple hex map test
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class GammaTest extends BaseScreen {
	private static final String TAG = GammaTest.class.getSimpleName();
	ShapeRenderer gammaRenderer;
	public GammaTest (final GameReset game) {
		super(game);
		// same params as default
		gammaRenderer = new ShapeRenderer(5000, createDefaultShader(false, true, 0));
	}

	private Color[][] colors = {
		{Color.BLACK, Color.WHITE},
		{Color.RED, Color.GREEN},
		{Color.GREEN, Color.BLUE},
		{Color.BLUE, Color.RED},
		{Color.MAGENTA, Color.YELLOW},
		{Color.YELLOW, Color.CYAN},
		{Color.CYAN, Color.MAGENTA},
		{new Color(1, 0, 0, 0), Color.RED},
	};
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1f, 1f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);

		for (int i = 0; i < 32; i++) {
			float c = 1f/32f * i;
			renderer.setColor(c, c, c, 1);
			renderer.rect(-19 + i * .4f, 10, .4f, 1);
			c = (float)Math.pow(c, 2.2f);
			renderer.setColor(c, c, c, 1);
			renderer.rect(-19 + i * .4f, 9, .4f, 1);
		}

		float width = 7.5f;
		for (int i = 0; i < colors.length; i++) {
			Color[] pair = colors[i];
			renderer.rect(-19, 7 - i * 2.25f, width, 1, pair[0], pair[1], pair[1], pair[0]);
			renderer.rect(-19 + width, 7 - i * 2.25f, width, 1, pair[1], pair[0], pair[0], pair[1]);
		}
		renderer.end();

		gammaRenderer.setProjectionMatrix(gameCamera.combined);
		gammaRenderer.begin(ShapeRenderer.ShapeType.Filled);
		for (int i = 0; i < colors.length; i++) {
			Color[] pair = colors[i];
			gammaRenderer.rect(-19, 7 - i * 2.25f - 1, width, 1, pair[0], pair[1], pair[1], pair[0]);
			gammaRenderer.rect(-19 + width, 7 - i * 2.25f - 1, width, 1, pair[1], pair[0], pair[0], pair[1]);
		}
		gammaRenderer.end();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, GammaTest.class);
	}

	static private String createVertexShader (boolean hasNormals, boolean hasColors, int numTexCoords) {
		String shader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
			+ (hasNormals ? "attribute vec3 " + ShaderProgram.NORMAL_ATTRIBUTE + ";\n" : "")
			+ (hasColors ? "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" : "");

		for (int i = 0; i < numTexCoords; i++) {
			shader += "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + i + ";\n";
		}

		shader += "uniform mat4 u_projModelView;\n";
		shader += (hasColors ? "varying vec4 v_col;\n" : "");

		for (int i = 0; i < numTexCoords; i++) {
			shader += "varying vec2 v_tex" + i + ";\n";
		}

		shader += "void main() {\n" + "   gl_Position = u_projModelView * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
			+ (hasColors ? "   v_col = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" : "");

		for (int i = 0; i < numTexCoords; i++) {
			shader += "   v_tex" + i + " = " + ShaderProgram.TEXCOORD_ATTRIBUTE + i + ";\n";
		}
		shader += "   gl_PointSize = 1.0;\n";
		shader += "}\n";
		return shader;
	}

	static private String createFragmentShader (boolean hasNormals, boolean hasColors, int numTexCoords) {
		String shader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n";

		if (hasColors) shader += "varying vec4 v_col;\n";
		for (int i = 0; i < numTexCoords; i++) {
			shader += "varying vec2 v_tex" + i + ";\n";
			shader += "uniform sampler2D u_sampler" + i + ";\n";
		}

		shader += "void main() {\n" + "   gl_FragColor = " + (hasColors ? "pow(v_col, vec4(2.2))" : "vec4(1, 1, 1, 1)");

		if (numTexCoords > 0) shader += " * ";

		for (int i = 0; i < numTexCoords; i++) {
			if (i == numTexCoords - 1) {
				shader += " texture2D(u_sampler" + i + ",  v_tex" + i + ")";
			} else {
				shader += " texture2D(u_sampler" + i + ",  v_tex" + i + ") *";
			}
		}

		shader += ";\n}";
		return shader;
	}

	/** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
	static public ShaderProgram createDefaultShader (boolean hasNormals, boolean hasColors, int numTexCoords) {
		String vertexShader = createVertexShader(hasNormals, hasColors, numTexCoords);
		String fragmentShader = createFragmentShader(hasNormals, hasColors, numTexCoords);
		ShaderProgram program = new ShaderProgram(vertexShader, fragmentShader);
		if (!program.isCompiled()) {
			Gdx.app.log("", "Log = " + program.getLog());
		}
		return program;
	}
}

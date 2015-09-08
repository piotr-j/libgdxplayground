package io.piotrjastrzebski.playground.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Adpated from http://forums.tigsource.com/index.php?topic=48508.msg1163762#msg1163762
 *
 * Created by EvilEntity on 07/06/2015.
 */
public abstract class ShaderTestBase extends BaseScreen {
	public final static String DEF_SHADER = "default";
	ObjectMap<String, Texture> textures = new ObjectMap<>();
	Array<Sprite> sprites = new Array<>();
	ObjectMap<String, Shader> shaders = new ObjectMap<>();
	FrameBuffer fbo;
	TextureRegion fboRegion;

	public ShaderTestBase (GameReset game) {
		super(game);
		createShader(DEF_SHADER);
		init();
		reloadShaders();
	}

	protected abstract void init();

	protected void createShader(String name) {
		Shader shader = new Shader(name);
		shaders.put(name, shader);
	}

	private void reloadShaders () {
		for (Shader shader : shaders.values()) {
			shader.reload();
		}
		Gdx.app.log("" ,"Shaders reloaded!");
	}

	protected ShaderProgram loadShader (String name) {
		ShaderProgram.pedantic = false;
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/"+name+".vert"), Gdx.files.internal("shaders/"+name+".frag"));
		if (!shader.isCompiled()) {
			Gdx.app.error("", "Shader compilation failed!\n" + shader.getLog());
			shader.dispose();
			return null;
		}
		return shader;
	}

	protected Sprite createSprite (String path) {
		Texture texture = textures.get(path, null);
		if (texture == null) {
			texture = new Texture(path);
	 		textures.put(path, texture);
		}
		Sprite sprite = new Sprite(texture);
		sprite.setSize(texture.getWidth() * INV_SCALE, texture.getHeight() * INV_SCALE);
		sprite.setOriginCenter();
		sprites.add(sprite);
		return sprite;
	}

	@Override public void render (float delta) {
		super.render(delta);
		Gdx.gl.glClearColor(0f, 1f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.setShader(shaders.get(DEF_SHADER).get());
		fbo.begin();
		Gdx.gl.glClearColor(1f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		for (Sprite sprite : sprites) {
			sprite.draw(batch);
		}
		batch.end();
		fbo.end();

		batch.setProjectionMatrix(gameCamera.combined);

		preRender(delta);

		// got to bind it manually as batch doesnt do it
		fboRegion.getTexture().bind(0);
		batch.begin();
		batch.draw(fboRegion, -VP_WIDTH / 2, -VP_HEIGHT / 2, VP_WIDTH, VP_HEIGHT);
		batch.end();
	}

	protected abstract void preRender (float delta);

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		if (fbo != null) fbo.dispose();
		fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
		fboRegion = new TextureRegion(fbo.getColorBufferTexture());
		fboRegion.flip(false, true);
	}

	@Override public void dispose () {
		super.dispose();
		for (Texture texture : textures.values()) {
			texture.dispose();
		}
		textures.clear();
		for (Shader shader : shaders.values()) {
			shader.dispose();
		}
		shaders.clear();
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.SPACE) {
			reloadShaders();
		}
		return super.keyDown(keycode);
	}

	protected class Shader {
		public String name;
		public ShaderProgram program;

		public Shader (String name) {
			this.name = name;
		}

		public void reload () {
			ShaderProgram tempFire = loadShader(name);
			if (tempFire != null) {
				if (program != null) program.dispose();
				program = tempFire;
			}
		}

		public ShaderProgram get () {
			return program;
		}

		public void dispose () {
			if (program != null) program.dispose();
		}
	}
}

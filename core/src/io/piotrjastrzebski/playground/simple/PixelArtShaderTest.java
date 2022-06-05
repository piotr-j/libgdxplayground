package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 */
public class PixelArtShaderTest extends BaseScreen {
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	private final static int TEX_1X = 0;
	private final static int TEX_2X = 1;
	private final static int TEX_4X = 2;
	private final static int TEX_8X = 3;
	private Array<Texture> textures = new Array<>();
	protected ShaderProgram shader;
	private Texture current;

	private float width;
	private float height;
	protected ShaderProgram batchShader = null;

	public PixelArtShaderTest (GameReset game) {
		// ignore this
		super(game);

		textures.add(new Texture("pixels/scene.png"));
		textures.add(new Texture("pixels/scene2x.png"));
		textures.add(new Texture("pixels/scene4x.png"));
		textures.add(new Texture("pixels/scene8x.png"));
		current = textures.get(TEX_1X);
		width = current.getWidth() * INV_SCALE * 4;
		height = current.getHeight() * INV_SCALE * 4;

		shader = new ShaderProgram(Gdx.files.internal("pixels/filtering.vsh"), Gdx.files.internal("pixels/filtering.fsh"));
		shader.bind();
		shader.setUniformf("u_texels_per_pixel", VP_WIDTH/(float)Gdx.graphics.getBackBufferWidth());
		if (!shader.isCompiled())
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());

		Array<TexWrapper> wrappers = new Array<>();
		wrappers.add(new TexWrapper(textures.get(TEX_1X), "scene 1x"));
		wrappers.add(new TexWrapper(textures.get(TEX_2X), "scene 2x"));
		wrappers.add(new TexWrapper(textures.get(TEX_4X), "scene 4x"));
		wrappers.add(new TexWrapper(textures.get(TEX_8X), "scene 8x"));

		final VisSelectBox<TexWrapper> selectBox = new VisSelectBox<>();
		selectBox.setItems(wrappers);
		selectBox.setSelected(wrappers.get(0));

		selectBox.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				TexWrapper lastSelected = selectBox.getSelection().getLastSelected();
				current = lastSelected.texture;
			}
		});
		VisWindow window = new VisWindow("Options");
		window.add(selectBox);

		final VisCheckBox cb = new VisCheckBox("Custom Shader");
		cb.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				if (cb.isChecked()) {
					batchShader = shader;
				} else {
					batchShader = null;
				}
			}
		});
		window.row();
		window.add(cb).row();

		Array<FilterWrapper> filterWrappers = new Array<>();
		filterWrappers.add(new FilterWrapper(Texture.TextureFilter.Nearest, "Nearest"));
		filterWrappers.add(new FilterWrapper(Texture.TextureFilter.Linear, "Linear"));
		filterWrappers.add(new FilterWrapper(Texture.TextureFilter.MipMap, "MimMpa"));
		filterWrappers.add(new FilterWrapper(Texture.TextureFilter.MipMapNearestNearest, "MMNearestNearest"));
		filterWrappers.add(new FilterWrapper(Texture.TextureFilter.MipMapLinearNearest, "MMLinearNearest"));
		filterWrappers.add(new FilterWrapper(Texture.TextureFilter.MipMapNearestLinear, "MMNearestLinear"));
		filterWrappers.add(new FilterWrapper(Texture.TextureFilter.MipMapLinearLinear, "MMLinearLinear"));

		final VisSelectBox<FilterWrapper> minSB = new VisSelectBox<>();
		minSB.setItems(filterWrappers);
		minSB.setSelected(filterWrappers.get(0));

		minSB.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				FilterWrapper lastSelected = minSB.getSelection().getLastSelected();
				setMinFilter(lastSelected.filter);
			}
		});
		window.add(minSB).row();

		final VisSelectBox<FilterWrapper> magSB = new VisSelectBox<>();
		magSB.setItems(filterWrappers);
		magSB.setSelected(filterWrappers.get(0));

		magSB.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				FilterWrapper lastSelected = magSB.getSelection().getLastSelected();
				setMagFilter(lastSelected.filter);
			}
		});
		window.add(magSB).row();

		window.pack();
		stage.addActor(window);
		window.centerWindow();
	}

	private void setMinFilter(Texture.TextureFilter filter) {
		for (Texture texture : textures) {
			texture.setFilter(filter, texture.getMagFilter());
		}
	}

	private void setMagFilter(Texture.TextureFilter filter) {
		for (Texture texture : textures) {
			texture.setFilter(texture.getMinFilter(), filter);
		}
	}

	public class TexWrapper {
		Texture texture;
		String text;

		public TexWrapper (Texture texture, String  text) {
			this.texture = texture;
			this.text = text;
		}

		@Override public String toString () {
			return text;
		}
	}

	public class FilterWrapper {
		Texture.TextureFilter filter;
		String text;

		public FilterWrapper (Texture.TextureFilter filter, String  text) {
			this.filter = filter;
			this.text = text;
		}

		@Override public String toString () {
			return text;
		}
	}

	int moveX;
	int moveY;
	int shift = 1;
	float rotation;
	@Override public void render (float delta) {
		super.render(delta);
		if (moveX > 0) {
			gameCamera.position.x += delta * 0.1f * shift;
		} else if (moveX < 0){
			gameCamera.position.x -= delta * 0.1f * shift;
		}
		if (moveY > 0) {
			gameCamera.position.y += delta * 0.1f * shift;
		} else if (moveY < 0) {
			gameCamera.position.y -= delta * 0.1f * shift;
		}
		gameCamera.update();

		batch.setColor(Color.WHITE);
		batch.setShader(batchShader);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		shader.setUniformf("u_texels_per_pixel", current.getWidth()/gameViewport.getWorldWidth());
		batch.draw(current, -width/2, -height/2, width/2, height/2, width, height, 1, 1, rotation, 0, 0, current.getWidth(), current.getHeight(), false, false);
		batch.end();
		batch.setShader(null);
		stage.act(delta);
		stage.draw();
	}

	Vector3 tp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.Q:
			rotation+= 11.25f;
			break;
		case Input.Keys.E:
			rotation-= 11.25f;
			break;
		case Input.Keys.W:
		case Input.Keys.UP:
			moveY++;
			break;
		case Input.Keys.S:
		case Input.Keys.DOWN:
			moveY--;
			break;
		case Input.Keys.A:
		case Input.Keys.LEFT:
			moveX--;
			break;
		case Input.Keys.D:
		case Input.Keys.RIGHT:
			moveX++;
			break;
		case Input.Keys.SHIFT_LEFT:
			shift = 100;
			break;
		}
		return super.keyDown(keycode);
	}

	@Override public boolean keyUp (int keycode) {
		switch (keycode) {
		case Input.Keys.W:
		case Input.Keys.UP:
			moveY--;
			break;
		case Input.Keys.S:
		case Input.Keys.DOWN:
			moveY++;
			break;
		case Input.Keys.A:
		case Input.Keys.LEFT:
			moveX++;
			break;
		case Input.Keys.D:
		case Input.Keys.RIGHT:
			moveX--;
			break;
		case Input.Keys.SHIFT_LEFT:
			shift = 1;
			break;
		}
		return super.keyUp(keycode);
	}


	@Override public void dispose () {
		super.dispose();
		for (Texture texture : textures) {
			texture.dispose();
		}
		batch.setShader(null);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 4);
		PlaygroundGame.start(args, PixelArtShaderTest.class);
	}
}

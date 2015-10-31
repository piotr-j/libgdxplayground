package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class AssetReloadTest extends BaseScreen {
	private final static String TAG = AssetReloadTest.class.getSimpleName();
	private final static String F_V1 = "livereload/v1/";
	private final static String F_V2 = "livereload/v2/";

	AssetManager manager;
	String folder = F_V1;
	Array<Sprite> sprites = new Array<>();
	Array<ReloadingSprite> reloading = new Array<>();

	public AssetReloadTest (GameReset game) {
		super(game);
		manager = new AssetManager();
		Sprite green = newReloader("green.png");
		green.setSize(2, 3);
		green.setPosition(-4, 0);
		sprites.add(green);

		Sprite red = newReloader("red.png");
		red.setSize(1, 5);
		red.setPosition(3, -2);
		sprites.add(red);

		Sprite yellow = newReloader("yellow.png");
		yellow.setSize(6, 4);
		yellow.setPosition(0, 6);
		sprites.add(yellow);
	}

	private ReloadingSprite newReloader (String name) {
		ReloadingSprite texReg = new ReloadingSprite(name);
		reloading.add(texReg);
		reload(texReg);
		return texReg;
	}

	private void reload(ReloadingSprite texReg) {
		String path = folder + texReg.name;
		// TODO cache
		// TODO dispose old
		manager.load(path, Texture.class);
		manager.finishLoading();
		texReg.set(manager.get(path, Texture.class));
	}

	public static class ReloadingSprite extends Sprite {
		public String name;

		public ReloadingSprite (Texture texture) {
			super(texture);
		}

		public ReloadingSprite (String name) {
			this.name = name;
		}

		public void set (Texture texture) {
			setTexture(texture);
			setRegion(0, 0, texture.getWidth(), texture.getHeight());
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Sprite sprite : sprites) {
			sprite.draw(batch);
		}
		batch.end();
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.F5:
			Gdx.app.log("", "Reload textures");
			// swap folders
			if (F_V1.equals(folder)) {
				folder = F_V2;
			} else {
				folder = F_V1;
			}
			for (ReloadingSprite texReg : reloading) {
				reload(texReg);
			}

			break;
		case Input.Keys.F6:
			Gdx.app.log("", "repack atlases");
			// swap folders
			if (F_V1.equals(folder)) {
				folder = F_V2;
			} else {
				folder = F_V1;
			}
			break;
		}
		return super.keyDown(keycode);
	}
}

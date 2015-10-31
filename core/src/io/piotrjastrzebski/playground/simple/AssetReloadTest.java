package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
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
	Array<Sprite> atlasSprites = new Array<>();
	PixmapPacker packer;
	Atlas atlas;

	public AssetReloadTest (GameReset game) {
		super(game);
		manager = new AssetManager();

		atlas = new Atlas();
		reload();

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

	private void createAtlasSorutes () {
		FancySprite green = new FancySprite(atlas, "green");
		green.setSize(2, 3);
		green.setPosition(-4, - 10);
		sprites.add(green);
		atlasSprites.add(green);

		FancySprite red = new FancySprite(atlas, "red");
		red.setSize(1, 5);
		red.setPosition(3, -12);
		sprites.add(red);
		atlasSprites.add(red);

		FancySprite yellow = new FancySprite(atlas, "yellow");
		yellow.setSize(6, 4);
		yellow.setPosition(0, -4);
		sprites.add(yellow);
		atlasSprites.add(yellow);
	}

	private void reload () {
		if (packer != null) packer.dispose();
		packer = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 2, false);
		// TODO need a list of assets to load i guess
		// TODO async, load via manager and add ?
		pack("green", packer);
		pack("yellow", packer);
		pack("red", packer);
		atlas.reload(packer);
		if (atlasSprites.size == 0) {
			createAtlasSorutes();
		}
	}

	public interface AssetOwner {
		void setRegion(TextureRegion region);
	}

	public static class Atlas {
		private TextureAtlas atlas;
		ObjectMap<String, TextureAtlas.AtlasRegion> cache = new ObjectMap<>();
		ObjectMap<String, Array<AssetOwner>> owners = new ObjectMap<>();

		public Atlas () {
			this.atlas = new TextureAtlas();
		}

		public TextureAtlas.AtlasRegion get (String name, AssetOwner owner) {
			TextureAtlas.AtlasRegion region = get(name);
			Array<AssetOwner> list = owners.get(name);
			if (list == null) {
				list = new Array<>();
				owners.put(name, list);
			}
			if (!list.contains(owner, true)) {
				list.add(owner);
			}
			return region;
		}

		protected TextureAtlas.AtlasRegion get (String name) {
			TextureAtlas.AtlasRegion region = cache.get(name);
			if (region == null) {
				region = atlas.findRegion(name);
				cache.put(name, region);
			}
			return region;
		}

		public void reload (PixmapPacker packer) {
			// we cant fully clear atlas without reflection, so we will make new one
			atlas.dispose();
			atlas = new TextureAtlas();

			packer.updateTextureAtlas(atlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
			for (ObjectMap.Entry<String, TextureAtlas.AtlasRegion> entry : cache.iterator()) {
				TextureAtlas.AtlasRegion region = atlas.findRegion(entry.key);
				if (region == null) {
					// TODO put some fake asset
					Gdx.app.error("", "Missing region for " + entry.key);
				} else {
					entry.value.setRegion(region);
				}
				Array<AssetOwner> list = owners.get(entry.key);
				if (list != null) {
					for (AssetOwner owner : list){
						owner.setRegion(region);
					}
				}
			}
		}
	}

	private void pack(String name, PixmapPacker packer) {
		Texture texture = new Texture(folder + name + ".png");
		TextureData textureData = texture.getTextureData();
		textureData.prepare();
		packer.pack(name, textureData.consumePixmap());
		texture.dispose();
	}

	public static class FancySprite extends Sprite implements AssetOwner {
		public FancySprite (Atlas atlas, String name) {
			setRegion(atlas.get(name, this));
		}
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
		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
			// clear existing resources
			manager.clear();
			// reload stuff
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
			reload();
			break;
		}
		return super.keyDown(keycode);
	}

	@Override public void dispose () {
		super.dispose();
		manager.dispose();
	}
}

package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.Sizes;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.color.BasicColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIColorPickerTest extends BaseScreen {
	private static final String TAG = UIColorPickerTest.class.getSimpleName();
	private final static int ROW = 5;
	PaletteColorPicker colorPicker;
	Array<ColorPalette> palettes = new Array<>();
	public UIColorPickerTest (GameReset game) {
		super(game);
		colorPicker = new PaletteColorPicker();
		VisTextButton showPicker = new VisTextButton("Show picker");
		showPicker.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				showPicker();
			}
		});
		root.add(showPicker);

		palettes.add(createColorPalette());
		palettes.add(createColorPalette());
		colorPicker.setColorPalette(palettes.get(0));
		showPicker();
	}

	private ColorPalette createColorPalette () {
		ColorPalette palette = new ColorPalette();
//		for (int i = 0, n = MathUtils.random(16, 32); i < n; i++) {
		for (int i = 0, n = 8; i < n; i++) {
			palette.baseColors.add(new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1));
		}
		return palette;
	}

	protected void showPicker () {
		stage.addActor(colorPicker);
		colorPicker.centerWindow();
		colorPicker.fadeIn();
	}

	@Override public void render (float delta) {
		super.render(delta);
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			Gdx.app.log(TAG, "Recreating picker...");
			colorPicker.remove();
			colorPicker.dispose();
			colorPicker = new PaletteColorPicker();
			showPicker();
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			Gdx.app.log(TAG, "Using palette 1");
			colorPicker.setColorPalette(palettes.get(0));
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			Gdx.app.log(TAG, "Using palette 2");
			colorPicker.setColorPalette(palettes.get(1));
		}
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
		colorPicker.dispose();
	}

	protected static class PaletteColorPicker extends VisDialog implements Disposable {
		protected static final String TAG = PaletteColorPicker.class.getSimpleName();
		protected BasicColorPicker colorPicker;
		protected Table colorsTable;
		protected VisScrollPane colorsPane;
		protected ColorPalette palette;
		protected TextureRegion region;
		protected TextureRegion regionSelected;
		protected Array<PickerColor> pickerColors = new Array<>();
		protected PickerColor selected = null;
		public PaletteColorPicker () {
			super("Color Picker 2 ");
			{
				// colorPicker uses Sizes to calculate size of various parts, we have to override it temporarily
				Sizes sizes = VisUI.getSizes();
				Sizes cloned = new Sizes(sizes);
				cloned.scaleFactor = cloned.scaleFactor * 2;
				Skin visSkin = VisUI.getSkin();
				String[] skinSizes = {"default", "x1", "x2"}; // order matters
				for (String skinSize : skinSizes) {
					if (visSkin.has(skinSize, Sizes.class)) {
						visSkin.add(skinSize, cloned);
						colorPicker = new BasicColorPicker();
						visSkin.add(skinSize, sizes);
						break;
					}
				}
			}
			addCloseButton();
			colorPicker.setShowHexFields(false);
			try {
				// ...
				// we want to remove color preview table, but stuff is private and coping all this crap is too much work
				Field field = ClassReflection.getDeclaredField(BasicColorPicker.class, "mainTable");
				field.setAccessible(true);
				Table mainTable = (Table)field.get(colorPicker);
				SnapshotArray<Actor> children = mainTable.getChildren();
				mainTable.removeActor(children.get(children.size - 1));

			} catch (ReflectionException e) {
				e.printStackTrace();
			}
			colorPicker.debug();
			// we dont really care the reason for change
			colorPicker.setListener(new ColorPickerListener() {
				@Override public void canceled (Color oldColor) {
					colorChanged(oldColor);
				}

				@Override public void changed (Color newColor) {
					colorChanged(newColor);
				}

				@Override public void reset (Color previousColor, Color newColor) {
					colorChanged(newColor);
				}

				@Override public void finished (Color newColor) {
					colorChanged(newColor);
				}
			});
			colorPicker.setAllowAlphaEdit(false);
			colorPicker.setTouchable(Touchable.disabled);
			getContentTable().add(colorPicker).expand().fill().row();
			colorsTable = new Table();
			colorsPane = new VisScrollPane(colorsTable);
			colorsPane.setFadeScrollBars(false);
			colorsPane.setScrollingDisabled(true, false);

			{
				Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGB888);
				pixmap.setColor(Color.WHITE);
				pixmap.drawRectangle(0, 0, 2, 2);
				region = new TextureRegion(new Texture(pixmap));
				pixmap.dispose();
			}
			{
				Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGB888);
				pixmap.setColor(Color.GRAY);
				pixmap.fillRectangle(0, 0, 16, 16);
				pixmap.setColor(Color.WHITE);
				pixmap.fillRectangle(2, 2, 12, 12);
				regionSelected = new TextureRegion(new Texture(pixmap));
				pixmap.dispose();
			}
			getContentTable().add(colorsPane).pad(8).expandX().fillX().height(128);

			pack();
		}

		private Vector2 v2 = new Vector2();
		protected void colorChanged(Color color) {
			if (palette == null) return;
			if (selected != null && selected.base) {
				addColor(new Color(color), false);
				PickerColor pickerColor = pickerColors.get(pickerColors.size - 1);
				selected(pickerColor);
				v2.set(pickerColor.button.getX(), pickerColor.button.getY());
				v2.set(0, 0);
				colorsTable.pack();
				pickerColor.button.localToAscendantCoordinates(colorsTable, v2);
				colorsPane.scrollTo(v2.x, v2.y, 60, 60);
				Gdx.app.log(TAG, "base color duplicated " + pickerColor);
			}
			if (selected != null) {
				selected.color.set(color);
				selected.button.getImage().setColor(selected.color);
			}
		}

		public void setColorPalette(ColorPalette palette) {
			this.palette = palette;
			if (palette != null) {
				if (palette.baseColors.size == 0) throw new AssertionError("!!!!");
				colorPicker.setTouchable(Touchable.enabled);
				pickerColors.clear();
				selected = null;
				for (Color color : palette.baseColors) {
					addColor(color, true);
				}
				for (Color color : palette.userColors) {
					addColor(color, true);
				}
				if (pickerColors.size > 0) {
					selected(pickerColors.get(0));
				}
			} else {
				colorPicker.setTouchable(Touchable.disabled);
			}
		}

		private void addColor (Color color, boolean base) {
			SpriteDrawable imageUp = new SpriteDrawable(new Sprite(region));
			SpriteDrawable imageDown = new SpriteDrawable(new Sprite(region));
			SpriteDrawable imageChecked = new SpriteDrawable(new Sprite(regionSelected));
			ImageButton button = new ImageButton(imageUp, imageDown, imageChecked);
			button.debug();
			final PickerColor pickerColor = new PickerColor(pickerColors.size, color, button, base);
			pickerColors.add(pickerColor);
//				button.setColor(color);
			button.getImageCell().expand().fill().pad(4);
			button.getImage().setColor(color);
			button.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					selected(pickerColor);
				}
			});
			colorsTable.add(button).size(60);
			Gdx.app.log(TAG, "Added color, total = " + pickerColors.size +", row="+((pickerColors.size)%ROW==0));
			if ((pickerColors.size ) % ROW == 0) {
				colorsTable.row();
			}

		}

		protected void selected (PickerColor pickerColor) {
			if (selected != null) {
				selected.button.setChecked(false);
			}
			selected = null;
			colorPicker.setColor(pickerColor.color);
			selected = pickerColor;
			selected.button.setChecked(true);
		}

		static class PickerColor {
			public final Color color;
			public final ImageButton button;
			public final int id;
			public final boolean base;

			public PickerColor (int id, Color color, ImageButton button, boolean base) {
				this.id = id;
				this.color = color;
				this.button = button;
				this.base = base;
			}
		}

		@Override public void dispose () {
			colorPicker.dispose();
		}
	}

	// stores a set of colors
	static class ColorPalette {
		// would it be simpler with one array and id for base colors?
		// base colors, always available, cannot be removed
		public Array<Color> baseColors = new Array<>();
		// user colors, initially empty, added as user modifies base colors, can be removed
		public Array<Color> userColors = new Array<>();
		// currently selected color, uses baseColors as default, if its larger then baseColors.size, selected-baseColors.size is used for userColors
		public int selected = -1;
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
//		config.width *= .5f;
//		config.height *= .5f;
		PlaygroundGame.start(args, config, UIColorPickerTest.class);
	}
}

package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

public class ResolutionResolverTest extends BaseScreen {
	private final static String TAG = ResolutionResolverTest.class.getSimpleName();

	public ResolutionResolverTest (GameReset game) {
		super(game);
		Resolution[] resolutions = new Resolution[]{
			new MyResolution(50, 50, "50"),
			new MyResolution(480, 800, "480"),
			new MyResolution(720, 1280, "720"),
			new MyResolution(1080, 1920, "1080"),
		};

		test(480, 320, resolutions);
		test(800, 480, resolutions);
		test(1024, 600, resolutions);
		test(1280, 720, resolutions);
		test(1680, 1050, resolutions);
		test(1920, 1080, resolutions);
	}

	private void test (int w, int h, Resolution[] resolutions) {
		Gdx.app.log("test " + w+ "x" + h, choose(w, h, resolutions).toString());
	}

	public Resolution choose (int w, int h, Resolution... descriptors) {
		// this is direct copy from ResolutionFileResolver#choose()
//		int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

		// Prefer the shortest side.
		Resolution best = descriptors[0];
		if (w < h) {
			for (int i = 0, n = descriptors.length; i < n; i++) {
				Resolution other = descriptors[i];
				if (w >= other.portraitWidth && other.portraitWidth >= best.portraitWidth && h >= other.portraitHeight
					&& other.portraitHeight >= best.portraitHeight) best = descriptors[i];
			}
		} else {
			for (int i = 0, n = descriptors.length; i < n; i++) {
				Resolution other = descriptors[i];
				if (w >= other.portraitHeight && other.portraitHeight >= best.portraitHeight && h >= other.portraitWidth
					&& other.portraitWidth >= best.portraitWidth) best = descriptors[i];
			}
		}
		return best;
	}

	public static class MyResolution extends Resolution {
		/**
		 * Constructs a {@code Resolution}.
		 *
		 * @param portraitWidth  This resolution's width.
		 * @param portraitHeight This resolution's height.
		 * @param folder         The name of the folder, where the assets which fit this resolution, are located.
		 */
		public MyResolution (int portraitWidth, int portraitHeight, String folder) {
			super(portraitWidth, portraitHeight, folder);
		}

		@Override public String toString () {
			return "Resolution{"+portraitWidth +"x"+portraitHeight+"}";
		}
	}
}

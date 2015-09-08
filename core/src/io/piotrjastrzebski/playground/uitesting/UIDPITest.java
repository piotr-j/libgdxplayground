package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDPITest extends BaseScreen {

	Resolution[] resolutions;

	public UIDPITest (GameReset game) {
		super(game);

		// retina mac: 2880 1800
		// most common pc resolution: 1920 1080 todo check steam charts
		// lowest supported? 1280 720
		// lowest x2 = 2560 1440
		// unless all our assets are vector based this is a major pain in the neck
		// Common resolutions from steam surveys:
		// 1920x1080, 34%
		// 1366z768, 27%
		// 1600x900, 7%
		// 1440x900, 5%
		// 1280z1024, 5%
		// 2560x1440, 1%
		// 1280x720, 1%
		testStuff();
	}

	private void testStuff() {
		// this is super dumb, cus we need portrait resolution here...
		Resolution lowRes = new Resolution(720, 1280, "low"); // 720p,
		Resolution mediumRes = new Resolution(1080, 1920, "med"); // 1080p
		Resolution highRes = new Resolution(1440, 2560, "high"); // retina
		Resolution ultraRes = new Resolution(2160, 3840, "ultra"); // 4k

		// also these must be specified in ascending order
		resolutions = new Resolution[] {lowRes, mediumRes, highRes, ultraRes};
		test(800, 600);
		test(1280, 720);
		test(1650, 1050);
		test(1920, 1080);
		test(1920, 1200);
		test(2560, 1440);
		test(2880, 1800);
	}

	private void test(int w, int h) {
		Gdx.app.log("", w + " " + h + " " + str(choose(w, h, resolutions)));
		Gdx.app.log("", h + " " + w + " " + str(choose(h, w, resolutions)));
	}

	public Resolution choose (int w, int h, Resolution... descriptors) {

		// Prefer the shortest side.
		Resolution best = descriptors[0];

		 if (w < h) {
			for (int i = 0, n = descriptors.length; i < n; i++) {
				Resolution other = descriptors[i];
				if (w >= other.portraitWidth && other.portraitWidth >= best.portraitWidth && h >= other.portraitHeight
					&& other.portraitHeight >= best.portraitHeight) {
					best = descriptors[i];
				}
			}
		} else {
			for (int i = 0, n = descriptors.length; i < n; i++) {
				Resolution other = descriptors[i];
				if (w >= other.portraitHeight && other.portraitHeight >= best.portraitHeight && h >= other.portraitWidth
					&& other.portraitWidth >= best.portraitWidth) {
					best = descriptors[i];
				}
			}
		}
		return best;
	}

	private String str(Resolution resolution) {
		return "Resolution<"+resolution.portraitWidth + "x" + resolution.portraitHeight+">";
	}

	@Override public boolean keyDown (int keycode) {

		return super.keyDown(keycode);
	}
}

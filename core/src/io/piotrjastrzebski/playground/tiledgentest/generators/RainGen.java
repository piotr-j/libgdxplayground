package io.piotrjastrzebski.playground.tiledgentest.generators;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by EvilEntity on 14/06/2015.
 */
public class RainGen {

	public static float[][] generate (float[][] terrainData, int dst, float waterLevel) {
		int width = terrainData.length;
		int height = terrainData[0].length;
		float[][] rainData = new float[width][height];
		float[][] blurData = new float[width][height];

		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				// init rain data to 1 when there is water and 0 otherwise
				rainData[mx][my] = terrainData[mx][my] <= waterLevel? 1 : 0;
			}
		}

		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				// horizontal blur
				blur(blurData, rainData, mx, my, width, height, dst, false);
			}
		}
		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				rainData[mx][my] += blurData[mx][my];
				blurData[mx][my] = 0;
			}
		}

		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				// vertical blur
				blur(blurData, rainData, mx, my, width, height, dst, true);
			}
		}
		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				rainData[mx][my] += blurData[mx][my];
				blurData[mx][my] = 0;
			}
		}

		// find max value so we can normalize
		float maxRF = 1;
		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				float rainfall = rainData[mx][my];
				if (rainfall > maxRF) maxRF = rainfall;
			}
		}

		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				float val = (terrainData[mx][my] - waterLevel) / (1 - waterLevel);
				// normalize
				rainData[mx][my] /= maxRF;
				rainData[mx][my] -= Interpolation.pow5In.apply(val)/5;
				rainData[mx][my] = MathUtils.clamp(rainData[mx][my], 0, 1);
			}
		}
		return rainData;
	}

	private static void blur (float[][] blurData, float[][]rainData, int x, int y, int width, int height, int dst, boolean horizontal) {
		// do some magic to blur stuff a bit
		float rainfall = rainData[x][y];
		if (horizontal) {
			for (int ox = -dst; ox <= dst; ox++) {
				// ignore source tile
				if (ox == 0) continue;
				int mx = x + ox;
				// bounds check
				if (mx < 0 || mx >= width) continue;
				int dx = ox;
				if (dx < 0) dx = -dx;

				blurData[mx][y] += rainfall * (1-(dx/(1.1f * dst)));
			}
		} else {
			for (int oy = -dst; oy <= dst; oy++) {
				if (oy == 0) continue;
				int my = y + oy;
				if (my < 0 || my >= height) continue;
				int dy = oy;
				if (dy < 0) dy = -dy;
				blurData[x][my] += rainfall * (1-(dy/(1.1f * dst)));
			}
		}
	}
}

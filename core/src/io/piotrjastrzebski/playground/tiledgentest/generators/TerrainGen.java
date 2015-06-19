package io.piotrjastrzebski.playground.tiledgentest.generators;

import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.playground.tiledgentest.OpenNoise;

/**
 * Created by EvilEntity on 14/06/2015.
 */
public class TerrainGen {
	public static float[][] generate(long seed, int width, int height) {
		return generate(256, 0.55f, seed, width, height);
	}

	public static float[][] generate(int largestFeature, float persistence, long seed, int width, int height) {
		OpenNoise noise = new OpenNoise(largestFeature, persistence, seed);
		double xStart = 0;
		double XEnd = width;
		double yStart = 0;
		double yEnd = height * 2;

		float[][] terrainData = new float[width][height];
		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				int nx = (int)(xStart + mx * ((XEnd - xStart) / width));
				int ny = (int)(yStart + my * ((yEnd - yStart) / height));
				// normalize
				double dVal = 0.5d + noise.getNoise(nx, ny);
				float val = (float)dVal;
				terrainData[mx][my] = MathUtils.clamp(val, 0, 1);
			}
		}
		return terrainData;
	}
}

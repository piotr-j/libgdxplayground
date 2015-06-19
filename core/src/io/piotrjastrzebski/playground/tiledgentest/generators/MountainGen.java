package io.piotrjastrzebski.playground.tiledgentest.generators;

import com.badlogic.gdx.math.Interpolation;
import io.piotrjastrzebski.playground.tiledgentest.OpenNoise;

/**
 * Created by EvilEntity on 14/06/2015.
 */
public class MountainGen {

	public static float[][] generate (long seed, float[][] terrainData) {
		int width = terrainData.length;
		int height = terrainData[0].length;
		OpenNoise noise = new OpenNoise(33, 0.5f, seed);
		double XEnd = width;
		double yEnd = height * 2;

		float[][] mountainData = new float[width][height];
		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				int nx = (int)(mx * ((XEnd) / width));
				int ny = (int)(my * ((yEnd) / height));
				// normalize
				double dVal = 0.5d + noise.getNoise(nx, ny);
				float val = (float)dVal;
				mountainData[mx][my] = Interpolation.pow3In.apply(val);
			}
		}
		return mountainData;
	}
}

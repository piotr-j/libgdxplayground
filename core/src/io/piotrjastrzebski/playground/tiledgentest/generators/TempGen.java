package io.piotrjastrzebski.playground.tiledgentest.generators;

import com.badlogic.gdx.math.Interpolation;
import io.piotrjastrzebski.playground.tiledgentest.OpenNoise;

/**
 * Created by EvilEntity on 14/06/2015.
 */
public class TempGen {

	private TempGen () {

	}

	public static float[][] generate(long seed, int width, int height){
		return generate(513, 0.55f, seed, width, height);
	}

	public static float[][] generate(int largestFeature, float persistence, long seed, int width, int height) {
		OpenNoise noise = new OpenNoise(largestFeature, persistence, seed);
		double xStart = 0;
		double XEnd = width;
		double yStart = 0;
		double yEnd = height * 10;

		float[][] result = new float[width][height];
		float max = 1.0f;
		for (int mx = 0; mx < width; mx++) {
			for (int my = 0; my < height; my++) {
				int nx = (int)(xStart + mx * ((XEnd - xStart) / width));
				int ny = (int)(yStart + my * ((yEnd - yStart) / height));
				// normalize
				double dVal = 0.5d + noise.getNoise(nx, ny);

				float alpha = my/(float)height;
				if (alpha > 0.5) alpha = 1-alpha;
				alpha = Interpolation.pow3Out.apply(alpha)*0.75f;

				float val = (float)dVal;
				if (val > max) max = val;

				val = Interpolation.sine.apply(val * alpha) + alpha;
				result[mx][my] = val;
			}
		}
		return result;
	}
}

package io.piotrjastrzebski.playground.tiledgentest;

import java.util.Random;

/**
 * Utilities for noise generation
 * Created by EvilEntity on 2015-02-23.
 */
public class OpenNoise {
	private OpenSimplexNoise[] octaves;
	private double[] frequencies;
	private double[] amplitudes;

	private int largestFeature;
	private double persistence;
	private long seed;

	public OpenNoise (int largestFeature, double persistence, long seed) {
		this.largestFeature = largestFeature;
		this.persistence = persistence;
		this.seed = seed;

		// receives a number (eg 128) and calculates what power of 2 it is (eg 2^7)
		int numberOfOctaves = (int)Math.ceil(Math.log10(largestFeature) / Math.log10(2));

		octaves = new OpenSimplexNoise[numberOfOctaves];
		frequencies = new double[numberOfOctaves];
		amplitudes = new double[numberOfOctaves];

		Random rnd = new Random(seed);

		for (int i = 0; i < numberOfOctaves; i++) {
			octaves[i] = new OpenSimplexNoise(rnd.nextInt());

			frequencies[i] = Math.pow(2, i);
			amplitudes[i] = Math.pow(persistence, octaves.length - i);
		}
	}

	public double getNoise (double x, double y) {
		double result = 0;
		for (int i = 0; i < octaves.length; i++) {
			result = result + octaves[i].eval(x / frequencies[i], y / frequencies[i]) * amplitudes[i];
		}
		return result;
	}

	public double getNoise (double x, double y, double z) {
		double result = 0;
		for (int i = 0; i < octaves.length; i++) {
			result = result + octaves[i].eval(x / frequencies[i], y / frequencies[i], z / frequencies[i]) * amplitudes[i];
		}
		return result;
	}

	public double getNoise (double x, double y, double z, double w ) {
		double result = 0;
		for (int i = 0; i < octaves.length; i++) {
			result = result + octaves[i].eval(x / frequencies[i], y / frequencies[i], z / frequencies[i], w / frequencies[i]) * amplitudes[i];
		}
		return result;
	}
}

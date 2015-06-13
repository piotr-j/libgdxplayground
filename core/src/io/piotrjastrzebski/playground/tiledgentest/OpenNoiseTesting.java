package io.piotrjastrzebski.playground.tiledgentest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Simple facility to play around with noise generation for terrain
 * Created by EvilEntity on 2015-02-23.
 */
public class OpenNoiseTesting {

	public static void main (String[] arg) {
		int largestFeature = 250;
		double persistence = 0.35;
		long seed = 5000;
		OpenNoise noise = new OpenNoise(largestFeature, persistence, seed);
		int octaves = (int)Math.ceil(Math.log10(largestFeature) / Math.log10(2));
		double xStart = 0;
		double XEnd = 500;
		double yStart = 0;
		double yEnd = 500;

		int xResolution = 512;
		int yResolution = 512;

		double[][] result = new double[xResolution][yResolution];

		for (int i = 0; i < xResolution; i++) {
			for (int j = 0; j < yResolution; j++) {
				int x = (int)(xStart + i * ((XEnd - xStart) / xResolution));
				int y = (int)(yStart + j * ((yEnd - yStart) / yResolution));
				result[i][j] = 0.5 * (1 + noise.getNoise(x, y));
			}
		}

		greyWriteImage(result, "lf=" + largestFeature + " p=" + persistence + " s=" + seed + " o=" + octaves);

	}

	public static void greyWriteImage (double[][] data, String extra) {
		//this takes and array of doubles between 0 and 1 and generates a grey scale image from them

		BufferedImage image = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < data[0].length; y++) {
			for (int x = 0; x < data.length; x++) {
				if (data[x][y] > 1) {
					data[x][y] = 1;
				}
				if (data[x][y] < 0) {
					data[x][y] = 0;
				}
				Color col = new Color((float)data[x][y], (float)data[x][y], (float)data[x][y]);
				image.setRGB(x, y, col.getRGB());
			}
		}

		try {
			// retrieve image
			File outputfile = new File("open noise (" + extra + ") " + data.length + "x" + data[0].length + ".png");
			outputfile.createNewFile();

			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			//o no! Blank catches are bad
			throw new RuntimeException("I didn't handle this very well");
		}
	}

}

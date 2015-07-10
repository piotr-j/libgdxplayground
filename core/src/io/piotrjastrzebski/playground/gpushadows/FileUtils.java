package io.piotrjastrzebski.playground.gpushadows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

	public static String read(InputStream in) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			char[] buffer = new char[8192];

			int read;
			while ((read = bufferedReader.read(buffer, 0, buffer.length)) > 0) {
				stringBuilder.append(buffer, 0, read);
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			throw new RuntimeException("failed to get string from input stream", e);
		} finally {
			try {
				in.close();
			} catch (Exception e) {

			}
		}
	}
	
	public static InputStream getClasspathInputStream(String path) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream in = classLoader.getResourceAsStream(path);
		if (in == null)
			throw new RuntimeException("couldnt find stream for " + path);
		return in;
	}
	
	public static String getContent(String path) {
		return read(getClasspathInputStream(path));
	}
}

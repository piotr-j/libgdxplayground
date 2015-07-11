package io.piotrjastrzebski.playground.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
//		final Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
//		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//			@Override public void uncaughtException (Thread thread, Throwable ex) {
//				System.err.println("Exploded!");
//				// run default
//				ex.printStackTrace();
//				handler.uncaughtException(thread, ex);
//			}
//		});

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		new LwjglApplication(new PlaygroundGame(), config);
	}
}

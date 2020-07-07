package io.piotrjastrzebski.playground.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import io.piotrjastrzebski.playground.PlatformBridge;
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

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1280, 720);
		config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 0);
		config.setHdpiMode(HdpiMode.Logical);
		new Lwjgl3Application(new PlaygroundGame(new DesktopBridge()), config);
	}

	private static class DesktopBridge implements PlatformBridge {

		@Override public float getPixelScaleFactor () {
			// cba to find replacement right now
//			return org.lwjgl.opengl.Display.getPixelScaleFactor();
			return 1.0f;
		}
	}
}

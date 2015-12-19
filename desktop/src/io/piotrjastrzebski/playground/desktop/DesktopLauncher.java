package io.piotrjastrzebski.playground.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.piotrjastrzebski.playground.PlatformBridge;
import io.piotrjastrzebski.playground.PlaygroundGame;
import org.lwjgl.opengl.Display;

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
		config.useHDPI = true;
		config.stencil = 8;
		new LwjglApplication(new PlaygroundGame(new DesktopBridge()), config);
	}

	private static class DesktopBridge implements PlatformBridge {

		@Override public float getPixelScaleFactor () {
			return Display.getPixelScaleFactor();
		}
	}
}

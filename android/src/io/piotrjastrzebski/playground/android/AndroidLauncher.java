package io.piotrjastrzebski.playground.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import io.piotrjastrzebski.playground.PlatformBridge;
import io.piotrjastrzebski.playground.PlaygroundGame;

public class AndroidLauncher extends AndroidApplication {
//	public AndroidLauncher () {
//		final Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
//		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//			@Override public void uncaughtException (Thread thread, Throwable ex) {
//				System.err.println("Exploded!");
//				// run default
//				handler.uncaughtException(thread, ex);
//			}
//		});
//	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new PlaygroundGame(new AndroidBridge()), config);
	}

	private static class AndroidBridge implements PlatformBridge {
		@Override public float getPixelScaleFactor () {
			return 1;
		}
	}
}

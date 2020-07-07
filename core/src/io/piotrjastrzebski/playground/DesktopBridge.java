package io.piotrjastrzebski.playground;

/**
 * Created by EvilEntity on 16/12/2015.
 */
public class DesktopBridge implements PlatformBridge {
	@Override public float getPixelScaleFactor () {
//		return org.lwjgl.opengl.Display.getPixelScaleFactor();
		return 1.0f;
	}
}

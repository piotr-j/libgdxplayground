package io.piotrjastrzebski.playground;

import org.lwjgl.opengl.Display;

/**
 * Created by EvilEntity on 16/12/2015.
 */
public class DesktopBridge implements PlatformBridge {
	@Override public float getPixelScaleFactor () {
		return Display.getPixelScaleFactor();
	}
}

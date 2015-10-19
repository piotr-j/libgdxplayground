package io.piotrjastrzebski.playground.bttests.simplishedittest;

/**
 * Created by PiotrJ on 16/10/15.
 */
interface Logger {
	void log (String tag, String msg);

	void error (String tag, String msg);

	void error (String tag, String msg, Exception e);
}

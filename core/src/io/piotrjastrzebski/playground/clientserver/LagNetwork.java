package io.piotrjastrzebski.playground.clientserver;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.Iterator;

/**
 * Simple message queue with delay
 * Created by PiotrJ on 21/06/15.
 */
public class LagNetwork<T> {
	Pool<Message<T>> messagePool = new Pool<Message<T>>(64) {
		@Override protected Message<T> newObject () {
			return new Message<>();
		}
	};
	private Array<Message> messages = new Array<>();

	/**
	 *
	 * @param lag lag in ms
	 * @param msg
	 */
	public void send(int lag, T msg) {
		Message<T> message = messagePool.obtain();
		message.init(lag, msg);
		messages.add(message);
	}

	public Message<T> receive() {
		long now = System.currentTimeMillis();
		Iterator<Message> msgIter = messages.iterator();
		while (msgIter.hasNext()) {
			Message<T> message = msgIter.next();
			if (message.deliver <= now) {
				msgIter.remove();
				return message;
			}
		}
		return null;
	}

	public void free(Message<T> message) {
		messagePool.free(message);
	}

	public class Message<E> implements Pool.Poolable {
		long deliver;
		E data;
		public long ts;

		public void init(int lag, E msg) {
			ts = System.currentTimeMillis();
			deliver = ts + lag;
			this.data = msg;

		}

		@Override public void reset () {
			deliver = 0;
			data = null;
		}
	}
}

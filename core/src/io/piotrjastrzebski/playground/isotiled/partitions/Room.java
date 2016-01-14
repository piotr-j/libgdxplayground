package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;
import io.piotrjastrzebski.playground.isotiled.partitions.MapRegion.SubRegion;

/**
 * A collection of sub regions forming single contiguous space
 *
 * Created by EvilEntity on 13/01/2016.
 */
public class Room implements Pool.Poolable {
	private static Pool<Room> pool = new Pool<Room>() {
		@Override protected Room newObject () {
			return new Room();
		}
	};

	public static Room obtain () {
		return pool.obtain();
	}

	public static void free (Room room) {
		pool.free(room);
	}

	public static void free (Array<Room> rooms) {
		pool.freeAll(rooms);
		rooms.clear();
	}

	public static void clear() {
		pool.clear();
	}

	public int id;
	public Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
	public ObjectSet<SubRegion> subRegions = new ObjectSet<>();

	/**
	 * Merge two rooms, assimilated room should be freed
	 * @return if this room assimilated other
	 */
	public boolean merge(Room other) {
		if (subRegions.size > other.subRegions.size) {

			return true;
		} else {

			return false;
		}
	}

	public void add (SubRegion region) {
		subRegions.add(region);
	}

	public boolean remove (SubRegion region) {
		return subRegions.remove(region);
	}

	@Override public void reset () {
		subRegions.clear();
	}
}

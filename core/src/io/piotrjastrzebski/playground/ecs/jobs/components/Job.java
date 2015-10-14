package io.piotrjastrzebski.playground.ecs.jobs.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.badlogic.gdx.utils.IntArray;
import io.piotrjastrzebski.playground.ecs.jobs.ECSJobsTest;

/**
 * Created by EvilEntity on 17/08/2015.
 */
public class Job extends Component {
	public String name;
	// id if entity
	public int eid = ECSJobsTest.NULL_ID;
	public int workerID = ECSJobsTest.NULL_ID;
	public IntArray required = new IntArray();
	public int next = ECSJobsTest.NULL_ID;
	public int previous = ECSJobsTest.NULL_ID;
	public float progress;


	public void setName (String name) {
		this.name = name;
	}

	/**
	 * Job that must be done after this one by same entity
	 */
	public void next(Job job) {
		next = job.eid;
		job.prev(this);
	}

	/**
	 * Previous unfinished job in a chain if any
	 */
	public void prev(Job job) {
		previous = job.eid;
	}

	/**
	 * That job must be done before this one is available
	 */
	public void require(Job job) {
		required.add(job.eid);
	}

	public void parent(Entity e) {
		eid = e.getId();
	}

	@Override public String toString () {
		return "Job " + name;
	}
}

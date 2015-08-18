package io.piotrjastrzebski.playground.ecs.jobs.components;

import com.artemis.Component;

/**
 * Created by EvilEntity on 17/08/2015.
 */
public class Worker extends Component {
	// id if entity
	public int eid = -1;
	public int jobID = -1;
	public String name;

	public void claim (Job job) {
		jobID = job.eid;
		job.workerID = eid;
	}

	@Override public String toString () {
		return "Worker " + name;
	}
}

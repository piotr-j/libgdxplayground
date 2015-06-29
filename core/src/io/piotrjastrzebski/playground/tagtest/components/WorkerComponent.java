package io.piotrjastrzebski.playground.tagtest.components;

import com.artemis.Component;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.tagtest.systems.JobSystem;

import java.util.UUID;

/**
 * Created by PiotrJ on 27/06/15.
 */
public class WorkerComponent extends Component {
    public static final int NULL_ID = -1;
    // probably need more data about each jon, skill or whatever
    public Array<String> jobPref = new Array<>();
    // component of current job, if any
    public transient JobComponent job;
    // id of entity that has the job component, -1 == not set
    public transient int jobEntityID = NULL_ID;
    /*
    Note to restore, we need to find job by the uuid via JobSystem and call setJob()
    jobSystem.restoreJob(worker);
     */
    public UUID jobEntityUUID;

    public void clearJob() {
        job = null;
        jobEntityID = NULL_ID;
    }

    public void setJob(JobSystem.Job job) {
        this.jobEntityUUID = job.uuid;
        this.job = job.comp;
    }
}

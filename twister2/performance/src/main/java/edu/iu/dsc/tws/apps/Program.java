package edu.iu.dsc.tws.apps;

import edu.iu.dsc.tws.api.JobConfig;
import edu.iu.dsc.tws.api.Twister2Submitter;
import edu.iu.dsc.tws.api.basic.job.BasicJob;
import edu.iu.dsc.tws.apps.stream.Reduce;
import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.rsched.core.ResourceAllocator;
import edu.iu.dsc.tws.rsched.spi.resource.ResourceContainer;

import java.util.HashMap;

public class Program {
  public static void main(String[] args) {
    // first load the configurations from command line and config files
    Config config = ResourceAllocator.loadConfig(new HashMap<>());

    int containers = Integer.parseInt(args[0]);
    int size = Integer.parseInt(args[1]);
    int itr = Integer.parseInt(args[2]);
    int col = Integer.parseInt(args[3]);
    boolean stream = Boolean.parseBoolean(args[4]);
    String taskStages = args[5];

    // build JobConfig
    JobConfig jobConfig = new JobConfig();
    jobConfig.put(Constants.ARGS_ITR, Integer.toString(itr));
    jobConfig.put(Constants.ARGS_COL, Integer.toString(col));
    jobConfig.put(Constants.ARGS_SIZE, Integer.toString(size));
    jobConfig.put(Constants.ARGS_CONTAINERS, Integer.toString(containers));
    jobConfig.put(Constants.ARGS_TASK_STAGES, taskStages);

    // build the job
    BasicJob basicJob = null;
    if (!stream) {
      if (col == 0) {
        basicJob = BasicJob.newBuilder()
            .setName("reduce-bench")
            .setContainerClass(Reduce.class.getName())
            .setRequestResource(new ResourceContainer(2, 1024), containers)
            .setConfig(jobConfig)
            .build();
        // now submit the job
        Twister2Submitter.submitContainerJob(basicJob, config);
      }
    }
  }
}

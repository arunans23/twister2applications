package edu.iu.dsc.tws.apps.batch;

import edu.iu.dsc.tws.apps.data.DataGenerator;
import edu.iu.dsc.tws.apps.utils.JobParameters;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;
import edu.iu.dsc.tws.comms.api.MessageFlags;
import edu.iu.dsc.tws.comms.mpi.io.IntData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MultiSource implements Runnable {
  private static final Logger LOG = Logger.getLogger(MultiSource.class.getName());

  private long startSendingTime;

  private int task;

  private DataFlowOperation operation;

  private DataGenerator generator;

  private JobParameters jobParameters;

  private List<Integer> destinations;

  public MultiSource(int task, JobParameters jobParameters, DataFlowOperation op, DataGenerator dataGenerator) {
    this.task = task;
    this.jobParameters = jobParameters;
    this.operation = op;
    this.generator = dataGenerator;
    this.destinations = new ArrayList<>();
    int fistStage = jobParameters.getTaskStages().get(0);
    int secondStage = jobParameters.getTaskStages().get(1);
    for (int i = 0; i < secondStage; i++) {
      destinations.add(i + fistStage);
    }
  }

  @Override
  public void run() {
    int noOfDestinations = destinations.size();

    startSendingTime = System.nanoTime();
    IntData data = generator.generateData();
    int iterations = jobParameters.getIterations();
    int nextIndex = 0;
    for (int i = 0; i < iterations; i++) {
      nextIndex = nextIndex % noOfDestinations;
      if (i >= iterations - destinations.size()) {
        nextIndex = iterations - i - 1;
      }
      int dest = destinations.get(nextIndex);
      nextIndex++;
      int flag = 0;
      if (i >= iterations - destinations.size()) {
        flag = MessageFlags.FLAGS_LAST;
      }
      while (!operation.send(task, data, flag, dest)) {
        // lets wait a litte and try again
        operation.progress();
//        try {
//          Thread.sleep(1);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
      }
//      LOG.info(String.format("%d Sent message with flag %d %d", task, i, flag));
    }
  }

  public long getStartSendingTime() {
    return startSendingTime;
  }
}

package edu.iu.dsc.tws.apps.storm;

import edu.iu.dsc.tws.apps.batch.Source;
import edu.iu.dsc.tws.apps.data.DataGenerator;
import edu.iu.dsc.tws.apps.utils.JobParameters;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;
import edu.iu.dsc.tws.comms.api.MessageFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PartitionSource {
  private static final Logger LOG = Logger.getLogger(Source.class.getName());

  private long startSendingTime;

  private int task;

  private DataFlowOperation operation;

  private DataGenerator generator;

  private JobParameters jobParameters;

  private List<Long> startOfMessages;

  private int gap;

  private boolean genString;

  private List<Integer> destinations;

  private long lastMessageTime = 0;

  private int currentIteration = 0;

  private int nextIndex = 0;

  private int executorId;

  public PartitionSource(int task, JobParameters jobParameters, DataFlowOperation op, DataGenerator dataGenerator, boolean getString) {
    this.task = task;
    this.jobParameters = jobParameters;
    this.operation = op;
    this.generator = dataGenerator;
    this.startOfMessages = new ArrayList<>();
    this.gap = jobParameters.getGap();
    this.genString = getString;
    this.destinations = new ArrayList<>();
  }

  public PartitionSource(int task, JobParameters jobParameters, DataFlowOperation op, DataGenerator dataGenerator) {
    this.task = task;
    this.jobParameters = jobParameters;
    this.operation = op;
    this.generator = dataGenerator;
    this.startOfMessages = new ArrayList<>();
    this.gap = jobParameters.getGap();
    this.genString = false;
    this.destinations = new ArrayList<>();

    int fistStage = jobParameters.getTaskStages().get(0);
    int secondStage = jobParameters.getTaskStages().get(1);
    for (int i = 0; i < secondStage; i++) {
      destinations.add(i + fistStage);
    }
  }

  public PartitionSource(int task, JobParameters jobParameters, DataGenerator dataGenerator, int executorId) {
    this.task = task;
    this.jobParameters = jobParameters;
    this.generator = dataGenerator;
    this.startOfMessages = new ArrayList<>();
    this.gap = jobParameters.getGap();
    this.genString = false;
    this.destinations = new ArrayList<>();
    this.executorId = executorId;

    int fistStage = jobParameters.getTaskStages().get(0);
    int secondStage = jobParameters.getTaskStages().get(1);
    for (int i = 0; i < secondStage; i++) {
      destinations.add(i + fistStage);
    }
  }

  public void setOperation(DataFlowOperation operation) {
    this.operation = operation;
  }

  public void execute() {
    int noOfDestinations = destinations.size();
    startSendingTime = System.currentTimeMillis();
    Object data;
    if (genString) {
      data = generator.generateStringData();
    } else {
      data = generator.generateData();
    }
    int iterations = jobParameters.getIterations();
    operation.progress();

    long currentTime = System.currentTimeMillis();
    if (gap > (currentTime - lastMessageTime)) {
      return;
    }

    if (currentIteration < iterations) {
      startOfMessages.add(System.nanoTime());
      nextIndex = nextIndex % noOfDestinations;
      int dest = destinations.get(nextIndex);
      nextIndex++;
      int flag = 0;
      if (currentIteration >= iterations - destinations.size()) {
        flag = MessageFlags.FLAGS_LAST;
      }
      lastMessageTime = System.currentTimeMillis();
      if (!operation.send(task, data, flag, dest)) {
        // lets wait a litte and try again
        return;
      }
//      LOG.info(String.format("%d task %d sends %d", executorId, task, currentIteration));
      currentIteration++;
    }
  }

  public long getStartSendingTime() {
    return startSendingTime;
  }

  public List<Long> getStartOfMessages() {
    return startOfMessages;
  }
}

public class PCB {

    // Stores all information needed to track a process during scheduling.
    private int processId;
    private String state;
    private int burstTime;
    private int remainingTime;
    private int priority;
    private int originalPriority;
    private int memoryRequired;
    private int arrivalOrder;
    private int arrivalTime;
    private int startTime;
    private int terminationTime;
    private int waitingTime;
    private int turnaroundTime;
    private boolean starved;
    private int readyQueueEnterTime;
    private int lastAgingTime;

    public PCB(int processId, int burstTime, int priority, int memoryRequired, int arrivalOrder) {
        this.processId = processId;
        this.state = "NEW";
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.originalPriority = priority;
        this.memoryRequired = memoryRequired;
        this.arrivalOrder = arrivalOrder;
        this.arrivalTime = 0;
        this.startTime = -1;
        this.terminationTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.starved = false;
        this.readyQueueEnterTime = -1;
        this.lastAgingTime = -1;
    }

    // Reduces the remaining burst time after the process uses the CPU.
    public void reduceRemainingTime(int timeUsed) {
        remainingTime -= timeUsed;

        if (remainingTime < 0) {
            remainingTime = 0;
        }
    }

    // Improves priority for aging. Lower number means higher priority.
    public void applyAging() {
        if (priority > 1) {
            priority--;
        }
    }

    // Calculates final waiting and turnaround times after termination.
    public void calculateFinalTimes() {
        turnaroundTime = terminationTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;

        if (waitingTime < 0) {
            waitingTime = 0;
        }
    }

    // Records the first time the process starts running.
    public void setStartTimeIfFirstRun(int currentTime) {
        if (startTime == -1) {
            startTime = currentTime;
        }
    }

    public int getProcessId() {
        return processId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getOriginalPriority() {
        return originalPriority;
    }

    public int getMemoryRequired() {
        return memoryRequired;
    }

    public int getArrivalOrder() {
        return arrivalOrder;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(int terminationTime) {
        this.terminationTime = terminationTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public boolean isStarved() {
        return starved;
    }

    public void setStarved(boolean starved) {
        this.starved = starved;
    }

    public int getReadyQueueEnterTime() {
        return readyQueueEnterTime;
    }

    public void setReadyQueueEnterTime(int readyQueueEnterTime) {
        this.readyQueueEnterTime = readyQueueEnterTime;
    }

    public int getLastAgingTime() {
        return lastAgingTime;
    }

    public void setLastAgingTime(int lastAgingTime) {
        this.lastAgingTime = lastAgingTime;
    }

    @Override
    public String toString() {
        return "P" + processId
                + " [burst=" + burstTime
                + ", remaining=" + remainingTime
                + ", priority=" + priority
                + ", memory=" + memoryRequired + "MB"
                + ", state=" + state
                + "]";
    }
}
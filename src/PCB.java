/**
 * PCB = Process Control Block.
 *
 * This class represents ONE process in the CPU scheduling simulation.
 *
 * Example line from job.txt:
 *     1:25:4;500
 *
 * This becomes:
 *     PCB p1 = new PCB(1, 25, 4, 500, 0);
 *
 * Meaning:
 *     processId      = 1
 *     burstTime      = 25 ms
 *     priority       = 4
 *     memoryRequired = 500 MB
 *     arrivalOrder   = 0, because it was the first process read from the file
 */
public class PCB {

    // -----------------------------
    // Basic process information
    // -----------------------------

    private int processId;

    /*
     * State is stored as a simple String to keep the project easy for the group.
     * Expected values:
     *     "NEW"
     *     "READY"
     *     "RUNNING"
     *     "TERMINATED"
     *
     * Example:
     *     process.setState("READY");
     */
    private String state;

    /*
     * burstTime is the original CPU burst.
     * It should NOT change.
     *
     * Example:
     *     P1 has burstTime = 25
     *
     * Even after Round Robin runs P1 for 5 ms, burstTime remains 25.
     */
    private int burstTime;

    /*
     * remainingTime changes while the process executes.
     *
     * Example:
     *     P1 original burst = 25
     *     After running for 5 ms in Round Robin:
     *         remainingTime = 20
     */
    private int remainingTime;

    /*
     * Priority is used only in Priority Scheduling.
     * Important rule from the project:
     *     smaller priority number = higher priority
     *
     * Example:
     *     priority 1 is better than priority 5.
     */
    private int priority;

    /*
     * originalPriority is useful because aging changes priority.
     * This lets us remember the original value from the input file.
     */
    private int originalPriority;

    /*
     * Memory required by this process in MB.
     * Example:
     *     if memoryRequired = 500, this process consumes 500 MB while loaded.
     */
    private int memoryRequired;

    /*
     * arrivalOrder is the order in which the process appears in job.txt.
     * Since all processes arrive at time 0, this is used for tie-breaking.
     *
     * Example:
     *     If P1 and P2 have the same burst time in SJF,
     *     choose the one with smaller arrivalOrder.
     */
    private int arrivalOrder;

    /*
     * The project assumes all processes arrive at time 0.
     */
    private int arrivalTime;

    // -----------------------------
    // Timing results
    // -----------------------------

    /*
     * startTime = the first time this process gets the CPU.
     *
     * For Round Robin, the process may run many times, but startTime is only
     * the first CPU start.
     *
     * We use -1 to mean: "this process has not started yet".
     */
    private int startTime;

    /*
     * terminationTime = the time when the process completely finishes.
     */
    private int terminationTime;

    /*
     * waitingTime = total time spent waiting.
     * Formula:
     *     waitingTime = turnaroundTime - burstTime
     */
    private int waitingTime;

    /*
     * turnaroundTime = total time from arrival to completion.
     * Formula:
     *     turnaroundTime = terminationTime - arrivalTime
     *
     * Since arrivalTime is 0 in this project:
     *     turnaroundTime = terminationTime
     */
    private int turnaroundTime;

    // -----------------------------
    // Priority starvation / aging fields
    // -----------------------------

    /*
     * true if the process suffered starvation in Priority Scheduling.
     */
    private boolean starved;

    /*
     * The simulated time when this process entered the ready queue.
     * Used to calculate how long it waited in the ready queue.
     */
    private int readyQueueEnterTime;

    /*
     * The last simulated time when aging was applied to this process.
     */
    private int lastAgingTime;

    /**
     * Constructor used when reading one process from job.txt.
     *
     * Example:
     *     PCB p1 = new PCB(1, 25, 4, 500, 0);
     *
     * This creates process P1 with:
     *     burstTime = 25
     *     remainingTime = 25
     *     priority = 4
     *     memoryRequired = 500
     */
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
        this.readyQueueEnterTime = 0;
        this.lastAgingTime = 0;
    }

    /**
     * Reduces remainingTime when the process runs on the CPU.
     *
     * Example:
     *     remainingTime = 25
     *     reduceRemainingTime(5)
     *     remainingTime becomes 20
     *
     * This method protects us from going below zero.
     */
    public void reduceRemainingTime(int timeUsed) {
        remainingTime -= timeUsed;

        if (remainingTime < 0) {
            remainingTime = 0;
        }
    }

    /**
     * Returns true if the process has finished all of its CPU burst.
     *
     * Example:
     *     if (process.isFinished()) {
     *         // set termination time and release memory
     *     }
     */
    public boolean isFinished() {
        return remainingTime == 0;
    }

    /**
     * Applies aging to this process.
     *
     * Project rule:
     *     Aging improves priority by decreasing the priority number by 1.
     *
     * Example:
     *     priority 10 becomes 9
     *     priority 9 becomes 8
     *
     * Priority should not go below 1 because 1 is already the highest priority.
     */
    public void applyAging() {
        if (priority > 1) {
            priority--;
        }
    }

    /**
     * Calculates turnaround time and waiting time after the process finishes.
     *
     * Formulas:
     *     turnaroundTime = terminationTime - arrivalTime
     *     waitingTime = turnaroundTime - burstTime
     *
     * Example:
     *     burstTime = 25
     *     arrivalTime = 0
     *     terminationTime = 58
     *
     *     turnaroundTime = 58 - 0 = 58
     *     waitingTime = 58 - 25 = 33
     */
    public void calculateFinalTimes() {
        turnaroundTime = terminationTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;

        if (waitingTime < 0) {
            waitingTime = 0;
        }
    }

    /**
     * Sets the first CPU start time only once.
     *
     * This is very useful for Round Robin because the same process may run
     * multiple times.
     *
     * Example:
     *     P1 first runs at time 0, so startTime = 0.
     *     P1 later runs again at time 15, but startTime should remain 0.
     */
    public void setStartTimeIfFirstRun(int currentTime) {
        if (startTime == -1) {
            startTime = currentTime;
        }
    }

    // -----------------------------
    // Getters and setters
    // -----------------------------
    // These methods let other classes read/update private fields safely.

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

    /**
     * Useful for quick testing.
     *
     * Example:
     *     System.out.println(process);
     */
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

import java.util.ArrayList;

/**
 * Scheduler contains the CPU scheduling algorithms and printing methods.
 *
 * This simplified design keeps all scheduling-related logic in one file:
 *     1. SJF
 *     2. Round Robin
 *     3. Priority Scheduling
 *     4. Gantt chart printing
 *     5. Process table printing
 *     6. Average waiting/turnaround printing
 *
 * The methods are intentionally written as a starter skeleton.
 * Some TODO sections are left for the team to implement.
 */
public class Scheduler {

    private SharedData data;
    private int currentTime;

    /**
     * Constructor.
     *
     * Example usage:
     *     Scheduler scheduler = new Scheduler(data);
     *     scheduler.runSJF();
     */
    public Scheduler(SharedData data) {
        this.data = data;
        this.currentTime = 0;
    }

    /**
     * Runs Shortest Job First scheduling.
     *
     * Rule:
     *     Choose the process with the smallest remainingTime.
     *
     * Tie-breaker:
     *     If two processes have equal burst time, choose the one that appeared
     *     earlier in job.txt. That means smaller arrivalOrder.
     *
     * Example:
     *     P1 burst = 25
     *     P2 burst = 13
     *     P3 burst = 20
     *
     * SJF order:
     *     P2 -> P3 -> P1
     *
     * TODO implementation steps:
     *     while not all processes completed:
     *         if readyQueue is empty:
     *             currentTime++
     *             continue
     *
     *         selected = findShortestJob(readyQueue)
     *         remove selected from readyQueue
     *         run selected until completion
     *         add GanttEntry
     *         calculate times
     *         release memory
     *
     * After finishing:
     *     printAllResults("Shortest Job First", false)
     */
    public void runSJF() {
        System.out.println("\nRunning Shortest Job First...");

        // TODO: Implement SJF scheduling here.
        // Use findShortestJob(data.getReadyQueueSnapshot()).

        printNotImplementedMessage("SJF");
        data.setSimulationFinished(true);
    }

    /**
     * Runs Round Robin scheduling.
     *
     * Rule:
     *     Each process gets at most 5 ms of CPU time.
     *
     * The project quantum is:
     *     SharedData.ROUND_ROBIN_QUANTUM = 5
     *
     * Example:
     *     P1 remaining = 12
     *     runTime = min(5, 12) = 5
     *     after running: P1 remaining = 7
     *
     * If process is not finished:
     *     put it back at the end of readyQueue.
     *
     * If process is finished:
     *     calculate times and release memory.
     *
     * TODO implementation steps:
     *     while not all processes completed:
     *         process = data.pollReadyQueue()
     *
     *         if process is null:
     *             currentTime++
     *             continue
     *
     *         runTime = min(5, process.remainingTime)
     *         record burstBefore
     *         reduce remaining time
     *         currentTime += runTime
     *         record burstAfter
     *         add GanttEntry
     *
     *         if process finished:
     *             finishProcess(process)
     *         else:
     *             add process back to readyQueue
     */
    public void runRoundRobin() {
        System.out.println("\nRunning Round Robin...");

        // TODO: Implement Round Robin scheduling here.
        // Use data.pollReadyQueue() to get the next process.

        printNotImplementedMessage("Round Robin");
        data.setSimulationFinished(true);
    }

    /**
     * Runs non-preemptive Priority Scheduling.
     *
     * Rule:
     *     Choose the process with the smallest priority number.
     *
     * Important:
     *     priority 1 is higher than priority 5.
     *
     * Tie-breaker:
     *     If two processes have the same priority, choose the earlier arrivalOrder.
     *
     * Starvation rule from the project:
     *     A process is starved if it waits in readyQueue for more than N * 5 ms,
     *     where N is the number of processes currently in readyQueue.
     *
     * Aging rule:
     *     Every 4 ms, decrease priority number by 1.
     *
     * TODO implementation steps:
     *     while not all processes completed:
     *         if readyQueue is empty:
     *             currentTime++
     *             continue
     *
     *         for each process in readyQueue:
     *             checkStarvation(process)
     *             applyAgingIfNeeded(process)
     *
     *         selected = findHighestPriorityProcess(readyQueue)
     *         remove selected from readyQueue
     *         run selected until completion
     *         add GanttEntry
     *         calculate times
     *         release memory
     */
    public void runPriority() {
        System.out.println("\nRunning Priority Scheduling...");

        // TODO: Implement Priority Scheduling here.
        // Use findHighestPriorityProcess(data.getReadyQueueSnapshot()).

        printNotImplementedMessage("Priority Scheduling");
        data.setSimulationFinished(true);
    }

    /**
     * Finds the process with the shortest remaining time.
     * Used by SJF.
     *
     * Example:
     *     P1 remaining = 10, arrivalOrder = 0
     *     P2 remaining = 10, arrivalOrder = 1
     *
     * Both have equal remaining time, so choose P1 because it arrived first.
     */
    private PCB findShortestJob(ArrayList<PCB> readyProcesses) {
        if (readyProcesses.isEmpty()) {
            return null;
        }

        PCB shortest = readyProcesses.get(0);

        for (PCB process : readyProcesses) {
            if (process.getRemainingTime() < shortest.getRemainingTime()) {
                shortest = process;
            } else if (process.getRemainingTime() == shortest.getRemainingTime()
                    && process.getArrivalOrder() < shortest.getArrivalOrder()) {
                shortest = process;
            }
        }

        return shortest;
    }

    /**
     * Finds the process with the highest priority.
     * Used by Priority Scheduling.
     *
     * Remember:
     *     smaller priority number = higher priority.
     *
     * Example:
     *     P1 priority = 4
     *     P2 priority = 2
     *
     * Choose P2 because 2 is better than 4.
     */
    private PCB findHighestPriorityProcess(ArrayList<PCB> readyProcesses) {
        if (readyProcesses.isEmpty()) {
            return null;
        }

        PCB highest = readyProcesses.get(0);

        for (PCB process : readyProcesses) {
            if (process.getPriority() < highest.getPriority()) {
                highest = process;
            } else if (process.getPriority() == highest.getPriority()
                    && process.getArrivalOrder() < highest.getArrivalOrder()) {
                highest = process;
            }
        }

        return highest;
    }

    /**
     * Marks a process as finished and calculates final times.
     *
     * Example:
     *     currentTime = 58
     *     P1 burst = 25
     *
     *     terminationTime = 58
     *     turnaroundTime = 58 - 0 = 58
     *     waitingTime = 58 - 25 = 33
     */
    private void finishProcess(PCB process) {
        process.setTerminationTime(currentTime);
        process.setState("TERMINATED");
        process.calculateFinalTimes();
        data.removeProcessFromMemory(process);
    }

    /**
     * Adds one Gantt chart entry after running a process.
     *
     * Example usage:
     *     recordGanttEntry(process, 0, 5, 25, 20);
     *
     * Output later:
     *     [0 - 5] P1 burst: 25 -> 20
     */
    private void recordGanttEntry(PCB process, int start, int end, int burstBefore, int burstAfter) {
        GanttEntry entry = new GanttEntry(
                process.getProcessId(),
                start,
                end,
                burstBefore,
                burstAfter
        );

        data.addGanttEntry(entry);
    }

    /**
     * Checks if a process suffered starvation.
     * Used only in Priority Scheduling.
     *
     * Project rule:
     *     starved if waiting time in readyQueue > N * 5 ms
     *
     * where:
     *     N = current number of processes in readyQueue
     *
     * Example:
     *     readyQueue size = 4
     *     threshold = 4 * 5 = 20 ms
     *
     * If process waited 21 ms, mark it as starved.
     */
    private void checkStarvation(PCB process) {
        int readyQueueSize = data.getReadyQueueSize();
        int threshold = readyQueueSize * 5;
        int waitingInReadyQueue = currentTime - process.getReadyQueueEnterTime();

        if (waitingInReadyQueue > threshold) {
            process.setStarved(true);
        }
    }

    /**
     * Applies aging if enough time passed.
     * Used only in Priority Scheduling.
     *
     * Project rule:
     *     Every 4 ms, improve priority by decreasing priority number by 1.
     *
     * Example:
     *     current priority = 10
     *     after aging: priority = 9
     */
    private void applyAgingIfNeeded(PCB process) {
        int timeSinceLastAging = currentTime - process.getLastAgingTime();

        if (timeSinceLastAging >= SharedData.AGING_INTERVAL) {
            process.applyAging();
            process.setLastAgingTime(currentTime);
        }
    }

    /**
     * Prints everything required by the project:
     *     1. Algorithm name
     *     2. Gantt chart
     *     3. Process table
     *     4. Average waiting time
     *     5. Average turnaround time
     *     6. Starved processes if priority algorithm was selected
     *
     * Example usage:
     *     printAllResults("Round Robin", false);
     *     printAllResults("Priority Scheduling", true);
     */
    private void printAllResults(String algorithmName, boolean showStarvedProcesses) {
        System.out.println("\n====================================");
        System.out.println("Algorithm: " + algorithmName);
        System.out.println("====================================");

        printGanttChart();
        printProcessTable();
        printAverages();

        if (showStarvedProcesses) {
            printStarvedProcesses();
        }
    }

    /**
     * Prints the Gantt chart.
     *
     * Example output:
     *     [0 - 5] P1 burst: 25 -> 20
     *     [5 - 10] P2 burst: 13 -> 8
     */
    private void printGanttChart() {
        System.out.println("\nGantt Chart:");

        ArrayList<GanttEntry> entries = data.getGanttChartSnapshot();

        if (entries.isEmpty()) {
            System.out.println("No Gantt entries yet.");
            return;
        }

        for (GanttEntry entry : entries) {
            System.out.println(entry);
        }
    }

    /**
     * Prints the process table required by the project.
     *
     * Required columns:
     *     Process ID, burst time, start time, termination time,
     *     waiting time, turnaround time
     */
    private void printProcessTable() {
        System.out.println("\nProcess Table:");
        System.out.printf("%-8s %-8s %-8s %-14s %-10s %-12s%n",
                "PID", "Burst", "Start", "Termination", "Waiting", "Turnaround");

        ArrayList<PCB> processes = data.getAllProcessesSnapshot();

        for (PCB process : processes) {
            System.out.printf("%-8d %-8d %-8d %-14d %-10d %-12d%n",
                    process.getProcessId(),
                    process.getBurstTime(),
                    process.getStartTime(),
                    process.getTerminationTime(),
                    process.getWaitingTime(),
                    process.getTurnaroundTime());
        }
    }

    /**
     * Calculates and prints average waiting time and average turnaround time.
     *
     * Formula:
     *     average waiting = total waiting / number of processes
     *     average turnaround = total turnaround / number of processes
     */
    private void printAverages() {
        ArrayList<PCB> processes = data.getAllProcessesSnapshot();

        if (processes.isEmpty()) {
            System.out.println("\nNo processes available for average calculation.");
            return;
        }

        int totalWaiting = 0;
        int totalTurnaround = 0;

        for (PCB process : processes) {
            totalWaiting += process.getWaitingTime();
            totalTurnaround += process.getTurnaroundTime();
        }

        double averageWaiting = (double) totalWaiting / processes.size();
        double averageTurnaround = (double) totalTurnaround / processes.size();

        System.out.printf("\nAverage Waiting Time: %.2f ms%n", averageWaiting);
        System.out.printf("Average Turnaround Time: %.2f ms%n", averageTurnaround);
    }

    /**
     * Prints starved processes.
     * Used only for Priority Scheduling.
     *
     * Example output:
     *     Starved Processes:
     *     P4
     *     P7
     */
    private void printStarvedProcesses() {
        System.out.println("\nStarved Processes:");

        ArrayList<PCB> processes = data.getAllProcessesSnapshot();
        boolean found = false;

        for (PCB process : processes) {
            if (process.isStarved()) {
                System.out.println("P" + process.getProcessId());
                found = true;
            }
        }

        if (!found) {
            System.out.println("No process suffered from starvation.");
        }
    }

    /**
     * Temporary message used because algorithm bodies are TODO skeletons.
     * Remove this method after implementing the algorithms.
     */
    private void printNotImplementedMessage(String algorithmName) {
        System.out.println(algorithmName + " structure is ready, but the algorithm body is still TODO.");
        System.out.println("Open Scheduler.java and complete the TODO steps inside run" + algorithmName.replace(" ", "") + "().");
    }
}

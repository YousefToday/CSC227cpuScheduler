import java.util.ArrayList;

public class Scheduler {

    private SharedData data;
    private int currentTime;

    public Scheduler(SharedData data) {
        this.data = data;
        this.currentTime = 0;
    }

    public void runSJF() {
        System.out.println("\nRunning Shortest Job First...");

        while (!data.allProcessesCompleted()) {
            ArrayList<PCB> ready = data.getReadyQueueSnapshot();

            if (ready.isEmpty()) {
                continue;
            }

            PCB selected = findShortestJob(ready);

            if (selected == null) {
                currentTime++;
                continue;
            }

            data.removeFromReadyQueue(selected);

            selected.setState("RUNNING");
            selected.setStartTimeIfFirstRun(currentTime);

            int start = currentTime;
            int burstBefore = selected.getRemainingTime();

            currentTime += selected.getRemainingTime();
            selected.setRemainingTime(0);

            int end = currentTime;
            int burstAfter = selected.getRemainingTime();

            recordGanttEntry(selected, start, end, burstBefore, burstAfter);
            finishProcess(selected);
        }

        printAllResults("Shortest Job First", false);
        data.setSimulationFinished(true);
    }

    public void runRoundRobin() {
        System.out.println("\nRunning Round Robin...");

        while (!data.allProcessesCompleted()) {
            PCB process = data.pollReadyQueue();
            if (process == null) {
                continue;
            }

            int startTime = currentTime;
            if(process.getStartTime()==-1)
                process.setStartTimeIfFirstRun(startTime);
            int brustBefore = process.getRemainingTime();

            if (process.getRemainingTime() > SharedData.ROUND_ROBIN_QUANTUM) {
                currentTime += SharedData.ROUND_ROBIN_QUANTUM;
            } else {
                currentTime += process.getRemainingTime();
            }

            process.reduceRemainingTime(SharedData.ROUND_ROBIN_QUANTUM);

            int endTime = currentTime;
            int brustAfter = process.getRemainingTime();

            recordGanttEntry(process, startTime, endTime, brustBefore, brustAfter);

            if(brustBefore>SharedData.ROUND_ROBIN_QUANTUM)
                data.addToReadyQueue(process);
            else {
                finishProcess(process);
            }
        }
        printAllResults("Round Robin", false);
        data.setSimulationFinished(true);
    }

    public void runPriority() {
        while(!data.allProcessesCompleted()){
            if(data.isReadyQueueEmpty()) {
                Thread.yield();
                continue;
            }
            for(PCB p : data.getReadyQueueSnapshot()){
                checkStarvation(p);
                applyAgingIfNeeded(p);
            }
            PCB p = findHighestPriorityProcess(data.getReadyQueueSnapshot());
            data.removeFromReadyQueue(p);
            p.setState("RUNNING");
            p.setStartTimeIfFirstRun(currentTime);
            p.setTerminationTime(currentTime + p.getBurstTime());
            p.setRemainingTime(0);
            currentTime = p.getTerminationTime();
            finishProcess(p);
            recordGanttEntry(p , p.getStartTime() ,p.getTerminationTime() , p.getBurstTime() , 0);;

        }
        data.setSimulationFinished(true);
        printAllResults("non preemptive priority" , true);
    }

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

    private void finishProcess(PCB process) {
        process.setTerminationTime(currentTime);
        process.setState("TERMINATED");
        process.calculateFinalTimes();
        data.removeProcessFromMemory(process);
    }

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

    private void checkStarvation(PCB process) {
        int N = data.getReadyQueueSize();
        int limit = N * 5;
        int waitingInReadyQueue = currentTime - process.getReadyQueueEnterTime();

        if (waitingInReadyQueue > limit) {
            process.setStarved(true);
        }
    }

    private void applyAgingIfNeeded(PCB process) {
        int timeSinceLastAging = currentTime - process.getLastAgingTime();

        if (timeSinceLastAging >= SharedData.AGING_INTERVAL) {
            process.applyAging();
            process.setLastAgingTime(currentTime);
        }
    }

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
}

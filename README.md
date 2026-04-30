# CSC227 Multithreaded CPU Scheduling Simulator

## Purpose

This is a simple Java starter structure for the CSC227 Operating Systems course project.

The project simulates CPU scheduling in a single-CPU system. The program reads processes from `job.txt`, creates PCB objects, loads processes into memory, moves them to the ready queue, and then runs one selected scheduling algorithm.

Supported algorithms required by the project:

1. Shortest Job First
2. Round Robin with quantum = 5 ms
3. Non-preemptive Priority Scheduling with starvation detection and aging

Main memory is limited to:

```text
2048 MB
```

This starter repository focuses on the project structure. The scheduling algorithm bodies in `Scheduler.java` are intentionally left as TODO sections for the team to complete.

---

## Why This Structure Is Simple

This project uses only 7 Java files:

```text
src/
├── Main.java
├── PCB.java
├── GanttEntry.java
├── SharedData.java
├── JobReaderThread.java
├── JobLoaderThread.java
└── Scheduler.java
```

The idea is:

```text
Main.java
    starts the program

PCB.java
    represents one process

GanttEntry.java
    represents one Gantt chart line

SharedData.java
    stores queues, process list, Gantt chart, memory, and flags

JobReaderThread.java
    reads job.txt and fills the job queue

JobLoaderThread.java
    moves jobs from the job queue to the ready queue if memory is enough

Scheduler.java
    contains SJF, Round Robin, Priority, and output printing
```

No packages, no interfaces, no advanced architecture. This makes it easier for the group to understand quickly.

---

## Main Pipeline

The full program pipeline is:

```text
1. Main creates one SharedData object.

2. Main starts JobReaderThread.
   This thread reads job.txt.

3. JobReaderThread creates PCB objects.

4. JobReaderThread adds each process to:
   - jobQueue
   - allProcesses

5. Main starts JobLoaderThread.

6. JobLoaderThread checks the first process in jobQueue.

7. If the process fits in memory:
   - it is removed from jobQueue
   - memory is consumed
   - process state becomes READY
   - process is added to readyQueue

8. Main asks the user to choose an algorithm:
   1. SJF
   2. Round Robin
   3. Priority

9. Main creates Scheduler and calls the selected method.

10. Scheduler runs the algorithm.

11. Scheduler records execution in the Gantt chart.

12. When a process finishes:
   - termination time is saved
   - waiting time is calculated
   - turnaround time is calculated
   - memory is released

13. Scheduler prints:
   - Gantt chart
   - process table
   - average waiting time
   - average turnaround time
   - starved processes for Priority Scheduling
```

---

## Input File: job.txt

The program reads from `job.txt`.

Each line has this format:

```text
ProcessID:BurstTime:Priority;MemoryRequired
```

Example:

```text
1:25:4;500
2:13:3;700
3:20:3;100
```

Meaning:

| Field | Meaning |
|---|---|
| ProcessID | Unique process number |
| BurstTime | CPU burst time in ms |
| Priority | Priority from 1 to 30 |
| MemoryRequired | Memory required in MB |

Important priority rule:

```text
smaller priority number = higher priority
```

So priority `1` is better than priority `5`.

---

# Class Guide

---

## 1. Main.java

### What it does

`Main.java` is the program entry point.

It does not implement scheduling logic. It only connects the project pieces together.

Responsibilities:

```text
1. Create SharedData
2. Start JobReaderThread
3. Start JobLoaderThread
4. Ask user for scheduling algorithm
5. Call Scheduler
```

### Important code example

```java
SharedData data = new SharedData();

JobReaderThread readerThread = new JobReaderThread(data, "job.txt");
JobLoaderThread loaderThread = new JobLoaderThread(data);

readerThread.start();
loaderThread.start();

Scheduler scheduler = new Scheduler(data);
scheduler.runSJF();
```

### Methods in Main.java

#### `public static void main(String[] args)`

This is where the program starts.

It creates the shared object, starts the threads, asks the user for a choice, then runs the selected scheduler method.

Example flow:

```text
User chooses 2
Main calls scheduler.runRoundRobin()
```

#### `private static int readUserChoice()`

This method prints the menu and reads the user's selected algorithm.

Example:

```text
Choose Scheduling Algorithm:
1. Shortest Job First
2. Round Robin
3. Priority Scheduling
Enter choice: 2
```

Then the method returns:

```java
2
```

---

## 2. PCB.java

### What it does

`PCB` means Process Control Block.

This class represents one process.

Example input line:

```text
1:25:4;500
```

Becomes:

```java
PCB p1 = new PCB(1, 25, 4, 500, 0);
```

Meaning:

```text
processId = 1
burstTime = 25
priority = 4
memoryRequired = 500
arrivalOrder = 0
```

### Important fields

| Field | Meaning |
|---|---|
| `processId` | Process number, such as 1 or 2 |
| `state` | NEW, READY, RUNNING, TERMINATED |
| `burstTime` | Original CPU burst |
| `remainingTime` | Remaining CPU burst during simulation |
| `priority` | Current priority, may change because of aging |
| `originalPriority` | Priority from input file |
| `memoryRequired` | Memory needed in MB |
| `arrivalOrder` | Order in job.txt |
| `arrivalTime` | Always 0 in this project |
| `startTime` | First time the process gets CPU |
| `terminationTime` | Time when process finishes |
| `waitingTime` | Total waiting time |
| `turnaroundTime` | Total time from arrival to completion |
| `starved` | true if process suffered starvation |
| `readyQueueEnterTime` | Time process entered ready queue |
| `lastAgingTime` | Last time aging was applied |

### Important idea: burstTime vs remainingTime

`burstTime` should not change.

`remainingTime` changes while the process runs.

Example:

```text
P1 burstTime = 25
P1 remainingTime = 25

After running for 5 ms:
burstTime = 25
remainingTime = 20
```

This is especially important for Round Robin.

### Methods in PCB.java

#### Constructor

```java
public PCB(int processId, int burstTime, int priority, int memoryRequired, int arrivalOrder)
```

Creates a new process.

Example:

```java
PCB p1 = new PCB(1, 25, 4, 500, 0);
```

#### `reduceRemainingTime(int timeUsed)`

Reduces the remaining CPU burst.

Example:

```java
PCB p1 = new PCB(1, 25, 4, 500, 0);
p1.reduceRemainingTime(5);

System.out.println(p1.getRemainingTime()); // 20
```

#### `isFinished()`

Returns true if remaining time is 0.

Example:

```java
if (p1.isFinished()) {
    System.out.println("P1 finished.");
}
```

#### `applyAging()`

Improves priority by decreasing the priority number by 1.

Example:

```java
// priority = 10
p1.applyAging();
// priority becomes 9
```

Priority does not go below 1.

#### `calculateFinalTimes()`

Calculates:

```text
turnaroundTime = terminationTime - arrivalTime
waitingTime = turnaroundTime - burstTime
```

Example:

```text
terminationTime = 58
arrivalTime = 0
burstTime = 25

turnaroundTime = 58
waitingTime = 58 - 25 = 33
```

Usage:

```java
p1.setTerminationTime(58);
p1.calculateFinalTimes();
```

#### `setStartTimeIfFirstRun(int currentTime)`

Sets start time only if the process never started before.

This is useful for Round Robin.

Example:

```java
p1.setStartTimeIfFirstRun(0);  // startTime becomes 0
p1.setStartTimeIfFirstRun(15); // startTime remains 0
```

#### Getters and setters

These methods read or update private fields.

Examples:

```java
int id = p1.getProcessId();
p1.setState("READY");
int memory = p1.getMemoryRequired();
```

#### `toString()`

Returns a simple readable representation of the process.

Example:

```java
System.out.println(p1);
```

Possible output:

```text
P1 [burst=25, remaining=25, priority=4, memory=500MB, state=NEW]
```

---

## 3. GanttEntry.java

### What it does

`GanttEntry` represents one CPU execution segment.

Example Gantt chart line:

```text
[0 - 5] P1 burst: 25 -> 20
```

This means process P1 ran from time 0 to time 5.

Before running, remaining burst was 25.
After running, remaining burst became 20.

### Fields

| Field | Meaning |
|---|---|
| `processId` | Which process ran |
| `startTime` | When it started running |
| `endTime` | When it stopped running |
| `burstBefore` | Remaining burst before running |
| `burstAfter` | Remaining burst after running |

### Methods in GanttEntry.java

#### Constructor

```java
public GanttEntry(int processId, int startTime, int endTime, int burstBefore, int burstAfter)
```

Example:

```java
GanttEntry entry = new GanttEntry(1, 0, 5, 25, 20);
```

#### Getters

Examples:

```java
int pid = entry.getProcessId();
int start = entry.getStartTime();
int end = entry.getEndTime();
```

#### `toString()`

Creates a printable Gantt chart line.

Example:

```java
System.out.println(entry);
```

Output:

```text
[0 - 5] P1 burst: 25 -> 20
```

---

## 4. SharedData.java

### What it does

`SharedData` stores all shared data used by the threads and scheduler.

It contains:

```text
jobQueue
readyQueue
allProcesses
ganttChart
usedMemoryMB
readerFinished
simulationFinished
```

This is the central shared object.

### Why synchronized methods are used

Because multiple threads access the same data.

Example:

```text
JobReaderThread adds to jobQueue.
JobLoaderThread removes from jobQueue.
```

Without synchronization, the two threads could conflict.

### Important constants

```java
public static final int TOTAL_MEMORY_MB = 2048;
public static final int ROUND_ROBIN_QUANTUM = 5;
public static final int AGING_INTERVAL = 4;
```

### Memory explanation

We do not use:

```java
int[] memory = new int[2048];
```

Instead, we use:

```java
int usedMemoryMB;
```

Because the project only asks whether enough memory exists.

Example:

```text
Total memory = 2048 MB
usedMemoryMB = 1200 MB
availableMemory = 848 MB
```

If P3 needs 1000 MB:

```text
1200 + 1000 > 2048
```

So P3 cannot load yet.

### Methods in SharedData.java

#### `addToJobQueue(PCB process)`

Adds process to job queue.

Used by `JobReaderThread`.

Example:

```java
data.addToJobQueue(p1);
```

#### `peekJobQueue()`

Looks at first job without removing it.

Used by `JobLoaderThread`.

Example:

```java
PCB next = data.peekJobQueue();
```

#### `removeFromJobQueue()`

Removes first job from job queue.

Example:

```java
PCB process = data.removeFromJobQueue();
```

#### `isJobQueueEmpty()`

Returns true if no jobs are waiting in job queue.

Example:

```java
if (data.isJobQueueEmpty()) {
    System.out.println("No more jobs to load.");
}
```

#### `addToReadyQueue(PCB process)`

Adds process to ready queue.

Used after memory is allocated.

Example:

```java
process.setState("READY");
data.addToReadyQueue(process);
```

#### `removeFromReadyQueue(PCB process)`

Removes a specific process from the ready queue.

Useful for SJF and Priority.

Example:

```java
PCB shortest = findShortestJob(data.getReadyQueueSnapshot());
data.removeFromReadyQueue(shortest);
```

#### `pollReadyQueue()`

Removes and returns the first process in ready queue.

Useful for Round Robin.

Example:

```java
PCB process = data.pollReadyQueue();
```

#### `isReadyQueueEmpty()`

Returns true if ready queue is empty.

Example:

```java
if (data.isReadyQueueEmpty()) {
    currentTime++;
}
```

#### `getReadyQueueSize()`

Returns number of processes in ready queue.

Used for starvation threshold:

```text
threshold = readyQueueSize * 5
```

#### `getReadyQueueSnapshot()`

Returns a copy of the ready queue.

Example:

```java
ArrayList<PCB> ready = data.getReadyQueueSnapshot();
```

#### `addToAllProcesses(PCB process)`

Adds process to the full process list.

Used for final process table.

Example:

```java
data.addToAllProcesses(p1);
```

#### `getAllProcessesSnapshot()`

Returns a copy of all processes.

Used when printing the final table.

Example:

```java
ArrayList<PCB> processes = data.getAllProcessesSnapshot();
```

#### `addGanttEntry(GanttEntry entry)`

Adds one Gantt chart line.

Example:

```java
data.addGanttEntry(new GanttEntry(1, 0, 5, 25, 20));
```

#### `getGanttChartSnapshot()`

Returns a copy of the Gantt chart entries.

Example:

```java
ArrayList<GanttEntry> entries = data.getGanttChartSnapshot();
```

#### `hasEnoughMemoryFor(PCB process)`

Checks whether the process can fit in memory.

Example:

```java
if (data.hasEnoughMemoryFor(p1)) {
    data.loadProcessToMemory(p1);
}
```

#### `loadProcessToMemory(PCB process)`

Increases used memory.

Example:

```java
// usedMemoryMB = 0
// p1 memory = 500
data.loadProcessToMemory(p1);
// usedMemoryMB = 500
```

#### `removeProcessFromMemory(PCB process)`

Decreases used memory when process finishes.

Example:

```java
// usedMemoryMB = 1200
// p1 memory = 500
data.removeProcessFromMemory(p1);
// usedMemoryMB = 700
```

#### `getUsedMemoryMB()`

Returns used memory.

Example:

```java
System.out.println(data.getUsedMemoryMB());
```

#### `getAvailableMemoryMB()`

Returns available memory.

Formula:

```text
available = 2048 - usedMemoryMB
```

Example:

```java
System.out.println(data.getAvailableMemoryMB());
```

#### `setReaderFinished(boolean value)`

Used by `JobReaderThread` when file reading is done.

Example:

```java
data.setReaderFinished(true);
```

#### `isReaderFinished()`

Used by `JobLoaderThread` to know whether the reader is finished.

Example:

```java
if (data.isReaderFinished()) {
    // no more new jobs will be added
}
```

#### `setSimulationFinished(boolean value)`

Used to stop the loader thread when simulation ends.

Example:

```java
data.setSimulationFinished(true);
```

#### `isSimulationFinished()`

Used by `JobLoaderThread` loop.

Example:

```java
while (!data.isSimulationFinished()) {
    // keep checking jobs
}
```

#### `allProcessesCompleted()`

Returns true if all processes have state `TERMINATED`.

Used by Scheduler loops.

Example:

```java
while (!data.allProcessesCompleted()) {
    // keep scheduling
}
```

---

## 5. JobReaderThread.java

### What it does

This is Thread 1.

It reads `job.txt` and creates PCB objects.

Pipeline:

```text
job.txt line -> parse line -> create PCB -> add to jobQueue -> add to allProcesses
```

### Example

Input line:

```text
1:25:4;500
```

Parsed as:

```text
processId = 1
burstTime = 25
priority = 4
memoryRequired = 500
```

Created as:

```java
PCB process = new PCB(1, 25, 4, 500, arrivalOrder);
```

### Methods in JobReaderThread.java

#### Constructor

```java
public JobReaderThread(SharedData data, String fileName)
```

Example:

```java
JobReaderThread reader = new JobReaderThread(data, "job.txt");
reader.start();
```

#### `run()`

Runs automatically when `start()` is called.

Do this:

```java
reader.start();
```

Do not do this:

```java
reader.run();
```

The `run()` method:

```text
1. Opens job.txt
2. Reads each line
3. Parses the line
4. Creates PCB
5. Adds PCB to jobQueue
6. Adds PCB to allProcesses
7. Sets readerFinished = true
```

#### `parseLineToPCB(String line, int arrivalOrder)`

This private helper method converts one line into a PCB object.

Example:

```java
PCB p = parseLineToPCB("1:25:4;500", 0);
```

Returns a PCB representing process P1.

---

## 6. JobLoaderThread.java

### What it does

This is Thread 2.

It moves processes from job queue to ready queue only if memory is enough.

Pipeline:

```text
peek first process in jobQueue
check memory
if enough memory:
    remove from jobQueue
    load into memory
    set state READY
    add to readyQueue
```

### Example

```text
Total memory = 2048 MB
Used memory = 0 MB
P1 requires 500 MB
```

Check:

```text
0 + 500 <= 2048
```

So P1 can be loaded.

After loading:

```text
Used memory = 500 MB
Available memory = 1548 MB
```

### Methods in JobLoaderThread.java

#### Constructor

```java
public JobLoaderThread(SharedData data)
```

Example:

```java
JobLoaderThread loader = new JobLoaderThread(data);
loader.start();
```

#### `run()`

Runs automatically when `loader.start()` is called.

The method keeps checking the job queue and memory.

If a process fits:

```java
data.loadProcessToMemory(process);
process.setState("READY");
data.addToReadyQueue(process);
```

---

## 7. Scheduler.java

### What it does

`Scheduler` contains:

```text
SJF algorithm skeleton
Round Robin algorithm skeleton
Priority algorithm skeleton
Gantt chart printing
Process table printing
Average calculations
Starved process printing
```

This is the file your team will edit the most.

### Methods in Scheduler.java

#### Constructor

```java
public Scheduler(SharedData data)
```

Creates a scheduler using the shared data.

Example:

```java
Scheduler scheduler = new Scheduler(data);
```

#### `runSJF()`

Runs Shortest Job First.

Currently contains TODO comments.

Expected logic:

```text
while not all processes completed:
    if readyQueue is empty:
        currentTime++
        continue

    selected = shortest job in readyQueue
    remove selected from readyQueue
    run selected until completion
    add GanttEntry
    calculate final times
    release memory
```

Example selection:

```text
P1 burst = 25
P2 burst = 13
P3 burst = 20
```

SJF chooses:

```text
P2 -> P3 -> P1
```

#### `runRoundRobin()`

Runs Round Robin.

Currently contains TODO comments.

Expected logic:

```text
while not all processes completed:
    process = first process in readyQueue

    if no process:
        currentTime++
        continue

    runTime = min(5, process.remainingTime)
    reduce remainingTime
    add GanttEntry

    if finished:
        calculate final times
        release memory
    else:
        add process back to readyQueue
```

Example:

```text
P1 remaining = 12
quantum = 5
runTime = 5
P1 remaining becomes 7
```

#### `runPriority()`

Runs non-preemptive Priority Scheduling.

Currently contains TODO comments.

Expected logic:

```text
while not all processes completed:
    if readyQueue is empty:
        currentTime++
        continue

    check starvation for waiting processes
    apply aging if needed

    selected = process with smallest priority number
    remove selected from readyQueue
    run selected until completion
    add GanttEntry
    calculate final times
    release memory
```

Priority rule:

```text
smaller number = higher priority
```

So priority 1 beats priority 5.

#### `findShortestJob(ArrayList<PCB> readyProcesses)`

Finds the process with the smallest remaining time.

Tie-breaker: smaller arrival order.

Example:

```text
P1 remaining = 10, arrivalOrder = 0
P2 remaining = 10, arrivalOrder = 1
```

Choose P1.

#### `findHighestPriorityProcess(ArrayList<PCB> readyProcesses)`

Finds the process with the smallest priority number.

Tie-breaker: smaller arrival order.

Example:

```text
P1 priority = 4
P2 priority = 2
```

Choose P2.

#### `finishProcess(PCB process)`

Marks process as terminated.

Does:

```text
set termination time
set state TERMINATED
calculate waiting and turnaround
release memory
```

Example:

```java
finishProcess(p1);
```

#### `recordGanttEntry(PCB process, int start, int end, int burstBefore, int burstAfter)`

Adds one Gantt chart entry.

Example:

```java
recordGanttEntry(p1, 0, 5, 25, 20);
```

Creates:

```text
[0 - 5] P1 burst: 25 -> 20
```

#### `checkStarvation(PCB process)`

Used only in Priority Scheduling.

Rule:

```text
starved if waiting in readyQueue > N * 5
```

Example:

```text
readyQueue size = 4
threshold = 4 * 5 = 20 ms
```

If process waited 21 ms, mark it as starved.

#### `applyAgingIfNeeded(PCB process)`

Used only in Priority Scheduling.

Rule:

```text
Every 4 ms, decrease priority number by 1
```

Example:

```text
priority 10 -> 9
```

#### `printAllResults(String algorithmName, boolean showStarvedProcesses)`

Prints all required output.

Example:

```java
printAllResults("Round Robin", false);
printAllResults("Priority Scheduling", true);
```

#### `printGanttChart()`

Prints all Gantt chart entries.

Example output:

```text
[0 - 5] P1 burst: 25 -> 20
[5 - 10] P2 burst: 13 -> 8
```

#### `printProcessTable()`

Prints the required process table:

```text
PID   Burst   Start   Termination   Waiting   Turnaround
```

#### `printAverages()`

Prints:

```text
Average Waiting Time
Average Turnaround Time
```

Formulas:

```text
average waiting = total waiting / number of processes
average turnaround = total turnaround / number of processes
```

#### `printStarvedProcesses()`

Used only for Priority Scheduling.

Prints every process where:

```java
process.isStarved() == true
```

#### `printNotImplementedMessage(String algorithmName)`

Temporary helper method.

It prints that the algorithm body is still TODO.

Remove this after implementing the scheduling algorithms.

---

# How to Implement the Algorithms Later

## SJF Implementation Template

Inside `runSJF()`:

```java
while (!data.allProcessesCompleted()) {
    ArrayList<PCB> ready = data.getReadyQueueSnapshot();

    if (ready.isEmpty()) {
        currentTime++;
        continue;
    }

    PCB selected = findShortestJob(ready);
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
```

---

## Round Robin Implementation Template

Inside `runRoundRobin()`:

```java
while (!data.allProcessesCompleted()) {
    PCB process = data.pollReadyQueue();

    if (process == null) {
        currentTime++;
        continue;
    }

    process.setState("RUNNING");
    process.setStartTimeIfFirstRun(currentTime);

    int start = currentTime;
    int burstBefore = process.getRemainingTime();

    int runTime = Math.min(SharedData.ROUND_ROBIN_QUANTUM, process.getRemainingTime());
    process.reduceRemainingTime(runTime);
    currentTime += runTime;

    int end = currentTime;
    int burstAfter = process.getRemainingTime();

    recordGanttEntry(process, start, end, burstBefore, burstAfter);

    if (process.isFinished()) {
        finishProcess(process);
    } else {
        process.setState("READY");
        process.setReadyQueueEnterTime(currentTime);
        data.addToReadyQueue(process);
    }
}

printAllResults("Round Robin", false);
```

---

## Priority Implementation Template

Inside `runPriority()`:

```java
while (!data.allProcessesCompleted()) {
    ArrayList<PCB> ready = data.getReadyQueueSnapshot();

    if (ready.isEmpty()) {
        currentTime++;
        continue;
    }

    for (PCB process : ready) {
        checkStarvation(process);
        applyAgingIfNeeded(process);
    }

    PCB selected = findHighestPriorityProcess(ready);
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

printAllResults("Priority Scheduling", true);
```

---

# How to Run

From the project root:

```bash
javac src/*.java
java -cp src Main
```

Make sure `job.txt` is in the project root.

Example menu:

```text
Choose Scheduling Algorithm:
1. Shortest Job First
2. Round Robin
3. Priority Scheduling
Enter choice:
```

---

# Git Branch Suggestions

Recommended branches:

```text
main
dev
feature-reader-loader
feature-sjf
feature-round-robin
feature-priority-aging
feature-report
```

Simple workflow:

```text
1. Everyone branches from dev.
2. Each member works on one feature branch.
3. Merge finished features into dev.
4. Final working version is merged into main.
```

---

# Suggested Team Distribution

For 4 members:

| Member | Task |
|---|---|
| Member 1 | PCB.java and GanttEntry.java |
| Member 2 | SharedData.java memory and queue methods |
| Member 3 | JobReaderThread.java and JobLoaderThread.java |
| Member 4 | Scheduler.java algorithms and output |

For 3 members:

| Member | Task |
|---|---|
| Member 1 | PCB, GanttEntry, SharedData |
| Member 2 | Reader thread, loader thread, input parsing |
| Member 3 | Scheduler algorithms and output |

---

# What Is Still TODO

The structure is ready.

The remaining main work is inside `Scheduler.java`:

```text
runSJF()
runRoundRobin()
runPriority()
```

The helper methods are already provided to make implementation easier:

```text
findShortestJob()
findHighestPriorityProcess()
finishProcess()
recordGanttEntry()
checkStarvation()
applyAgingIfNeeded()
printAllResults()
```


/**
 * GanttEntry represents ONE execution segment in the Gantt chart.
 *
 * Example Round Robin output line:
 *     [0 - 5] P1 burst: 25 -> 20
 *
 * That means:
 *     processId    = 1
 *     startTime    = 0
 *     endTime      = 5
 *     burstBefore  = 25
 *     burstAfter   = 20
 */
public class GanttEntry {

    private int processId;
    private int startTime;
    private int endTime;
    private int burstBefore;
    private int burstAfter;

    /**
     * Constructor for one Gantt chart segment.
     *
     * Example usage:
     *     GanttEntry entry = new GanttEntry(1, 0, 5, 25, 20);
     */
    public GanttEntry(int processId, int startTime, int endTime, int burstBefore, int burstAfter) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.burstBefore = burstBefore;
        this.burstAfter = burstAfter;
    }

    public int getProcessId() {
        return processId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getBurstBefore() {
        return burstBefore;
    }

    public int getBurstAfter() {
        return burstAfter;
    }

    /**
     * Converts this Gantt entry into a readable output line.
     *
     * Example output:
     *     [0 - 5] P1 burst: 25 -> 20
     */
    @Override
    public String toString() {
        return "[" + startTime + " - " + endTime + "] "
                + "P" + processId
                + " burst: " + burstBefore + " -> " + burstAfter;
    }
}

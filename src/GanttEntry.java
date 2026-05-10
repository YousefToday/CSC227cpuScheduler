public class GanttEntry {

    // Represents one execution segment in the Gantt chart.
    private int processId;
    private int startTime;
    private int endTime;
    private int burstBefore;
    private int burstAfter;

    public GanttEntry(int processId, int startTime, int endTime, int burstBefore, int burstAfter) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.burstBefore = burstBefore;
        this.burstAfter = burstAfter;
    }

    @Override
    public String toString() {
        return "[" + startTime + " - " + endTime + "] "
                + "P" + processId
                + " burst: " + burstBefore + " -> " + burstAfter;
    }
}
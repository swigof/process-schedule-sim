import java.util.Arrays;

public class Process {
    private String id;                  // the process ID
    private int arrivalTime;            // the arrival time
    private int execTime;               // the number of cycle needed for complete execution
    private int[] ioRequestTimes;       // the times of any IO requests
    private String state;               // the current state either null (not arrived), ready, running, IO or terminated
    private int execStartTime;          // when the process first executed
    private int execStopTime;           // when the process terminates
    private int readyCycleCount;        // the total number of cycles spent in ready state
    private int runningCycleCount;      // the total number of cycles spent in running state
    private int currentIOCycle;         // the number of cycles spent in an IO request before returning to ready state
    private int currentRunningCycle;    // the number of cycles spent in running state before interruption
    private int currentReadyCycle;      // the number of cycles spent in ready state before running

    public Process(String id, int arrivalTime, int execTime, int[] ioRequestTimes) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.execTime = execTime;
        this.ioRequestTimes = ioRequestTimes;
        this.state = "null";
    }

    /**
     * @return whether the process is finished an IO request
     */
    public boolean doneIORequest(){
        return currentIOCycle == 2;
    }

    /**
     * @return whether the process currently has terminated
     */
    public boolean hasTerminated(){
        return runningCycleCount == execTime;
    }

    /**
     * @return whether the process currently has an IO request
     */
    public boolean hasIORequest(){
        for(int ioTime : ioRequestTimes){
            if(ioTime == runningCycleCount)
                return true;
        }
        return false;
    }

    /**
     * @param cycle the current cycle
     * @return whether the process arrives on the given cycle
     */
    public boolean hasArrived(int cycle){
        return arrivalTime == cycle;
    }

    /**
     * @return whether the process has entered running state before
     */
    public boolean hasRun(){
        return runningCycleCount != 0;
    }

    /**
     * @return the number of cycles before termination
     */
    public int remainingTime(){
        return execTime-runningCycleCount;
    }

    /**
     * Increments relevant cycle counts based on current state
     */
    public void incrementStateCount(){
        if(state.equals("ready")) {
            readyCycleCount++;
            currentReadyCycle++;
        }
        else if(state.equals("io"))
            currentIOCycle++;
        else if(state.equals("running")) {
            runningCycleCount++;
            currentRunningCycle++;
        }
    }

    /**
     * Resets a process for use in a new simulation
     */
    public void resetProcess(){
        this.state = "null";
        this.execStartTime = 0;
        this.execStopTime = 0;
        this.readyCycleCount = 0;
        this.runningCycleCount = 0;
        this.currentIOCycle = 0;
        this.currentRunningCycle = 0;
        this.currentReadyCycle = 0;
    }

    @Override
    public String toString() {
        return  "Process{" +
                "id='" + id + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", execTime=" + execTime +
                ", ioRequestTimes=" + Arrays.toString(ioRequestTimes) +
                ", state='" + state + '\'' +
                ", execStartTime=" + execStartTime +
                ", execStopTime=" + execStopTime +
                ", readyCycleCount=" + readyCycleCount +
                ", runningCycleCount=" + runningCycleCount +
                '}';
    }

    public String getId(){
        return id;
    }

    public int getArrivalTime(){
        return arrivalTime;
    }

    public int getExecTime(){
        return execTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getExecStartTime() {
        return execStartTime;
    }

    public void setExecStartTime(int execStartTime) {
        this.execStartTime = execStartTime;
    }

    public int getExecStopTime() {
        return execStopTime;
    }

    public void setExecStopTime(int execStopTime) {
        this.execStopTime = execStopTime;
    }

    public int getReadyCycleCount() {
        return readyCycleCount;
    }

    public void resetCurrentIOCycle(){
        this.currentIOCycle = 0;
    }

    public int getCurrentRunningCycle(){
        return currentRunningCycle;
    }

    public void resetCurrentRunningCycle(){
        currentRunningCycle = 0;
    }

    public int getCurrentReadyCycle(){
        return currentReadyCycle;
    }

    public void resetCurrentReadyCycle(){
        currentReadyCycle = 0;
    }
}

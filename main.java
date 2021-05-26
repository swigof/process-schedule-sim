import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Simulates four different scheduling algorithms
 * Process objects hold most of the relevant information for the scheduling
 * CPUs are a simple string array that saves the ID of the process running on each
 */
public class main {
    public static void main(String args[]){
        if(args.length == 0) {
            System.out.println("ERROR - no input provided");
            System.exit(0);
        }

        ArrayList<String> input = getInput(args[0]);
        Process[] processes = getProcesses(input);

        int cpuCount = 0;
        int q = 0;
        Scanner scanIn = new Scanner(System.in);
        while (cpuCount <= 0) {
            System.out.print("Enter non-zero positive integer for cpu count: ");
            cpuCount = scanIn.nextInt();
        }
        while (q <= 0) {
            System.out.print("Enter non-zero positive integer for round robin quantum: ");
            q = scanIn.nextInt();
        }
        scanIn.close();

        FCFS(processes.clone(), cpuCount);
        SJB(processes.clone(), cpuCount);
        SRTF(processes.clone(), cpuCount);
        RR(processes.clone(), cpuCount, q);
    }

    /**
     * Retrieve process file contents as a list of lines
     * @return a list of string for each line in the file
     */
    static ArrayList<String> getInput(String filepath){
        ArrayList<String> input = null;
        try
        {
            Scanner file = new Scanner(new FileInputStream(filepath));
            input = new ArrayList<String>();
            while(file.hasNext()){
                input.add(file.nextLine());
            }
            file.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("ERROR - file not found");
            System.exit(0);
        }
        return input;
    }

    /**
     * Extract processes from string list of file
     * @param input the string list of process file contents
     * @return an array of processes
     */
    static Process[] getProcesses(ArrayList<String> input){
        ArrayList<Process> processes = new ArrayList<Process>();
        for (String s : input) {
            String[] properties = s.split("\t", 4);
            Process p;
            if (properties.length < 4) {
                int[] noIo = new int[0];
                p = new Process(properties[0], Integer.parseInt(properties[1]), Integer.parseInt(properties[2]), noIo);
            } else {
                int[] ioTimes = Arrays.stream(properties[3].split("\t")).mapToInt(Integer::parseInt).toArray();
                p = new Process(properties[0], Integer.parseInt(properties[1]), Integer.parseInt(properties[2]), ioTimes);
            }
            processes.add(p);
        }
        return processes.toArray(new Process[0]);
    }

    /**
     * First come first serve scheduling simulation, accounts only for initial arrival time
     * @param processes the processes to schedule
     * @param cpuCount the number of cpus to use
     */
    static void FCFS(Process[] processes, int cpuCount){
        System.out.println("---FCFS---");
        printHeader(cpuCount);

        String[] cpus = new String[cpuCount];
        Arrays.fill(cpus, "-");
        boolean done = false;
        int cycle = 0;
        int cpuUsage = 0;

        //sort processes array such that ready processes are always in FCFS order
        for(int i = 0; i < processes.length; i++){
            for(int j = i+1; j < processes.length; j++){
                if(processes[i].getArrivalTime() > processes[j].getArrivalTime()){
                    Process temp = processes[i];
                    processes[i] = processes[j];
                    processes[j] = temp;
                }
            }
        }

        while(!done) {
            handleStateChanges(cpus, processes, cycle);

            loadCpus(cpus, processes, cycle);

            printCycle(cpus, processes, cycle);

            //increment cpu usage based on number of cpus with process loaded
            for(String cpu : cpus){
                if(!cpu.equals("-")){
                    cpuUsage++;
                }
            }

            //increment state counts and check if all processes are done
            done = true;
            for(Process process : processes) {
                process.incrementStateCount();
                if(!process.getState().equals("terminated"))
                    done = false;
            }

            if(!done)
                cycle++;
        }

        printStats(processes, cycle, cpuCount, cpuUsage);
        for(Process p : processes){
            p.resetProcess();
        }
    }

    /**
     * Shortest job first scheduling simulation, accounts only for shortest total execution time, not remaining
     * @param processes the processes to schedule
     * @param cpuCount the number of cpus to use
     */
    static void SJB(Process[] processes, int cpuCount){
        System.out.println("---SJB---");
        printHeader(cpuCount);

        String[] cpus = new String[cpuCount];
        Arrays.fill(cpus, "-");
        boolean done = false;
        int cycle = 0;
        int cpuUsage = 0;

        //sort processes array such that ready processes are always in SJB order
        for(int i = 0; i < processes.length; i++){
            for(int j = i+1; j < processes.length; j++){
                if(processes[i].getExecTime() > processes[j].getExecTime()){
                    Process temp = processes[i];
                    processes[i] = processes[j];
                    processes[j] = temp;
                }
            }
        }

        while(!done) {
            handleStateChanges(cpus, processes, cycle);

            loadCpus(cpus, processes, cycle);

            printCycle(cpus, processes, cycle);

            //increment cpu usage based on number of cpus with process loaded
            for(String cpu : cpus){
                if(!cpu.equals("-")){
                    cpuUsage++;
                }
            }

            //increment state counts and check if all processes are done
            done = true;
            for(Process process : processes) {
                process.incrementStateCount();
                if(!process.getState().equals("terminated"))
                    done = false;
            }

            if(!done)
                cycle++;
        }

        printStats(processes, cycle, cpuCount, cpuUsage);
        for(Process p : processes){
            p.resetProcess();
        }
    }

    /**
     * Shortest remaining time first scheduling simulation
     * @param processes the processes to schedule
     * @param cpuCount the number of cpus to use
     */
    static void SRTF(Process[] processes, int cpuCount){
        System.out.println("---SRTF---");
        printHeader(cpuCount);

        String[] cpus = new String[cpuCount];
        Arrays.fill(cpus, "-");
        boolean done = false;
        int cycle = 0;
        int cpuUsage = 0;

        while(!done) {
            //sort processes array every cycle such that ready processes are always in SRTF order
            for(int i = 0; i < processes.length; i++){
                for(int j = i+1; j < processes.length; j++){
                    if(processes[i].remainingTime() > processes[j].remainingTime()){
                        Process temp = processes[i];
                        processes[i] = processes[j];
                        processes[j] = temp;
                    }
                }
            }

            handleStateChanges(cpus, processes, cycle);

            loadCpus(cpus, processes, cycle);

            //swap processes that are running for others that have lower remaining time
            for (Process process : processes) {
                if (process.getState().equals("ready")) {
                    boolean loaded = false;
                    for (int j = processes.length - 1; j >= 0; j--) {
                        if (!loaded && processes[j].getState().equals("running")
                        && process.remainingTime() < processes[j].remainingTime()) {
                            loaded = true;
                            int cpuIndex = removeFromCpu(cpus, processes[j]);
                            processes[j].setState("ready");
                            processes[j].resetCurrentRunningCycle();
                            cpus[cpuIndex] = process.getId();
                            process.setState("running");
                            process.resetCurrentReadyCycle();
                        }
                    }
                }
            }

            printCycle(cpus, processes, cycle);

            //increment cpu usage based on number of cpus with process loaded
            for(String cpu : cpus){
                if(!cpu.equals("-")){
                    cpuUsage++;
                }
            }

            //increment state counts and check if all processes are done
            done = true;
            for(Process process : processes) {
                process.incrementStateCount();
                if(!process.getState().equals("terminated"))
                    done = false;
            }

            if(!done)
                cycle++;
        }

        printStats(processes, cycle, cpuCount, cpuUsage);
        for(Process p : processes){
            p.resetProcess();
        }
    }

    /**
     * Round robin scheduling simulation
     * @param processes the processes to schedule
     * @param cpuCount the number of cpus to use
     * @param q the quantum to use
     */
    static void RR(Process[] processes, int cpuCount, int q){
        System.out.println("---RR---");
        printHeader(cpuCount);

        String[] cpus = new String[cpuCount];
        Arrays.fill(cpus, "-");
        boolean done = false;
        int cycle = 0;
        int cpuUsage = 0;

        while(!done) {
            handleStateChanges(cpus, processes, cycle);

            //additional state change due to quantum being reached
            for(Process process : processes){
                if(process.getCurrentRunningCycle() == q){
                    removeFromCpu(cpus, process);
                    process.setState("ready");
                    process.resetCurrentRunningCycle();
                }
            }

            //sort processes array every cycle such that ready processes are in order of most aged first
            for(int i = 0; i < processes.length; i++){
                for(int j = i+1; j < processes.length; j++){
                    if(processes[i].getCurrentReadyCycle() < processes[j].getCurrentReadyCycle()){
                        Process temp = processes[i];
                        processes[i] = processes[j];
                        processes[j] = temp;
                    }
                }
            }

            loadCpus(cpus, processes, cycle);

            printCycle(cpus, processes, cycle);

            //increment cpu usage based on number of cpus with process loaded
            for(String cpu : cpus){
                if(!cpu.equals("-")){
                    cpuUsage++;
                }
            }

            //increment state counts and check if all processes are done
            done = true;
            for(Process process : processes) {
                process.incrementStateCount();
                if(!process.getState().equals("terminated"))
                    done = false;
            }

            if(!done)
                cycle++;
        }

        printStats(processes, cycle, cpuCount, cpuUsage);
        for(Process p : processes){
            p.resetProcess();
        }
    }

    /**
     * Makes any necessary non cpu-loading state changes common to all schedulers
     * @param cpus the cpus to use
     * @param processes the processes to use
     * @param cycle the current cycle
     */
    private static void handleStateChanges(String[] cpus, Process[] processes, int cycle){
        for (Process process : processes) {
            if (process.getState().equals("null") && process.hasArrived(cycle)) {
                process.setState("ready");
            }
            else if (process.getState().equals("io") && process.doneIORequest()) {
                process.setState("ready");
                process.resetCurrentIOCycle();
            }
            else if (process.getState().equals("running")) {
                if (process.hasTerminated()) {
                    process.setState("terminated");
                    process.resetCurrentRunningCycle();
                    removeFromCpu(cpus, process);
                    process.setExecStopTime(cycle);
                }
                else if (process.hasIORequest()) {
                    process.setState("io");
                    removeFromCpu(cpus, process);
                    process.resetCurrentRunningCycle();
                }
            }
        }
    }

    /**
     * Makes any necessary cpu-loading state changes common to all schedulers
     * @param cpus the cpus to use
     * @param processes the processes to use
     * @param cycle the current cycle
     */
    private static void loadCpus(String[] cpus, Process[] processes, int cycle){
        for(int i = 0; i < cpus.length; i++){
            if(cpus[i].equals("-")){
                boolean loaded = false;
                for(Process process : processes){
                    if(!loaded && process.getState().equals("ready")){
                        loaded = true;
                        cpus[i] = process.getId();
                        process.setState("running");
                        process.resetCurrentReadyCycle();
                        if(!process.hasRun())
                            process.setExecStartTime(cycle);
                    }
                }
            }
        }
    }

    /**
     * Unloads a process from its cpu
     * @param cpus the cpus
     * @param p the process to remove from one of the cpus
     * @return the index of the remove process, -1 if not found
     */
    private static int removeFromCpu(String[] cpus, Process p) {
        for(int i = 0; i < cpus.length; i++){
            if(cpus[i].equals(p.getId())){
                cpus[i] = "-";
                return i;
            }
        }
        return -1;
    }

    /**
     * Prints the table header for cpu cycle prints
     * @param cpuCount the number of cpus
     */
    private static void printHeader(int cpuCount){
        System.out.printf("%-7s|","CYCLE");
        for(int i = 0; i<cpuCount; i++){
            System.out.printf("%-7s|", "CPU "+i);
        }
        System.out.printf("%-10s|", "READY");
        System.out.printf("%-10s|\n", "IO");
    }

    /**
     * Prints information about the current cycle
     * @param cpus the cpus
     * @param processes the processes
     * @param cycle the current cycle
     */
    private static void printCycle(String[] cpus, Process[] processes, int cycle){
        System.out.printf("%-7s|", cycle);
        for (String s : cpus) {
            System.out.printf("%-7s|", s);
        }
        String readyStr = "";
        String ioStr = "";
        for(Process process : processes){
            if(process.getState().equals("ready")){
                readyStr += process.getId()+" ";
            }
            else if(process.getState().equals("io")){
                ioStr += process.getId()+" ";
            }
        }
        if(readyStr.equals(""))
            readyStr = "-";
        else if(readyStr.length() > 10)
            readyStr = readyStr.substring(0, 7)+"...";
        if(ioStr.equals(""))
            ioStr = "-";
        else if(ioStr.length() > 10)
            ioStr = ioStr.substring(0, 7)+"...";
        System.out.printf("%-10s|", readyStr);
        System.out.printf("%-10s|\n", ioStr);
    }

    /**
     * Prints full details and statistics about the simulation
     * @param processes the processes
     * @param cycle the cycle count
     * @param cpuCount the number of cpus
     * @param cpuUsage the number of cycles the cpus were loaded
     */
    private static void printStats(Process[] processes, int cycle, int cpuCount, int cpuUsage){
        int totalWaitTime = 0;
        int totalTurnaroundTime = 0;
        int totalResponseTime = 0;
        System.out.println();
        for(Process p : processes){
            int turnaroundTime = p.getExecStopTime()-p.getExecStartTime();
            int responseTime = p.getExecStartTime()-p.getArrivalTime();

            totalWaitTime += p.getReadyCycleCount();
            totalTurnaroundTime += turnaroundTime;
            totalResponseTime += responseTime;

            System.out.println(p+"\n\tTurnaroundTime: "+turnaroundTime+"\tResponseTime: "+responseTime);
        }
        double utilization = (double)cpuUsage / (cycle * cpuCount);
        double avgWait = (double)totalWaitTime/processes.length;
        double avgTurnaround = (double)totalTurnaroundTime/processes.length;
        double avgResponse = (double)totalResponseTime/processes.length;
        double throughput = (double)processes.length/cycle;
        System.out.printf("""

                        CPUUtilization: %.2f\tAverageWaitTime: %.2f\tAverageTurnaroundTime: %.2f\tAverageResponseTime: %.2f\tThroughput: %.2f


                        """,
                        utilization, avgWait, avgTurnaround, avgResponse, throughput);
    }
}

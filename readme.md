COMP 346 - Operating Systems

Simple processor scheduling algorithm simulator

Simulates first come first serve, shortest job first, shortest remaining time first, and round robin scheduling.

# Execution
```java -jar process-schedule-sim.jar <input filepath>```

# Input
Each line of the input file represent a process with tab separated values as below, where all times are in cpu cycles.

```ID arrival_time execution_time ...```

Any values past execution_time are IO request times and are optional values for when IO requests are made relative to the processes executed cycles. These IO requests are assumed to be serviced in 2 cpu cycles.

# Output
For each scheduling algorithm, a table with process statuses at each cpu cycle and process statistics are displayed.
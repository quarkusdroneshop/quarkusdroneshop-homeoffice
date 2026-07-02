package io.quarkusdroneshop.homeoffice.viewmodels;

public class ServiceMetricsResult {

    public String name;
    public double cpuUsage;
    public long heapUsedBytes;
    public long heapMaxBytes;
    public long nonHeapUsedBytes;
    public int liveThreads;
    public double uptimeSeconds;
    public String error;

    public ServiceMetricsResult() {}

    public ServiceMetricsResult(String name, double cpuUsage, long heapUsedBytes, long heapMaxBytes,
                                 long nonHeapUsedBytes, int liveThreads, double uptimeSeconds) {
        this.name = name;
        this.cpuUsage = cpuUsage;
        this.heapUsedBytes = heapUsedBytes;
        this.heapMaxBytes = heapMaxBytes;
        this.nonHeapUsedBytes = nonHeapUsedBytes;
        this.liveThreads = liveThreads;
        this.uptimeSeconds = uptimeSeconds;
    }

    public ServiceMetricsResult(String name, String error) {
        this.name = name;
        this.error = error;
    }
}

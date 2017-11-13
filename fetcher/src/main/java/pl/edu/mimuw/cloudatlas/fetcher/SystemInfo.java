package pl.edu.mimuw.cloudatlas.fetcher;

import com.sun.management.OperatingSystemMXBean;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.io.File;
import java.lang.management.ManagementFactory;


//TODO missing attributes
public class SystemInfo {

    private static final String CPU_LOAD = "cpu_load";
    private static final String FREE_DISK = "free_disk";
    private static final String TOTAL_DISK = "total_disk";
    private static final String FREE_RAM = "free_ram";
    private static final String TOTAL_RAM = "total_ram";
    private static final String FREE_SWAP = "free_swap";
    private static final String TOTAL_SWAP  = "total_swap";
    private static final String NUM_PROCESSES = "num_processes";
    private static final String CORES = "cores";
    private static final String KERNEL_VER = "kernel_ver";
    private static final String LOGGED_USERS = "logged_users";
    private static final String DNS_NAMES = "dns_names";


    private AttributesMap attributes;
    private OperatingSystemMXBean osMXBean;

    public SystemInfo() {
        attributes = new AttributesMap();
        osMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    }

    public void updateAttributes() {
        attributes.addOrChange(CPU_LOAD, new ValueDouble(osMXBean.getSystemLoadAverage()));
        attributes.addOrChange(FREE_DISK, new ValueInt(getFreeDisk()));
        attributes.addOrChange(TOTAL_DISK, new ValueInt(getTotalDisk()));
        attributes.addOrChange(FREE_RAM, new ValueInt(osMXBean.getFreePhysicalMemorySize()));
        attributes.addOrChange(TOTAL_RAM, new ValueInt(osMXBean.getTotalPhysicalMemorySize()));
        attributes.addOrChange(FREE_SWAP, new ValueInt(osMXBean.getFreeSwapSpaceSize()));
        attributes.addOrChange(TOTAL_SWAP, new ValueInt(osMXBean.getTotalSwapSpaceSize()));
//        attributes.addOrChange(NUM_PROCESSES, new ValueInt());
        attributes.addOrChange(CORES, new ValueInt((long) osMXBean.getAvailableProcessors()));
        attributes.addOrChange(KERNEL_VER, new ValueString(osMXBean.getVersion()));
//        attributes.addOrChange(LOGGED_USERS, new ValueInt());
//        attributes.addOrChange(DNS_NAMES, new ValueInt());
    }

    private Long getTotalDisk() {
        File[] roots = File.listRoots();
        Long result = 0L;
        for(File root : roots) {
            result += root.getTotalSpace();
        }
        return result;
    }

    private Long getFreeDisk() {
        File[] roots = File.listRoots();
        Long result = 0L;
        for(File root : roots) {
            result += root.getFreeSpace();
        }
        return result;
    }

    AttributesMap getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "attributes=" + attributes +
                '}';
    }
}

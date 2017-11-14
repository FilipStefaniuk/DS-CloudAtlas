package pl.edu.mimuw.cloudatlas.fetcher;

import com.sun.management.OperatingSystemMXBean;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;


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
        attributes.addOrChange(NUM_PROCESSES, new ValueInt((long)execCommand("ps aux").size()));
        attributes.addOrChange(CORES, new ValueInt((long) osMXBean.getAvailableProcessors()));
        attributes.addOrChange(KERNEL_VER, new ValueString(osMXBean.getVersion()));
        attributes.addOrChange(LOGGED_USERS, new ValueInt((long)execCommand("users").size()));
        attributes.addOrChange(DNS_NAMES, new ValueInt((long) execCommand("hostname").size()));
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

    List<String> execCommand(String command) {
        List<String> result = new ArrayList<>();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader((p.getInputStream())));
            String line;
            while((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (Exception e) {}
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

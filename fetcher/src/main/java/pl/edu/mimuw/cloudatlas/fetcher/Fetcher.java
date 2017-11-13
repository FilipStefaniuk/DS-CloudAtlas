package pl.edu.mimuw.cloudatlas.fetcher;

import com.sun.management.OperatingSystemMXBean;
import pl.edu.mimuw.cloudatlas.agent.AgentInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Fetcher {
    public static void main(String[] args) throws Exception {

        Registry registry = LocateRegistry.getRegistry("localhost");
        AgentInterface stub = (AgentInterface) registry.lookup("Agent");
        stub.setValue(20);

        //        OperatingSystemMXBean osMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        Long free_disk = 0L, total_disk = 0L;
//
//        for (File disk: File.listRoots()) {
//            free_disk += disk.getFreeSpace();
//            total_disk += disk.getTotalSpace();
//        }
//
//        System.out.println("cpu_load: " + osMXBean.getSystemCpuLoad());
//        System.out.println("free_disk: " + free_disk);
//        System.out.println("total_disk: " + total_disk);
//        System.out.println("free_ram: " + osMXBean.getFreePhysicalMemorySize());
//        System.out.println("total_ram: " + osMXBean.getTotalPhysicalMemorySize());
//        System.out.println("free_swap: " + osMXBean.getFreeSwapSpaceSize());
//        System.out.println("total_swap: " + osMXBean.getTotalSwapSpaceSize());
//        System.out.println("num_processes: ");
//        System.out.println("num_cores: " + osMXBean.getAvailableProcessors());
//        System.out.println("kernel_ver: " + osMXBean.getVersion());
//        System.out.println("logged_users: ");
//        System.out.println("dns_names: ");



    }

//    private static Long getLoggedUsers() throws Exception{
//        ProcessBuilder builder = new ProcessBuilder();
//        builder.command("users");
//        Process process = builder.start();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        process.waitFor();
//
//        Long users = 0L;
//        while (reader.ready()) {
//            ++users;
//        }
//        return users;
//    }
}

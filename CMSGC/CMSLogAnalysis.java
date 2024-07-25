package com.code.tryOne.jvmGc.CMSGC;


import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CMSLogAnalysis {
    public static void analysis(String filePath){
        File file = new File(filePath);
        int parNewCount = 0;
        int cmsCount = 0;
        int stopCount =0;
        double totalPauseTime = 0;
        double maxPauseTime = 0;
        double allRunTime = 0;
        Pattern parNewPattern = Pattern.compile("\\[GC \\(Allocation Failure\\).*?ParNew:.*?\\]");
        Pattern cmsInitialMarkPattern = Pattern.compile("GC \\(CMS Initial Mark\\)");
        Pattern stopTimePattern = Pattern.compile("Total time for which application threads were stopped: (\\d+\\.\\d+) seconds");
        Pattern timePattern = Pattern.compile(": (\\d+\\.\\d+):");

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                Matcher parNewMatcher = parNewPattern.matcher(line);
                if (parNewMatcher.find()) {
                    parNewCount++;
                }
                Matcher cmsMatcher = cmsInitialMarkPattern.matcher(line);
                if (cmsMatcher.find()) {
                    cmsCount++;
                }
                Matcher stopMatcher = stopTimePattern.matcher(line);
                if (stopMatcher.find()){
                    stopCount++;
                    double pauseTime = Double.parseDouble(stopMatcher.group(1));
                    totalPauseTime += pauseTime;
                    maxPauseTime = Math.max(maxPauseTime, pauseTime);
                }
                Matcher endTimeMatcher = timePattern.matcher(line);
                if (endTimeMatcher.find()){
                    double endTime = Double.parseDouble(endTimeMatcher.group(1));
                    allRunTime = Math.max(allRunTime, endTime);
                }

            }
        } catch (FileNotFoundException e) {
            System.out.println("无法找到文件: " + filePath);
        }
        double avgPauseTime = totalPauseTime / stopCount;
        double throughput = 100 * (1 - totalPauseTime / allRunTime);

        String fileName = new File(filePath).getName();
        System.out.printf("文件名称：%20s | ParNew GC事件数量: %5d | CMS GC事件数量: %5d | 程序暂停次数：%5d |平均暂停时间: %.3fms | 最大暂停时间: %.3fms  | 吞吐量: %.2f%% | 总暂停时间: %.3fms  | 程序运行总时间：%.3fs%n",
                fileName, parNewCount, cmsCount,stopCount ,avgPauseTime * 1000, maxPauseTime * 1000, throughput, totalPauseTime * 1000, allRunTime );
    }

    public static void main(String[] args) {
        String directoryPath = "./"; // 指定目录路径
        File directory = new File(directoryPath);
        File[] logFiles = directory.listFiles((dir, name) -> name.endsWith(".log"));

        for (File logFile : logFiles) {
            analysis(logFile.getAbsolutePath());
        }
    }
}
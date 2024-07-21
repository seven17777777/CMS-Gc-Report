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
        double totalPauseTime = 0;
        double maxPauseTime = 0;
        Pattern parNewPattern = Pattern.compile("\\[GC \\(Allocation Failure\\).*?ParNew:.*?\\]");
        Pattern cmsPattern = Pattern.compile("\\[CMS-concurrent-mark-start\\].*?\\[CMS-concurrent-reset: (\\d+\\.\\d+)/(\\d+\\.\\d+) secs\\]");
        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                Matcher parNewMatcher = parNewPattern.matcher(line);
                if (parNewMatcher.find()) {
                    parNewCount++;
                }
                Matcher cmsMatcher = cmsPattern.matcher(line);
                if (cmsMatcher.find()) {
                    cmsCount++;
                    double pauseTime = Double.parseDouble(cmsMatcher.group(2));
                    totalPauseTime += pauseTime;
                    maxPauseTime = Math.max(maxPauseTime, pauseTime);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("无法找到文件: " + filePath);
        }
        double avgPauseTime = totalPauseTime / cmsCount;
        double throughput = 100 * (1 - totalPauseTime / (totalPauseTime + parNewCount));

        String fileName = new File(filePath).getName();
        System.out.printf("文件名称：%20s | ParNew GC事件数量: %5d | CMS GC事件数量: %5d | 平均暂停时间: %.6fms | 最大暂停时间: %.3fms  | 吞吐量: %.2f%% | 总暂停时间: %.3fms%n",
                fileName, parNewCount, cmsCount, avgPauseTime, maxPauseTime, throughput, totalPauseTime);
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

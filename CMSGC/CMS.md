# 1、java程序

```java
public class CMSGCExample {
    public static void main(String[] args) {
        List<byte[]> memoryHog = new ArrayList<>();
        int k = 1;
        while (true) {
            for (int i = 0; i < 1000; i++) {
                // 分配1MB的数组
                byte[] array = new byte[1024 * 1024];
                memoryHog.add(array);
            }
            System.out.println("第" + k++ + "分配1M内存" + "List 大小：" + memoryHog.size());
            // 模拟内存使用情况
            if (memoryHog.size() > 1000) {
                memoryHog.subList(0, 500).clear();
            }

            try {
                // 暂停一段时间，模拟应用程序的其他工作
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

# 2、java命令

```
  -XX:+UseConcMarkSweepGC   
  -XX:+PrintGCDetails   
  -XX:+PrintGCDateStamps   
  -Xloggc:./gc.log  
```

命令解释：

```angular2html
-XX:+UseConcMarkSweepGC：启用并发标记清除（CMS）垃圾收集器。
-XX:+PrintGCDetails：在垃圾收集时打印详细的GC日志信息。包括每次GC的类型、时间、回收的内存量等详细信息。
-XX:+PrintGCDateStamps：在GC日志中添加真实的时间戳
-Xloggc:./gc.log：指定GC日志的输出文件路径。
```

# 3、运行结果

![img](image/clip_image001.png)

# 4、GC日志部分分析
-[log日志](cmsLog/1.log)

## 1、现在机器上的配置

![img](image/clip_image002.jpg)

```
Memory: 4k page, physical 16672068k(6305752k free), swap 19686724k(5627108k free)
```

指的是系统页面大小为4KB。"physical 16672068k"表示物理内存总量约为16GB，其中有"6305752k free"约为6GB的空闲内存。"swap
19686724k"表示交换空间总量约为19GB，其中有"5627108k free"约为5GB的空闲交换空间。

## 2、该程序运行的参数

```angular2html
-XX:InitialHeapSize=266753088 // 设置JVM启动时堆的初始大小为约266MB。
-XX:MaxHeapSize=4268049408 // 设置JVM堆的最大大小为约4GB。
-XX:MaxNewSize=697933824 // 设置年轻代的最大大小为约697MB。
-XX:MaxTenuringThreshold=6 // 设置对象在年轻代中的最大存活次数为6次，之后会被移动到老年代。
-XX:OldPLABSize=16 // 设置老年代中PLAB(私有分配缓冲区)的大小。
-XX:+PrintGC //启用打印GC信息。
-XX:+PrintGCDateStamps // 启用打印GC的日期戳。
-XX:+PrintGCDetails // 启用打印GC的详细信息。
-XX:+PrintGCTimeStamps // 启用打印GC的时间戳。 -XX:
+UseCompressedClassPointers // 启用压缩类指针，减少内存使用，默认打开
-XX:+UseCompressedOops // 启用压缩普通对象指针，减少内存使用，默认打开。
-XX:+UseConcMarkSweepGC // 使用并发标记清除(CMS)垃圾收集器。
-XX:-UseLargePagesIndividualAllocation // 禁用大页的单独分配。
-XX:+UseParNewGC // 使用ParNew垃圾收集器，它是一个并行的年轻代收集器。
```

CMS（并发标记清除）垃圾收集器主要用于老年代，而ParNew垃圾收集器则是专门为年轻代设计的。当启用CMS垃圾收集器时，它通常会与ParNew垃圾收集器一起工作，因为ParNew是CMS的默认年轻代收集器。

## 3、垃圾回收过程

| 相对时间  | 日志及其解释                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0.224 | 2024-07-06T17:15:56.851+0800:  0.224: [GC (Allocation Failure)]  垃圾收集器在年轻代(Young Generation)中进行了一次垃圾回收，因为没有足够的空间分配新的对象。其中0.224表示jvm启动后的绝对时间。  [ParNew:  69734K->8222K(78656K), 0.0449721 secs] - 使用ParNew收集器，它将使用的内存从69734K减少到8222K，总共有78656K的空间。  69734K->66593K(253440K),  0.0452289 secs] - 整个堆的使用从69734K减少到66593K，堆的总大小为253440K。  [Times: user=0.19  sys=0.05, real=0.04 secs]  user: 垃圾收集过程中，所有GC线程在用户模式下执行的CPU时间总和。  sys: 垃圾收集过程中，所有GC线程在内核模式下执行的CPU时间总和。  real: 垃圾收集事件的总经过时间，也就是从开始到结束的墙钟时间（wall clock time）。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 0.278 | 2024-07-06T17:15:56.905+0800: 0.278: [GC  (Allocation Failure) 2024-07-06T17:15:56.905+0800: 0.278: [ParNew:  77178K->8277K(78656K), 0.0356354 secs] 135549K->135088K(253440K),  0.0357393 secs] [Times: user=0.19 sys=0.05, real=0.04 secs]  第二次进行垃圾回收。过程和上面一样                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| 0.314 | 篇幅原因，省略时间戳  [GC (CMS Initial Mark)  [1 CMS-initial-mark: 126811K(174784K)] 136112K(253440K), 0.0006083 secs]  [Times: user=0.00 sys=0.00, real=0.00 secs]  [GC (CMS Initial Mark)] - 这表示进行的是CMS垃圾收集器的初始标记阶段。这是CMS收集器开始工作的第一步，用于标记老年代中可达的对象。  [1 CMS-initial-mark: 126811K(174784K)] - 在老年代中，初始标记阶段标记了126811K的存活对象，老年代的总大小为174784K。  136112K(253440K) - 在整个堆中，初始标记阶段结束时，堆的使用量为136112K，而堆的总大小为253440K。  0.0006083 secs - 初始标记阶段的持续时间，非常短，只有大约0.6毫秒。  [Times: user=0.00 sys=0.00, real=0.00  secs] - 这表示用户态、系统态和实际经过的时间。在这个例子中，所有这些时间都接近零，表明初始标记阶段几乎没有引  起任何停顿。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 0.315 | [CMS-concurrent-mark-start]   开始了CMS并发标记阶段。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 0.318 | [CMS-concurrent-mark:  0.003/0.003 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]  标记阶段结束，总共花费了0.003秒。  user=0.00 sys=0.00, real=0.00 secs表示在这个过程中用户态和内核态的CPU时间都是0，总时间也是0秒。这是由于计时精度或者这个阶段非常快                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|       | [CMS-concurrent-preclean-start]  说明在0.318开始了CMS并发预清理阶段。  [CMS-concurrent-preclean: 0.002/0.002  secs] [Times: user=0.00 sys=0.00, real=0.00 secs]  预清理阶段结束，总共花费了0.002秒。  同样的，CPU时间和总时间都是0秒。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 0.320 | [CMS-concurrent-abortable-preclean-start]  说明在17:15:56.946开始了CMS并发可中止预清理阶段。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 1.088 | [CMS-concurrent-abortable-preclean:  0.067/0.769 secs] - 这表示CMS的可中断并发预清理阶段已经完成。这个阶段尝试清理一些工作，以减少最终标记阶段的工作量。它耗时0.769秒，其中用户态时间为3.08秒，系统态时间为0.55秒，实际时间为0.77秒                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| 1.089 | [GC (CMS Final Remark)   [YG occupancy: 11619 K (78656 K)]  [Rescan (parallel) , 0.0008016 secs]  [weak refs processing, 0.0000298 secs]  [class unloading, 0.0003171 secs]  [scrub symbol table, 0.0004576 secs]  [scrub string table, 0.0001314 secs]  [1 CMS-remark: 1085637K(1087004K)]  1097256K(1165660K), 0.0018498 secs] [Times: user=0.00 sys=0.00, real=0.00  secs]  [GC (CMS Final Remark)] - 这是CMS收集器的最终标记阶段，也称为"remark"阶段。这个阶段是为了完成老年代中存活对象的标记。  [YG occupancy: 11619 K  (78656 K)] - 在这个阶段开始时，年轻代占用了11619K的内存，总共有78656K的空间。  [Rescan (parallel) ,  0.0008016 secs] - 这是并行重新扫描阶段，用于重新检查对象引用，确保所有存活对象都被标记。耗时约0.8毫秒。  [weak refs processing,  0.0000298 secs] - 处理弱引用的阶段，耗时约0.03毫秒。  [class unloading,  0.0003171 secs] - 卸载不再使用的类的阶段，耗时约0.32毫秒。  [scrub symbol table,  0.0004576 secs] - 清理符号表的阶段，耗时约0.46毫秒。  [scrub string table,  0.0001314 secs] - 清理字符串表的阶段，耗时约0.13毫秒。  [1 CMS-remark: 1085637K(1087004K)] - 在老年代中，最终标记阶段结束时，存活对象占用了1085637K的内存，老年代总大小为1087004K。  1097256K(1165660K), 0.0018498 secs] - 在整个堆中，最终标记阶段结束时，堆的使用量为1097256K，而堆的总大小为1165660K。整个阶段耗时约1.85毫秒。 |
| 1.090 | [CMS-concurrent-sweep-start]  - 开始CMS的并发清除阶段，这个阶段会清除掉标记为垃圾的对象。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 1.091 | [CMS-concurrent-sweep:  0.001/0.001 secs] - 并发清除阶段完成，耗时1毫秒。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 1.097 | [CMS-concurrent-reset-start] - 开始CMS的并发重置阶段，这个阶段会重置CMS收集器的内部状态，为下一次垃圾回收做准备。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 1.400 | [CMS-concurrent-reset:  0.039/0.304 secs] - 表示CMS垃圾收集器的并发重置阶段。  [Times: user=1.39 sys=0.41, real=0.30  secs] - 提供了执行该阶段时的时间消耗细节。其中用户态消耗的CPU 1.39秒，系统态消耗的CPU0.41秒。从开始到结束的实际经过0.30秒。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

## 4、垃圾回收器特点的体现

由于之前的log只能体现垃圾回收器过程，所以下面增加参数

```angular2html
-XX:+PrintGCApplicationStoppedTime //打印因GC而导致应用程序停止的时间。
-XX:+PrintGCApplicationConcurrentTime//打印应用程序在并发GC期间的运行时间
```
-[包含应用程序停止的时间的日志](cmsLog/1detail.log)


### 1、0.314秒处，初始标记：

暂停所有的其他线程，并记录下直接与 root 相连的对象，速度很快 ；

```
Total time for which application threads were stopped: 0.0005789 seconds, Stopping threads took:  0.0000763 seconds
```

垃圾收集过程中，应用程序线程总共被暂停的时间0.0005789秒 实际停止线程的操作所花费的时间0.0000763 秒

### 2、并发标记阶段

这个阶段程序并没有停止，一直在进行新生代的垃圾回收

### 3、1.017秒处，重新标记阶段

程序进行了暂停，重新标记阶段结束，程序暂停时间是初始标记的大约三倍多。

```angular2html
Total time for which application threads were stopped: 0.0018594 seconds, Stopping threads took:  0.0000947 seconds
```

垃圾收集过程中，应用程序线程总共被暂停的时间0.0018594秒 实际停止线程的操作所花费的时间0.0000947秒

### 4、日志中的展示如图

![img](image/clip_image006.jpg)

# 5、调整参数
重新编写调优程序
[进行调优的java程序](ZgcOptimization.java)
<details>
    <summary>进行调优的java程序</summary>

```java

public class CMSOptimization {
    public static void main(String[] args) {

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<byte[]> memoryHog = new ArrayList<>();
        int k = 1;
        int allAllocate = 10000;
        int oneAllocate = 10;
        while (k <= allAllocate / oneAllocate) {
            for (int i = 0; i < oneAllocate; i++) {
                // 分配1MB的数组
                byte[] array = new byte[1024 * 1024];
                memoryHog.add(array);
            }

            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long usedHeapMemorySize = heapMemoryUsage.getUsed();
            long committedHeapMemorySize = heapMemoryUsage.getCommitted();
            System.out.println("第" + k++ + "次分配内存。已使用的堆内存大小：" + usedHeapMemorySize / (1024 * 1024) + " MB。已分配的堆内存：" + committedHeapMemorySize / (1024 * 1024) + " MB");
            // 模拟内存使用情况
            if (memoryHog.size() > oneAllocate) {
                memoryHog.subList(0, memoryHog.size()).clear();
            }
            try {
                // 暂停一段时间，模拟应用程序的其他工作
                Thread.sleep(oneAllocate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

```

</details>
程序的介绍：程序分配10000M内存，并使用。中间有暂停程序10000ms作为模拟其他程序运行，可以通过调整oneAllocate的大小确定一次使用空间大小，中间打印各种数据。

## 1、对程序的Xmx（最大堆内存）参数进行调优
参数为
```
-XX:+UseConcMarkSweepGC
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xmx4g
-Xloggc:./gc.log
-XX:+PrintGCApplicationStoppedTime
-XX:+PrintGCApplicationConcurrentTime
```
增加调整参数及其并分析日志得到下图，可以看出再最大堆达到256m以后，程序对堆变化就不敏感，甚至停止了。这是因为，我们的程序一次分配1m，10次才使用。所以该程序并不能最好的体现CMS的痛点。
![img.png](img.png)
修改程序的oneAllocate为100



![img](image/clip_image008.jpg)

## 2、启用并发清除的增量模式: 适用于多核CPU，可以减少单次清除的暂停时间

<details>
    <summary>并发清除的增量模式CMSIncrementalMode.log日志</summary>

```
Java HotSpot(TM) 64-Bit Server VM (25.301-b09) for windows-amd64 JRE (1.8.0_301-b09), built on Jun  9 2021 06:46:21 by "java_re" with MS VC++ 15.9 (VS2017)
Memory: 4k page, physical 16672068k(7592568k free), swap 19686724k(5385672k free)
CommandLine flags: -XX:+CMSIncrementalMode -XX:InitialHeapSize=266753088 -XX:MaxHeapSize=4268049408 -XX:MaxNewSize=697933824 -XX:MaxTenuringThreshold=6 -XX:OldPLABSize=16 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:-UseLargePagesIndividualAllocation -XX:+UseParNewGC 
0.285: [GC (Allocation Failure) 0.285: [ParNew: 69734K->8205K(78656K), 0.0491721 secs] 69734K->66576K(253440K), 0.0494759 secs] [Times: user=0.22 sys=0.03, real=0.05 secs] 
0.343: [GC (Allocation Failure) 0.343: [ParNew: 77161K->8278K(78656K), 0.0340848 secs] 135532K->135089K(253440K), 0.0341773 secs] [Times: user=0.13 sys=0.08, real=0.03 secs] 
0.377: [GC (CMS Initial Mark) [1 CMS-initial-mark: 126811K(174784K)] 136113K(253440K), 0.0004892 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
0.378: [CMS-concurrent-mark-start]
0.386: [GC (Allocation Failure) 0.386: [ParNew: 77236K->8213K(78656K), 0.0327086 secs] 204047K->203634K(275028K), 0.0327799 secs] [Times: user=0.19 sys=0.03, real=0.03 secs] 
0.427: [GC (Allocation Failure) 0.427: [ParNew: 77178K->8193K(78656K), 0.0369587 secs] 272598K->272223K(343904K), 0.0370719 secs] [Times: user=0.17 sys=0.05, real=0.04 secs] 
0.472: [GC (Allocation Failure) 0.472: [ParNew: 77162K->8192K(78656K), 0.0336890 secs] 341191K->340832K(412780K), 0.0337865 secs] [Times: user=0.24 sys=0.05, real=0.03 secs] 
0.513: [GC (Allocation Failure) 0.513: [ParNew: 77163K->8194K(78656K), 0.0341074 secs] 409803K->409443K(480628K), 0.0342015 secs] [Times: user=0.17 sys=0.05, real=0.03 secs] 
0.551: [CMS-concurrent-mark: 0.005/0.174 secs] [Times: user=0.81 sys=0.17, real=0.17 secs] 
0.551: [CMS-concurrent-preclean-start]
0.555: [GC (Allocation Failure) 0.555: [ParNew: 77167K->8192K(78656K), 0.0371476 secs] 478416K->478052K(549504K), 0.0372092 secs] [Times: user=0.16 sys=0.05, real=0.04 secs] 
0.598: [GC (Allocation Failure) 0.598: [ParNew: 77166K->8192K(78656K), 0.0490786 secs] 547026K->546661K(618380K), 0.0491424 secs] [Times: user=0.30 sys=0.03, real=0.05 secs] 
0.654: [GC (Allocation Failure) 0.654: [ParNew: 77167K->8195K(78656K), 0.0377231 secs] 615636K->615273K(687256K), 0.0377898 secs] [Times: user=0.22 sys=0.06, real=0.04 secs] 
0.695: [CMS-concurrent-preclean: 0.003/0.144 secs] [Times: user=0.69 sys=0.14, real=0.14 secs] 
0.696: [CMS-concurrent-abortable-preclean-start]
0.699: [GC (Allocation Failure) 0.699: [ParNew: 77171K->8192K(78656K), 0.0374328 secs] 684249K->683882K(755104K), 0.0375174 secs] [Times: user=0.19 sys=0.03, real=0.04 secs] 
0.743: [GC (Allocation Failure) 0.743: [ParNew: 77168K->8192K(78656K), 0.0483034 secs] 752858K->752491K(823980K), 0.0484265 secs] [Times: user=0.27 sys=0.06, real=0.05 secs] 
0.794: [CMS-concurrent-abortable-preclean: 0.001/0.098 secs] [Times: user=0.47 sys=0.09, real=0.10 secs] 
0.794: [GC (CMS Final Remark) [YG occupancy: 41327 K (78656 K)]0.794: [Rescan (parallel) , 0.0006167 secs]0.795: [weak refs processing, 0.0000282 secs]0.795: [class unloading, 0.0002503 secs]0.795: [scrub symbol table, 0.0003472 secs]0.795: [scrub string table, 0.0001009 secs][1 CMS-remark: 744299K(745324K)] 785627K(823980K), 0.0014283 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
0.795: [CMS-concurrent-sweep-start]
0.798: [GC (Allocation Failure) 0.798: [ParNew: 77543K->8192K(78656K), 0.0483141 secs] 821842K->820076K(891828K), 0.0484086 secs] [Times: user=0.25 sys=0.05, real=0.05 secs] 
0.849: [CMS-concurrent-sweep: 0.001/0.054 secs] [Times: user=0.27 sys=0.05, real=0.05 secs] 
0.853: [CMS-concurrent-reset-start]
0.853: [GC (Allocation Failure) 0.853: [ParNew: 77158K->8196K(78656K), 0.0361353 secs] 889042K->888689K(1431796K), 0.0362060 secs] [Times: user=0.14 sys=0.06, real=0.04 secs] 
0.895: [GC (Allocation Failure) 0.895: [ParNew: 77167K->8192K(78656K), 0.0300832 secs] 957659K->957298K(1431796K), 0.0302175 secs] [Times: user=0.09 sys=0.02, real=0.03 secs] 
1.043: [GC (Allocation Failure) 1.043: [ParNew: 78144K->8541K(78656K), 0.0468224 secs] 1027250K->1025233K(1431796K), 0.0469092 secs] [Times: user=0.31 sys=0.01, real=0.05 secs] 
1.099: [GC (Allocation Failure) 1.099: [ParNew: 77501K->8192K(78656K), 0.0478698 secs] 1094193K->1093828K(1431796K), 0.0479417 secs] [Times: user=0.33 sys=0.00, real=0.05 secs] 
1.154: [GC (Allocation Failure) 1.154: [ParNew: 77158K->8192K(78656K), 0.0501479 secs] 1162794K->1162437K(1431796K), 0.0502215 secs] [Times: user=0.28 sys=0.03, real=0.05 secs] 
1.210: [GC (Allocation Failure) 1.210: [ParNew: 77161K->8192K(78656K), 0.0553599 secs] 1231407K->1231046K(1431796K), 0.0554506 secs] [Times: user=0.28 sys=0.05, real=0.06 secs] 
1.271: [GC (Allocation Failure) 1.271: [ParNew: 77164K->8199K(78656K), 0.0361662 secs] 1300019K->1299663K(1431796K), 0.0362443 secs] [Times: user=0.19 sys=0.03, real=0.04 secs] 
1.312: [GC (Allocation Failure) 1.312: [ParNew: 77173K->8192K(78656K), 0.0305011 secs] 1368637K->1368272K(1440020K), 0.0305756 secs] [Times: user=0.16 sys=0.03, real=0.03 secs] 
1.349: [GC (Allocation Failure) 1.349: [ParNew: 77166K->8192K(78656K), 0.0474940 secs] 1437247K->1436881K(1508896K), 0.0475523 secs] [Times: user=0.28 sys=0.03, real=0.05 secs] 
1.402: [GC (Allocation Failure) 1.402: [ParNew: 77167K->8192K(78656K), 0.0487411 secs] 1505856K->1505490K(1576744K), 0.0488177 secs] [Times: user=0.33 sys=0.00, real=0.05 secs] 
1.456: [GC (Allocation Failure) 1.456: [ParNew: 77168K->8192K(78656K), 0.0500983 secs] 1574466K->1574099K(1645620K), 0.0501912 secs] [Times: user=0.27 sys=0.06, real=0.05 secs] 
1.512: [GC (Allocation Failure) 1.512: [ParNew: 77168K->8192K(78656K), 0.0491879 secs] 1643075K->1642708K(1714496K), 0.0492726 secs] [Times: user=0.33 sys=0.00, real=0.05 secs] 
1.567: [GC (Allocation Failure) 1.567: [ParNew: 77168K->8192K(78656K), 0.0483121 secs] 1711684K->1711317K(1783372K), 0.0483798 secs] [Times: user=0.30 sys=0.03, real=0.05 secs] 
1.620: [GC (Allocation Failure) 1.620: [ParNew: 77168K->8192K(78656K), 0.0489104 secs] 1780293K->1779926K(1851220K), 0.0489864 secs] [Times: user=0.30 sys=0.03, real=0.05 secs] 
1.675: [GC (Allocation Failure) 1.675: [ParNew: 77168K->8192K(78656K), 0.0496041 secs] 1848903K->1848535K(1920096K), 0.0496907 secs] [Times: user=0.25 sys=0.06, real=0.05 secs] 
1.732: [GC (Allocation Failure) 1.732: [ParNew: 77168K->8202K(78656K), 0.0350121 secs] 1917512K->1917155K(1988972K), 0.0351233 secs] [Times: user=0.16 sys=0.06, real=0.04 secs] 
1.773: [GC (Allocation Failure) 1.773: [ParNew: 77179K->8192K(78656K), 0.0282759 secs] 1986132K->1985764K(2057848K), 0.0283661 secs] [Times: user=0.14 sys=0.06, real=0.03 secs] 
1.921: [GC (Allocation Failure) 1.921: [ParNew: 77168K->8194K(78656K), 0.0494194 secs] 2054741K->2054375K(2125696K), 0.0495340 secs] [Times: user=0.22 sys=0.05, real=0.05 secs] 
1.978: [GC (Allocation Failure) 1.978: [ParNew: 77170K->8192K(78656K), 0.0520012 secs] 2123352K->2122982K(2194572K), 0.0520810 secs] [Times: user=0.26 sys=0.00, real=0.05 secs] 
2.037: [GC (Allocation Failure) 2.037: [ParNew: 77168K->8192K(78656K), 0.0509279 secs] 2191959K->2191591K(2263448K), 0.0509965 secs] [Times: user=0.30 sys=0.02, real=0.05 secs] 
2.095: [GC (Allocation Failure) 2.095: [ParNew: 77168K->8192K(78656K), 0.0481107 secs] 2260568K->2260200K(2331296K), 0.0481874 secs] [Times: user=0.30 sys=0.02, real=0.05 secs] 
2.150: [GC (Allocation Failure) 2.150: [ParNew: 77168K->8192K(78656K), 0.0495806 secs] 2329177K->2328809K(2400172K), 0.0496571 secs] [Times: user=0.30 sys=0.03, real=0.05 secs] 
2.207: [GC (Allocation Failure) 2.207: [ParNew: 77168K->8192K(78656K), 0.0497734 secs] 2397786K->2397418K(2469048K), 0.0498406 secs] [Times: user=0.30 sys=0.03, real=0.05 secs] 
2.264: [GC (Allocation Failure) 2.264: [ParNew: 77168K->8192K(78656K), 0.0503823 secs] 2466395K->2466028K(2537924K), 0.0505052 secs] [Times: user=0.27 sys=0.03, real=0.05 secs] 
2.323: [GC (Allocation Failure) 2.323: [ParNew: 77168K->8192K(78656K), 0.0504763 secs] 2535004K->2534637K(2605772K), 0.0505752 secs] [Times: user=0.25 sys=0.03, real=0.05 secs] 
2.382: [GC (Allocation Failure) 2.382: [ParNew: 77168K->8192K(78656K), 0.0513315 secs] 2603613K->2603246K(2674648K), 0.0514185 secs] [Times: user=0.27 sys=0.03, real=0.05 secs] 
2.442: [GC (Allocation Failure) 2.442: [ParNew: 77168K->8192K(78656K), 0.0506169 secs] 2672222K->2671855K(2743524K), 0.0507076 secs] [Times: user=0.28 sys=0.03, real=0.05 secs] 
2.500: [GC (Allocation Failure) 2.500: [ParNew: 77168K->8192K(78656K), 0.0513625 secs] 2740831K->2740464K(2812400K), 0.0514584 secs] [Times: user=0.31 sys=0.06, real=0.05 secs] 
2.558: [GC (Allocation Failure) 2.558: [ParNew: 77168K->8192K(78656K), 0.0496782 secs] 2809440K->2809073K(2880248K), 0.0497446 secs] [Times: user=0.27 sys=0.03, real=0.05 secs] 
2.616: [GC (Allocation Failure) 2.616: [ParNew: 77168K->8192K(78656K), 0.0500290 secs] 2878049K->2877682K(2949124K), 0.0501166 secs] [Times: user=0.26 sys=0.05, real=0.05 secs] 
2.672: [GC (Allocation Failure) 2.672: [ParNew: 77168K->8192K(78656K), 0.0497784 secs] 2946658K->2946291K(3018000K), 0.0498417 secs] [Times: user=0.31 sys=0.03, real=0.05 secs] 
2.729: [GC (Allocation Failure) 2.729: [ParNew: 77168K->8192K(78656K), 0.0498977 secs] 3015267K->3014900K(3086876K), 0.0499932 secs] [Times: user=0.25 sys=0.05, real=0.05 secs] 
2.900: [GC (Allocation Failure) 2.900: [ParNew: 77168K->8192K(78656K), 0.0508297 secs] 3083877K->3083509K(3154724K), 0.0509118 secs] [Times: user=0.27 sys=0.01, real=0.05 secs] 
2.959: [GC (Allocation Failure) 2.959: [ParNew: 77168K->8192K(78656K), 0.0542813 secs] 3152486K->3152118K(3223600K), 0.0543703 secs] [Times: user=0.25 sys=0.03, real=0.05 secs] 
3.023: [GC (Allocation Failure) 3.023: [ParNew: 77168K->8192K(78656K), 0.0518934 secs] 3221095K->3220727K(3292476K), 0.0520222 secs] [Times: user=0.23 sys=0.03, real=0.05 secs] 
3.083: [GC (Allocation Failure) 3.083: [ParNew: 77168K->8192K(78656K), 0.0517719 secs] 3289704K->3289336K(3361352K), 0.0519279 secs] [Times: user=0.25 sys=0.05, real=0.05 secs] 
3.144: [GC (Allocation Failure) 3.144: [ParNew: 77168K->8192K(78656K), 0.0501679 secs] 3358313K->3357945K(3429200K), 0.0502903 secs] [Times: user=0.27 sys=0.03, real=0.05 secs] 
3.199: [CMS-concurrent-reset: 0.039/2.346 secs] [Times: user=9.95 sys=1.25, real=2.35 secs] 
3.203: [GC (Allocation Failure) 3.204: [ParNew: 77168K->8192K(78656K), 0.0518399 secs] 3426922K->3426554K(3498076K) icms_dc=25 , 0.0519632 secs] [Times: user=0.38 sys=0.03, real=0.05 secs] 
3.265: [GC (Allocation Failure) 3.265: [ParNew (promotion failed): 77168K->77168K(78656K), 0.0498991 secs]3.315: [CMS: 3485947K->2471108K(3487268K), 0.5434656 secs] 3495531K->2471108K(3565924K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=40 , 0.5954428 secs] [Times: user=0.77 sys=0.05, real=0.60 secs] 
3.861: [GC (CMS Initial Mark) [1 CMS-initial-mark: 2471108K(3488192K)] 2484005K(4101632K), 0.0003095 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
3.861: [CMS-concurrent-mark-start]
3.872: [CMS-concurrent-mark: 0.011/0.011 secs] [Times: user=0.05 sys=0.00, real=0.01 secs] 
3.872: [CMS-concurrent-preclean-start]
3.875: [CMS-concurrent-preclean: 0.003/0.003 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
3.875: [CMS-concurrent-abortable-preclean-start]
4.091: [GC (Allocation Failure) 4.091: [ParNew4.123: [CMS-concurrent-abortable-preclean: 0.006/0.248 secs] [Times: user=0.30 sys=0.19, real=0.25 secs] 
: 544360K->67601K(613440K), 0.0938523 secs] 3015469K->3024109K(4101632K) icms_dc=55 , 0.0939743 secs] [Times: user=0.69 sys=0.06, real=0.09 secs] 
4.185: [GC (CMS Final Remark) [YG occupancy: 68625 K (613440 K)]4.185: [Rescan (parallel) , 0.0014991 secs]4.186: [weak refs processing, 0.0000104 secs]4.186: [class unloading, 0.0003690 secs]4.187: [scrub symbol table, 0.0004516 secs]4.187: [scrub string table, 0.0001639 secs][1 CMS-remark: 2956508K(3488192K)] 3025133K(4101632K), 0.0026012 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
4.188: [CMS-concurrent-sweep-start]
4.302: [CMS-concurrent-sweep: 0.002/0.115 secs] [Times: user=0.02 sys=0.02, real=0.11 secs] 
4.303: [CMS-concurrent-reset-start]
4.311: [CMS-concurrent-reset: 0.009/0.009 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
4.336: [GC (Allocation Failure) 4.336: [ParNew: 612937K->612937K(613440K), 0.0000302 secs]4.336: [CMS: 2956508K->3047634K(3488192K), 0.5057828 secs] 3569445K->3047634K(4101632K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=70 , 0.5059181 secs] [Times: user=0.48 sys=0.00, real=0.51 secs] 
4.992: [GC (Allocation Failure) 4.992: [ParNew: 544345K->544345K(613440K), 0.0000170 secs]4.992: [CMS: 3047634K->3079378K(3488192K), 0.5178418 secs] 3591980K->3079378K(4101632K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=87 , 0.5180754 secs] [Times: user=0.51 sys=0.00, real=0.52 secs] 
5.510: [GC (CMS Initial Mark) [1 CMS-initial-mark: 3079378K(3488192K)] 3092282K(4101632K), 0.0008984 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
5.511: [CMS-concurrent-mark-start]
5.526: [CMS-concurrent-mark: 0.014/0.014 secs] [Times: user=0.05 sys=0.00, real=0.01 secs] 
5.526: [CMS-concurrent-preclean-start]
5.530: [CMS-concurrent-preclean: 0.004/0.004 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
5.530: [CMS-concurrent-abortable-preclean-start]
5.555: [GC (Allocation Failure) 5.555: [ParNew5.637: [CMS-concurrent-abortable-preclean: 0.004/0.107 secs] [Times: user=0.59 sys=0.05, real=0.11 secs] 
 (promotion failed): 544367K->611966K(613440K), 0.1148738 secs]5.670: [CMS (concurrent mode failure): 3487963K->3487961K(3488192K), 0.0832051 secs] 3623746K->3623131K(4101632K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=100 , 0.1982268 secs] [Times: user=0.91 sys=0.05, real=0.20 secs] 
5.891: [Full GC (Allocation Failure) 5.891: [CMS: 3487961K->3487961K(3488192K), 0.6008503 secs] 4099927K->3587291K(4101632K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=100 , 0.6009703 secs] [Times: user=0.61 sys=0.00, real=0.60 secs] 
6.492: [GC (CMS Initial Mark) [1 CMS-initial-mark: 3487961K(3488192K)] 3599743K(4101632K), 0.0007277 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
6.493: [CMS-concurrent-mark-start]
6.509: [CMS-concurrent-mark: 0.017/0.017 secs] [Times: user=0.05 sys=0.00, real=0.02 secs] 
6.509: [CMS-concurrent-preclean-start]
6.518: [CMS-concurrent-preclean: 0.008/0.008 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
6.518: [CMS-concurrent-abortable-preclean-start]
6.518: [CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
6.518: [GC (CMS Final Remark) [YG occupancy: 365531 K (613440 K)]6.518: [Rescan (parallel) , 0.0018111 secs]6.520: [weak refs processing, 0.0000103 secs]6.520: [class unloading, 0.0003140 secs]6.520: [scrub symbol table, 0.0004396 secs]6.521: [scrub string table, 0.0001326 secs][1 CMS-remark: 3487961K(3488192K)] 3853493K(4101632K), 0.0027885 secs] [Times: user=0.05 sys=0.00, real=0.00 secs] 
6.521: [CMS-concurrent-sweep-start]
6.523: [CMS-concurrent-sweep: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
6.523: [CMS-concurrent-reset-start]
6.530: [CMS-concurrent-reset: 0.007/0.007 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
6.542: [GC (Allocation Failure) 6.542: [ParNew: 612810K->612810K(613440K), 0.0000160 secs]6.542: [CMS: 3487961K->3487961K(3488192K), 0.0776852 secs] 4100772K->4099299K(4101632K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=100 , 0.0777900 secs] [Times: user=0.08 sys=0.00, real=0.08 secs] 
6.620: [Full GC (Allocation Failure) 6.620: [CMS: 3487961K->3487961K(3488192K), 0.0091647 secs] 4100323K->4100323K(4101632K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=100 , 0.0092219 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
6.629: [Full GC (Allocation Failure) 6.629: [CMS: 3487961K->3487909K(3488192K), 0.4091790 secs] 4100323K->4100271K(4101632K), [Metaspace: 3889K->3889K(1056768K)] icms_dc=100 , 0.4092383 secs] [Times: user=0.41 sys=0.00, real=0.41 secs] 
7.039: [GC (CMS Initial Mark) [1 CMS-initial-mark: 3487909K(3488192K)] 4100838K(4101632K), 0.0007517 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
7.040: [CMS-concurrent-mark-start]
Heap
 par new generation   total 613440K, used 612933K [0x00000006c1800000, 0x00000006eb190000, 0x00000006eb190000)
  eden space 545344K, 100% used [0x00000006c1800000, 0x00000006e2c90000, 0x00000006e2c90000)
  from space 68096K,  99% used [0x00000006e2c90000, 0x00000006e6e915a0, 0x00000006e6f10000)
  to   space 68096K,   0% used [0x00000006e6f10000, 0x00000006e6f10000, 0x00000006eb190000)
 concurrent mark-sweep generation total 3488192K, used 3487909K [0x00000006eb190000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 3920K, capacity 4636K, committed 4864K, reserved 1056768K
  class space    used 430K, capacity 460K, committed 512K, reserved 1048576K

```

</details>
```
-XX:+CMSIncrementalMode
```

没有使用，一次GC耗时0.777秒

![img](image/clip_image010.jpg)

使用后耗时0.517秒

![img](image/clip_image012.jpg)

应用程序的内存分配和回收更加高效，那么老年代中的垃圾积累速度会减慢，从而减少了CMS收集器的触发次数

![img](image/clip_image014.jpg)

 
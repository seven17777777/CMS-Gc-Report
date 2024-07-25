@echo off
setlocal enabledelayedexpansion

for /L %%i in (1,1,2) do (
    set "logfile=gcLog/%%igc.log"
    F:\TencentKona-8.0.18-412\bin\java.exe -cp F:\java_learn\pure-test\src\main\java ^
        -XX:+UseConcMarkSweepGC ^
        -XX:+PrintGCDetails ^
        -XX:+PrintGCDateStamps ^
        -Xloggc:!logfile! ^
        -XX:+PrintGCApplicationStoppedTime ^
        -XX:+PrintGCApplicationConcurrentTime ^
        com.code.tryOne.jvmGc.CMSGC.CMSOptimization
)

endlocal
pause

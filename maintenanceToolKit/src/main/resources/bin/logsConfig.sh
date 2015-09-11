#!/bin/bash
#Config the path of log

Dir=$HPBA_HOME
Date=`date "+%Y_%m_%d"`

LOGS_INFO=$Dir/glassfish/glassfish/domains/BTOA/logs/=btoaLogs_$Date.tar=btoaLogs,$Dir/webserver/httpd/logs/=webserverLogs_$Date.tar=webserverLogs,$Dir/*.log=installLog_$Date.tar=installLog,$Dir/glassfish/glassfish/domains/BTOA/config/*.log=btoaConfigLog_$Date.tar=btoaConfigLog,$Dir/Tools/log/*.log=toolLog_$Date.tar=toolLog,$Dir/jdk/jre/bin/*.log=jdkBinLog_$Date.tar=jdkBinLog,$Dir/bin/*.log=binLog_$Date.tar=binLog


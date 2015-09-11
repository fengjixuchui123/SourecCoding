#!/bin/bash
#To collect the HPBA's logs

#The logsConig.sh is a shell to config the path of log
source ./logsConfig.sh
#Creat the folder to store the logs
Logs_Path=$HPBA_HOME/maintenance/log
allLog_Path=$HPBA_HOME/maintenance
if [ ! -x $Logs_Path ] 
then
	mkdir -p $Logs_Path
fi

#collect the logs to "maintenance/log" and compress logs.
#if you want to uncompress allLogs.tar,you should use "-P" operation
export Logs_Path
IFS=','
for log_info in $LOGS_INFO
do
	logPath=`echo $log_info | awk -F'=' '{print $1}'`
	tarFile=`echo $log_info | awk -F'=' '{print $2}'` 
        folderName=`echo $log_info | awk -F'=' '{print $3}'` 
        mkdir $folderName
        cp -r $logPath ./$folderName
        tar -czf $Logs_Path/$tarFile $folderName --remove-files 
done

tar -czPf $allLog_Path/allLogs.tar $Logs_Path
mv $allLog_Path/allLogs.tar $Logs_Path

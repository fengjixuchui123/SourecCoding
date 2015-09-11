#!/bin/bash

backupFolder=$HPBA_HOME/pgsql/backup
WORKPATH=$HPBA_HOME/pgsql/bin
export WORKPATH
if [ ! -d $backupFolder ]
then
    mkdir $backupFolder
fi
$WORKPATH/pg_dump -d xs_mng -U xsadmin > backup.txt
tar -czf $HPBA_HOME/pgsql/backup/backup.tar.gz backup.txt --remove-files


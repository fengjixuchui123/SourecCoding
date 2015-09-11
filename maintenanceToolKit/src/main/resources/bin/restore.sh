#!/bin/bash


WORKPATH=$HPBA_HOME/pgsql/bin
export WORKPATH
tar -xzvf $HPBA_HOME/pgsql/backup/$1 -C $HPBA_HOME/pgsql/backup
$WORKPATH/createdb -U xsadmin xs_mng_bak
$WORKPATH/psql -d xs_mng_bak -U xsadmin < $HPBA_HOME/pgsql/backup/backup.txt
rm $HPBA_HOME/pgsql/backup/backup.txt

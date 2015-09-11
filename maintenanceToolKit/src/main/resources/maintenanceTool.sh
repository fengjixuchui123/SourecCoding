. ../bin/setenv.sh
JAVA=$HPBA_HOME/jdk/bin/java

JAVA_OPTION="-Dlog4j.configuration=file:./config/maintenance-tool-log4j.xml -Djava.util.prefs.systemRoot=$JAVA_HOME/.java -Djava.util.prefs.userRoot=$JAVA_HOME/.java/.userPrefs"

CLASSPATH="./lib/maintenanceToolKit-10.10.00-SNAPSHOT.jar:./lib/*"
MAINCLASS=com.hp.btoe.maintenanceTool.pages.ValidationPage

$JAVA $JAVA_OPTION -classpath $CLASSPATH $MAINCLASS  $1

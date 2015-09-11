package com.hp.btoe.maintenanceTool.action;

import java.io.File;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.config.MaintenanceConfig;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class BackupAndRestorePsql {

    private static org.apache.log4j.Logger log = Logger.getLogger(BackupAndRestorePsql.class);

    private String backupFile;

    private String restoreFile;

    private String backupShell;

    private String restoreOldFile;

    private String oldBackupFile;

    private String backupPath;

    public BackupAndRestorePsql() {

        init();
    }

    private void init() {
        MaintenanceConfig config = MaintenanceConfig.getLogConfig();
        backupPath = config.getString("BACKUP_PATH");
        backupFile = config.getString("BACKUP_FILE");
        restoreFile = config.getString("RESTORE_FILE");
        backupShell = config.getString("BACKUP_SHELL");
        restoreOldFile = config.getString("OLD_RESTORE_FILE");
        oldBackupFile = config.getString("OLD_BACKUP_FILE");
    }

    /*
     * private static String separator = File.separator; private String
     * backupPath; private String restoreShell; private String backupShell;
     * public boolean flag = true; public BackupAndRestorePsql() { init(); }
     * private void init() { String path = System.getenv("HPBA_HOME"); String
     * shell = path + separator + "Tools" + separator + "bin" + separator;
     * backupPath = path + separator + "pgsql" + separator + "backup" +
     * separator; restoreShell = shell + "restore.sh"; backupShell = shell +
     * "backup.sh"; }
     */
    public void backup() {
        try {
            File file = new File(backupFile);
            if (file.exists()) {
                String overWrite = MConsole.readLine("Do you want to overwrite " + backupFile + "(y or n)?");
                while (true) {
                    if (overWrite.equals("y")) {
                        file.delete();
                        runShell(backupShell);
                        MConsole.writeLine("\nCompleted backup !!!");
                        break;
                    } else if (overWrite.equals("n")) {
                        File oldFile = new File(oldBackupFile);
                        file.renameTo(oldFile);
                        runShell(backupShell);
                        break;
                    } else {
                        overWrite = MConsole.readLine("\nInvalid input!Please input y or n again : ");
                    }
                }
            } else {
                runShell(backupShell);
                MConsole.writeLine("\nCompleted backup !!!");
                log.info("backup successful!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("failed to backup");
        }
    }

    public void restore() {
        try {
            int j = 1;
            File file = new File(backupPath);
            if (file.exists()) {
                String[] fileList = file.list();
                MConsole.writeLine("The data which you should select is :");
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].endsWith(".tar.gz")) {
                        MConsole.writeLine(j + ": " + backupPath + fileList[i]);
                        j++;
                    }
                }
            }

            if (j == 1) {
                MConsole.writeLine("\nSorry,no data to restore !!!");
                Thread.currentThread();
                Thread.sleep(100);
                return;
            }
            String shell  = null;
            String data = null;
            data = MConsole.readLine("\nPlease input the data to restore :");
            while (true) {
                if (data.equals("1")) {
                    shell = restoreFile;
                    runShell(shell);
                    break;
                } else if (data.equals("2")) {
                    shell = restoreOldFile;
                    runShell(shell);
                    break;
                } else {
                    data = MConsole.readLine("The data which you select is not exist or invalid ,please select again : ");
                }
            }
            log.info("Restore successful!");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to restore");
        }
    }

    protected void runShell(String shell) {
        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(shell);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("run  shell error");
        }
    }

}

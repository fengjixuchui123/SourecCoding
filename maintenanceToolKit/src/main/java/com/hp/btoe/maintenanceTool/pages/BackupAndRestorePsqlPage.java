package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.BackupAndRestorePsql;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class BackupAndRestorePsqlPage {
    private static org.apache.log4j.Logger log = Logger.getLogger(BackupAndRestorePsqlPage.class);

    public static void showPage() {
        String choice = "";
        MConsole.writePageTitle("Backup and restore page");
        MConsole.writeLine("Please select backup or restore: ");
        MConsole.writeLine("1:Backup data");
        MConsole.writeLine("2:Restore data");
        MConsole.writeLine("0:exit");
        choice = MConsole.readLine("\nPlease select:");
        try{
            while (true) {
                if (choice.equals("1")) {
                    new BackupAndRestorePsql().backup();
                    Thread.currentThread();
                    Thread.sleep(100);
                    break;
                } else if (choice.equals("2")) {
                    new BackupAndRestorePsql().restore();
                    Thread.currentThread();
                    Thread.sleep(100);
                    break;
                } else if (choice.equals("0")) {
                    break;
                } else {
                    choice = MConsole.readLine("Invaild select,please select again : ");
                }
                }
        }catch(InterruptedException e){
            e.printStackTrace();
            log.error("sleep error");
        }
    }
}
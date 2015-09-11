package com.hp.btoe.maintenanceTool.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.utils.MaintenanceUtils;

public class MaintenanceConfig {

    private static org.apache.log4j.Logger log = Logger.getLogger(MaintenanceConfig.class);


    private String path = System.getenv("HPBA_HOME");

    private String configFile = new MaintenanceUtils().setPath("Tools", "config", "backupAndRestoreConfig.properties");

    private static MaintenanceConfig singleton = null;

    Properties prop = new Properties();

    private MaintenanceConfig() throws IOException {
        try {
            prop.load(new FileInputStream(new File(configFile)));
        } catch (IOException e) {
            log.error("load configFile failed");
            throw e;
        }
    }

    public synchronized static MaintenanceConfig getLogConfig() {
        try {
            if (singleton == null) {
                singleton = new MaintenanceConfig();
            }
            return singleton;
        } catch (Exception e) {
            log.error("singleton modle error");
        }

        return singleton;
    }

    public String getString(String info) {
        String shell = path + prop.getProperty(info);
        return shell;
    }

}

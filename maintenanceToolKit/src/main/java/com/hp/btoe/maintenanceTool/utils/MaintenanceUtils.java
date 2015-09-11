package com.hp.btoe.maintenanceTool.utils;

import java.io.File;

import org.apache.log4j.Logger;

public class MaintenanceUtils {

    private static org.apache.log4j.Logger log = Logger.getLogger(MaintenanceUtils.class);

    private String separator = File.separator;

    private String hpbaPath = System.getenv("HPBA_HOME");

    private String path;

    public String setPath(String... paths) {
        path = hpbaPath + separator;
        try {
            for (String childPath : paths) {
                if (childPath.contentEquals(".")) {
                    path += childPath;
                    break;
                }
                path = path + childPath + separator;
            }
        } catch (Exception e) {
            log.error("setPath is error");
        }
        return path;
    }

}

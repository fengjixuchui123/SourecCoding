package com.hp.btoe.maintenanceTool.utils;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.framework.ITrustedPrincipal;
import com.crystaldecisions.sdk.occa.infostore.CePropertyID;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.pluginmgr.IPluginInfo;
import com.crystaldecisions.sdk.occa.pluginmgr.IPluginMgr;
import com.crystaldecisions.sdk.plugin.CeKind;
import com.crystaldecisions.sdk.plugin.CeProgID;
import com.crystaldecisions.sdk.plugin.desktop.licensekey.ILicenseKey;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.properties.IProperties;
//import com.sap.connectivity.cis.plugin.api.IDataConnection;
//import com.sap.connectivity.foundation.api.CSException;
//import com.sap.connectivity.foundation.api.MutableConnection;

/**
 * BOUtils.
 * @author Elvin chou
 */
public class BOUtils {

    public static final String BOE_PERMANENT_LICENSE = "D9007-7SUYE3M-7107DBG-2D200WM-JU";

    public static final String BOE_TEMPORARY_LICENSE = "DC00U-X693F3M-7107DB4-GD200WC-7D";

    static Logger logger = LoggerFactory.getLogger(BOUtils.class);

    public static IEnterpriseSession getEnterpriseSession(String serverName, String port, String sharedSecret) {

        try {
            ISessionMgr sessionMgr = CrystalEnterprise.getSessionMgr();
            ITrustedPrincipal tp = sessionMgr.createTrustedPrincipal("administrator", serverName + ":" + port, sharedSecret);
            return sessionMgr.logon(tp);
        } catch (SDKException ex) {
            logger.error("Failed to get EnterpriseSession", ex);
            return null;
        }
    }

    public static void getBOLicense(IEnterpriseSession enterpriseSession) {
        Boolean prmLicenseFound = false;
        Boolean internalLicense = false;
        ILicenseKey licenseKey = null;
        try {
            IInfoStore infoStore = (IInfoStore)enterpriseSession.getService("InfoStore");

            IInfoObjects licenseKeyObjs = infoStore.query("SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='" + CeKind.LICENSEKEY + "'");

            for (int i = 0; i < licenseKeyObjs.size() && !prmLicenseFound; i++) {
                IInfoObject obj = (IInfoObject)licenseKeyObjs.get(i);
                licenseKey = (ILicenseKey)obj;
                String license = licenseKey.getLicenseKey();
                if (BOE_PERMANENT_LICENSE.equals(license)) {
                    prmLicenseFound = true;
                    internalLicense = true;
                } else if (BOE_TEMPORARY_LICENSE.equals(license)) {
                    internalLicense = true;
                }
            }
            if (!prmLicenseFound && internalLicense) {
                licenseKey.setLicenseKey(BOE_PERMANENT_LICENSE);
                licenseKey.save();
                if (logger.isDebugEnabled()) {
                    logger.debug("BOE license has been updated to permanent");
                }
            }
        } catch (SDKException ex) {
            enterpriseSession.logoff();
            logger.error("Failed to retrieve license key: ", ex);
        }
    }

    public static boolean doesUserExist(String username, IEnterpriseSession enterpriseSession) {
        try {
            IInfoStore iStore = (IInfoStore)enterpriseSession.getService("InfoStore");
            IInfoObjects userExistsResults =
                    iStore.query("SELECT COUNT(SI_ID) FROM CI_SYSTEMOBJECTS WHERE SI_KIND='" + CeKind.USER + "' AND SI_NAME='" + username + "'");
            IInfoObject userExistsResult = (IInfoObject)userExistsResults.get(0);
            IProperties counts = userExistsResult.properties().getProperties("SI_AGGREGATE_COUNT");
            int userIdCount = counts.getInt("SI_ID");
            return userIdCount != 0;
        } catch (SDKException e) {
            enterpriseSession.logoff();
            logger.error("Failed to check user. Exception caught: ", e);
            return true;
        }
    }

    public static String createUser(String userName, String psw, IEnterpriseSession enterpriseSession) {
        String description = "";
        int objectID = 0;

        try {
            IInfoStore infoStore = (IInfoStore)enterpriseSession.getService("InfoStore");

            // _BEGINSNIPPET_:Codeblock_addUserObjectJAVA.txt
            // Retrieve the Plugin Manager.
            IPluginMgr pluginMgr = infoStore.getPluginMgr();

            // Retrieve the User plugin.
            IPluginInfo userPlugin = pluginMgr.getPluginInfo("CrystalEnterprise.User");

            // Create a new InfoObjects collection.
            IInfoObjects newInfoObjects = infoStore.newInfoObjectCollection();

            // Add the User plugin to the collection.
            newInfoObjects.add(userPlugin);

            // Retrieve the newly created user object.
            IInfoObject iObject = (IInfoObject)newInfoObjects.get(0);

            // Set the user object's InfoObject properties.
            iObject.setTitle(userName);
            iObject.setDescription(description);
            int newUserID = iObject.getID();
            // Save the new group to the CMS.
            infoStore.commit(newInfoObjects);

            // Retrieve the specified user object.
            IInfoObjects rUser = infoStore.query("Select SI_ID, SI_PROGID From " + "CI_SYSTEMOBJECTS Where SI_ID=" + newUserID);
            if (rUser.size() == 0) {
                // The query returned a blank collection (no object found).

            }
            IInfoObject iUser = (IInfoObject)rUser.get(0);

            // Set the user object's plugin-specific properties.

            boolean passwordNeverExpires = true;
            boolean mustChangePassword = false;
            boolean cannotChangePassword = false;

            ((IUser)iUser).setFullName(userName);

            ((IUser)iUser).setPasswordExpiryAllowed(passwordNeverExpires);
            ((IUser)iUser).setPasswordToChangeAtNextLogon(mustChangePassword);
            ((IUser)iUser).setPasswordChangeAllowed(cannotChangePassword);

            // could be named - connection 1 or concurrent - 0
            ((IUser)iUser).setConnection(1);
            ((IUser)iUser).setNewPassword(psw);

            infoStore.commit(rUser);
            return Integer.toString(newUserID);
            // _ENDSNIPPET_

        } catch (SDKException e) {
            logger.error("Failed to add the new user. Exception caught: ", e);
        }
        return null;
    }

    public static void attachToAdminGrp(String uid, String grId, IEnterpriseSession enterpriseSession) throws SDKException {

        if (!doesUserGroupExistByUid(grId, enterpriseSession)) {
            // todo localization
            logger.error("UserGroup with id: " + grId + " does not exist");
            return;
        }

        try {
            /*
             * Query for the group. Only select SI_ID because the group is going
             * to be removed and you don't need to query the server for a lot.
             */
            IInfoStore infoStore = (IInfoStore)enterpriseSession.getService("InfoStore");

            // Retrieve the Group object you want to add the User to.
            IInfoObjects rGroup = infoStore.query("Select SI_ID, SI_USERGROUPS, SI_ALIASES From " + "CI_SYSTEMOBJECTS Where SI_ID='" + grId + "'");
            if (rGroup.size() == 0) {
                // The query returned a blank collection (no object found).
                logger.error("The user group with id: " + grId + " could not be found.");
                return;
            }
            IInfoObject iGroup = (IInfoObject)rGroup.get(0);

            // Retrieve the User object you want to add to the Group.
            IInfoObjects rUser =
                    infoStore.query("SELECT * FROM CI_SYSTEMOBJECTS " + "WHERE SI_PROGID='CrystalEnterprise.USER' " + "And SI_ID='" + uid + "'");
            if (rUser.size() == 0) {
                // The query returned a blank collection (no object found).
                logger.error("The user with id: " + uid + " could not be found.");
                return;
            }
            IInfoObject iUser = (IInfoObject)rUser.get(0);

            // Check that the InfoObject has the User ProgID.
            String uProgID = (String)iUser.properties().getProperty(CePropertyID.SI_PROGID).getValue();
            if (uProgID.equals(CeProgID.USER)) {
                Set memberGroups = ((IUser)iUser).getGroups();
                Integer groupInt = Integer.valueOf(iGroup.getID());

                // Add the group to the user's groups collection.
                ((IUser)iUser).getGroups().add(groupInt);
                if (logger.isDebugEnabled()) {
                    logger.debug("User with id: " + uid + " was attached to group with id: " + grId);
                }
                infoStore.commit(rUser);
            }
        } catch (SDKException e) {
            logger.error("Failed to attach User " + uid + " to group " + grId);
            throw e;
        }
    }

    private static boolean doesUserGroupExistByUid(String uName, IEnterpriseSession enterpriseSession) {
        try {
            IInfoStore iStore = (IInfoStore)enterpriseSession.getService("InfoStore");
            IInfoObjects userExistsResults =
                    iStore.query("SELECT COUNT(SI_ID) FROM CI_SYSTEMOBJECTS WHERE SI_KIND='" + CeKind.USERGROUP + "' AND SI_ID='" + uName + "'");
            IInfoObject userExistsResult = (IInfoObject)userExistsResults.get(0);
            IProperties counts = userExistsResult.properties().getProperties("SI_AGGREGATE_COUNT");
            int userIdCount = counts.getInt("SI_ID");
            return userIdCount != 0;
        } catch (SDKException e) {
            enterpriseSession.logoff();
            logger.error("Failed to check if group " + uName + " exists.", e);
            return false;
        }
    }

    /**
     * &lt;DataConnectionCUID&gt;=&lt;username&gt;,&lt;password&gt;,&lt;
     * dataSource&gt;,&lt;RDMSName&gt; where RDMSName is one of the following
     * @param connectionUID - DataConnectionCUID
     * @param rdmsName - database type, one of the following:
     *            <ul>
     *            <li>MySQL 4</li>
     *            <li>MySQL 5</li>
     *            <li>MS SQL Server 7.x</li>
     *            <li>MS SQL Server 2000 CLI</li>
     *            <li>MS SQL Server 2005</li>
     *            <li>Oracle 8.1</li>
     *            <li>Oracle 9</li>
     *            <li>Oracle 10</li>
     *            <li>Sybase ASIQ 12</li>
     *            <li>Sybase Adaptive Server 12</li>
     *            <li>Sybase Adaptive Server 15</li>
     *            <li>DB2 UDB v8</li>
     *            <li>DB2 UDB v7</li>
     *            </ul>
     * @param driverClass
     * @throws OMException
     * @throws SDKException
     * @throws CSException
     */
//    public static void updateJDBCDataConnection(String connectionUID, String boPort, String boUserID, String boPassword, String boServer,
//            Map dbContext, String rdmsName, String driverClass, String boCMSAuthScheme) throws OMException, SDKException, CSException
//    {
//        // Log in, and use the active session to create an instance of
//        // BIARFactory that is used
//        // to instantiate the objects we'll need to conduct the import.
//        IEnterpriseSession enSession = CrystalEnterprise.getSessionMgr().logon(boUserID, boPassword, boServer + ":" + boPort, boCMSAuthScheme);
//        try {
//            // modified by Jeff Zhu, use new api to update JDBC connection
//            IInfoStore infoStore = (IInfoStore)enSession.getService("InfoStore");
//            String queryString = "SELECT * FROM CI_APPOBJECTS WHERE SI_NAME='" + connectionUID + "'";
//            IInfoObjects infoObjects = infoStore.query(queryString);
//            if (infoObjects.isEmpty()) {
//                logger.warn("Unable to find data connection with uid = " + connectionUID);
//                return;
//            }
//             IDataConnection dataConn = (IDataConnection)infoObjects.get(0);
//            
//             MutableConnection conn =
//             (MutableConnection)dataConn.getConnection();
//            // conn.putProperty("DATASOURCE", "CREDENTIALS", "String",
//            // dbContext.getHostName() + ":" + dbContext.getPort());
//            // conn.putProperty("DATABASE", "CREDENTIALS", "String",
//            // dbContext.getDbName());
//            // conn.putProperty("USER", "CREDENTIALS", "String",
//            // dbContext.getUserName());
//            // conn.putProperty("PASSWORD", "CREDENTIALS", "String",
//            // dbContext.getPassword());
//            // conn.putProperty("URL", "CREDENTIALS", "String",
//            // "jdbc:sqlserver://" + dbContext.getHostName() + ":" +
//            // dbContext.getPort()
//            // + ";DatabaseName=" + dbContext.getDbName());
//            // dataConn.setConnection(conn);
//            // infoStore.commit(infoObjects, false, CommitMode.LENIENT);
//            //
//            // String logMessage =
//            // new
//            // StringBuilder().append("Finished updateJDBCDataConnection for connection ").append(connectionUID)
//            // .append(" with parameters: username=").append(dbContext.getUserName()).append(", hostName: ")
//            // .append(dbContext.getHostName()).append(", port: ").append(dbContext.getPort()).append(", driverClass: ")
//            // .append(driverClass).append(", dbName: ").append(dbContext.getDbName()).toString();
//            // logger.info(logMessage);
//        } finally {
//            enSession.logoff();
//        }
//    }
}

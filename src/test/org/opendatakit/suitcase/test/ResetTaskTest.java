package org.opendatakit.suitcase.test;

import junit.framework.TestCase;
import org.apache.wink.json4j.JSONObject;
import org.junit.Before;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.net.*;
import org.opendatakit.sync.client.SyncClient;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class ResetTaskTest extends TestCase {

    CloudEndpointInfo cloudEndpointInfo = null;
    String serverUrl = null;
    String appId = null;
    String absolutePathOfTestFiles = null;
    String userName = null;
    String password = null;
    String version = null;
    String testTableId = "testTable";
    @Before
    public final void setUp() throws MalformedURLException {
        serverUrl = System.getProperty("test.aggUrl");
        appId = System.getProperty("test.appId");
        absolutePathOfTestFiles = System.getProperty("test.absolutePathOfTestFiles");
        userName = System.getProperty("test.userName");
        password = System.getProperty("test.password");
        version = "2";
        cloudEndpointInfo = new CloudEndpointInfo(serverUrl, appId, userName, password);
    }

    public void testUploadTableBeforeReset_ExpectPass() {
        String csvFile = absolutePathOfTestFiles + "plot" + File.separator + "definition.csv";
        String tableSchemaETag = null;
        SyncClient sc = null;
        int retCode;

        try {
            sc = new SyncClient();

            String cloud_endpoint_url = cloudEndpointInfo.getHostUrl();
            cloud_endpoint_url = cloud_endpoint_url.substring(0, cloud_endpoint_url.length()-1);

            URL url = new URL(cloud_endpoint_url);
            String host = url.getHost();

            sc.init(host, cloudEndpointInfo.getUserName(), cloudEndpointInfo.getPassword());

            LoginTask lTask = new LoginTask(cloudEndpointInfo, false);
            retCode = lTask.blockingExecute();
            assertEquals(retCode, SuitcaseSwingWorker.okCode);

            JSONObject result = sc.createTableWithCSV(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(),
                    testTableId, null, csvFile);
            System.out.println("testUploadTableBeforeReset_ExpectPass : result is " + result);

            if (result.containsKey(SyncClient.TABLE_ID_JSON)) {
                String tableId = result.getString(SyncClient.TABLE_ID_JSON);
                assertEquals(tableId, testTableId);
                tableSchemaETag = result.getString(SyncClient.SCHEMA_ETAG_JSON);
            }

            // Get the table definition
            JSONObject tableDef = sc.getTableDefinition(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(),
                    testTableId, tableSchemaETag);

            // Make sure it is the same as the csv definition
            assertTrue(TestUtilities.checkThatTableDefAndCSVDefAreEqual(csvFile, tableDef));

        } catch (Exception e) {
            System.out.println("ResetTaskTest: Exception thrown in testUploadTableBeforeReset_ExpectPass");
            e.printStackTrace();
            fail();
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }

    public void testReset_ExpectPass() {
        int retCode;
        SyncClient sc = null;

        try {
            sc = new SyncClient();

            String cloud_endpoint_url = cloudEndpointInfo.getHostUrl();
            cloud_endpoint_url = cloud_endpoint_url.substring(0, cloud_endpoint_url.length()-1);

            URL url = new URL(cloud_endpoint_url);
            String host = url.getHost();

            sc.init(host, cloudEndpointInfo.getUserName(), cloudEndpointInfo.getPassword());

            LoginTask lTask = new LoginTask(cloudEndpointInfo, false);
            retCode = lTask.blockingExecute();
            assertEquals(retCode, SuitcaseSwingWorker.okCode);

            ResetTask rTask = new ResetTask(version,false);
            retCode = rTask.blockingExecute();
            assertEquals(retCode, SuitcaseSwingWorker.okCode);

            JSONObject obj = sc.getTable(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), testTableId);
            assertNull(obj);

            sc.close();

        } catch (Exception e) {
            System.out.println("TableTaskTest: Exception thrown in testResetTable_ExpectPass");
            e.printStackTrace();
            fail();
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }
}


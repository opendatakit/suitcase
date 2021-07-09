package org.opendatakit.suitcase.test;

import org.apache.wink.json4j.JSONObject;
import org.assertj.swing.core.ComponentLookupScope;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.*;
import org.assertj.swing.testing.AssertJSwingTestCaseTemplate;
import org.assertj.swing.timing.Pause;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendatakit.suitcase.Suitcase;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.net.*;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;
import org.opendatakit.sync.client.SyncClient;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.junit.Assert.assertEquals;

public class DownloadGUITest extends AssertJSwingTestCaseTemplate {

    private FrameFixture frame;
    private CloudEndpointInfo cloudEndpointInfo = null;
    private static final int LOGIN_TIMEOUT = 15000;
    private static final int DOWNLOAD_TIMEOUT = 200000;      // 200 seconds time to upload
    private String serverUrl = null;
    private String testTableId = "testTable";
    private String appId = null;
    private String userName = null;
    private String password = null;
    private String absolutePathOfTestFiles = null;
    @BeforeClass
    public static final void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @Before
    public final void setUp() throws MalformedURLException {
        serverUrl = System.getProperty("test.aggUrl");
        appId = System.getProperty("test.appId");
        absolutePathOfTestFiles = System.getProperty("test.absolutePathOfTestFiles");
        userName = System.getProperty("test.userName");
        password = System.getProperty("test.password");
        cloudEndpointInfo = new CloudEndpointInfo(serverUrl, appId, userName, password);
        // Upload table before starting the application
        uploadTableForDownload();
        // call provided AssertJSwingTestCaseTemplate.setUpRobot()
        this.setUpRobot();
        robot().settings().componentLookupScope(ComponentLookupScope.ALL);
        // initialize the graphical user interface
        application(Suitcase.class).start();
        this.frame = findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            protected boolean isMatching(Frame frame) {
                return "org.opendatakit.suitcase.Suitcase".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());
        this.frame.show();
    }

    public void uploadTableForDownload() {
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
            System.out.println("uploadTableForDownload: result is " + result);

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
            System.out.println("DownloadGUITest: Exception thrown in uploadTableForDownload");
            e.printStackTrace();
            fail();
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }

    public void login() {
        JPanelFixture ioPanelFixture = frame.panel(new GenericTypeMatcher<JPanel>(JPanel.class) {
            @Override
            protected boolean isMatching(JPanel panel) {
                return "io_panel".equals(panel.getName());
            }
        });
        frame.textBox("username").setText(userName);
        frame.textBox("app_id").setText(appId);
        frame.textBox("password").setText(password);
        frame.textBox("server_url").setText(serverUrl);
        frame.button("login_button").click();
        Pause.pause(LOGIN_TIMEOUT);                          // Wait for 15 seconds for login to complete
        ioPanelFixture.requireVisible();                     // IOPanel should be visible after login
    }

    @Test
    public void downloadTest(){
        login();
        JTextComponentFixture textComponentFixture = frame.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override
            protected boolean isMatching(JTextField textField) {
                if("table_id_text".equals(textField.getName())&&textField.isVisible()) {
                    return true;
                }
                else {
                    return false;
                }
            }
        });
        textComponentFixture.setText(testTableId);
        JButtonFixture buttonFixture = frame.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return "download_button".equals(button.getName());
            }
        });
        JProgressBarFixture progressBarFixture = frame.progressBar(new GenericTypeMatcher<JProgressBar>(JProgressBar.class) {
            @Override
            protected boolean isMatching(JProgressBar progressBar) {
                return "progress_bar".equals(progressBar.getName());
            }
        });
        buttonFixture.click();
        Pause.pause(DOWNLOAD_TIMEOUT);
        progressBarFixture.requireText(SuitcaseProgressBar.PB_DONE);
    }

    @After
    public final void tearDown() {
        try {
            this.frame = null;
        } finally {
            cleanUp();
        }
    }
}
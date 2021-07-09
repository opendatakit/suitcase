package org.opendatakit.suitcase.test;

import com.github.caciocavallosilano.cacio.ctc.junit.CacioTestRunner;
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
import org.junit.runner.RunWith;
import org.opendatakit.suitcase.Suitcase;
import org.opendatakit.suitcase.ui.IOPanel;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;

import javax.swing.*;
import java.awt.*;

import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

@RunWith(CacioTestRunner.class)
public class UploadGUITest extends AssertJSwingTestCaseTemplate {

    private FrameFixture frame;
    private static final int LOGIN_TIMEOUT = 15000;
    private static final int UPLOAD_TIMEOUT = 200000;      // 200 seconds time to upload
    private String serverUrl = null;
    private String appId = null;
    private String userName = null;
    private String password = null;
    private String absolutePathOfTestFiles = null;

    @BeforeClass
    public static final void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @Before
    public final void setUp() {
        // call provided AssertJSwingTestCaseTemplate.setUpRobot()
        this.setUpRobot();
        robot().settings().componentLookupScope(ComponentLookupScope.ALL);
        // initialize the graphical user interface
        application(Suitcase.class).start();

        serverUrl = System.getProperty("test.aggUrl");
        appId = System.getProperty("test.appId");
        userName = System.getProperty("test.userName");
        password = System.getProperty("test.password");
        absolutePathOfTestFiles = System.getProperty("test.absolutePathOfTestFiles");

        this.frame = findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            protected boolean isMatching(Frame frame) {
                return "org.opendatakit.suitcase.Suitcase".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());
        this.frame.show();
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
    public void uploadTest() {
        login();
        JTabbedPaneFixture tabsPanelFixture = frame.tabbedPane(new GenericTypeMatcher<JTabbedPane>(JTabbedPane.class) {
            @Override
            protected boolean isMatching(JTabbedPane panel) {
                return "tabs".equals(panel.getName());
            }
        });
        tabsPanelFixture.requireTabTitles(IOPanel.PULL_TAB_LABEL, IOPanel.PUSH_TAB_LABEL);
        tabsPanelFixture.selectTab(IOPanel.PUSH_TAB_LABEL);
        JButtonFixture uploadButtonFixture = frame.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return "upload_button".equals(button.getName());
            }
        });
        JTextComponentFixture pathFieldFixture = frame.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override
            protected boolean isMatching(JTextField textField) {
                if ("path_text".equals(textField.getName()) && textField.isShowing()) {      // isShowing() should be true because multiple instances of path chooser is initialized across different tabs
                    return true;
                } else {
                    return false;
                }
            }
        });
        pathFieldFixture.setText(absolutePathOfTestFiles + "dataToUpload");
        uploadButtonFixture.click();
        JProgressBarFixture progressBarFixture = frame.progressBar(new GenericTypeMatcher<JProgressBar>(JProgressBar.class) {
            @Override
            protected boolean isMatching(JProgressBar progressBar) {
                return "progress_bar".equals(progressBar.getName());
            }
        });
        Pause.pause(UPLOAD_TIMEOUT);
        progressBarFixture.requireText(SuitcaseProgressBar.PB_DONE);            // After Upload completes progress bar should display "Done!" .if there is an error "Error" is displayed
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

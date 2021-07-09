package org.opendatakit.suitcase.test;

import com.github.caciocavallosilano.cacio.ctc.junit.CacioTestRunner;
import org.assertj.swing.core.ComponentLookupScope;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.testing.AssertJSwingTestCaseTemplate;
import org.assertj.swing.timing.Pause;
import org.junit.*;
import org.junit.runner.RunWith;
import org.opendatakit.suitcase.Suitcase;

import javax.swing.*;
import java.awt.*;

import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

@RunWith(CacioTestRunner.class)
public class LoginGUITest extends AssertJSwingTestCaseTemplate {

    private FrameFixture frame;
    private static final int LOGIN_TIMEOUT = 15000;
    private String serverUrl = null;
    private String appId = null;
    private String userName = null;
    private String password = null;
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

        this.frame = findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            protected boolean isMatching(Frame frame) {
                return "org.opendatakit.suitcase.Suitcase".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());
        this.frame.show();
    }

    @Test
    public void loginTest() {
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
     ioPanelFixture.requireNotVisible();          // IOPanel should not be visible on starting the application
     frame.panel("main_panel").requireVisible();  // MainPanel should be visible on start of the application
     frame.button("login_button").click();
     Pause.pause(LOGIN_TIMEOUT);                          // Wait for 15 seconds for login to complete
     ioPanelFixture.requireVisible();             // IOPanel should be visible after login
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
package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.utils.ButtonState;

import javax.swing.*;
import java.awt.*;

public class IOPanel extends JPanel {
  public static final String PULL_TAB_LABEL = "Download";
  public static final String PUSH_TAB_LABEL = "Upload";

  private MainPanel parent;
  private PullPanel pullPanel;
  private PushPanel pushPanel;

  public IOPanel(MainPanel parent) {
    super(new GridBagLayout());

    this.parent = parent;

    JTabbedPane tabs = new JTabbedPane();
    pullPanel = new PullPanel(this);
    pushPanel = new PushPanel(this);
    tabs.addTab(PULL_TAB_LABEL, pullPanel);
    tabs.addTab(PUSH_TAB_LABEL, pushPanel);

    JSplitPane splitPane =
            new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, parent.getProgressBar());
    splitPane.setResizeWeight(0.8);
    splitPane.setDividerSize(0);
    splitPane.setEnabled(false);

    this.add(splitPane, LayoutDefault.getDefaultGbc());
  }

  public void setButtonsState(ButtonState pushButtonState, ButtonState pullButtonState, ButtonState resetButtonState, ButtonState refreshButtonState)
  {
    pushPanel.setButtonsState(pushButtonState,resetButtonState);
    pullPanel.setButtonsState(pullButtonState,refreshButtonState);
  }

  public CloudEndpointInfo getCloudEndpointInfo() {
    return parent.getCloudEndpointInfo();
  }

  public SuitcaseProgressBar getProgressBar() {
    return parent.getProgressBar();
  }

  public PullPanel getPullPanel() {
    return pullPanel;
  }

}

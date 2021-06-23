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

    UIManager.put("TabbedPane.selected", LayoutConsts.BUTTON_BACKGROUND_COLOR);
    JTabbedPane tabs = new JTabbedPane(){
      public Color getForegroundAt(int index){
        if(getSelectedIndex() == index){
        return Color.WHITE;
        }
        else {
          return Color.BLACK;
        }
      }
    };
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

  public void disableAllButtons(){
    pushPanel.setButtonsState(ButtonState.DISABLED,ButtonState.DISABLED);
    pullPanel.setButtonsState(ButtonState.DISABLED,ButtonState.DISABLED,ButtonState.DISABLED,ButtonState.DISABLED,ButtonState.DISABLED);
  }

  public void enableAllButtons(){
    pushPanel.setButtonsState(ButtonState.ENABLED,ButtonState.ENABLED);
    pullPanel.setButtonsState(ButtonState.ENABLED,ButtonState.ENABLED,ButtonState.ENABLED,ButtonState.ENABLED,ButtonState.ENABLED);
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

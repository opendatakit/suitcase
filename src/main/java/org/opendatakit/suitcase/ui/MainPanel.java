package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.model.CloudEndpointInfo;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
  private SuitcaseProgressBar progressBar;
  private LoginPanel loginPanel;
  private IOPanel ioPanel;

  public MainPanel() {
    super(new CardLayout());

    this.progressBar = new SuitcaseProgressBar();
    this.loginPanel = new LoginPanel(this);
    this.ioPanel = new IOPanel(this);

    this.add(loginPanel);
    this.add(ioPanel);
    loginPanel.loginFromPreferences();
  }

  public CloudEndpointInfo getCloudEndpointInfo() {
    return loginPanel.getCloudEndpointInfo();
  }

  public SuitcaseProgressBar getProgressBar() {
    return progressBar;
  }

  public IOPanel getIoPanel() {
      return ioPanel;
  }
}

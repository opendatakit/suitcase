package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.model.CloudEndpointInfo;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
  private SuitcaseProgressBar progressBar;
  private LoginPanel loginPanel;

  public MainPanel() {
    super(new CardLayout());

    this.setName("main_panel");
    this.progressBar = new SuitcaseProgressBar();
    this.progressBar.setName("progress_bar");
    this.loginPanel = new LoginPanel(this);

    this.add(loginPanel);
    this.add(new IOPanel(this));
  }

  public CloudEndpointInfo getCloudEndpointInfo() {
    return loginPanel.getCloudEndpointInfo();
  }

  public SuitcaseProgressBar getProgressBar() {
    return progressBar;
  }
}

package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.net.ResetTask;
import org.opendatakit.suitcase.net.SuitcaseSwingWorker;
import org.opendatakit.suitcase.net.UploadTask;
import org.opendatakit.suitcase.utils.ButtonState;
import org.opendatakit.suitcase.utils.FieldsValidatorUtils;
import org.opendatakit.suitcase.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PushPanel extends JPanel implements PropertyChangeListener {
  private static final String PUSH_LABEL = "Upload";
  private static final String PUSHING_LABEL = "Uploading";
  private static final String RESET_LABEL = "Reset";
  private static final String RESETTING_LABEL = "Resetting";
  private static final String DATA_PATH_LABEL = "Upload";
  private static final String FILE_CHOOSER_LABEL = "Select";

  private JTextField sVersionPushText;
  private JButton sPushButton;
  private JButton sResetButton;
  private PathChooserPanel dataPathChooser;

  private IOPanel parent;

  public PushPanel(IOPanel parent) {
    super(new GridBagLayout());

    this.parent = parent;

    this.sVersionPushText = new JTextField(1);
    this.sPushButton = new JButton();
    this.sResetButton = new JButton();
    this.dataPathChooser = new PathChooserPanel(
            DATA_PATH_LABEL, FILE_CHOOSER_LABEL ,FileUtils.getDefaultUploadPath().toString()
    );

    GridBagConstraints gbc = LayoutDefault.getDefaultGbc();
    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;

    JPanel pushInputPanel = new InputPanel(
            new String[] {"Version"},
            new JTextField[] {sVersionPushText},
            new String[] {"2"}
    );
    gbc.weighty = 2;
    this.add(pushInputPanel, gbc);

    // Will add upload options in the future, adding these now makes layout easier (a lot easier)
    JCheckBox placeholderCheckbox = new JCheckBox();
    placeholderCheckbox.setVisible(false);
    JCheckBox placeholderCheckbox2 = new JCheckBox();
    placeholderCheckbox2.setVisible(false);
    JPanel pushPrefPanel = new CheckboxPanel(
            new String[] {"Option Placeholder", "Option Placeholder 2"},
            new JCheckBox[] {placeholderCheckbox, placeholderCheckbox2},
            2, 1
    );
    gbc.weighty = 5;
    this.add(pushPrefPanel, gbc);

    gbc.weighty = 1;
    this.add(dataPathChooser, gbc);

    JPanel pushButtonPanel = new JPanel(new GridBagLayout());
    buildPushButtonArea(pushButtonPanel);
    gbc.insets = new Insets(10, -100, 0, 0);
    gbc.weighty = 2;
    this.add(pushButtonPanel, gbc);
  }

  private void buildPushButtonArea(JPanel pushButtonPanel) {
    GridBagConstraints gbc = LayoutDefault.getDefaultGbc();
    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridy = 0;

    sPushButton.setText(PUSH_LABEL);
    sPushButton.setName("upload_button");
    sPushButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sVersionPushText.setText(sVersionPushText.getText().trim());

        String error = FieldsValidatorUtils.checkUploadFields(sVersionPushText.getText(),
                dataPathChooser.getPath(), UploadTask.RESET_APP_OP);

        if (error != null) {
          DialogUtils.showError(error, true);
        } else {
          sPushButton.setText(PUSHING_LABEL);
          parent.setButtonsState(ButtonState.DISABLED, ButtonState.DISABLED, ButtonState.DISABLED, ButtonState.DISABLED);

          UploadTask worker = new UploadTask(parent.getCloudEndpointInfo(), dataPathChooser.getPath(),
                  sVersionPushText.getText(), true, null, null);
          worker.addPropertyChangeListener(parent.getProgressBar());
          worker.addPropertyChangeListener(PushPanel.this);
          worker.execute();
        }
      }
    });

    sResetButton.setText(RESET_LABEL);
    sResetButton.setName("reset_button");
    sResetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sVersionPushText.setText(sVersionPushText.getText().trim());

        String error = FieldsValidatorUtils.checkResetFields(sVersionPushText.getText());

        if (error != null) {
          DialogUtils.showError(error, true);
        } else {

          if(DialogUtils.promptConfirm("Are you sure you want to RESET? "
                  + "This will delete ALL your data on the server?", true, false)) {
            sResetButton.setText(RESETTING_LABEL);
            parent.setButtonsState(ButtonState.DISABLED, ButtonState.DISABLED , ButtonState.DISABLED, ButtonState.DISABLED);

            ResetTask worker = new ResetTask(sVersionPushText.getText(), true);
            worker.addPropertyChangeListener(parent.getProgressBar());
            worker.addPropertyChangeListener(PushPanel.this);
            worker.execute();
          }

        }
      }
    });

    pushButtonPanel.add(sResetButton, gbc);
    pushButtonPanel.add(sPushButton, gbc);
  }

  public void setButtonsState(ButtonState pushButtonState , ButtonState resetButtonState) {
  sPushButton.setEnabled(pushButtonState.getButtonStateBooleanValue());
  sResetButton.setEnabled(resetButtonState.getButtonStateBooleanValue());
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getNewValue() != null && evt.getPropertyName().equals(SuitcaseSwingWorker.DONE_PROPERTY)) {
      parent.setButtonsState(ButtonState.ENABLED, ButtonState.ENABLED, ButtonState.ENABLED, ButtonState.ENABLED);
      sPushButton.setText(PUSH_LABEL);
      sResetButton.setText(RESET_LABEL);
    }
  }
}

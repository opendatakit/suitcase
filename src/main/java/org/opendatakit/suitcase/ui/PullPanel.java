package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.model.CsvConfig;
import org.opendatakit.suitcase.model.ODKCsv;
import org.opendatakit.suitcase.net.*;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.utils.ButtonState;
import org.opendatakit.suitcase.utils.FieldsValidatorUtils;
import org.opendatakit.suitcase.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PullPanel extends JPanel implements PropertyChangeListener {
    private static final String DOWNLOAD_LABEL = "Download";
    private static final String REFRESH_LABEL = "Refresh Tables List Metadata";
    private static final String DOWNLOADING_LABEL = "Downloading";
    private static final String SAVE_PATH_LABEL = "Save to";
    private static final String FILE_CHOOSER_LABEL = "Save";

    // ui components
    private JCheckBox sDownloadAttachment;
    private JCheckBox sApplyScanFmt;
    private JCheckBox sExtraMetadata;
    private JButton sPullButton;
    private JButton sRefreshButton;
    private JTextField sTableIdText;
    private PathChooserPanel savePathChooser;

    // other instance vars
    private IOPanel parent;
    private AttachmentManager attachMngr;
    private ODKCsv csv;

    public PullPanel(IOPanel parent) {
        super(new GridBagLayout());

        this.parent = parent;
        this.attachMngr = null;
        this.csv = null;

        this.sDownloadAttachment = new JCheckBox();
        this.sApplyScanFmt = new JCheckBox();
        this.sExtraMetadata = new JCheckBox();
        this.sPullButton = new JButton();
        this.sRefreshButton = new JButton();
        this.sTableIdText = new JTextField(1);
        this.savePathChooser = new PathChooserPanel(
                SAVE_PATH_LABEL,FILE_CHOOSER_LABEL ,FileUtils.getDefaultSavePath().toString()
        );

        this.sTableIdText.setName("table_id_text");
        GridBagConstraints gbc = LayoutDefault.getDefaultGbc();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        JPanel pullInputPanel = new InputPanel(
                new String[]{"Table ID"},
                new JTextField[]{sTableIdText},
                new String[]{"table_id"}
        );
        gbc.weighty = 2;
        this.add(pullInputPanel, gbc);

        JPanel pullPrefPanel = new CheckboxPanel(
                new String[]{"Download attachments?", "Apply Scan formatting?", "Extra metadata columns?"},
                new JCheckBox[]{sDownloadAttachment, sApplyScanFmt, sExtraMetadata}, 3, 1
        );
        gbc.weighty = 5;
        this.add(pullPrefPanel, gbc);

        gbc.weighty = 1;
        this.add(this.savePathChooser, gbc);

        JPanel pullButtonPanel = new JPanel(new GridLayout(1, 1));
        buildPullButtonArea(pullButtonPanel);
        gbc.weighty = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(pullButtonPanel, gbc);
    }

    private void buildPullButtonArea(JPanel pullButtonPanel) {
        sPullButton.setText(DOWNLOAD_LABEL);
        sRefreshButton.setText(REFRESH_LABEL);
        sRefreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setButtonsState(ButtonState.DISABLED,ButtonState.DISABLED,ButtonState.DISABLED,ButtonState.DISABLED);
                RefreshTask worker = new RefreshTask();
                worker.addPropertyChangeListener(parent.getProgressBar());
                worker.addPropertyChangeListener(PullPanel.this);
                worker.execute();
            }
        });
        sPullButton.setName("download_button");
        sPullButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sTableIdText.setText(sTableIdText.getText().trim());

                String error = FieldsValidatorUtils.checkDownloadFields(
                        sTableIdText.getText(), savePathChooser.getPath(), parent.getCloudEndpointInfo());

                if (error != null) {
                    DialogUtils.showError(error, true);
                } else {
                    // disable download button
                    parent.setButtonsState(ButtonState.DISABLED, ButtonState.DISABLED, ButtonState.DISABLED,ButtonState.DISABLED);

                    sPullButton.setText(DOWNLOADING_LABEL);

                    CsvConfig config = new CsvConfig(sDownloadAttachment.isSelected(), sApplyScanFmt.isSelected(), sExtraMetadata.isSelected());

                    if (attachMngr == null) {
                        attachMngr = new AttachmentManager(parent.getCloudEndpointInfo(), sTableIdText.getText(),
                                savePathChooser.getPath());
                    } else {
                        attachMngr.setSavePath(savePathChooser.getPath());
                    }

                    // create a new csv instance when csv == null or when table id changed
                    if (csv == null || !csv.getTableId().equals(sTableIdText.getText())) {
                        try {
                            csv = new ODKCsv(attachMngr, parent.getCloudEndpointInfo(), sTableIdText.getText());
                        } catch (JSONException e1) { /*should never happen*/ }
                    }

                    DownloadTask worker = new DownloadTask(parent.getCloudEndpointInfo(), csv, config, savePathChooser.getPath(), true);
                    worker.addPropertyChangeListener(parent.getProgressBar());
                    worker.addPropertyChangeListener(PullPanel.this);
                    worker.execute();
                }
            }
        });
        pullButtonPanel.add(sRefreshButton);
        pullButtonPanel.add(sPullButton);
    }

    public void setButtonsState(ButtonState pullButtonState,ButtonState refreshButtonState) {
        sPullButton.setEnabled(pullButtonState.getButtonStateBooleanValue());
        sRefreshButton.setEnabled(refreshButtonState.getButtonStateBooleanValue());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() != null && evt.getPropertyName().equals(SuitcaseSwingWorker.DONE_PROPERTY)) {
            // re-enable download button and restore its label
            sPullButton.setText(DOWNLOAD_LABEL);
            parent.setButtonsState(ButtonState.ENABLED, ButtonState.ENABLED, ButtonState.ENABLED,ButtonState.ENABLED);
        }
    }
}

package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.model.CsvConfig;
import org.opendatakit.suitcase.model.ODKCsv;
import org.opendatakit.suitcase.net.*;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.utils.ButtonAction;
import org.opendatakit.suitcase.utils.ButtonState;
import org.opendatakit.suitcase.utils.FieldsValidatorUtils;
import org.opendatakit.suitcase.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.opendatakit.suitcase.ui.MessageString.getOverwriteCsvString;

public class PullPanel extends JPanel implements PropertyChangeListener {
    private static final String DOWNLOAD_LABEL = "Download";
    private static final String REFRESH_LABEL = "Refresh Tables List Metadata";
    private static final String ADD_ALL_LABEL = "Add all";
    private static final String DOWNLOADING_LABEL = "Downloading";
    private static final String SAVE_PATH_LABEL = "Save to";
    private static final String FILE_CHOOSER_LABEL = "Save";

    // ui components
    private JCheckBox sDownloadAttachment;
    private JCheckBox sApplyScanFmt;
    private JCheckBox sExtraMetadata;
    private JButton sPullButton;
    private JButton sRefreshButton;
    private JButton sAddAllButton;
    private PathChooserPanel savePathChooser;
    private JComboBox<String> sTableIdDropdown;
    private SelectedTablesListPanel selectedTablesListPanel;
    private DropdownPanel pullDropdown;

    // other instance vars
    private IOPanel parent;
    private MutableComboBoxModel<String> comboBoxModel;
    private AttachmentManager attachMngr;
    private ODKCsv csv;
    private List<String> selectedTableIds;
    private Dimension tableIdsScrollPaneDimension;

    public PullPanel(IOPanel parent) {
        super(new GridBagLayout());

        this.parent = parent;
        this.attachMngr = null;
        this.csv = null;
        this.selectedTableIds = new ArrayList<>();
        this.sDownloadAttachment = new JCheckBox();
        this.sApplyScanFmt = new JCheckBox();
        this.sExtraMetadata = new JCheckBox();
        this.sPullButton = new JButton();
        this.sRefreshButton = new JButton();
        this.sAddAllButton = new JButton();
        this.sTableIdDropdown = new JComboBox<>();
        this.savePathChooser = new PathChooserPanel(
                SAVE_PATH_LABEL,FILE_CHOOSER_LABEL ,FileUtils.getDefaultSavePath().toString()
        );
        this.tableIdsScrollPaneDimension = new Dimension(350,100);
        this.sRefreshButton.setBackground(LayoutConsts.BUTTON_BACKGROUND_COLOR);
        this.sRefreshButton.setForeground(LayoutConsts.BUTTON_FOREGROUND_COLOR);
        this.sAddAllButton.setBackground(LayoutConsts.BUTTON_BACKGROUND_COLOR);
        this.sAddAllButton.setForeground(LayoutConsts.BUTTON_FOREGROUND_COLOR);
        this.sPullButton.setForeground(LayoutConsts.BUTTON_FOREGROUND_COLOR);
        this.sPullButton.setBackground(LayoutConsts.BUTTON_BACKGROUND_COLOR);
        GridBagConstraints gbc = LayoutDefault.getDefaultGbc();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        comboBoxModel = new DefaultComboBoxModel<>();
        sTableIdDropdown.setModel(comboBoxModel);
        pullDropdown = new DropdownPanel(
                "Select Table ID",
                sTableIdDropdown,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String action = ((JButton) e.getSource()).getActionCommand();
                        if (ButtonAction.ADD.getStringValueOfAction().equals(action)&&comboBoxModel.getSelectedItem()!=null) {
                            addTableId((String)comboBoxModel.getSelectedItem());
                        }
                    }
                }

        );
        gbc.weighty = 1;
        this.add(pullDropdown, gbc);
        selectedTablesListPanel = new SelectedTablesListPanel(e -> {         //Action listener for remove button
            removeSelectedTableId(((RemoveButton)e.getSource()).getTableId());
            }
        );
        JPanel tableOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,15,0));

        buildTableOptionsArea(tableOptionsPanel,gbc);
        buildSelectedTableIdsArea(selectedTablesListPanel,gbc);

        JPanel pullPrefPanel = new CheckboxPanel(
                new String[]{"Download attachments?", "Apply Scan formatting?", "Extra metadata columns?"},
                new JCheckBox[]{sDownloadAttachment, sApplyScanFmt, sExtraMetadata}, 1, 3
        );
        gbc.weighty = 3;

        gbc.insets = new Insets(0,0,0,0);
        this.add(pullPrefPanel, gbc);
        gbc.insets = new Insets(0,0,0,20);
        gbc.weighty = 1;
        this.add(this.savePathChooser, gbc);

        JPanel pullButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buildPullButtonArea(pullButtonPanel);
        gbc.weighty = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(pullButtonPanel, gbc);
    }

    private void buildSelectedTableIdsArea(SelectedTablesListPanel selectedTablesListPanel,GridBagConstraints gridBagConstraints){

        gridBagConstraints.weighty = 1;
        JPanel labelPanel = new JPanel(new GridLayout(1, 1));

        labelPanel.setBorder(BorderFactory.createEmptyBorder());
        labelPanel.add(new JLabel("Selected Table Ids for download"));
        gridBagConstraints.insets = new Insets(0,15,0,20);
        this.add(labelPanel,gridBagConstraints);

        JScrollPane tableIdsScrollPane = new JScrollPane(selectedTablesListPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,   //Make JScrollPane scroll only vertically
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tableIdsScrollPane.setMinimumSize(tableIdsScrollPaneDimension);
        tableIdsScrollPane.setPreferredSize(tableIdsScrollPaneDimension);
        tableIdsScrollPane.setMaximumSize(tableIdsScrollPaneDimension);
        tableIdsScrollPane.setSize(tableIdsScrollPaneDimension);
        tableIdsScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        tableIdsScrollPane.getVerticalScrollBar().setBackground(Color.WHITE);
        gridBagConstraints.weighty = 2;
        this.add(tableIdsScrollPane,gridBagConstraints);
    }

    private void buildTableOptionsArea(JPanel tableOptionsPanel,GridBagConstraints gbc){
        sRefreshButton.setText(REFRESH_LABEL);
        sRefreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.disableAllButtons();
                RefreshTask worker = new RefreshTask();
                worker.addPropertyChangeListener(parent.getProgressBar());
                worker.addPropertyChangeListener(PullPanel.this);
                worker.execute();
            }
        });
        tableOptionsPanel.add(sRefreshButton);
        gbc.weighty = 1;
        sAddAllButton.setText(ADD_ALL_LABEL);
        sAddAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((DefaultComboBoxModel)comboBoxModel).removeAllElements();
                selectedTablesListPanel.removeAllTableIds(selectedTableIds.size());
                selectedTableIds.clear();
                for(String s:parent.getCloudEndpointInfo().getAllTableId()){
                    addTableId(s);
                }
            }
        });
        tableOptionsPanel.add(sAddAllButton);
        this.add(tableOptionsPanel,gbc);
    }

    private void buildPullButtonArea(JPanel pullButtonPanel) {
        sPullButton.setText(DOWNLOAD_LABEL);
        sPullButton.setPreferredSize(LayoutConsts.DEFAULT_BUTTON_DIMENSION);
        sPullButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String error = FieldsValidatorUtils.checkDownloadFields(
                        selectedTableIds, savePathChooser.getPath(), parent.getCloudEndpointInfo());

                if (error != null) {
                    DialogUtils.showError(error, true);
                } else {
                    // disable download button
                    parent.disableAllButtons();

                    sPullButton.setText(DOWNLOADING_LABEL);

                    CsvConfig config = new CsvConfig(sDownloadAttachment.isSelected(), sApplyScanFmt.isSelected(), sExtraMetadata.isSelected());
                    List<String> alreadyDownloadedTableIds = new ArrayList<>();
                    for (String tableId:selectedTableIds){
                        if (FileUtils.isDownloaded(parent.getCloudEndpointInfo(), tableId, config, savePathChooser.getPath())){
                            alreadyDownloadedTableIds.add(tableId);
                        }
                    }

                    if(alreadyDownloadedTableIds.size()!=0){
                        if (DialogUtils.promptConfirm(getOverwriteCsvString(alreadyDownloadedTableIds), true, false)) {
                            for(String tableId:alreadyDownloadedTableIds){
                                try {
                                    FileUtils.deleteCsv(parent.getCloudEndpointInfo(), config, tableId, savePathChooser.getPath()); // Delete existing csv if user selects "yes" to overwrite
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        else {
                            for(String tableId:alreadyDownloadedTableIds){
                                removeSelectedTableId(tableId);                                             // Remove from list of table ids to download if user selects "no" to overwrite.
                            }
                        }
                    }

                    DownloadTask worker = new DownloadTask(parent.getCloudEndpointInfo(), selectedTableIds, config, savePathChooser.getPath(), true);
                    worker.addPropertyChangeListener(parent.getProgressBar());
                    worker.addPropertyChangeListener(PullPanel.this);
                    worker.execute();

                }
            }
        });
        pullButtonPanel.add(sPullButton);
    }

    public void setButtonsState(ButtonState pullButtonState,ButtonState refreshButtonState,ButtonState addButtonState,ButtonState removeButtonState,ButtonState addAllButton) {
        sPullButton.setEnabled(pullButtonState.getButtonStateBooleanValue());
        sRefreshButton.setEnabled(refreshButtonState.getButtonStateBooleanValue());
        sAddAllButton.setEnabled(addAllButton.getButtonStateBooleanValue());
        pullDropdown.setButtonsState(addButtonState);
        selectedTablesListPanel.setRemoveButtonState(removeButtonState);
    }

    private void removeSelectedTableId(String tableId) {
        for(int i=0;i<selectedTableIds.size();i++) {
            if(selectedTableIds.get(i).equals(tableId))    //finds the selected table id in the list
            {
                selectedTablesListPanel.removeTableId(i,selectedTableIds.size()-1);                         // removes the component from UI
                comboBoxModel.addElement(selectedTableIds.get(i));         // Add element back to dropdown
                selectedTableIds.remove(i);                                // remove from the list of selected table ids
                break;
            }

        }
    }

    private void addTableId(String tableId){
        selectedTableIds.add(tableId);
        selectedTablesListPanel.addNewTableId(tableId,selectedTableIds.size());
        comboBoxModel.removeElement(comboBoxModel.getSelectedItem());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() != null && evt.getPropertyName().equals(SuitcaseSwingWorker.DONE_PROPERTY)) {
            // re-enable download button and restore its label
            sPullButton.setText(DOWNLOAD_LABEL);
            parent.enableAllButtons();
            ((DefaultComboBoxModel)comboBoxModel).removeAllElements();
            final Set<String> allTableIds = parent.getCloudEndpointInfo().getAllTableId();
            for (String s : allTableIds) {
                if(!selectedTableIds.contains(s)){
                    comboBoxModel.addElement(s);
                }
            }
        }
    }
}

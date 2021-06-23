package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.utils.ButtonAction;
import org.opendatakit.suitcase.utils.ButtonState;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionListener;

public class DropdownPanel extends JPanel{
    private static final String ADD_BUTTON_LABEL = "Add";

    public static class ComboBoxUI extends BasicComboBoxUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
        }

        @Override
        protected JButton createArrowButton() {
            final Color triangle = Color.WHITE;
            final JButton button = new BasicArrowButton(BasicArrowButton.SOUTH, LayoutConsts.BUTTON_BACKGROUND_COLOR, LayoutConsts.BUTTON_BACKGROUND_COLOR, triangle, LayoutConsts.BUTTON_BACKGROUND_COLOR);
            button.setName("ComboBox.arrowButton"); // Mandatory as per BasicComboBoxUI
            return button;
        }

    }

    private JButton addButton;

    public DropdownPanel(String label, JComboBox<String> dropdown, ActionListener addActionListener) {
        super(new FlowLayout(FlowLayout.LEFT));  // flow layout with left alignment of elements
        buildLabelPanel(label);
        buildDropDown(dropdown,addActionListener,new ComboBoxUI());

    }

    // build label on the left of the dropdown
    private void buildLabelPanel(String label) {
        JPanel labelPanel = new JPanel(new GridLayout(1, 1));

        labelPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,20));

        this.add(labelPanel);
        labelPanel.add(new JLabel(label));
    }

    // build the drop down of table ids with an add button
    private void buildDropDown(JComboBox<String> dropdown,ActionListener addActionListener,BasicComboBoxUI comboBoxUI) {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        inputPanel.add(dropdown);
        addButton = new JButton();
        dropdown.setMaximumSize(new Dimension(200,25));
        dropdown.setMinimumSize(new Dimension(150,25));
        dropdown.setPreferredSize(new Dimension(200,25));
        dropdown.setUI(comboBoxUI);
        dropdown.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addButton.setActionCommand(ButtonAction.ADD.getStringValueOfAction());
        addButton.setText(ADD_BUTTON_LABEL);
        addButton.addActionListener(addActionListener);
        addButton.setBackground(LayoutConsts.BUTTON_BACKGROUND_COLOR);
        addButton.setForeground(LayoutConsts.BUTTON_FOREGROUND_COLOR);
        addButton.setSize(LayoutConsts.ADD_AND_REMOVE_BUTTON_DIMENSION);
        inputPanel.add(addButton);

        this.add(inputPanel);
    }

    public void setButtonsState(ButtonState addButtonState) {
        addButton.setEnabled(addButtonState.getButtonStateBooleanValue());
    }
}



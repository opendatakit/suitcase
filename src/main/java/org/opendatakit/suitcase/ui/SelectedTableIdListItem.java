package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.utils.ButtonState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SelectedTableIdListItem extends JPanel {
    private JButton removeButton;

    SelectedTableIdListItem(String tableId,ActionListener removeActionListener){
        super(new FlowLayout(FlowLayout.LEFT));
        this.setBackground(Color.WHITE);
        JLabel label = new JLabel();                                    // JLabel containing the tableId
        label.setPreferredSize(new Dimension(200, 25));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        label.setText(tableId);
        this.add(label);
        removeButton = new RemoveButton(tableId);          // Remove Button on the left side of JLabel
        removeButton.addActionListener(removeActionListener);
        this.add(removeButton);
    }

    public void setRemoveButtonState(ButtonState removeButtonState)
    {
        removeButton.setEnabled(removeButtonState.getButtonStateBooleanValue());
    }
}

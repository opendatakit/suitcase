package org.opendatakit.suitcase.ui;

import org.opendatakit.suitcase.utils.ButtonState;

import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

public class SelectedTablesListPanel extends JPanel implements PropertyChangeListener {
    private final ActionListener removeActionListener;
    private int totalSelectedTableIds;

    SelectedTablesListPanel(ActionListener removeActionListener) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.setSize(new Dimension(400, 100));
        this.setMaximumSize(new Dimension(400, 1000));
        this.setPreferredSize(new Dimension(400, 150));
        this.setMinimumSize(new Dimension(400,150));
        this.setBackground(Color.WHITE);
        this.removeActionListener = removeActionListener;
        this.totalSelectedTableIds=0;
    }

    public void addNewTableId(String tableId,int totalSelectedTableIds) {      // Method to add new Table Id in UI.

        updateSize(totalSelectedTableIds);
        SelectedTableIdListItem selectedTableIdListItem = new SelectedTableIdListItem(tableId,removeActionListener);
        this.add(selectedTableIdListItem);
        this.revalidate();
        this.repaint();
    }

    public void removeTableId(int index,int totalSelectedTableIds){
        this.remove(index);
        this.revalidate();                              // Call to revalidate and repaint the UI
        this.repaint();
        updateSize(totalSelectedTableIds);
    }

    public void removeAllTableIds(int totalSelectedTableIds){
        for(int i=0;i<totalSelectedTableIds;i++)
        {
            this.removeTableId(0,totalSelectedTableIds);
        }
    }

    public void updateSize(int totalSelectedTableIds) {                                    // Method to update size of the panel inside the JScrollPane. Height increases with increasing number of elements.
        this.setPreferredSize(new Dimension(400, 25*totalSelectedTableIds));
        this.totalSelectedTableIds=totalSelectedTableIds;
    }

    public void setRemoveButtonState(ButtonState removeButtonState){
        for(int i=0;i<totalSelectedTableIds;i++)
        {
            ((SelectedTableIdListItem)this.getComponent(i)).setRemoveButtonState(removeButtonState);
        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}

package org.opendatakit.suitcase.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PathChooserPanel extends JPanel{
  private static final int LABEL_WEIGHT = 5;
  private static final int PATH_TEXT_WEIGHT = 90;
  private static final int BUTTON_WEIGHT = 5;

  private JLabel label;
  private String fileChooserLabel;
  private JTextField pathText;
  private JButton browseBtn;

  public PathChooserPanel(String label, String fileChooserLabel,final String defaultPath) {
    super(new GridBagLayout());

    GridBagConstraints gbc = LayoutDefault.getDefaultGbc();
    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridy = 0;

    this.label = new JLabel(label);
    this.fileChooserLabel = fileChooserLabel;
    this.label.setHorizontalAlignment(JLabel.CENTER);
    gbc.weightx = LABEL_WEIGHT;
    add(this.label, gbc);

    this.pathText = new JTextField(1);
    this.pathText.setText(defaultPath);
    this.pathText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    gbc.weightx = PATH_TEXT_WEIGHT;
    add(this.pathText, gbc);

    this.browseBtn = new JButton();
    this.browseBtn.setText("...");
    this.browseBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(defaultPath));

        int result = chooser.showDialog(null,fileChooserLabel);
        if (result == JFileChooser.APPROVE_OPTION) {
        	if(LayoutConsts.EMPTY_DIR == true) {
        		File file = chooser.getSelectedFile();
        		if(isDirectoryEmpty(file)) {
        			DialogUtils.showError(MessageString.DIRECTORY_EMPTY, true);
        		}
        	}
          else
          pathText.setText(chooser.getSelectedFile().toString());
        }
      }
    });
    gbc.weightx = BUTTON_WEIGHT;
    add(this.browseBtn, gbc);
  }

  public String getPath() {
    pathText.setText(sanitizePath(pathText.getText()));

    return pathText.getText();
  }

  private static String sanitizePath(String path) {
    return path.replaceAll("^\\s+", "");
  }
  public static boolean isDirectoryEmpty(File directory) {  
	    String[] files = directory.list();
	    return files.length == 0;  
	}
}
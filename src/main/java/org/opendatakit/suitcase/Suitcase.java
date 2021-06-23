package org.opendatakit.suitcase;

import org.opendatakit.suitcase.net.SuitcaseSwingWorker;
import org.opendatakit.suitcase.ui.MainPanel;
import org.opendatakit.suitcase.ui.SuitcaseCLI;

import javax.swing.*;
import java.awt.*;

import static org.opendatakit.suitcase.ui.LayoutConsts.WINDOW_HEIGHT;
import static org.opendatakit.suitcase.ui.LayoutConsts.WINDOW_WIDTH;

public class Suitcase {
  private static final String SUITCASE_TITLE = "Suitcase";
  public static void main(String[] args) {
    int retCode = SuitcaseSwingWorker.okCode;
    if (args.length > 0) {
      retCode = new SuitcaseCLI(args).startCLI();
      System.exit(retCode);
    } else {
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          JFrame frame = new JFrame(SUITCASE_TITLE);
          frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
          frame.setLocationRelativeTo(null);
          frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

          frame.add(new MainPanel());
          frame.setVisible(true);
        }
      });
    }
  }
}

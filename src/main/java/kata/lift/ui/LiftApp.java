package kata.lift.ui;

import javax.swing.*;
import java.awt.*;

import static java.util.Comparator.comparing;

public class LiftApp {
   public static void main(String[] args) {
      JFrame frame = new JFrame();
      frame.setTitle("Lift Simulator");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(480, 20 + (Canvas.FLOORS + 1) * 40 + 120);
      frame.setLocationRelativeTo(null);
      frame.getContentPane().setLayout(new BorderLayout());
      frame.getContentPane().add(new Canvas(), BorderLayout.CENTER);
      frame.setVisible(true);
   }
}

package kata.lift;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static kata.lift.Direction.DOWN;
import static kata.lift.Direction.UP;

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

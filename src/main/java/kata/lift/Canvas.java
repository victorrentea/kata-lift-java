package kata.lift;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static kata.lift.Direction.DOWN;
import static kata.lift.Direction.UP;

@Slf4j
public class Canvas extends JPanel {
   public static final int FLOORS = 11;
   private static final int LIFT_COUNT = 1;
   public static final int Y0 = 20 + (FLOORS + 1) * 40 + 1;

   List<LiftView> liftViews = new ArrayList<>();

   Canvas() {
      setBackground(Color.white);
      setLayout(null); // manual position elements

      ILiftController liftController = new LiftController(0);

//      for (int e = 0; e < LIFT_COUNT; e++) {
      int e = 0;

      int xElevator = 60 + e * 50;

      JLabel currentFloorLabel = createCurrentFloorLabel(xElevator);

      Map<Integer, JButton> internalButtons = createInternalButtons(xElevator);

      Map<Integer, Map<Direction, JButton>> externalButtons = createExternalButtons();

      LiftView liftView = new LiftView(currentFloorLabel, internalButtons, externalButtons, xElevator);
      liftViews.add(liftView);

      List<Integer> floorHeights = IntStream.range(0, FLOORS + 1).map(i -> i * 40).boxed().collect(toList());

      LiftSystem liftSystem = new LiftSystem(liftController, new LiftEngine(), liftView, floorHeights);

      // register button actions
      for (int f = 0; f <= FLOORS; f++) {
         int floor = f; // effective final sh*t

         liftView.getInternalButton(f).addActionListener(a -> liftSystem.callInternal(floor));
         liftView.getExternalButton(f, UP).addActionListener(a -> liftSystem.callExternal(floor, UP));
         liftView.getExternalButton(f, DOWN).addActionListener(a -> liftSystem.callExternal(floor, DOWN));
      }


      startTicker(liftSystem::tick);
//      }
   }

   private void startTicker(Runnable tick) {
      new Thread(() -> {
         while (true) {
            try {
               Thread.sleep(100);
            } catch (InterruptedException ex) {
               throw new RuntimeException(ex);
            }
            SwingUtilities.invokeLater(() -> {
               tick.run();
               repaint();
            });
         }
      }).start();
   }

   private Map<Integer, Map<Direction, JButton>> createExternalButtons() {
      Map<Integer, Map<Direction, JButton>> externalButtons = new HashMap<>();
      for (int i = FLOORS; i >= 0; i--) {
         int y = 20 + 40 * i;
         int floor = FLOORS - i;
         JLabel floorLabel = new JLabel(Call.translateFloor(floor), SwingConstants.CENTER);
         floorLabel.setFont(getFont().deriveFont(20.0f));
         floorLabel.setVerticalAlignment(SwingConstants.CENTER);
         floorLabel.setBounds(0, y, 30, 40);
         add(floorLabel);

         JButton upButton = createExternalButton(UP.symbol, y + 1);
         JButton downButton = createExternalButton(DOWN.symbol, y + 20);
         externalButtons.put(floor, Map.of(UP, upButton, DOWN, downButton));
      }
      externalButtons.get(0).get(DOWN).setVisible(false);
      externalButtons.get(FLOORS).get(UP).setVisible(false);
      return externalButtons;
   }

   private JLabel createCurrentFloorLabel(int xElevator) {
      JLabel currentFloorLabel = new JLabel("", SwingConstants.CENTER);
      currentFloorLabel.setForeground(Color.ORANGE);
      currentFloorLabel.setFont(new Font("Consolas", Font.PLAIN, 16));
      currentFloorLabel.setBounds(xElevator, 0, 50, 20);
      add(currentFloorLabel);
      return currentFloorLabel;
   }

   private Map<Integer, JButton> createInternalButtons(int xElevator) {
      Map<Integer, JButton> internalButtons = new HashMap<>();
      int h = (int) Math.ceil((FLOORS + 1) / 3d);
      int YMAX = Y0 + h * 16;
      int XMAX = xElevator + 3 * 16;
      for (int f = 0; f <= FLOORS; f++) {
         int x = XMAX - (f % 3 + 1) * 16;
         int y = YMAX - (f / 3 + 1) * 16;

         JButton button = new JButton(Call.translateFloor(f));
         button.setHorizontalAlignment(SwingConstants.CENTER);
         button.setFont(getFont().deriveFont(8f));
         button.setMargin(new Insets(0, 0, 0, 0));
         button.setSize(16, 16);
         button.setFocusPainted(false);
         button.setLocation(x, y);
         internalButtons.put(f, button);
         add(button);
      }
      return internalButtons;
   }

   private JButton createExternalButton(String icon, int y) {
      JButton button = new JButton(icon);
      button.setMargin(new Insets(0, 0, 0, 0));
      button.setSize(16, 18);
      button.setFocusPainted(false);
      button.setFont(getFont().deriveFont(10f));
      button.setLocation(30, y);
      add(button);
      return button;
   }


   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D graphics = (Graphics2D) g;

      for (LiftView liftView : liftViews) {
         liftView.drawLift(graphics);
      }

      graphics.setColor(Color.black);
      for (int i = 0; i < FLOORS + 2; i++) {
         int y = 20 + 40 * i;
         graphics.drawLine(5, y, 500, y);
      }

   }

}

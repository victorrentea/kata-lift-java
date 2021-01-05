package kata.lift.ui;

import kata.lift.Call;
import kata.lift.Direction;
import kata.lift.ui.Canvas;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class LiftView {
   private final JLabel currentFloorLabel;
   private final Map<Integer, JButton> internalButtons;
   private final Map<Integer, Map<Direction, JButton>> externalButtons;
   @Getter
   private final int x;
   @Setter
   private int h;
   @Setter
   private double fillRatio;

   LiftView(JLabel currentFloorLabel, Map<Integer, JButton> internalButtons, Map<Integer, Map<Direction, JButton>> externalButtons, int x) {
      this.currentFloorLabel = currentFloorLabel;
      this.internalButtons = internalButtons;
      this.externalButtons = externalButtons;
      this.x = x;
   }

   public void showCurrentFloor(int floor, Direction direction) {
      String label = " " + Call.translateFloor(floor);
      if (direction != null) {
         label += " " + direction.symbol;
      } else {
         label += "  ";
      }
      currentFloorLabel.setText(label);
   }

   public void showInternalCallFloors(List<Integer> callFloors) {
      for (Integer floor : internalButtons.keySet()) {
         Color color = callFloors.contains(floor) ? Color.orange : Color.white;
         internalButtons.get(floor).setBackground(color);
      }
   }

   public JButton getInternalButton(int floor) {
      return internalButtons.get(floor);
   }

   public void colorFloorButtons(List<Call> floorCalls) {
      for (Map<Direction, JButton> map : externalButtons.values()) {
         for (JButton button : map.values()) {
            button.setBackground(Color.white);
         }
      }
      for (Call call : floorCalls) {
         externalButtons.get(call.getFloor()).get(call.getDirection()).setBackground(Color.orange);
      }
   }

   public JButton getExternalButton(int floor, Direction direction) {
      return externalButtons.get(floor).get(direction);
   }

   public void drawLift(Graphics2D g2d) {
      int y = Canvas.Y0 - 40 - h;
      g2d.setColor(Color.ORANGE);
      int doorW = (int) (24 * fillRatio);
      g2d.fillRect(getX() + 1, y, doorW, 38);
      g2d.fillRect(getX() + 1 + 48 - doorW, y, doorW, 38);
   }
}

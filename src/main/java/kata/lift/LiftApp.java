package kata.lift;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import kata.lift.LiftController.Status;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

enum LiftEngineCommand {
   GO_UP, GO_DOWN, OPEN_DOORS
}

enum CallDirection {
   UP, DOWN
}

public class LiftApp extends JFrame {

   public static void main(String[] args) {

      JFrame frame = new LiftApp();
      frame.setTitle("Lift Simulator");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.setSize(480, 20 + (Canvas.FLOORS + 1) * 40 + 100);
      frame.setLocationRelativeTo(null);
      frame.getContentPane().setLayout(new BorderLayout());
      frame.getContentPane().add(new Canvas(), BorderLayout.CENTER);


      frame.setVisible(true);

   }
}

@Data
class LiftView {
   private final Map<Integer, JButton> internalFloorButtons = new HashMap<>();
   private final Map<Integer, Map<CallDirection, JButton>> floorButtons = new HashMap<>();
   private final JLabel upLabel;
   private final JLabel downLabel;
   private final int x;
   private int h;
   private double fillRatio;

   public void addFloorButtons(Integer floor, JButton upButton, JButton downButton) {
      floorButtons.put(floor, new HashMap<>());
      floorButtons.get(floor).put(CallDirection.UP, upButton);
      floorButtons.get(floor).put(CallDirection.DOWN, downButton);
   }

}

@Slf4j
class Canvas extends JPanel {
   public static final int FLOORS = 11;
   private static final int LIFT_COUNT = 1;
   int Y0 = 20 + (FLOORS + 1) * 40 + 1;

   List<LiftView> elevators = new ArrayList<>();

   Canvas() {
      setBackground(Color.white);

      setLayout(null);

      LiftController liftController = new LiftController(0);


//      for (int e = 0; e < LIFT_COUNT; e++) {
      int e = 0;

      int xElevator = 60 + e * 50;

      JLabel upLabel = new JLabel("▲", SwingConstants.CENTER);
      upLabel.setForeground(Color.ORANGE);
      upLabel.setBounds(xElevator, 0, 25, 20);
      upLabel.setVisible(false);
      add(upLabel);

      JLabel downLabel = new JLabel("▼", SwingConstants.CENTER);
      downLabel.setForeground(Color.ORANGE);
      downLabel.setBounds(xElevator + 25, 0, 25, 20);
      downLabel.setVisible(false);
      add(downLabel);

      LiftView liftView = new LiftView(upLabel, downLabel, xElevator);
      elevators.add(liftView);

      List<Integer> floorHeights = IntStream.range(0, FLOORS + 1).map(i -> i * 40).boxed().collect(toList());
      LiftSystem liftSystem = new LiftSystem(liftController, new LiftEngine(), liftView, floorHeights);


      for (int i = FLOORS; i >= 0; i--) {
         int y = 20 + 40 * i;
         int f = FLOORS - i;
         JLabel floorLabel = new JLabel(translateFloor(f), SwingConstants.CENTER);
         floorLabel.setFont(getFont().deriveFont(20.0f));
         floorLabel.setVerticalAlignment(SwingConstants.CENTER);
         floorLabel.setBounds(0, y, 30, 40);
         add(floorLabel);


         JButton upButton = createUpDownButton("▲", y + 1, () -> liftSystem.callFloor(f, CallDirection.UP));
         add(upButton);
         if (i==0) {
            upButton.setVisible(false);
         }
         JButton downButton = createUpDownButton("▼", y + 20, () -> liftSystem.callFloor(f, CallDirection.DOWN));
         add(downButton);
         if (i == FLOORS) {
            downButton.setVisible(false);
         }
         liftView.addFloorButtons(f, upButton, downButton);
      }


      // internal call buttons
      int h = (int) Math.ceil((FLOORS + 1) / 3d);
      int YMAX = Y0 + h * 16;
      int XMAX = xElevator + 3 * 16;
      for (int f = 0; f <= FLOORS; f++) {

         int x = XMAX - (f % 3 + 1) * 16;
         int y = YMAX - (f / 3 + 1) * 16;


         JButton button = new JButton(translateFloor(f));
         button.setHorizontalAlignment(SwingConstants.CENTER);
         button.setFont(getFont().deriveFont(8f));
         button.setMargin(new Insets(0, 0, 0, 0));
         button.setSize(16, 16);
         button.setFocusPainted(false);
         button.setLocation(x, y);
         int floor = f;
         button.addActionListener(a -> liftSystem.callInternal(floor));
         elevators.get(e).getInternalFloorButtons().put(f, button);
         add(button);
      }

      new Thread(() -> {
         while (true) {
            try {
               Thread.sleep(100);
            } catch (InterruptedException ex) {
               throw new RuntimeException(ex);
            }
            SwingUtilities.invokeLater(() -> {
               liftSystem.tick();
               repaint();
            });
         }
      }).start();
//      }
   }

   private String translateFloor(int f) {
      return f == 0 ? "P" : f + "";
   }

   private JButton createUpDownButton(String icon, int y, Runnable action) {
      JButton button = new JButton(icon);
      button.setMargin(new Insets(0, 0, 0, 0));
      button.setSize(16, 18);
      button.setFocusPainted(false);
      button.setLocation(30, y);
      button.addActionListener(a -> action.run());
      return button;
   }


   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;

      for (LiftView elevator : elevators) {
         int y = Y0 - 40 - elevator.getH();
         g2d.setColor(Color.ORANGE);


         int doorW = (int) (24 * elevator.getFillRatio());
         g2d.fillRect(elevator.getX() + 1, y, doorW, 40);

         g2d.fillRect(elevator.getX() + 1 + 48 - doorW, y, doorW, 40);

      }

      g2d.setColor(Color.black);
      for (int i = 0; i < FLOORS + 2; i++) {
         int y = 20 + 40 * i;
         g2d.drawLine(5, y, 500, y);
      }

   }
}

class LiftSystem {
   private final LiftController controller;
   private final LiftEngine engine;
   private final LiftView view;
   private final List<Integer> floorHeights;
   int prevFloor;

   LiftSystem(LiftController controller, LiftEngine engine, LiftView view, List<Integer> floorHeights) {
      this.controller = controller;
      this.engine = engine;
      this.view = view;
      this.floorHeights = floorHeights;
      prevFloor = controller.getCurrentFloor();
   }

   public void callFloor(int floor, CallDirection direction) {
      controller.call(new Call(floor, direction))
          .ifPresent(engine::process);

   }

   public void callInternal(int floor) {
      controller.call(new Call(floor))
          .ifPresent(engine::process);
   }

   public void tick() {
      engine.tick();
      if (engine.doorsJustClosed()) {
         controller.onDoorsClosed()
             .ifPresent(engine::process);
      }
      if (engine.getCommand() == LiftEngineCommand.GO_UP && engine.h >= floorHeights.get(prevFloor + 1)) {
         engine.h = floorHeights.get(prevFloor + 1);
         LiftEngineCommand command = controller.onFloor();
         engine.process(command);
         prevFloor++;
      }
      if (engine.getCommand() == LiftEngineCommand.GO_DOWN && engine.h <= floorHeights.get(prevFloor - 1)) {
         engine.h = floorHeights.get(prevFloor - 1);
         LiftEngineCommand command = controller.onFloor();
         engine.process(command);
         prevFloor--;
      }
      view.setH(engine.h);
      view.setFillRatio(engine.getDoorFillRatio());
      updateDirectionDisplay();
      colorInternalFloorButtons();
      colorFloorButtons();
   }

   private void colorFloorButtons() {
      for (Map<CallDirection, JButton> map : view.getFloorButtons().values()) {
         for (JButton button : map.values()) {
            button.setBackground(Color.white);
         }
      }
      controller.getNextCalls().stream()
          .filter(call -> call.getDirection() != null)
          .forEach(call -> {
             view.getFloorButtons().get(call.getFloor()).get(call.getDirection()).setBackground(Color.orange);
          });
   }

   private void colorInternalFloorButtons() {
      for (JButton button : view.getInternalFloorButtons().values()) {
         button.setBackground(Color.white);
      }
      for (Integer floor : controller.getNextCallFloors()) {
         view.getInternalFloorButtons().get(floor).setBackground(Color.orange);
      }
   }

   private void updateDirectionDisplay() {
      view.getDownLabel().setVisible(false);
      view.getUpLabel().setVisible(false);
      if (controller.getStatus() == Status.GOING_UP) {
         view.getUpLabel().setVisible(true);
      }
      if (controller.getStatus() == Status.GOING_DOWN) {
         view.getDownLabel().setVisible(true);
      }
   }
}

class LiftEngine {
   public static final int DOORS_OPEN_TICKS = 20;
   private static final double[] DOOR_FILL_RATIOS = new double[DOORS_OPEN_TICKS + 1];

   static {
      int third = DOORS_OPEN_TICKS / 3;
      for (int i = 0; i < DOOR_FILL_RATIOS.length; i++) {
         if (i < third) {
            DOOR_FILL_RATIOS[i] = 0.1 + 0.9 * (third - i) / third;
         } else if (i < 2 * third) {
            DOOR_FILL_RATIOS[i] = 0.1;
         } else {
            int j = i - 2 * third;
            DOOR_FILL_RATIOS[i] = Math.min(1, 1 - 0.9 * (third - j) / third);
         }
      }
//      System.out.println(Arrays.toString(DOOR_FILL_RATIOS));
   }

   int h;
   private LiftEngineCommand command;
   private Integer doorsOpenTicksLeft;

   public LiftEngineCommand getCommand() {
      return command;
   }

   public void process(LiftEngineCommand command) {
      this.command = command;
      if (command == LiftEngineCommand.OPEN_DOORS) {
         doorsOpenTicksLeft = DOORS_OPEN_TICKS;
      }
   }

   public void tick() {
      h += 5 * getMoveFactor();
      if (doorsOpenTicksLeft != null && doorsOpenTicksLeft == 0) {
         doorsOpenTicksLeft = null;
      }
      if (command == LiftEngineCommand.OPEN_DOORS) {
         if (doorsOpenTicksLeft != null) {
            doorsOpenTicksLeft--;
         }
      }
   }

   public double getDoorFillRatio() {
      if (doorsOpenTicksLeft == null) {
         return 1;
      }
      return DOOR_FILL_RATIOS[doorsOpenTicksLeft];
   }

   public boolean doorsJustClosed() {
      return doorsOpenTicksLeft != null && doorsOpenTicksLeft == 0;
   }

   private int getMoveFactor() {
      if (command == null) {
         return 0;
      }
      switch (command) {
         case GO_UP:
            return 1;
         case GO_DOWN:
            return -1;
         case OPEN_DOORS:
            return 0;

         default:
            throw new IllegalStateException("Unexpected value: " + command);
      }
   }
}


@Slf4j
class LiftController {

   private int currentFloor;
   private Status status = Status.STOPPED;
   private List<Call> nextCalls = new ArrayList<>();

   public LiftController(int currentFloor) {
      this.currentFloor = currentFloor;
   }

   static List<Call> reorderCalls(List<Call> calls, Status status, int currentFloor) {
//      if (status == Status.STOPPED) {
//         throw new IllegalArgumentException("Should be moving, otherwise it's the first call, no need to reorder");
//      }

      List<Call> callsAbove = calls.stream()
          .filter(call -> call.getFloor() > currentFloor)
          .collect(toList());

      List<Call> callsAboveUp = callsAbove.stream()
          .filter(call -> call.isUp() || call.isInternal())
          .sorted(comparing(Call::getFloor))
          .collect(toList());
      List<Call> callsAboveDown = callsAbove.stream()
          .filter(Call::isDown)
          .sorted(comparing(Call::getFloor).reversed())
          .collect(toList());

      List<Call> callsBelow = calls.stream()
          .filter(call -> call.getFloor() < currentFloor)
          .collect(toList());

      List<Call> callsBelowDown = callsBelow.stream()
          .filter(call -> call.isDown() || call.isInternal())
          .sorted(comparing(Call::getFloor).reversed())
          .collect(toList());
      List<Call> callsBelowUp = callsBelow.stream()
          .filter(Call::isUp)
          .sorted(comparing(Call::getFloor))
          .collect(toList());


      List<Call> result = new ArrayList<>();
      List<Call> currentFloorCalls = calls.stream()
          .filter(call -> call.getFloor() == currentFloor)
          .collect(toList());

      if (status == Status.GOING_UP) {
         result.addAll(callsAboveUp);
         result.addAll(callsAboveDown);
         result.addAll(currentFloorCalls);
         result.addAll(callsBelowDown);
         result.addAll(callsBelowUp);
      } else {
         result.addAll(callsBelowDown);
         result.addAll(callsBelowUp);
         result.addAll(currentFloorCalls);
         result.addAll(callsAboveUp);
         result.addAll(callsAboveDown);
      }
      log.info("Reordered {} into {}", calls, result);
      return result;
   }

   public int getCurrentFloor() {
      return currentFloor;
   }

   public Status getStatus() {
      return status;
   }

   public List<Call> getNextCalls() {
      return nextCalls;
   }

   public List<Integer> getNextCallFloors() {
      return nextCalls.stream().map(Call::getFloor).collect(toList());
   }

   public LiftEngineCommand onFloor() {
      if (status == Status.GOING_UP) {
         currentFloor++;
      } else {
         currentFloor--;
      }
      log.info("On Floor." + currentFloor + ". Next calls: " + nextCalls);

      if (nextCalls.get(0).getFloor() == currentFloor) {
         log.info("Opening doors");
         if (nextCalls.get(0).getDirection() != null) {
            status = Status.from(nextCalls.get(0).getDirection());
         } else {
            status = Status.STOPPED;
         }
         return LiftEngineCommand.OPEN_DOORS;
      }
      return getUpDownCommand();
   }

   public Optional<LiftEngineCommand> onDoorsClosed() {
      log.info("Doors closed");
      nextCalls.removeIf(call -> call.getFloor() == currentFloor && (call.isInternal() || status.canServe(call.getDirection())));
      log.info("Remaining calls: " + nextCalls);
      if (nextCalls.isEmpty()) {
         status = Status.STOPPED;
         return empty();
      }
      return of(getUpDownCommand());
   }

   public Optional<LiftEngineCommand> call(Call call) {
      log.info("Call " + call);
      if (nextCalls.contains(call)) {
         log.info("Ingoring duplicate call");
         return empty();
      }
      nextCalls.add(call);
      if (nextCalls.size() >= 2) {
         nextCalls = reorderCalls(nextCalls, status, currentFloor);
         return empty();
      }

      if (currentFloor == nextCalls.get(0).getFloor()) {
         status = nextCalls.get(0).getDirection() == CallDirection.UP ? Status.GOING_UP : Status.GOING_DOWN;
         return of(LiftEngineCommand.OPEN_DOORS);
      }
      return of(getUpDownCommand());
   }

   private LiftEngineCommand getUpDownCommand() {
      if (currentFloor == nextCalls.get(0).getFloor()) {
         status = Status.from(nextCalls.get(0).getDirection());
         return LiftEngineCommand.OPEN_DOORS;
      } if (currentFloor < nextCalls.get(0).getFloor()) {
         status = Status.GOING_UP;
         return LiftEngineCommand.GO_UP;
      } else {
         status = Status.GOING_DOWN;
         return LiftEngineCommand.GO_DOWN;
      }
   }

   enum Status {
      GOING_UP, GOING_DOWN, STOPPED;

      public static Status from(CallDirection direction) {
         switch (direction) {
            case UP:return GOING_UP;
            case DOWN:return GOING_DOWN;
            default:
               throw new IllegalStateException("Unexpected value: " + direction);
         }
      }

      public boolean canServe(CallDirection direction) {
         if (direction == CallDirection.DOWN && this == GOING_DOWN) {
            return true;
         }
         if (direction == CallDirection.UP && this == GOING_UP) {
            return true;
         }
         return this == STOPPED;
      }
   }
}
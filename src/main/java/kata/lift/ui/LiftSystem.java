package kata.lift.ui;

import kata.lift.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class LiftSystem {
   private final ILiftController controller;
   private final LiftEngine engine;
   private final LiftView view;
   private final List<Integer> floorHeights;
   int prevFloor;

   LiftSystem(ILiftController controller, LiftEngine engine, LiftView view, List<Integer> floorHeights) {
      this.controller = controller;
      this.engine = engine;
      this.view = view;
      this.floorHeights = floorHeights;
      prevFloor = controller.getCurrentFloor();
   }

   public void callExternal(int floor, Direction direction) {
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

      view.showCurrentFloor(controller.getCurrentFloor(), controller.getCurrentDirection().orElse(null));

      List<Integer> internalCalls = controller.getNextCalls().stream()
          .filter(Call::isInternal).map(Call::getFloor).collect(toList());
      view.showInternalCallFloors(internalCalls);

      List<Call> floorCalls = controller.getNextCalls().stream()
          .filter(Call::isExternal).collect(toList());
      view.colorFloorButtons(floorCalls);
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



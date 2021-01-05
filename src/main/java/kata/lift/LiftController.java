package kata.lift;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@Slf4j
public class LiftController implements ILiftController {
   private int currentFloor;

   private Status status = Status.STOPPED;

   private List<Call> nextCalls = new ArrayList<>();

   public LiftController(int currentFloor) {
      this.currentFloor = currentFloor;
   }

   static List<Call> reorderCalls(List<Call> calls, Status status, int currentFloor) {
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

   @Override
   public int getCurrentFloor() {
      return currentFloor;
   }

   @Override
   public Optional<Direction> getCurrentDirection() {
      return switch (status) {
         case GOING_UP -> Optional.of(Direction.UP);
         case GOING_DOWN -> Optional.of(Direction.DOWN);
         default -> Optional.empty();
      };
   }

   @Override
   public List<Call> getNextCalls() {
      return nextCalls;
   }

   @Override
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

   @Override
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

   @Override
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
         status = nextCalls.get(0).getDirection() == Direction.UP ? Status.GOING_UP : Status.GOING_DOWN;
         return of(LiftEngineCommand.OPEN_DOORS);
      }
      return of(getUpDownCommand());
   }

   private LiftEngineCommand getUpDownCommand() {
      if (currentFloor == nextCalls.get(0).getFloor()) {
         status = Status.from(nextCalls.get(0).getDirection());
         return LiftEngineCommand.OPEN_DOORS;
      }
      if (currentFloor < nextCalls.get(0).getFloor()) {
         status = Status.GOING_UP;
         return LiftEngineCommand.GO_UP;
      } else {
         status = Status.GOING_DOWN;
         return LiftEngineCommand.GO_DOWN;
      }
   }

   enum Status {
      GOING_UP, GOING_DOWN, STOPPED;

      public static Status from(Direction direction) {
         switch (direction) {
            case UP:
               return GOING_UP;
            case DOWN:
               return GOING_DOWN;
            default:
               throw new IllegalStateException("Unexpected value: " + direction);
         }
      }

      public boolean canServe(Direction direction) {
         if (direction == Direction.DOWN && this == GOING_DOWN) {
            return true;
         }
         if (direction == Direction.UP && this == GOING_UP) {
            return true;
         }
         return this == STOPPED;
      }
   }
}

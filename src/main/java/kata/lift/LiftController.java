package kata.lift;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static kata.lift.Direction.DOWN;
import static kata.lift.Direction.UP;
import static kata.lift.LiftEngineCommand.GO_DOWN;
import static kata.lift.LiftEngineCommand.GO_UP;

@Slf4j
public class LiftController implements ILiftController {

   private int currentFloor;
   private int calledFromFloor;

   public LiftController(int initialFloor) {
      this.currentFloor = initialFloor;
   }

   @Override
   public int getCurrentFloor() {
      return currentFloor;
   }

   @Override
   public Optional<Direction> getCurrentDirection() {
      if (calledFromFloor > currentFloor) {
         return of(UP);
      } else if (calledFromFloor < currentFloor) {
         return of(DOWN);
      }
      return empty();
   }

   @Override
   public Optional<LiftEngineCommand> call(Call call) {
      calledFromFloor = call.getFloor();
      if (calledFromFloor > currentFloor) {
         return of(GO_UP);
      } else if (calledFromFloor < currentFloor) {
         return of(GO_DOWN);
      }
      return empty();
   }

   @Override
   public List<Call> getNextCalls() {
      return Collections.emptyList();
   }

   @Override
   public LiftEngineCommand onFloor() {
      if (calledFromFloor > currentFloor) {
         currentFloor ++;
      } else /*if (calledFromFloor < currentFloor)*/{ // impossible imputs (assumed)
         currentFloor --;
      }
      return LiftEngineCommand.OPEN_DOORS;
   }

   @Override
   public Optional<LiftEngineCommand> onDoorsClosed() {
      return empty();
   }
}

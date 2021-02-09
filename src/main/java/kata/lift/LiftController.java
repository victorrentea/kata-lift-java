package kata.lift;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static kata.lift.Direction.DOWN;
import static kata.lift.Direction.UP;
import static kata.lift.LiftEngineCommand.*;

@Slf4j
public class LiftController implements ILiftController {

   private int currentFloor;
   private Call currentCall;

   public LiftController(int initialFloor) {
      this.currentFloor = initialFloor;
   }

   @Override
   public int getCurrentFloor() {
      return currentFloor;
   }

   @Override
   public Optional<Direction> getCurrentDirection() {
      if (currentCall == null) {
         return empty();
      }
      if (currentCall.getFloor() > currentFloor) {
         return of(UP);
      } else if (currentCall.getFloor() < currentFloor) {
         return of(DOWN);
      }
      return empty();
   }

   @Override
   public Optional<LiftEngineCommand> call(Call call) {
      currentCall = call;
      if (currentCall.getFloor() > currentFloor) {
         return of(GO_UP);
      } else if (currentCall.getFloor() < currentFloor) {
         return of(GO_DOWN);
      }
      return of(OPEN_DOORS);
   }

   @Override
   public List<Call> getNextCalls() {
      if (currentCall == null) {
         return Collections.emptyList();
      }
      return Arrays.asList(currentCall);
   }

   @Override
   public LiftEngineCommand onFloor() {
      if (currentCall.getFloor() > currentFloor) {
         currentFloor ++;
         if (currentFloor == currentCall.getFloor()) {
            currentCall = null;
            return OPEN_DOORS;
         } else {
            return GO_UP;
         }
      } else {
         currentFloor --;
         if (currentFloor == currentCall.getFloor()) {
            return OPEN_DOORS;
         } else {
            return GO_DOWN;
         }
      }
   }

   @Override
   public Optional<LiftEngineCommand> onDoorsClosed() {
      return empty(); // if (curentCall.floor == currentFloor) = null
   }
}

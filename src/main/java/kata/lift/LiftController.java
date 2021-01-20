package kata.lift;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;

@Slf4j
public class LiftController implements ILiftController {

   @Override
   public int getCurrentFloor() {
      return 0;
   }

   @Override
   public Optional<Direction> getCurrentDirection() {
      return empty();
   }

   @Override
   public List<Call> getNextCalls() {
      return Collections.emptyList();
   }

   @Override
   public LiftEngineCommand onFloor() {
      return LiftEngineCommand.OPEN_DOORS;
   }

   @Override
   public Optional<LiftEngineCommand> onDoorsClosed() {
      return empty();
   }

   @Override
   public Optional<LiftEngineCommand> call(Call call) {
      return empty();
   }
}

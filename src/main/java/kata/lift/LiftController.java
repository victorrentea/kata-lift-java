package kata.lift;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@Slf4j
public class LiftController implements ILiftController {
   public LiftController(int startFloor) {
   }

   @Override
   public int getCurrentFloor() {
      // TODO
      return 0;
   }

   @Override
   public Optional<Direction> getCurrentDirection() {
      // TODO
      return empty();
   }

   @Override
   public List<Call> getNextCalls() {
      // TODO
      return Collections.emptyList();
   }

   @Override
   public LiftEngineCommand onFloor() {
      // TODO
      return LiftEngineCommand.OPEN_DOORS;
   }

   @Override
   public Optional<LiftEngineCommand> onDoorsClosed() {
      // TODO
      return empty();
   }

   @Override
   public Optional<LiftEngineCommand> call(Call call) {
      // TODO
      return empty();
   }
}

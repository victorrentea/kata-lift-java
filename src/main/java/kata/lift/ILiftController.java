package kata.lift;

import java.util.List;
import java.util.Optional;

public interface ILiftController {
   int getCurrentFloor();

   Optional<Direction> getCurrentDirection();

   List<Call> getNextCalls();

   LiftEngineCommand onFloor();

   Optional<LiftEngineCommand> onDoorsClosed();

   Optional<LiftEngineCommand> call(Call call);
}

package kata.lift;

import lombok.SneakyThrows;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LiftControllerLogger implements ILiftController{
   private final ILiftController delegate;
   private final Consumer<String> logger;

   public LiftControllerLogger(ILiftController delegate, Consumer<String> logger) {
      this.delegate = delegate;
      this.logger = logger;
   }

   @Override
   public int getCurrentFloor() {
      return delegate.getCurrentFloor();
   }

   @Override
   public Optional<Direction> getCurrentDirection() {
      return delegate.getCurrentDirection();
   }

   @Override
   public List<Call> getNextCalls() {
      return delegate.getNextCalls();
   }

   private final Comparator<Call> comparator = Comparator.comparing(Call::getFloor)
       .thenComparing(call -> "" + call.getDirection());

   private void dumpState(String event, Object result) {
      String directionStr = delegate.getCurrentDirection().map(Direction::getSymbol).orElse("-");
      String leftCalls = delegate.getNextCalls().stream().sorted(comparator).map(Call::toString).collect(Collectors.joining(" "));
      String floorStr = Call.translateFloor(delegate.getCurrentFloor());
      logger.accept("%12s > %10s on %2s%s: %s".formatted(event, result, floorStr, directionStr, leftCalls));
   }

   @SneakyThrows
   @Override
   public LiftEngineCommand onFloor() {
      LiftEngineCommand result = delegate.onFloor();
      dumpState("FLOOR", result);
      return result;
   }

   @Override
   public Optional<LiftEngineCommand> onDoorsClosed() {
      Optional<LiftEngineCommand> result = delegate.onDoorsClosed();
      dumpState("DOORS_CLOSED", result.map(Objects::toString).orElse("NOOP"));
      return result;
   }

   @Override
   public Optional<LiftEngineCommand> call(Call call) {
      Optional<LiftEngineCommand> result = delegate.call(call);
      dumpState("CALL" + call, result.map(Objects::toString).orElse("NOOP"));
      return result;
   }
}

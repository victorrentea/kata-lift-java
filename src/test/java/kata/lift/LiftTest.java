package kata.lift;

import org.junit.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static kata.lift.Direction.DOWN;
import static kata.lift.Direction.UP;
import static kata.lift.LiftEngineCommand.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class LiftTest {

   private LiftController lift = new LiftController(0);

   @Test
   public void displaysGroundFloorIfStartAt0() {
      assertEquals(0, lift.getCurrentFloor());
   }

   @Test
   public void displaysFirstFloorIfStartAt1() {
      assertEquals(1, new LiftController(1).getCurrentFloor());
   }

   @Test
   public void displayItGoesUpWhenCalledFromAbove() {
      lift.call(new Call(2));
      assertThat(lift.getCurrentDirection()).hasValue(UP);
   }
   @Test
//   public void givenInitialLift_thenNoDirectionIsDisplayed() {
   public void displaysNoDirection_initially() {
      assertThat(lift.getCurrentDirection()).isEmpty();
   }
   @Test
   public void displayItGoesDownWhenCalledFromBellow() {
      lift.call(new Call(-1));
      assertThat(lift.getCurrentDirection()).hasValue(DOWN);
   }
   @Test
   public void displaysNoDirection_whenCalledFromTheSameFloor() {
      LiftController lift = new LiftController(1);
      lift.call(new Call(1));
      assertThat(lift.getCurrentDirection()).isEmpty();
   }

   @Test
   public void requestsToGoUp_whenCalledFromAbove() {
      Optional<LiftEngineCommand> command = lift.call(new Call(1));
      assertThat(command).hasValue(GO_UP);
   }
   @Test
   public void requestsToGoDown_whenCalledFromBellow() {
      Optional<LiftEngineCommand> command = lift.call(new Call(-1));
      assertThat(command).hasValue(GO_DOWN);
   }

   @Test
   public void opensDoorsWhenCalledFromTheCurrentFloor() {
      Optional<LiftEngineCommand> command = lift.call(new Call(0, UP));
      assertThat(command).hasValue(OPEN_DOORS);
   }


   //
   @Test
   public void displaysFirstFloor_afterOnFloorOnce_whenGoingUp() {
      lift.call(new Call(1));
      lift.onFloor();
      assertThat(lift.getCurrentFloor()).isEqualTo(1);
   }
   @Test
   public void displaysMinusOneFloor_afterOnFloorOnce_whenGoingDown() {
      lift.call(new Call(-1));
      lift.onFloor();
      assertThat(lift.getCurrentFloor()).isEqualTo(-1);
   }

   // invalid sequence of steps
//   @Test
//   public void weird() {
//      assertThat(lift.call(new Call(0))).isEmpty();
//      lift.onFloor();
//      assertThat(lift.getCurrentFloor()).isEqualTo(0);
//   }


   @Test
   public void reachesTheSecondFloorWhenCalledFromThere() {
       lift.call(new Call(2));
       assertThat(lift.onFloor()).isEqualTo(GO_UP);
       assertThat(lift.onFloor()).isEqualTo(OPEN_DOORS);
   }
   @Test
   public void reachesTheMInusSecondFloorWhenCalledFromThere() {
       lift.call(new Call(-2));
       assertThat(lift.onFloor()).isEqualTo(GO_DOWN);
       assertThat(lift.onFloor()).isEqualTo(OPEN_DOORS);
   }
   @Test
   public void displaysRequestedToGoToFirstFloorWhenRequestedToGoThere() {
       lift.call(new Call(1));
       assertThat(lift.getNextCalls()).containsExactly(new Call(1));
   }
   @Test
   public void displaysCalledFromFirstFloorWhenCalledFromThere() {
       lift.call(new Call(1, UP));
       assertThat(lift.getNextCalls()).containsExactly(new Call(1, UP));
   }


   @Test
   public void initiallyThereAreNoCallsInTheList() {
      assertThat(lift.getNextCalls()).isEmpty();
   }
   
   @Test
   public void theFloorIsRemovedFromNextCallsAfterReachingThatFloorUp() {
      lift.call(new Call(1));
      lift.onFloor();
      assertThat(lift.getNextCalls()).isNotEmpty();
      lift.onDoorsClosed();
      assertThat(lift.getNextCalls()).isEmpty();
   }
//   @Test
//   public void theFloorIsRemovedFromNextCallsAfterReachingThatFloorDown() {
//      lift.call(new Call(-1));
//      lift.onFloor();
//      assertThat(lift.getNextCalls()).isEmpty();
//   }


}

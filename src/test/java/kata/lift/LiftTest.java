package kata.lift;

import org.junit.Test;
import kata.lift.LiftController.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static kata.lift.CallDirection.DOWN;
import static kata.lift.CallDirection.UP;
import static kata.lift.LiftEngineCommand.*;

public class LiftTest {

   private LiftController lift = new LiftController(0);

   @Test
   public void opensDoorsWhenCalledFromOnSameFloor() {
      assertEquals(lift.call(new Call(0, UP)), of(OPEN_DOORS));
   }

   @Test
   public void movesUpWhenCalledFromFloorAbove() {
      assertEquals(lift.call(new Call(1, UP)), of(GO_UP));
   }

   @Test
   public void movesDownWhenCalledFromFloorBellow() {
      assertEquals(lift.call(new Call(-1, UP)), of(GO_DOWN));
   }

   @Test
   public void displaysCurrentFloor() {
      assertEquals(0, lift.getCurrentFloor());
   }

   @Test
   public void displaysAscendingFloorsWhenGoingUp() {
      lift.call(new Call(10, UP));
      lift.onFloor();
      assertEquals(1, lift.getCurrentFloor());
      lift.onFloor();
      assertEquals(2, lift.getCurrentFloor());
      lift.onFloor();
      assertEquals(3, lift.getCurrentFloor());
   }

   @Test
   public void displaysDescendingFloorsWhenGoingDown() {
      lift.call(new Call(-10, UP));
      lift.onFloor();
      assertEquals(-1, lift.getCurrentFloor());
      lift.onFloor();
      assertEquals(-2, lift.getCurrentFloor());
      lift.onFloor();
      assertEquals(-3, lift.getCurrentFloor());
   }

   @Test
   public void stopsAtCallFloor() {
      lift.call(new Call(1, UP));
      assertEquals(OPEN_DOORS, lift.onFloor());

   }

   @Test
   public void keepsMovingUpIfNotReachedCallFloor() {
      lift.call(new Call(10, UP));
      assertEquals(GO_UP, lift.onFloor());
      assertEquals(GO_UP, lift.onFloor());
   }

   @Test
   public void keepsMovingDownIfNotReachedCallFloor() {
      lift.call(new Call(-10, UP));
      assertEquals(GO_DOWN, lift.onFloor());
      assertEquals(GO_DOWN, lift.onFloor());
   }

   @Test
   public void twoConsecutiveCallsUp() {
      lift.call(new Call(1, UP));
      lift.call(new Call(2, UP));
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(GO_UP, lift.onDoorsClosed().get());
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertFalse(lift.onDoorsClosed().isPresent());
      assertEquals(Status.STOPPED, lift.getStatus());
   }

   @Test
   public void twoConsecutiveCallsDown() {
      lift.call(new Call(-1, DOWN));
      lift.call(new Call(-2, DOWN));
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(GO_DOWN, lift.onDoorsClosed().get());
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertFalse(lift.onDoorsClosed().isPresent());
   }
   @Test
   public void hesitatingDownwards() {
      lift.call(new Call(2, UP));
      lift.onFloor();
      lift.call(new Call(1, UP));
      assertEquals(asList(2, 1), lift.getNextCalls().stream().map(Call::getFloor).collect(Collectors.toList()));
   }
   @Test
   public void hesitatingUpwards() {
      lift.call(new Call(-2, DOWN));
      lift.onFloor();
      lift.call(new Call(-1, DOWN));
      assertEquals(asList(-2, -1), lift.getNextCalls().stream().map(Call::getFloor).collect(Collectors.toList()));
   }
   @Test
   public void duplicateCall() {
      lift.call(new Call(1, UP));
      lift.call(new Call(1, UP));
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(empty(), lift.onDoorsClosed());
   }

   @Test
   public void callUpThenDown() {
      assertEquals(of(GO_UP), lift.call(new Call(1, UP)));
      assertFalse(lift.call(new Call(-1, UP)).isPresent());
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(GO_DOWN, lift.onDoorsClosed().get());
      assertEquals(GO_DOWN, lift.onFloor());
      assertEquals(0, lift.getCurrentFloor());
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(Status.GOING_UP, lift.getStatus());
      assertFalse(lift.onDoorsClosed().isPresent());
   }
   @Test
   public void callDownThenUp() {
      assertEquals(of(GO_DOWN), lift.call(new Call(-1, UP)));
      assertFalse(lift.call(new Call(1, UP)).isPresent());
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(GO_UP, lift.onDoorsClosed().get());
      assertEquals(GO_UP, lift.onFloor());
      assertEquals(0, lift.getCurrentFloor());
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(Status.GOING_UP, lift.getStatus());
      assertFalse(lift.onDoorsClosed().isPresent());
   }
   @Test
   public void twoCallsOutOfOrder() {
      assertEquals(of(GO_UP), lift.call(new Call(2, UP)));
      assertFalse(lift.call(new Call(1, UP)).isPresent());

      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(1, lift.getCurrentFloor());
   }

   @Test
   public void callSequenceOutOfOrder() {
      lift.call(new Call(2, UP));
      lift.call(new Call(1, UP));
      lift.call(new Call(-1, DOWN));
      lift.call(new Call(-4, UP));

      List<String> stops = getStops();

      assertEquals(asList("*1","*2","1","0","*-1","-2","-3","*-4"), stops);
   }
   @Test
   public void callSequenceOutOfOrderDOWN() {
      lift.call(new Call(2, UP));
      lift.call(new Call(1, CallDirection.DOWN));
      lift.call(new Call(-1, DOWN));
      lift.call(new Call(-4, UP));

      assertEquals(asList(2,1,-1,-4), lift.getNextCallFloors());
      List<String> stops = getStops();

      assertEquals(asList("1","*2","*1","0","*-1","-2","-3","*-4"), stops);
   }

   private List<String> getStops() {
      List<String> stops = new ArrayList<>();
      do {
         while (lift.onFloor() != OPEN_DOORS) {
            stops.add(lift.getCurrentFloor() + "");
         }
         stops.add("*" + lift.getCurrentFloor());
      } while (lift.onDoorsClosed().isPresent());
      return stops;
   }

   @Test
   public void reorderCalls() {
      List<Call> calls = asList(
          new Call(4, UP),
          new Call(2, UP),
          new Call(1, DOWN),
          new Call(3, DOWN)
      );
      assertEquals(asList(2,4,3,1), LiftController.reorderCalls(calls, Status.GOING_UP, 0).stream().map(Call::getFloor).collect(toList()));
   }

   @Test
   public void reorderCallsHesitateBug() {
      List<Call> calls = asList(
          new Call(2, UP),
          new Call(1, UP)
      );
      assertEquals(asList(2,1), LiftController.reorderCalls(calls, Status.GOING_UP, 1).stream().map(Call::getFloor).collect(toList()));
   }
   @Test
   public void reorderCallsWithInternal() {
      List<Call> calls = asList(
          new Call(1, DOWN),
          new Call(3, DOWN),
          new Call(4, UP),
          new Call(2)
      );
      assertEquals(asList(3,2,1,4), LiftController.reorderCalls(calls, Status.GOING_DOWN, 5).stream().map(Call::getFloor).collect(toList()));
   }

   @Test
   public void callWhileDoorsOpen() {
      lift.call(new Call(1, UP));
      assertEquals(OPEN_DOORS, lift.onFloor());
      assertEquals(empty(), lift.call(new Call(-1, DOWN)));
      assertEquals(of(GO_DOWN), lift.onDoorsClosed());
   }

   @Test
   public void internalCall() {
      assertEquals(of(OPEN_DOORS), lift.call(new Call(0, UP)));
      assertEquals(empty(), lift.call(new Call(3)));
      assertEquals(Status.GOING_UP, lift.getStatus());
      assertEquals(of(GO_UP), lift.onDoorsClosed());
   }
   
   @Test
   public void doubleUpDownOnCurrentFloor() {
      assertEquals(of(OPEN_DOORS), lift.call(new Call(0, UP)));;
      assertEquals(empty(), lift.call(new Call(0, DOWN)));
      assertEquals(Status.GOING_UP, lift.getStatus());
      assertEquals(of(OPEN_DOORS), lift.onDoorsClosed());
      assertEquals(Status.GOING_DOWN, lift.getStatus());
      assertEquals(empty(), lift.onDoorsClosed());
      assertEquals(Status.STOPPED, lift.getStatus());

   }
   //
}

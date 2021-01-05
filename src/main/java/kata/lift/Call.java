package kata.lift;

import java.util.Objects;

public class Call {
   private final int floor;
   private final CallDirection direction;

   public Call(int floor, CallDirection direction) {
      this.floor = floor;
      this.direction = direction;
   }
   public Call(int floor) {
      this(floor, null);
   }

   boolean isDown() {
      return getDirection() == CallDirection.DOWN;
   }

   boolean isInternal() {
      return getDirection() == null;
   }

   boolean isUp() {
      return getDirection() == CallDirection.UP;
   }

   public int getFloor() {
      return floor;
   }

   public CallDirection getDirection() {
      return direction;
   }

   @Override
   public String toString() {
      String directionStr = "";
      if (direction != null) {
         directionStr = direction == CallDirection.UP ? "▲" : "▼";
      }
      return "(" + floor + directionStr+ ")";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Call call = (Call) o;
      return floor == call.floor &&
             direction == call.direction;
   }

   @Override
   public int hashCode() {
      return Objects.hash(floor, direction);
   }
}

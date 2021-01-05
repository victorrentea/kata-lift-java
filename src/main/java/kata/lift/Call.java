package kata.lift;

import java.util.Objects;

public class Call {
   private final int floor;
   private final Direction direction;

   public Call(int floor, Direction direction) {
      this.floor = floor;
      this.direction = direction;
   }
   public Call(int floor) {
      this(floor, null);
   }

   public static String translateFloor(int f) {
      return f == 0 ? "P" : f + "";
   }

   boolean isDown() {
      return getDirection() == Direction.DOWN;
   }

   public boolean isInternal() {
      return getDirection() == null;
   }

   boolean isUp() {
      return getDirection() == Direction.UP;
   }

   public int getFloor() {
      return floor;
   }

   public Direction getDirection() {
      return direction;
   }

   @Override
   public String toString() {
      String directionStr = "";
      if (direction != null) {
         directionStr = direction == Direction.UP ? "▲" : "▼";
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

   public boolean isExternal() {
      return getDirection() != null;
   }
}

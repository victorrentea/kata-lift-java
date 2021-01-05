package kata.lift;

public enum Direction {
   UP("^"),//"▲"),
   DOWN("v");//▼");
   public final String symbol;

   Direction(String symbol) {
      this.symbol = symbol;
   }

   public String getSymbol() {
      return symbol;
   }
}

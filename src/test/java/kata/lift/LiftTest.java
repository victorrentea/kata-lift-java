package kata.lift;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static kata.lift.Direction.DOWN;
import static kata.lift.Direction.UP;
import static kata.lift.LiftEngineCommand.*;

public class LiftTest {

   private ILiftController lift = new LiftController(0);

}

package io.xlogistx.iot.gpio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PinPosCalculator {




  public static class Coordinate
  {
    public final double x;
    public final double y;

    public Coordinate(double x, double y)
    {
      this.x = x;
      this.y = y;
    }

    public String toString()
    {
      return String.format("(%.2f,%.2f)", x,y);
    }
  }


  public static List<Coordinate> compute(Coordinate init, double xIncrement, int count)
  {
    List<Coordinate> ret = new ArrayList<Coordinate>();
    for (int i = 0; i<count; i++) {
      ret.add(init);
      double y = init.y;
      double x = init.x + xIncrement;
      init = new Coordinate(x, y);
    }

    return ret;
  }



  public static void main(String ...args)
  {
    try
    {
      int index =0;
      double x = Double.parseDouble(args[index++]);
      double y = Double.parseDouble(args[index++]);
      double inc = Double.parseDouble(args[index++]);
      int count = Integer.parseInt(args[index++]);

      List<Coordinate> coords = compute(new Coordinate(x,y), inc, count);
      System.out.println("Total:" + coords.size());
      AtomicInteger counter = new AtomicInteger();
      coords.forEach((c)->System.out.println(counter.incrementAndGet() + " " + c));

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}

package tech.ioengine.Login.fotopicker.CardPack;

import java.util.List;
import java.util.Locale;

/**
 * Created by virgile on 17.05.2017.
 */

public class Util {

    public static  long map(long x, long in_min, long in_max, long out_min, long out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}

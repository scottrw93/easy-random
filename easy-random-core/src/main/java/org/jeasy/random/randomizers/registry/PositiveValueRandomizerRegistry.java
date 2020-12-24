package org.jeasy.random.randomizers.registry;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.annotation.Priority;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerRegistry;
import org.jeasy.random.randomizers.range.BigDecimalRangeRandomizer;
import org.jeasy.random.randomizers.range.BigIntegerRangeRandomizer;
import org.jeasy.random.randomizers.range.ByteRangeRandomizer;
import org.jeasy.random.randomizers.range.DoubleRangeRandomizer;
import org.jeasy.random.randomizers.range.FloatRangeRandomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.jeasy.random.randomizers.range.LongRangeRandomizer;
import org.jeasy.random.randomizers.range.ShortRangeRandomizer;

@Priority(-2)
public class PositiveValueRandomizerRegistry implements RandomizerRegistry {
  private final Map<Class<?>, Randomizer<?>> randomizers = new HashMap<>();

  @Override
  public void init(EasyRandomParameters parameters) {
    long seed = parameters.getSeed();
    ByteRangeRandomizer byteRangeRandomizer = new ByteRangeRandomizer(
        (byte) 1,
        Byte.MAX_VALUE,
        seed
    );
    randomizers.put(Byte.class, byteRangeRandomizer);
    randomizers.put(byte.class, byteRangeRandomizer);

    ShortRangeRandomizer shortRangeRandomizer = new ShortRangeRandomizer(
        (short) 1,
        Short.MAX_VALUE,
        seed
    );
    randomizers.put(Short.class, shortRangeRandomizer);
    randomizers.put(short.class, shortRangeRandomizer);

    IntegerRangeRandomizer integerRangeRandomizer = new IntegerRangeRandomizer(
        1,
        Integer.MAX_VALUE,
        seed
    );
    randomizers.put(Integer.class, integerRangeRandomizer);
    randomizers.put(int.class, integerRangeRandomizer);

    LongRangeRandomizer longRangeRandomizer = new LongRangeRandomizer(
        1L,
        Long.MAX_VALUE,
        seed
    );
    randomizers.put(Long.class, longRangeRandomizer);
    randomizers.put(long.class, longRangeRandomizer);

    DoubleRangeRandomizer doubleRangeRandomizer = new DoubleRangeRandomizer(
        1D,
        Double.MAX_VALUE,
        seed
    );
    randomizers.put(Double.class, doubleRangeRandomizer);
    randomizers.put(double.class, doubleRangeRandomizer);

    FloatRangeRandomizer floatRangeRandomizer = new FloatRangeRandomizer(
        1f,
        Float.MAX_VALUE,
        seed
    );
    randomizers.put(Float.class, floatRangeRandomizer);
    randomizers.put(float.class, floatRangeRandomizer);

    BigIntegerRangeRandomizer bigIntegerRangeRandomizer = new BigIntegerRangeRandomizer(
        1,
        Integer.MAX_VALUE,
        seed
    );
    randomizers.put(BigInteger.class, bigIntegerRangeRandomizer);

    BigDecimalRangeRandomizer bigDecimalRangeRandomizer = new BigDecimalRangeRandomizer(
        1D,
        Double.MAX_VALUE,
        seed
    );
    randomizers.put(BigDecimal.class, bigDecimalRangeRandomizer);
  }

  @Override
  public Randomizer<?> getRandomizer(Field field) {
    return randomizers.get(field.getType());
  }

  @Override
  public Randomizer<?> getRandomizer(Class<?> aClass) {
    return randomizers.get(aClass);
  }
}

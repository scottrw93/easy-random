package org.jeasy.random;

import java.nio.charset.StandardCharsets;

import org.jeasy.random.randomizers.registry.PositiveValueRandomizerRegistry;

public class EasyRandomParametersFactory {
  public static EasyRandomParameters buildHubSpotSpecific(){
    EasyRandomParameters parameters = new EasyRandomParameters();
    parameters.scanClasspathForConcreteTypes(true)
        .randomizerRegistry(new PositiveValueRandomizerRegistry())
        .collectionSizeRange(1, 2)
        .stringLengthRange(5, 6);
    parameters.setCharset(StandardCharsets.UTF_8);
    parameters.setReuseFieldValues(true);
    return parameters;
  }
}

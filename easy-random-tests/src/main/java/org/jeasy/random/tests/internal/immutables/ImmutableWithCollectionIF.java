package org.jeasy.random.tests.internal.immutables;

import java.util.List;
import java.util.Set;

import org.immutables.value.Value.Immutable;

import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface ImmutableWithCollectionIF {
  int getPrimitiveInteger();
  Set<BasicImmutable> getBasicImmutables();
  List<Integer> getIntegers();
}

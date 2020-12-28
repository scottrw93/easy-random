package org.jeasy.random.tests.internal.immutables;

import java.util.Optional;

import org.immutables.value.Value.Immutable;

import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface BasicImmutableIF {
  int getPrimitiveInt();
  long getPrimitiveLong();
  boolean getPrimitiveBoolean();
  Integer getInteger();
  String getString();
  Optional<String> getOptionalString();
  Optional<Long> getOptionalLong();
  Optional<Integer> getOptionalInteger();
}

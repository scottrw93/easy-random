package org.jeasy.random.tests.internal.immutables;

import java.util.Optional;

import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface BasicImmutableWithDerivedOptionalIF {
  int getPrimitiveInt();

  @Derived
  default Optional<String> getDerivedOptional() {
    return getPrimitiveInt() == 6 ? Optional.of("six") : Optional.empty();
  }
}

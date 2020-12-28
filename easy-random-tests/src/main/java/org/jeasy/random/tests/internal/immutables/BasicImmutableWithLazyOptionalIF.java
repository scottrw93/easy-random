package org.jeasy.random.tests.internal.immutables;

import java.util.Optional;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;

import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface BasicImmutableWithLazyOptionalIF {
  int getPrimitiveInt();

  @Lazy
  default Optional<String> getLazyOptional() {
    return getPrimitiveInt() >= 0 ? Optional.of("positive") : Optional.empty();
  }
}

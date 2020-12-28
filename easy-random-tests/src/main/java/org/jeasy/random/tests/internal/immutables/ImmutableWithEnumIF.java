package org.jeasy.random.tests.internal.immutables;

import org.immutables.value.Value.Immutable;

import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface ImmutableWithEnumIF {
  AnEnum getAnEnum();
}

package org.jeasy.random.tests.internal.immutables;

import org.immutables.value.Value.Immutable;

import com.google.common.collect.Multimap;
import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface ImmutableWithMultimapIF {
  Multimap<String, String> getStringsByString();
}

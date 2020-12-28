package org.jeasy.random.tests.internal.immutables;

import java.util.Map;

import org.immutables.value.Value.Immutable;

import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface ImmutableWithMapIF {
  Map<String, String> getStringByString();
  Map<String, Long> getLongByString();
  Map<Long, String> getStringByLong();
}

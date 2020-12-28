package org.jeasy.random.tests.internal.immutables;

import java.util.Optional;

import org.immutables.value.Value.Immutable;

import com.hubspot.immutables.style.HubSpotStyle;

@Immutable
@HubSpotStyle
public interface ImmutableWithUserDefinedClassIF {
  Optional<UserDefinedClass> getOptionalUserDefinedClass();
  UserDefinedClass getUserDefinedClass();
  int getPrimitiveInt();
  Long getNonPrimitiveLong();
}

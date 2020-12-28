package org.jeasy.random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.jeasy.random.beans.Person;
import org.jeasy.random.tests.internal.immutables.AnEnum;
import org.jeasy.random.tests.internal.immutables.BasicImmutable;
import org.jeasy.random.tests.internal.immutables.BasicImmutableWithLazyOptional;
import org.jeasy.random.tests.internal.immutables.ImmutableWithCollection;
import org.jeasy.random.tests.internal.immutables.ImmutableWithEnum;
import org.jeasy.random.tests.internal.immutables.ImmutableWithMap;
import org.jeasy.random.tests.internal.immutables.ImmutableWithMultimap;
import org.jeasy.random.tests.internal.immutables.ImmutableWithUserDefinedClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;

@ExtendWith(MockitoExtension.class)
public class EasyRandomWithValueStoreTest {

  private EasyRandom easyRandom;

  @BeforeEach
  void setUp() {
    easyRandom = new EasyRandom(EasyRandomParametersFactory.buildHubSpotSpecific());
  }

  @Test
  void generatedBeansShouldBeEqualForSameIndex() {
    Person person1 = easyRandom.nextOrGetObject(0,Person.class);
    assertThat(easyRandom.nextOrGetObject(0, Person.class)).isEqualTo(person1);
  }

  @Test
  public void itCreatesBasicImmutable() {
    BasicImmutable actualBasicImmutable = easyRandom.nextObject(
        BasicImmutable.class
    );
    assertThat(actualBasicImmutable).isNotNull();
    assertThat(actualBasicImmutable).hasNoNullFieldsOrProperties();
    assertThat(actualBasicImmutable.getInteger()).isPositive();
    assertThat(actualBasicImmutable.getPrimitiveInt()).isPositive();
    assertThat(actualBasicImmutable.getPrimitiveLong()).isPositive();
    assertThat(actualBasicImmutable.getOptionalInteger())
        .hasValueSatisfying(value -> assertThat(value).isPositive());
    assertThat(actualBasicImmutable.getOptionalLong())
        .hasValueSatisfying(value -> assertThat(value).isPositive());
    assertThat(actualBasicImmutable.getString().length()).isBetween(5, 6);
  }

  @Test
  public void itCreatesBasicImmutableWithEmptyOptionals() {
    EasyRandomParameters parameters = EasyRandomParametersFactory.buildHubSpotSpecific();
    parameters.excludeField(FieldPredicates.isAnnotatedWith(Nullable.class));
    EasyRandom easyRandomWithValueStore = new EasyRandom(
        parameters
    );
    BasicImmutable actualBasicImmutable = easyRandomWithValueStore.nextObject(
        BasicImmutable.class
    );
    assertThat(actualBasicImmutable).isNotNull();
    assertThat(actualBasicImmutable).hasNoNullFieldsOrProperties();
    assertThat(actualBasicImmutable.getInteger()).isPositive();
    assertThat(actualBasicImmutable.getPrimitiveInt()).isPositive();
    assertThat(actualBasicImmutable.getPrimitiveLong()).isPositive();
    assertThat(actualBasicImmutable.getOptionalInteger()).isNotPresent();
    assertThat(actualBasicImmutable.getOptionalLong()).isNotPresent();
  }

  @Test
  public void itCreatesBasicImmutableWithLazyOptionals() {
    EasyRandomParameters parameters = EasyRandomParametersFactory.buildHubSpotSpecific();
    parameters.excludeField(FieldPredicates.isAnnotatedWith(Nullable.class));
    EasyRandom easyRandomWithValueStore = new EasyRandom(
        parameters
    );
    BasicImmutableWithLazyOptional basicImmutableWithLazyOptional = easyRandomWithValueStore.nextObject(
        BasicImmutableWithLazyOptional.class
    );
    assertThat(basicImmutableWithLazyOptional).isNotNull();
    assertThat(basicImmutableWithLazyOptional.getPrimitiveInt()).isPositive();
    assertThat(basicImmutableWithLazyOptional.getLazyOptional()).isPresent();

    // Checking a second time to make sure it's stored correctly
    assertThat(basicImmutableWithLazyOptional.getLazyOptional()).isPresent();
  }

  @Test
  public void itCreatesImmutableWithEnum() {
    ImmutableWithEnum actualImmutableWithEnum = easyRandom.nextOrGetObject(
        0,
        ImmutableWithEnum.class
    );
    assertThat(actualImmutableWithEnum.getAnEnum()).isIn((Object[]) AnEnum.values());
    assertThat(easyRandom.nextOrGetObject(
        0,
        ImmutableWithEnum.class
    )).isEqualTo(actualImmutableWithEnum);
  }

  @Test
  public void itCreatesImmutableWithUserDefinedClass() {
    ImmutableWithUserDefinedClass actualImmutableWithUserDefinedClass = easyRandom.nextObject(
        ImmutableWithUserDefinedClass.class
    );
    assertThat(actualImmutableWithUserDefinedClass.getOptionalUserDefinedClass())
        .hasValue(actualImmutableWithUserDefinedClass.getUserDefinedClass());
    assertThat(actualImmutableWithUserDefinedClass.getPrimitiveInt()).isPositive();
  }

  @Test
  public void itCreatesImmutableWithCollection() {
    ImmutableWithCollection actualImmutableWithCollection = easyRandom.nextObject(
        ImmutableWithCollection.class
    );
    assertThat(actualImmutableWithCollection.getBasicImmutables()).isNotEmpty();
    assertThat(actualImmutableWithCollection.getIntegers().size()).isBetween(1, 2);
  }

  @Test
  public void itCreatesImmutableWithMap() {
    ImmutableWithMap actualImmutableWithMap = easyRandom.nextObject(
        ImmutableWithMap.class
    );
    assertThat(actualImmutableWithMap.getLongByString()).isNotEmpty();
    assertThat(actualImmutableWithMap.getStringByLong()).isNotEmpty();
    assertThat(actualImmutableWithMap.getStringByString()).isNotEmpty();
  }

  @Test
  public void itDoesNotCreatesMultimapsWithoutCustomRandomizer() {
    assertThatThrownBy(() -> easyRandom.nextObject(
        ImmutableWithMultimap.class
    )).hasCauseExactlyInstanceOf(ObjectCreationException.class);
  }

  @Test
  public void itCreatesMultimapsWithCustomRandomizer() {
    EasyRandomParameters easyRandomParameters = EasyRandomParametersFactory.buildHubSpotSpecific();
    easyRandomParameters.randomize(ImmutableMultimap.class, ImmutableMultimap::of);
    EasyRandom easyRandomWithValueStore = new EasyRandom(
        easyRandomParameters
    );
    ImmutableWithMultimap immutableWithMultiMap = easyRandomWithValueStore.nextObject(
        ImmutableWithMultimap.class
    );
    assertThat(immutableWithMultiMap).hasNoNullFieldsOrProperties();
  }

  @Test
  public void itCreatesMultipleClasses() {
    ImmutableWithUserDefinedClass actualImmutableWithUserDefinedClass = easyRandom.nextOrGetObject(
        0,
        ImmutableWithUserDefinedClass.class
    );
    BasicImmutable actualBasicImmutable = easyRandom.nextOrGetObject(
        0,
        BasicImmutable.class
    );
    assertThat(actualBasicImmutable)
        .isEqualToComparingFieldByFieldRecursively(
            actualImmutableWithUserDefinedClass.getUserDefinedClass().getBasicImmutable()
        );
    assertThat(actualImmutableWithUserDefinedClass.getPrimitiveInt())
        .isEqualTo(actualBasicImmutable.getPrimitiveInt());
    assertThat(actualImmutableWithUserDefinedClass.getOptionalUserDefinedClass())
        .isPresent();
    assertThat(actualImmutableWithUserDefinedClass.getPrimitiveInt()).isPositive();
    assertThat(actualBasicImmutable.getInteger()).isPositive();
    assertThat(actualBasicImmutable.getPrimitiveInt()).isPositive();
    assertThat(actualBasicImmutable.getPrimitiveLong()).isPositive();
    assertThat(actualBasicImmutable.getOptionalInteger())
        .hasValueSatisfying(value -> assertThat(value).isPositive());
    assertThat(actualBasicImmutable.getOptionalLong())
        .hasValueSatisfying(value -> assertThat(value).isPositive());
  }

  @Test
  public void itCreatesMultipleInstances() {
    easyRandom.nextOrGetObject(4, ImmutableWithUserDefinedClass.class);
    Collection<ImmutableWithUserDefinedClass> actualCollection = easyRandom.objects(ImmutableWithUserDefinedClass.class, 10).collect(Collectors.toList());
    ImmutableList.Builder<ImmutableWithUserDefinedClass> expectedCollection = ImmutableList.builder();
    for (int i = 0; i < 10; i++) {
      expectedCollection.add(easyRandom.nextOrGetObject(i, ImmutableWithUserDefinedClass.class));
    }
    assertThat(actualCollection).containsAll(expectedCollection.build());
  }

  @Test
  public void itCreatesImmutableWithUserDefinedClassWithPredefinedFieldValue() {
    int predefinedIntegerFieldValue = 1263;
    EasyRandom easyRandomWithValueStore = new EasyRandom(
        EasyRandomParametersFactory.buildHubSpotSpecific()
            .randomize(FieldPredicates.named("integer"), () -> predefinedIntegerFieldValue)
    );
    ImmutableWithUserDefinedClass actualImmutableWithUserDefinedClass = easyRandomWithValueStore.nextObject(
        ImmutableWithUserDefinedClass.class
    );
    assertThat(actualImmutableWithUserDefinedClass.getOptionalUserDefinedClass())
        .hasValue(actualImmutableWithUserDefinedClass.getUserDefinedClass());
    assertThat(actualImmutableWithUserDefinedClass.getPrimitiveInt()).isPositive();
    assertThat(
        actualImmutableWithUserDefinedClass
            .getUserDefinedClass()
            .getBasicImmutable()
            .getInteger()
    )
        .isEqualTo(predefinedIntegerFieldValue);
  }
}

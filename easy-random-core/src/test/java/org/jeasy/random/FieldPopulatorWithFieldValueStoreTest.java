package org.jeasy.random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.jeasy.random.api.ContextAwareRandomizer;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.beans.Human;
import org.jeasy.random.util.FieldValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FieldPopulatorWithFieldValueStoreTest {

  private static final String NAME = "foo1";
  private static final long ID = 546546L;

  @Mock
  private EasyRandom easyRandom;
  @Mock
  private RegistriesRandomizerProvider randomizerProvider;
  @Mock
  private Randomizer randomizer;
  @Mock
  private ContextAwareRandomizer contextAwareRandomizer;
  @Mock
  private ArrayPopulator arrayPopulator;
  @Mock
  private CollectionPopulator collectionPopulator;
  @Mock
  private MapPopulator mapPopulator;
  @Mock
  private OptionalPopulator optionalPopulator;

  private FieldPopulatorWithFieldValueStore fieldPopulator;
  private FieldValueStore fieldValueStore;

  @BeforeEach
  void setUp() {
    fieldValueStore = new FieldValueStore();
    fieldPopulator = new FieldPopulatorWithFieldValueStore(easyRandom, randomizerProvider, arrayPopulator, collectionPopulator, mapPopulator, optionalPopulator, fieldValueStore);
  }

  @Test
  void whenFieldValuePresentInValueStoreForTheField_thenTheFieldShouldBePopulatedWithTheStoredValue() throws Exception {
    // Given
    Field name = Human.class.getDeclaredField("name");
    Human human = new Human();
    RandomizationContext context = new RandomizationContext(1, Human.class, new EasyRandomParameters());
    fieldValueStore.put(1, name, NAME);

    // When
    fieldPopulator.populateField(human, name, context);

    // Then
    assertThat(human.getName()).isEqualTo(NAME);
    verifyNoInteractions(randomizerProvider);
    verifyNoInteractions(randomizer);
    verifyNoInteractions(contextAwareRandomizer);
    verifyNoInteractions(arrayPopulator);
    verifyNoInteractions(collectionPopulator);
    verifyNoInteractions(mapPopulator);
    verifyNoInteractions(optionalPopulator);
  }

  @Test
  void whenFieldValueNotPresentInValueStoreForTheField_thenTheFieldShouldBePopulatedWithTheRandomValue() throws Exception {
    // Given
    Field id = Human.class.getDeclaredField("id");
    Human human = new Human();
    RandomizationContext context = new RandomizationContext(1, Human.class, new EasyRandomParameters());
    when(randomizerProvider.getRandomizerByField(id, context)).thenReturn(randomizer);
    when(randomizer.getRandomValue()).thenReturn(ID);

    // When
    fieldPopulator.populateField(human, id, context);

    // Then
    assertThat(human.getId()).isEqualTo(ID);
    assertThat(fieldValueStore.get(1, id)).hasValue(ID);
  }

  @Test
  void whenFieldValueNPresentInValueStoreForTheField_thenTheFieldShouldBePopulatedWithTheRandomValueForDifferentIndex() throws Exception {
    // Given
    Field id = Human.class.getDeclaredField("id");
    Human human1 = new Human();
    Human human10 = new Human();
    RandomizationContext context1 = new RandomizationContext(1, Human.class, new EasyRandomParameters());
    RandomizationContext context10 = new RandomizationContext(10, Human.class, new EasyRandomParameters());
    when(randomizerProvider.getRandomizerByField(id, context1)).thenReturn(randomizer);
    when(randomizerProvider.getRandomizerByField(id, context10)).thenReturn(randomizer);
    when(randomizer.getRandomValue())
        .thenReturn(ID)
        .thenReturn(ID + 1);

    // When
    fieldPopulator.populateField(human1, id, context1);
    fieldPopulator.populateField(human10, id, context10);

    // Then
    assertThat(human1.getId()).isEqualTo(ID);
    assertThat(human10.getId()).isEqualTo(ID + 1);
    assertThat(fieldValueStore.get(1, id)).hasValue(ID);
    assertThat(fieldValueStore.get(10, id)).hasValue(ID + 1);
  }
}

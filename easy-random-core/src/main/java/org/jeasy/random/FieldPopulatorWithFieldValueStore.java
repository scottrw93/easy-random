package org.jeasy.random;

import java.lang.reflect.Field;
import java.util.Optional;

import org.jeasy.random.api.RandomizerProvider;
import org.jeasy.random.util.FieldValueStore;
import org.jeasy.random.util.ReflectionUtils;

public class FieldPopulatorWithFieldValueStore extends FieldPopulator {

  private final FieldValueStore fieldValueStore;

  FieldPopulatorWithFieldValueStore(final EasyRandom easyRandom,
                                    final RandomizerProvider randomizerProvider,
                                    final ArrayPopulator arrayPopulator,
                                    final CollectionPopulator collectionPopulator,
                                    final MapPopulator mapPopulator,
                                    final OptionalPopulator optionalPopulator) {
    this(easyRandom, randomizerProvider, arrayPopulator, collectionPopulator, mapPopulator, optionalPopulator, new FieldValueStore());
  }

  FieldPopulatorWithFieldValueStore(final EasyRandom easyRandom,
                                    final RandomizerProvider randomizerProvider,
                                    final ArrayPopulator arrayPopulator,
                                    final CollectionPopulator collectionPopulator,
                                    final MapPopulator mapPopulator,
                                    final OptionalPopulator optionalPopulator,
                                    final FieldValueStore fieldValueStore) {
    super(easyRandom, randomizerProvider, arrayPopulator, collectionPopulator, mapPopulator, optionalPopulator);
    this.fieldValueStore = fieldValueStore;
  }

  @Override
  void populateField(final Object target, final Field field, final RandomizationContext context) throws IllegalAccessException {
    Optional<Object> maybeStoredFieldValue = fieldValueStore.get(context.getIndex(), field.getName());
    if (maybeStoredFieldValue.isPresent()) {
      setValue(target, field, maybeStoredFieldValue.get(), context);
      return;
    }
    super.populateField(target, field, context);
    fieldValueStore.put(context.getIndex(), field.getName(), ReflectionUtils.getFieldValue(target, field));
  }
}

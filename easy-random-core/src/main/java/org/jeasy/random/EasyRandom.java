/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.random;

import static org.jeasy.random.util.ReflectionUtils.getDeclaredFields;
import static org.jeasy.random.util.ReflectionUtils.getFieldValue;
import static org.jeasy.random.util.ReflectionUtils.getInheritedFields;
import static org.jeasy.random.util.ReflectionUtils.isArrayType;
import static org.jeasy.random.util.ReflectionUtils.isCollectionType;
import static org.jeasy.random.util.ReflectionUtils.isEnumType;
import static org.jeasy.random.util.ReflectionUtils.isIntrospectable;
import static org.jeasy.random.util.ReflectionUtils.isMapType;
import static org.jeasy.random.util.ReflectionUtils.isPrimitiveFieldWithDefaultValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jeasy.random.api.ContextAwareRandomizer;
import org.jeasy.random.api.ExclusionPolicy;
import org.jeasy.random.api.ObjectFactory;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerProvider;
import org.jeasy.random.api.RandomizerRegistry;
import org.jeasy.random.randomizers.misc.EnumRandomizer;
import org.jeasy.random.util.ReflectionUtils;

/**
 * Extension of {@link java.util.Random} that is able to generate random Java objects.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EasyRandom extends Random {

    private final EasyRandomParameters parameters;

    private final FieldPopulator fieldPopulator;

    private final ArrayPopulator arrayPopulator;

    private final Map<Class, EnumRandomizer> enumRandomizersByType;

    private final RandomizerProvider randomizerProvider;

    private final ObjectFactory objectFactory;

    private final ExclusionPolicy exclusionPolicy;

    /**
     * Create a new {@link EasyRandom} instance with default parameters.
     */
    public EasyRandom() {
        this(new EasyRandomParameters());
    }

    /**
     * Create a new {@link EasyRandom} instance.
     *
     * @param easyRandomParameters randomization parameters
     */
    public EasyRandom(final EasyRandomParameters easyRandomParameters) {
        Objects.requireNonNull(easyRandomParameters, "Parameters must not be null");
        super.setSeed(easyRandomParameters.getSeed());
        LinkedHashSet<RandomizerRegistry> registries = setupRandomizerRegistries(easyRandomParameters);
        RandomizerProvider customRandomizerProvider = easyRandomParameters.getRandomizerProvider();
        randomizerProvider = customRandomizerProvider == null ? new RegistriesRandomizerProvider() : customRandomizerProvider;
        randomizerProvider.setRandomizerRegistries(registries);
        objectFactory = easyRandomParameters.getObjectFactory();
        arrayPopulator = new ArrayPopulator(this);
        CollectionPopulator collectionPopulator = new CollectionPopulator(this);
        MapPopulator mapPopulator = new MapPopulator(this, objectFactory);
        OptionalPopulator optionalPopulator = new OptionalPopulator(this);
        enumRandomizersByType = new ConcurrentHashMap<>();
        fieldPopulator = setupFieldPopulator(easyRandomParameters, this,
                this.randomizerProvider, arrayPopulator,
                collectionPopulator, mapPopulator, optionalPopulator);
        exclusionPolicy = easyRandomParameters.getExclusionPolicy();
        parameters = easyRandomParameters;
    }

    /**
     * Generate a random instance of the given type.
     *
     * @param type           the type for which an instance will be generated
     * @param <T>            the actual type of the target object
     * @return a random instance of the given type
     * @throws ObjectCreationException when unable to create a new instance of the given type
     */
    public <T> T nextObject(final Class<T> type) {
        return doPopulateBean(type, new RandomizationContext(type, parameters));
    }

    /**
     * Generate or get a random instance of the given type.
     *
     * @param index          the index for which object to get or generate
     * @param type           the type for which an instance will be generated
     * @param <T>            the actual type of the target object
     * @return a random instance of the given type
     * @throws ObjectCreationException when unable to create a new instance of the given type
     */
    public <T> T nextOrGetObject(final int index, final Class<T> type) {
        return doPopulateBean(type, new RandomizationContext(index, type, parameters));
    }

    /**
     * Generate a stream of random instances of the given type.
     *
     * @param type           the type for which instances will be generated
     * @param streamSize         the number of instances to generate
     * @param <T>            the actual type of the target objects
     * @return a stream of random instances of the given type
     * @throws ObjectCreationException when unable to create a new instance of the given type
     */
    public <T> Stream<T> objects(final Class<T> type, final int streamSize) {
        if (streamSize < 0) {
            throw new IllegalArgumentException("The stream size must be positive");
        }
        return IntStream.range(0, streamSize)
            .mapToObj(index -> nextOrGetObject(index, type));
    }

    <T> T doPopulateBean(final Class<T> type, final RandomizationContext context) {
        if (exclusionPolicy.shouldBeExcluded(type, context)) {
            return null;
        }

        T result;
        try {

            Randomizer<?> randomizer = randomizerProvider.getRandomizerByType(type, context);
            if (randomizer != null) {
                if (randomizer instanceof ContextAwareRandomizer) {
                    ((ContextAwareRandomizer<?>) randomizer).setRandomizerContext(context);
                }
                return (T) randomizer.getRandomValue();
            }

            // Collection types are randomized without introspection for internal fields
            if (!isIntrospectable(type)) {
                return randomize(type, context);
            }

            // If the type has been already randomized, return one cached instance to avoid recursion.
            if (context.hasAlreadyRandomizedType(type)) {
                return (T) context.getPopulatedBean(type);
            }

            // create a new instance of the target type
            result = objectFactory.createInstance(type, context);
            context.setRandomizedObject(result);

            // cache instance in the population context
            context.addPopulatedBean(type, result);

            // retrieve declared and inherited fields
            List<Field> fields = getDeclaredFields(result);
            // we cannot use type here, because with classpath scanning enabled the result can be a subtype
            fields.addAll(getInheritedFields(result.getClass()));

            // inner classes (and static nested classes) have a field named "this$0" that references the enclosing class.
            // This field should be excluded
            if (type.getEnclosingClass() != null) {
                fields.removeIf(field -> field.getName().equals("this$0"));
            }

            // populate fields with random data
            populateFields(fields, result, context);

            return result;
        } catch (Throwable e) {
            if (parameters.isIgnoreRandomizationErrors()) {
                return null;
            } else {
                throw new ObjectCreationException("Unable to create a random instance of type " + type, e);
            }
        }
    }

    private <T> T randomize(final Class<T> type, final RandomizationContext context) {
        if (isEnumType(type)) {
            if (!enumRandomizersByType.containsKey(type)) {
                enumRandomizersByType.put(type, new EnumRandomizer(type, parameters.getSeed()));
            }
            return (T) enumRandomizersByType.get(type).getRandomValue();
        }
        if (isArrayType(type)) {
            return (T) arrayPopulator.getRandomArray(type, context);
        }
        if (isCollectionType(type)) {
            return (T) ReflectionUtils.getEmptyImplementationForCollectionInterface(type);
        }
        if (isMapType(type)) {
            return (T) ReflectionUtils.getEmptyImplementationForMapInterface(type);
        }
        return null;
    }

    private <T> void populateFields(final List<Field> fields, final T result, final RandomizationContext context) throws IllegalAccessException {
        for (final Field field : fields) {
            populateField(field, result, context);
        }
    }

    private <T> void populateField(final Field field, final T result, final RandomizationContext context) throws IllegalAccessException {
        if (exclusionPolicy.shouldBeExcluded(field, context)) {
            return;
        }
        if (!parameters.isOverrideDefaultInitialization() && getFieldValue(result, field) != null && !isPrimitiveFieldWithDefaultValue(result, field)) {
          return;
        }
        fieldPopulator.populateField(result, field, context);
    }

    private LinkedHashSet<RandomizerRegistry> setupRandomizerRegistries(EasyRandomParameters parameters) {
        LinkedHashSet<RandomizerRegistry> registries = new LinkedHashSet<>();
        registries.add(parameters.getCustomRandomizerRegistry());
        registries.add(parameters.getExclusionRandomizerRegistry());
        registries.addAll(parameters.getUserRegistries());
        registries.addAll(loadRegistries());
        registries.forEach(registry -> registry.init(parameters));
        return registries;
    }

    private Collection<RandomizerRegistry> loadRegistries() {
        List<RandomizerRegistry> registries = new ArrayList<>();
        ServiceLoader.load(RandomizerRegistry.class).forEach(registries::add);
        return registries;
    }

    private static FieldPopulator setupFieldPopulator(final EasyRandomParameters parameters,
                                                      final EasyRandom easyRandom,
                                                      final RandomizerProvider randomizerProvider,
                                                      final ArrayPopulator arrayPopulator,
                                                      final CollectionPopulator collectionPopulator,
                                                      final MapPopulator mapPopulator,
                                                      final OptionalPopulator optionalPopulator) {
        if (parameters.isReuseFieldValues()) {
            return new FieldPopulatorWithFieldValueStore(
                easyRandom,
                randomizerProvider,
                arrayPopulator,
                collectionPopulator,
                mapPopulator,
                optionalPopulator);
        }
        return new FieldPopulator(
            easyRandom,
            randomizerProvider,
            arrayPopulator,
            collectionPopulator,
            mapPopulator,
            optionalPopulator);
    }

}

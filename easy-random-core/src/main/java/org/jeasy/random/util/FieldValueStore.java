package org.jeasy.random.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FieldValueStore {
  private final Map<FieldValueKey, Object> valuesByFieldValueKey;

  public FieldValueStore() {
    this(new HashMap<>());
  }

  public FieldValueStore(Map<FieldValueKey, Object> valuesByFieldValueKey) {
    this.valuesByFieldValueKey = valuesByFieldValueKey;
  }

  public void put(int index, Field field, Object value) {
    if (value != null) {
      valuesByFieldValueKey.put(new FieldValueKey(index, field), value);
    }
  }

  public Optional<Object> get(int index, Field field) {
    return Optional.ofNullable(valuesByFieldValueKey.get(new FieldValueKey(index, field)));
  }

  public static class FieldValueKey {
    private final int index;
    private final String name;
    private final Class<?> type;

    FieldValueKey(int index, String name, Class<?> type) {
      this.index = index;
      this.name = name;
      this.type = type;
    }

    public FieldValueKey(int index, Field field) {
      this(index, field.getName(), field.getType());
    }

    @Override
    public int hashCode() {
      return getName().hashCode() ^ getType().hashCode() ^ getIndex();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof FieldValueKey) {
        FieldValueKey other = (FieldValueKey) obj;
        return (getName().equals(other.getName()))
            && (getType() == other.getType())
            && (getIndex() == other.getIndex());
      }
      return false;
    }

    public int getIndex() {
      return index;
    }

    public String getName() {
      return name;
    }

    public Class<?> getType() {
      return type;
    }
  }
}

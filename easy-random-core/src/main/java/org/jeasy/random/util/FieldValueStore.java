package org.jeasy.random.util;

import java.lang.reflect.Field;
import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FieldValueStore {
  private final Table<FieldKey, Integer, Object> valuesByField;

  public FieldValueStore() {
    this(HashBasedTable.create());
  }

  public FieldValueStore(Table<FieldKey, Integer, Object> valuesByField) {
    this.valuesByField = valuesByField;
  }

  public void put(int index, Field field, Object value) {
    if (value != null) {
      valuesByField.put(new FieldKey(field), index, value);
    }
  }

  public Optional<Object> get(int index, Field field) {
    return Optional.ofNullable(valuesByField.get(new FieldKey(field), index));
  }

  public static class FieldKey {
    private final String name;
    private final Class<?> type;

    FieldKey(String name, Class<?> type) {
      this.name = name;
      this.type = type;
    }

    public FieldKey(Field field) {
      this(field.getName(), field.getType());
    }

    @Override
    public int hashCode() {
      return getName().hashCode() & getType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof FieldKey) {
        FieldKey other = (FieldKey)obj;
        return (getName().equals(other.getName()))
            && (getType() == other.getType());
      }
      return false;
    }


    public String getName() {
      return name;
    }

    public Class<?> getType() {
      return type;
    }
  }
}

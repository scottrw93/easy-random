package org.jeasy.random.util;

import java.lang.reflect.Field;
import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FieldValueStore {
  private final Table<Field, Integer,  Object> valuesByField;

  public FieldValueStore() {
    this(HashBasedTable.create());
  }

  public FieldValueStore(Table<Field,Integer, Object> valuesByField) {
    this.valuesByField = valuesByField;
  }

  public void put(int index, Field field, Object value) {
    valuesByField.put(field, index,value);
  }

  public <T> Optional<T> get(int index, Field field) {
    return (Optional<T>) Optional.ofNullable(valuesByField.get(field, index));
  }
}

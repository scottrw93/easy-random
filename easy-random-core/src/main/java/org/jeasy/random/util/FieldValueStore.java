package org.jeasy.random.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FieldValueStore {
  private final Map<String, Object> valueByKey;

  public FieldValueStore() {
    this(new HashMap<>());
  }

  public FieldValueStore(Map<String, Object> valueByKey) {
    this.valueByKey = valueByKey;
  }

  public void put(int index, String fieldName, Object value) {
    String key = buildKey(index, fieldName);
    valueByKey.put(key, value);
  }

  public <T> Optional<T> get(int index, String fieldName) {
    String key = buildKey(index, fieldName);
    return (Optional<T>) Optional.ofNullable(valueByKey.get(key));
  }

  private static String buildKey(int index, String fieldName) {
    return String.format("%d-%s", index, fieldName);
  }
}

package com.mmodding.mmodding_lib.library.utils;

import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public class MixedHashMap<K> extends HashMap<K, TypedObject<?>> implements MixedMap<K> {

	public MixedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public MixedHashMap(int initialCapacity) {
		super(initialCapacity);
	}

	public MixedHashMap() {
		super();
	}

	public MixedHashMap(MixedMap<? extends K> m) {
		super(m);
	}

	@Override
	public <V> boolean containsValue(Class<V> type, V value) {
		return super.containsValue(TypedObject.of(type, value));
	}

	@Override
	public <V> V get(K key, Class<V> type) {
		TypedObject<?> typed = super.get(key);
		if (typed == null) {
			typed = MixedMap.emptyValue(type);
		}
		if (type.equals(typed.getType())) {
			return (V) typed.getValue();
		}
		else {
			throw new IllegalArgumentException("Given type does not match the targeted type!");
		}
	}

	@Override
	public <V> V put(K key, Class<V> type, V value) {
		TypedObject<V> typed = (TypedObject<V>) super.put(key, TypedObject.of(type, value));
		if (typed != null) {
			return typed.getValue();
		}
		else {
			return MixedMap.emptyValue(type).getValue();
		}
	}

	@Override
	public <V> void forEach(TriConsumer<? super K, ? super Class<V>, ? super V> action) {
		this.forEach((key, value) -> action.accept(key, (Class<V>) value.getType(), (V) value.getValue()));
	}
}

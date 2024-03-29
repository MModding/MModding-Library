package com.mmodding.mmodding_lib.library.utils;

import java.util.List;

public class WhiteList<E> implements FilterList<E> {

	private final List<E> elements;

	@SafeVarargs
	public WhiteList(E... elements) {
		this.elements = List.of(elements);
	}

	@Override
	public boolean check(E element) {
		return this.elements.contains(element);
	}
}

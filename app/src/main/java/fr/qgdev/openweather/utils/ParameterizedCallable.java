package fr.qgdev.openweather.utils;

public interface ParameterizedCallable<I, O> {
	O call(I input);
}

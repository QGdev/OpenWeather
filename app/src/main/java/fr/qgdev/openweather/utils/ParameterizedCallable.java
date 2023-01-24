package fr.qgdev.openweather.utils;

public interface ParameterizedCallable<Input, Output> {
	Output call(Input input);
}

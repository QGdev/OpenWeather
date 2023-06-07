package fr.qgdev.openweather.utils;

public interface ParameterizedRunnable<I> {
	void run(I input);
}
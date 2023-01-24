package fr.qgdev.openweather.utils;

public interface ParameterizedRunnable<Input> {
	void run(Input input);
}
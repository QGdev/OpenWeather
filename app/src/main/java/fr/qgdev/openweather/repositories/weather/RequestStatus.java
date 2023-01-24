package fr.qgdev.openweather.repositories.weather;

public enum RequestStatus {
	NO_ANSWER,
	TOO_MANY_REQUESTS,
	NOT_FOUND,
	ALREADY_PRESENT,
	AUTH_FAILED,
	NOT_CONNECTED,
	UNKNOWN_ERROR
}

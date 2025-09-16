package ru.practicum.booking.dto;

import java.util.Optional;

/**
 * Перечисление возможных состояний бронирования.
 * <p>
 * Используется для фильтрации и управления статусами бронирований в системе.
 */
public enum BookingState {
    /**
     * Все бронирования без фильтрации по статусу.
     */
    ALL,

    /**
     * Текущие активные бронирования (начавшиеся, но еще не завершенные).
     */
    CURRENT,

    /**
     * Будущие бронирования (которые начнутся позже).
     */
    FUTURE,

    /**
     * Завершенные бронирования.
     */
    PAST,

    /**
     * Отклоненные бронирования.
     */
    REJECTED,

    /**
     * Бронирования, ожидающие подтверждения.
     */
    WAITING;

    /**
     * Преобразует строковое представление состояния в соответствующий enum.
     *
     * @param stringState строковое представление состояния (без учета регистра)
     * @return {@link Optional}, содержащий соответствующий {@link BookingState},
     *         или пустой {@link Optional}, если соответствие не найдено
     * @throws NullPointerException если stringState равен null
     */
    public static Optional<BookingState> from(String stringState) {
        if (stringState == null) {
            throw new NullPointerException("State cannot be null");
        }

        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}

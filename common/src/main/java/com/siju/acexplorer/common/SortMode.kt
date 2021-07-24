package com.siju.acexplorer.common

import java.security.InvalidParameterException

enum class SortMode(val value: Int) {
    NAME(0),
    NAME_DESC(1),
    TYPE(2),
    TYPE_DESC(3),
    SIZE(4),
    SIZE_DESC(5),
    DATE(6),
    DATE_DESC(7);

    companion object {
        fun getSortModeFromValue(value: Int): SortMode {
            return when (value) {
                0    -> NAME
                1    -> NAME_DESC
                2    -> TYPE
                3    -> TYPE_DESC
                4    -> SIZE
                5    -> SIZE_DESC
                6    -> DATE
                7    -> DATE_DESC
                else -> throw InvalidParameterException(
                    "Invalid sortmode mode value should be either ${NAME.value} or ${NAME_DESC.value}" +
                            "or ${TYPE.value} or ${TYPE_DESC.value} or ${SIZE.value} or ${SIZE_DESC.value} + " +
                            "${DATE.value} or ${DATE_DESC.value}"
                )
            }
        }

        fun isAscending(sortMode: SortMode) = sortMode.value % 2 == 0

    }

}
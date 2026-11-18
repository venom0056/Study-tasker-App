package com.example.data

enum class Subject(val label: String) {
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry"),
    MATHS("Maths"),
    OTHER("Other");

    companion object {
        fun fromLabel(label: String): Subject {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHER
        }
    }
}

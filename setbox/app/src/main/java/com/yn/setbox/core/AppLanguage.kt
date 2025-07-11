package com.yn.setbox.core

enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    ARABIC;

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            return when (tag) {
                "en" -> ENGLISH
                "ar" -> ARABIC
                else -> SYSTEM
            }
        }

        fun toTag(language: AppLanguage): String? {
            return when (language) {
                ENGLISH -> "en"
                ARABIC -> "ar"
                SYSTEM -> null
            }
        }
    }
}
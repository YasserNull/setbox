package com.yn.setbox.core

enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    ARABIC,

    TURKISH;

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            return when (tag) {
                "en" -> ENGLISH
                "ar" -> ARABIC
                "tr" -> TURKISH
                else -> SYSTEM
            }
        }

        fun toTag(language: AppLanguage): String? {
            return when (language) {
                ENGLISH -> "en"
                ARABIC -> "ar"
                TURKISH -> "tr"
                SYSTEM -> null
            }
        }
    }
}
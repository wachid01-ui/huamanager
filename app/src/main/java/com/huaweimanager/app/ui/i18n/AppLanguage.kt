package com.huaweimanager.app.ui.i18n

enum class AppLanguage(
    val code: String,
    val title: String,
    val nativeName: String,
    val flagEmoji: String
) {
    INDONESIAN(
        code = "id",
        title = "Bahasa Indonesia",
        nativeName = "Indonesia",
        flagEmoji = "🇮🇩"
    ),
    ENGLISH(
        code = "en",
        title = "English (International)",
        nativeName = "English",
        flagEmoji = "🇬🇧"
    );

    val strings: AppStrings
        get() = when (this) {
            INDONESIAN -> IndonesianStrings
            ENGLISH -> EnglishStrings
        }
}

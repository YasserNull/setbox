package com.yn.setbox.data.model

// يمثل وحدة (Module) مثبتة محليًا.
data class Module(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: String?,
    val author: String,
    val description: String?,
    val path: String,       // المسار إلى مجلد الوحدة على الجهاز.
    val repository: String?, // رابط مستودع GitHub الخاص بالوحدة.
    var isEnabled: Boolean = false // هل الوحدة مفعلة حاليًا.
)
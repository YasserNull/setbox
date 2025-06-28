package com.yn.setbox.plugin

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Settings

class SettingsProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val context = context ?: return 0

        if (context.checkCallingOrSelfPermission("com.yn.setbox.PLUGIN_PROVIDER_ACCESS") != PackageManager.PERMISSION_GRANTED) {
            return 0
        }

        val table = values?.getAsString("table")?.lowercase()
        val key = values?.getAsString("key")
        val valueStr = values?.getAsString("value")

        if (table.isNullOrBlank() || key.isNullOrBlank() || valueStr == null) {
            return 0
        }

        if (!hasRequiredPermissions(context, table)) {
            return 0
        }

        val resolver = context.contentResolver
        try {
            val success: Boolean = when {
                valueStr.toIntOrNull() != null -> putInt(resolver, table, key, valueStr.toInt())
                valueStr.toFloatOrNull() != null -> putFloat(resolver, table, key, valueStr.toFloat())
                else -> putString(resolver, table, key, valueStr)
            }
            return if (success) 1 else 0
        } catch (se: SecurityException) {
            return 0
        } catch (e: Exception) {
            return 0
        }
    }

    private fun hasRequiredPermissions(context: Context, table: String): Boolean {
        val pid = android.os.Process.myPid()
        val uid = android.os.Process.myUid()

        val hasSettings = context.checkPermission(android.Manifest.permission.WRITE_SETTINGS, pid, uid) == PackageManager.PERMISSION_GRANTED
        if (table == "system" && !hasSettings) return false

        val hasSecureSettings = context.checkPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS, pid, uid) == PackageManager.PERMISSION_GRANTED
        if ((table == "secure" || table == "global") && !hasSecureSettings) return false

        return true
    }

    private fun putInt(r: android.content.ContentResolver, table: String, key: String, value: Int): Boolean {
        return when (table) {
            "system" -> Settings.System.putInt(r, key, value)
            "secure" -> Settings.Secure.putInt(r, key, value)
            "global" -> Settings.Global.putInt(r, key, value)
            else -> false
        }
    }

    private fun putFloat(r: android.content.ContentResolver, table: String, key: String, value: Float): Boolean {
        return when (table) {
            "system" -> Settings.System.putFloat(r, key, value)
            "secure" -> Settings.Secure.putFloat(r, key, value)
            "global" -> Settings.Global.putFloat(r, key, value)
            else -> false
        }
    }

    private fun putString(r: android.content.ContentResolver, table: String, key: String, value: String): Boolean {
        return when (table) {
            "system" -> Settings.System.putString(r, key, value)
            "secure" -> Settings.Secure.putString(r, key, value)
            "global" -> Settings.Global.putString(r, key, value)
            else -> false
        }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
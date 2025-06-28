package com.yn.setbox.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import com.yn.setbox.data.model.Module
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties
import java.util.zip.ZipInputStream

// مستودع لإدارة الوحدات المثبتة محليًا على الجهاز.
class ModuleRepository(private val context: Context) {

    private val modulesRootPath by lazy { File(context.filesDir, "modules").absolutePath }

    // عنوان Content Provider الخاص بالبلوقن لتنفيذ الأوامر.
    private val pluginProviderUri = Uri.parse("content://com.yn.setbox.plugin.provider/settings")

    suspend fun getModules(): List<Module> {
        return withContext(Dispatchers.IO) {
            val modulesList = mutableListOf<Module>()
            val modulesDir = File(modulesRootPath)
            if (!modulesDir.exists()) modulesDir.mkdirs()

            modulesDir.listFiles()?.forEach { moduleFolder ->
                if (moduleFolder.isDirectory) {
                    val modulePropFile = File(moduleFolder, "module.prop")
                    readModuleProp(modulePropFile)?.let { modulesList.add(it) }
                }
            }
            modulesList
        }
    }

    suspend fun installFromZip(zipUri: Uri, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            val tempDir = File(context.cacheDir, "unzip_temp_${System.currentTimeMillis()}").apply { mkdirs() }
            try {
                // فك ضغط الملف المؤقت.
                context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zipInputStream ->
                        var entry = zipInputStream.nextEntry
                        while (entry != null) {
                            val newFile = File(tempDir, entry.name)
                            if (entry.isDirectory) newFile.mkdirs() else {
                                newFile.parentFile?.mkdirs()
                                FileOutputStream(newFile).use { fos -> zipInputStream.copyTo(fos) }
                            }
                            zipInputStream.closeEntry()
                            entry = zipInputStream.nextEntry
                        }
                    }
                } ?: return@withContext false

                // قراءة module.prop للحصول على معرف الوحدة.
                val modulePropFile = findModuleProp(tempDir) ?: throw IOException("لم يتم العثور على module.prop.")
                val moduleContentDir = modulePropFile.parentFile ?: throw IOException("هيكل الوحدة غير صالح.")
                val props = Properties().apply { load(FileInputStream(modulePropFile)) }
                val moduleId = props.getProperty("id") ?: throw IOException("معرف الوحدة غير موجود.")

                // نسخ محتويات الوحدة إلى المجلد الدائم.
                val finalModuleDir = File(modulesRootPath, moduleId)
                if (finalModuleDir.exists()) finalModuleDir.deleteRecursively()
                finalModuleDir.mkdirs()
                moduleContentDir.copyRecursively(finalModuleDir, overwrite = true)
                true
            } catch (e: Exception) {
                false
            } finally {
                tempDir.deleteRecursively()
            }
        }
    }

    private fun findModuleProp(directory: File): File? {
        directory.walkTopDown().forEach { file ->
            if (file.isFile && file.name == "module.prop") return file
        }
        return null
    }

    /**
     * يطبق الإعدادات من ملف الأوامر (on أو off) عبر التواصل مع البلوقن.
     * @return `true` إذا نجح تنفيذ جميع الأوامر، و`false` إذا فشل أمر واحد على الأقل.
     */
    suspend fun applySettingsFromFile(modulePath: String, commandFileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val commandFile = File(modulePath, commandFileName)
            if (!commandFile.exists()) return@withContext true // يعتبر نجاحًا لأنه لا يوجد شيء لتنفيذه.

            var allSucceeded = true
            commandFile.forEachLine { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isNotBlank() && !trimmedLine.startsWith("#")) {
                    val parts = trimmedLine.split("\\s+".toRegex())
                    if (parts.size >= 3) {
                        val values = ContentValues().apply {
                            put("table", parts[0]) // system, global, secure
                            put("key", parts[1])   // اسم الإعداد
                            put("value", parts.drop(2).joinToString(" ")) // القيمة
                        }
                        try {
                            // إرسال الأمر إلى البلوقن لتنفيذه.
                            val updatedRows = context.contentResolver.update(pluginProviderUri, values, null, null)
                            if (updatedRows <= 0) {
                                allSucceeded = false
                            }
                        } catch (e: Exception) {
                            allSucceeded = false
                        }
                    }
                }
            }
            allSucceeded
        }
    }

    suspend fun uninstallModule(module: Module): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // حذف مجلد الوحدة.
                File(module.path).deleteRecursively()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // يقرأ ملف module.prop ويحوله إلى كائن Module.
    private fun readModuleProp(modulePropFile: File): Module? {
        if (!modulePropFile.exists()) return null
        val props = Properties()
        return try {
            FileInputStream(modulePropFile).use { props.load(it) }
            Module(
                id = props.getProperty("id") ?: return null,
                name = props.getProperty("name") ?: return null,
                version = props.getProperty("version") ?: return null,
                versionCode = props.getProperty("versionCode"),
                author = props.getProperty("author") ?: return null,
                description = props.getProperty("description"),
                path = modulePropFile.parentFile?.absolutePath ?: "",
                repository = props.getProperty("repository"),
                isEnabled = false
            )
        } catch (e: Exception) {
            null
        }
    }
}
package com.yn.setbox.data.repository

import android.content.Context
import android.util.Log
import com.yn.setbox.data.model.RemoteModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Properties

// كائن يمثل نتيجة عملية جلب المستودع.
data class RepoFetchResult(
    val modules: List<RemoteModule>? = null, // قائمة الوحدات في حالة النجاح
    val rawResponse: String? = null,        // الاستجابة الخام للمساعدة في التصحيح
    val errorMessage: String? = null        // رسالة الخطأ في حالة الفشل
)

// مستودع لإدارة التفاعل مع المستودع عن بعد (جلب القائمة وتنزيل الوحدات).
class RepoRepository(private val context: Context) {

    private val modulesJsonUrl = "https://raw.githubusercontent.com/YasserNull/setbox-repo/main/modules.yaml"
    private val modulesRootPath by lazy { File(context.filesDir, "modules").absolutePath }
    private val client = HttpClient(Android)

    // جلب قائمة الوحدات من ملف YAML عن بعد.
    suspend fun getRepoModules(): RepoFetchResult {
        return try {
            val response: HttpResponse = client.get(modulesJsonUrl)
            val rawYamlString = response.bodyAsText()

            if (rawYamlString.isBlank()) {
                return RepoFetchResult(errorMessage = "repo_file_empty")
            }

            val settings = LoadSettings.builder().build()
            val loader = Load(settings)
            val yamlData = loader.loadFromString(rawYamlString) as? List<*> ?: return RepoFetchResult(errorMessage = "repo_parse_failed")

            val modulesList = yamlData.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                val id = map["id"] as? String
                if (id.isNullOrBlank()) return@mapNotNull null
                RemoteModule(
                    id = id,
                    name = map["name"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    author = map["author"] as? String ?: "",
                    version = map["version"] as? String ?: "",
                    versionCode = map["versionCode"] as? String ?: "",
                    repository = map["repository"] as? String ?: ""
                )
            }
            RepoFetchResult(modules = modulesList, rawResponse = rawYamlString)
        } catch (e: Exception) {
            RepoFetchResult(errorMessage = "repo_load_failed")
        }
    }

    // تنزيل ملفات وحدة معينة من مستودعها على GitHub.
    suspend fun downloadModule(remoteModule: RemoteModule): Boolean {
        return withContext(Dispatchers.IO) {
            try {
            	triggerVisitorBadge(remoteModule)
                // تنزيل module.prop أولاً للحصول على المعرف.
                val modulePropContent = downloadFileContent(remoteModule, "module.prop")
                if (modulePropContent.isNullOrEmpty()) return@withContext false

                val props = Properties()
                props.load(modulePropContent.reader())
                val moduleId = props.getProperty("id") ?: return@withContext false

                val moduleDir = File(modulesRootPath, moduleId)
                if (!moduleDir.exists()) moduleDir.mkdirs()

                // حفظ الملفات التي تم تنزيلها.
                saveContentToFile(modulePropContent, File(moduleDir, "module.prop"))
                downloadAndSaveFile(remoteModule, "on", moduleDir) // اسم الملف on بدون امتداد
                downloadAndSaveFile(remoteModule, "off", moduleDir) // اسم الملف off بدون امتداد

                true
            } catch (e: Exception) {
                false
            }
        }
    }

    // بناء رابط الملف الخام على GitHub.
    private fun getRawFileUrl(repoUrl: String, fileName: String): String {
        val path = repoUrl.removePrefix("https://github.com/")
        return "https://raw.githubusercontent.com/$path/main/$fileName"
    }

   // زيارة رابط badge لتسجيل زيارة للمستودع
	private suspend fun triggerVisitorBadge(remoteModule: RemoteModule) {
	    try {
	        val repoPath = remoteModule.repository.removePrefix("https://github.com/")
	        val badgeUrl = "https://visitor-badge.laobi.icu/badge?page_id=$repoPath"
	        client.get(badgeUrl)
	    } catch (e: Exception) {
	    }
	}
	
    private suspend fun downloadFileContent(remoteModule: RemoteModule, fileName: String): String? {
        return try {
            val url = getRawFileUrl(remoteModule.repository, fileName)
            client.get(url).bodyAsText()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun downloadAndSaveFile(remoteModule: RemoteModule, fileName: String, targetDir: File) {
        val url = getRawFileUrl(remoteModule.repository, fileName)
        val targetFile = File(targetDir, fileName)
        try {
            client.get(url).body<InputStream>().use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // لا يعتبر خطأ فادحًا إذا لم يتم العثور على الملف (قد يكون اختياريًا).
        }
    }

    private fun saveContentToFile(content: String, file: File) {
        file.writeText(content)
    }
}
package me.liuli.mashiro.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.liuli.mashiro.event.EventMethod
import me.liuli.mashiro.event.Listener
import me.liuli.mashiro.event.UpdateEvent
import me.liuli.mashiro.util.MinecraftInstance
import me.liuli.mashiro.util.client.ClientUtils
import me.liuli.mashiro.util.timing.TheTimer
import org.reflections.Reflections
import java.io.*
import java.nio.charset.StandardCharsets

class ConfigManager : MinecraftInstance(), Listener {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val rootPath = File(mc.mcDataDir, "Mashiro")
    val configPath = File(rootPath, "configs")
    val configSetFile = File(rootPath, "config.json")

    private val sections = mutableListOf<ConfigSection>()
    private val timer = TheTimer()

    var nowConfig = "default"
    var configFile = File(configPath, "$nowConfig.json")

    init {
        // 使用Reflections自动加载sections
        val reflections = Reflections("${this.javaClass.`package`.name}.sections")
        val subTypes: Set<Class<out ConfigSection>> = reflections.getSubTypesOf(ConfigSection::class.java)
        for (theClass in subTypes) {
            try {
                sections.add(theClass.newInstance())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 初始化文件夹
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }

        if (!configPath.exists()) {
            configPath.mkdirs()
        }
    }

    fun load(name: String) {
        if (nowConfig != name) {
            save() // 保存老配置
        }

        nowConfig = name
        configFile = File(configPath, "$nowConfig.json")

        val json = if (configFile.exists()) {
            JsonParser().parse(BufferedReader(FileReader(configFile))).asJsonObject
        } else {
            JsonObject() // 这样方便一点,虽然效率会低
        }

        for (section in sections) {
            section.load(if (json.has(section.sectionName)) { json.getAsJsonObject(section.sectionName) } else { null })
        }

        if (!configFile.exists()) {
            save()
        }

        saveConfigSet()

        ClientUtils.logInfo("Config $nowConfig.json loaded.")
    }

    fun reload() {
        load(nowConfig)
    }

    fun save() {
        val config = JsonObject()

        for (section in sections) {
            config.add(section.sectionName, section.save())
        }

        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(configFile), StandardCharsets.UTF_8))
        writer.write(gson.toJson(config))
        writer.close()

        saveConfigSet()

        ClientUtils.logInfo("Config $nowConfig.json saved.")
    }

    fun loadDefault() {
        val configSet = if (configSetFile.exists()) { JsonParser().parse(BufferedReader(FileReader(configSetFile))).asJsonObject } else { JsonObject() }

        load(if (configSet.has("file")) {
            configSet.get("file").asString
        } else {
            "default"
        })
    }

    fun saveConfigSet() {
        val configSet = JsonObject()

        configSet.addProperty("file", nowConfig)

        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(configSetFile), StandardCharsets.UTF_8))
        writer.write(gson.toJson(configSet))
        writer.close()
    }

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        if (timer.hasTimePassed(60 * 1000L)) { // save it every minute
            ClientUtils.logInfo("Autosaving $nowConfig.json")
            timer.reset()
            save()
        }
    }

    override fun listen() = true
}
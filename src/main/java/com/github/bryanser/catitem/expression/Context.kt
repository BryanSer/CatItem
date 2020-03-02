package com.github.bryanser.catitem.expression

import com.github.bryanser.catitem.Main
import com.github.bryanser.catitem.Tools
import com.github.bryanser.catitem.expression.impl.DEBUG
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class Context(config: ConfigurationSection) {
    private val localVariable = mutableMapOf<String, Variable>()

    init {
        for (key in config.getKeys(false)) {
            localVariable[key.replace("$", "")] = Variable.loadVariable(config, key)
        }
    }

    fun getVariable(name: String, player: Player): String? {
        val lv = localVariable[name]
        if (lv != null) {
            val t = lv.invokeValue(player, this)
            return t
        }
        if (DEBUG) {
            println("查找全局变量$name")
        }
        val g = globalContext.localVariable[name]
        if (g != null) {
            val t = g.invokeValue(player, globalContext)
            return t
        }
        return null
    }

    companion object {
        lateinit var globalContext: Context
        val tempVar = mutableMapOf<String, String>()

        fun init() {
            val f = File(Main.Plugin.dataFolder, "global.yml")
            if (!f.exists()) {
                Tools.saveResource(Main.Plugin, "global.yml")
            }
            val config = YamlConfiguration.loadConfiguration(f)
            val section = config.getConfigurationSection("GlobalVariable")!!
            globalContext = Context(section)
        }
    }
}
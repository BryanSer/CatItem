package com.github.bryanser.catitem.expression

import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.bukkit.plugin.Plugin

object ScriptManager {

    fun createScriptEngine(plugin: Plugin): NashornScriptEngine {
        val factory = NashornScriptEngineFactory()
        val eng = factory.getScriptEngine(plugin.javaClass.classLoader) as NashornScriptEngine
        return eng
    }

}
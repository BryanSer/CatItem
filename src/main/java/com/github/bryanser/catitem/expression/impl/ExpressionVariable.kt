package com.github.bryanser.catitem.expression.impl

import com.github.bryanser.catitem.Main
import com.github.bryanser.catitem.expression.Context
import com.github.bryanser.catitem.expression.ScriptManager
import com.github.bryanser.catitem.expression.Variable
import jdk.nashorn.api.scripting.NashornScriptEngine
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.text.NumberFormat

class ExpressionVariable(
        config: ConfigurationSection,
        name: String
) : Variable(config, name) {
    val numberFormat: DecimalFormat
    val papiVar = mutableMapOf<String, String>()
    val locVar = mutableMapOf<String, String>()
    val script: NashornScriptEngine

    init {
        val section = config.getConfigurationSection(name)!!
        val input = section.getString("expression")!!
        numberFormat = DecimalFormat(section.getString("numberFormat")!!)
        val papi = EzExpressionVariable.papiPattern.matcher(input)
        var script = input
        var index = 0
        while (papi.find()) {
            val pattern = papi.group("pattern")
            val name = papi.group("name")
            if (!papiVar.containsKey(pattern)) {
                papiVar[pattern] = "var$index"
                index++
            }
        }
        val varm = EzExpressionVariable.varPattern.matcher(input)
        while (varm.find()) {
            val pattern = varm.group("pattern")
            val name = varm.group("name")
            if (!locVar.containsKey(name)) {
                locVar[name] = "var$index"
                index++
            }
        }
        var args = ""
        for ((p, v) in papiVar) {
            script = script.replace("$p", v)
            if (args.isNotEmpty()) {
                args += ","
            }
            args += v
        }
        for ((p, v) in locVar) {
            script = script.replace("\$$p\$", v)
            if (args.isNotEmpty()) {
                args += ","
            }
            args += v
        }
        val scrr = """
            function calc($args){
                return $script;
            }
            """.trimIndent()
        val t = ScriptManager.createScriptEngine(Main.Plugin)
        if (DEBUG) {
            println("编译脚本 $name: $scrr")
        }
        t.eval(scrr)
        this.script = t
    }

    override fun getValue(p: Player, context: Context): String? {
        val vari = mutableListOf<Any?>()
        for ((par, _) in papiVar) {
            val r = PlaceholderAPI.setPlaceholders(p, par)
            if (r != null && r.isNotEmpty()) {
                vari += r.toDouble()
            } else {
                vari += 0.0
            }
        }
        for ((par, _) in locVar) {
            val r = context.getVariable(par, p)
            vari += if (r != null && r.isNotEmpty()) {
                try {
                    r.toDouble()
                } catch (e: Throwable) {
                    r
                }
            } else {
                r
            }
        }
        val d = script.invokeFunction("calc", *vari.toTypedArray())
        if (DEBUG) {
            println("计算变量$name 参数表: $vari 结果$d")
        }
        if(d is Number){
            return numberFormat.format(d)
        }
        var value: Any? = null
        if (d is String) {
            try {
                value = d.toDouble()
            } catch (e: Throwable) {
                value = d
            }
        }
        return if (value is Number) {
            numberFormat.format(value)
        } else {
            value.toString()
        }
    }
}
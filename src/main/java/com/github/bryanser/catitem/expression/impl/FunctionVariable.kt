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

class FunctionVariable(
        config: ConfigurationSection,
        name: String
) : Variable(config, name) {
    var numberFormat: NumberFormat? = null
    val function: NashornScriptEngine

    class PAPIHolder(
            val player: Player
    ) {
        fun get(key: String): Any {
            val result = PlaceholderAPI.setPlaceholders(player, key)
            try {
                return result.toDouble()
            } catch (t: Throwable) {
                return result
            }
        }
    }

    class ContextHolder(
            val player: Player,
            val context:Context
    ){
        fun get(key: String): Any? {
            val result = context.getVariable(key,player)
            try {
                return result?.toDouble()
            } catch (t: Throwable) {
                return result
            }
        }
    }

    init {
        val func = config.getString("$name.function")!!
        if(config.contains("$name.numberFormat")) {
            numberFormat = DecimalFormat(config.getString("$name.numberFormat"))
        }
        val script = """
            function calc(papi, player, context){
                $func
            }
        """.trimIndent()
        val t = ScriptManager.createScriptEngine(Main.Plugin)
        if (DEBUG) {
            println("编译脚本: $script")
        }
        t.eval(script)
        this.function = t
    }

    override fun getValue(p: Player, context: Context): String? {
        val papi = PAPIHolder(p)
        val ct = ContextHolder(p,context)
        val d = function.invokeFunction("calc",papi,p,ct)
        if(d is Number){
            return numberFormat?.format(d) ?: d.toString()
        }
        var value:Any? = null
        if(d is String){
            try {
                value = d.toDouble()
            }catch (e:Throwable){
                value = d
            }
        }
        return numberFormat?.format(value) ?: value.toString()
    }
}
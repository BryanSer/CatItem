package com.github.bryanser.catitem.expression

import com.github.bryanser.catitem.expression.impl.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

abstract class Variable(
        config: ConfigurationSection,
        val name: String
) {
    val cache = !name.contains("$")

    protected abstract fun getValue(p: Player, context: Context): String?

    fun invokeValue(p: Player, context: Context): String? {
        val tmp = Context.tempVar[name]
        if (tmp != null && cache) {
            if (DEBUG) {
                println("§6读取缓存$name 结果: $tmp")
            }
            return tmp
        }
        val t = this.getValue(p, context)
        if (t != null && cache) {
            Context.tempVar[name] = t
        }
        if (DEBUG) {
            println("§6变量$name 结果: $t")
        }
        return t
    }

    companion object {
        fun loadVariable(config: ConfigurationSection, name: String): Variable {
            if (config.isInt(name) || config.isDouble(name) || config.isBoolean(name)) {
                return ConstVariable(config, name)
            }
            if (config.isString(name)) {
                return EzExpressionVariable(config, name)
            }
            if (config.isConfigurationSection(name)) {
                val section = config.getConfigurationSection(name)!!
                if (section.contains("expression")) {
                    return ExpressionVariable(config, name)
                } else if (section.contains("function")) {
                    return FunctionVariable(config, name)
                }
                throw IllegalArgumentException("表达式类型异常")
            }
            if (config.isList(name)) {
                return WeightVariable(config, name)
            }
            val wv = config.get(name)
            throw IllegalArgumentException("表达式类型异常${wv}")
        }
    }
}
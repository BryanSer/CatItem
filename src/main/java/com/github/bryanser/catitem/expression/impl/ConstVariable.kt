package com.github.bryanser.catitem.expression.impl

import com.github.bryanser.catitem.expression.Context
import com.github.bryanser.catitem.expression.Variable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class ConstVariable(
        config: ConfigurationSection,
        name: String
) : Variable(config, name) {
    val value: String? = config.getString(name)

    override fun getValue(p: Player, context: Context): String? = value
}
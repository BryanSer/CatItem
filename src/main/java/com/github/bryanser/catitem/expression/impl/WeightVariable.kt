package com.github.bryanser.catitem.expression.impl

import com.github.bryanser.catitem.expression.Context
import com.github.bryanser.catitem.expression.Variable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.util.*
import kotlin.random.Random

class WeightVariable(
        config: ConfigurationSection,
        name: String
) : Variable(config, name) {
    val entrys = mutableListOf<Entry>()
    val maxWeight: Int

    init {
        val list = config.getMapList(name)
        for (map in list) {
            val weight = map.get("weight") as Int
            val tmp = YamlConfiguration()
            val randomName = UUID.randomUUID().toString()
            if (map.containsKey("value")) {
                tmp[randomName] = map["value"]
            } else {
                val tmpsection = tmp.createSection(randomName)
                for ((k, v) in map) {
                    if (k != "weight") {
                        tmpsection[k as String] = v
                    }
                }
            }
            val v = loadVariable(tmp, randomName)
            entrys += Entry(weight, v)
        }
        maxWeight = entrys.map(Entry::weight).sum()
    }

    data class Entry(
            val weight: Int,
            val variable: Variable
    )

    override fun getValue(p: Player, context: Context): String? {
        var random = Random.Default.nextInt(maxWeight)
        for ((w, v) in entrys) {
            if (random < w) {
                return v.invokeValue(p, context)
            }
            random -= w
        }
        return null
    }
}
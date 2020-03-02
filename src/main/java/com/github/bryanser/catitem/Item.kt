package com.github.bryanser.catitem

import com.github.bryanser.catitem.expression.Context
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.regex.Pattern

class Item(
        file: File
) {
    val fileName: String

    val material: String
    val durability: Short
    val name: String
    val lore: List<String>
    val inftyDurability: Boolean
    val flags = mutableListOf<ItemFlag>()
    val enchs = mutableListOf<Pair<Enchantment, Int>>()

    val localVars = mutableSetOf<String>()
    val papiVars = mutableSetOf<String>()

    val context: Context

    init {
        val config = YamlConfiguration.loadConfiguration(file)
        fileName = config.getString("Name")!!
        context = Context(config.getConfigurationSection("Variable")!!)
        val item = config.getConfigurationSection("Item")!!
        material = item.getString("type")!!
        addVar(material)
//        if (item.isInt("type")) {
//            material = byId(item.getInt("type"))
//        } else {
//            material = Material.matchMaterial(item.getString("type")!!)!!
//        }
        durability = item.getInt("durability").toShort()
        name = ChatColor.translateAlternateColorCodes('&', item.getString("name")!!).replace("$$", P_HOLDER)
        lore = item.getStringList("lore").map {
            ChatColor.translateAlternateColorCodes('&', it).replace("%%", D_HOLDER)
        }
        for (flag in item.getStringList("flags")) {
            flags += ItemFlag.valueOf(flag)
        }
        for (ench in item.getStringList("enchs")) {
            val spl = ench.split(":")
            enchs += Enchantment.getByName(spl[0])!! to spl[1].toInt()
        }
        inftyDurability = item.getBoolean("inftyDurability", false)
        addVar(name)
        lore.forEach(::addVar)
    }


    fun createItem(p: Player, amount: Int = 1): ItemStack {
        Context.tempVar.clear()
        val item = ItemStack(Material.matchMaterial(replace(material, p)), amount)
        item.durability = durability
        val im = item.itemMeta!!
        val tname = replace(name, p)
        if (tname != null) {
            im.setDisplayName(tname)
        }
        val tlore = mutableListOf<String>()
        for (s in lore) {
            val rep = replace(s, p) ?: continue
            if (rep.contains("\n")) {
                for (t in rep.split("\n")) {
                    tlore += t
                }
            } else {
                tlore += rep
            }
        }
        im.lore = tlore
        if (inftyDurability) {
            im.isUnbreakable = true
        }
        for (flag in flags) {
            im.addItemFlags(flag)
        }
        item.itemMeta = im
        for ((e, lv) in enchs) {
            item.addUnsafeEnchantment(e, lv)
        }
        return item
    }

    private fun replace(str: String, p: Player): String? {
        var s = str
        for (v in papiVars) {
            val t = PlaceholderAPI.setPlaceholders(p, v)
            s = s.replace(v, t)
        }
        for (l in localVars) {
            val t = context.getVariable(l, p) ?: return null
            s = s.replace("\$$l\$", t)
        }
        s = s.replace(P_HOLDER, "%").replace(D_HOLDER, "$")
        return s
    }

    private fun addVar(str: String) {
        val pm = papiPattern.matcher(str)
        while (pm.find()) {
            val pattern = pm.group("pattern")
            papiVars += pattern
        }
        val vm = varPattern.matcher(str)
        while (vm.find()) {
            val name = vm.group("name")
            localVars += name
        }
    }

    companion object {
        const val P_HOLDER = "<USE_FOR_P>"
        const val D_HOLDER = "<USE_FOR_D>"

        val byId: (Int) -> Material by lazy {
            val method = Material::class.java.getMethod("getMaterial", Integer.TYPE)
            method.isAccessible = true
            val m = java.lang.invoke.MethodHandles.lookup().unreflect(method).bindTo(null)
            return@lazy { it: Int ->
                m.invoke(it) as? Material ?: throw IllegalArgumentException()
            }
        }

        val papiPattern = Pattern.compile("(?<pattern>%(?<name>[^%]+)%)")
        val varPattern = Pattern.compile("(?<pattern>\\\$(?<name>[^\$]+)\\\$)")

        val items = mutableMapOf<String, Item>()
        fun init() {
            items.clear()
            val folder = File(Main.Plugin.dataFolder, "items")
            if (!folder.exists()) {
                folder.mkdirs()
                Tools.saveResource(Main.Plugin, "example.yml", folder)
            }
            for (f in folder.listFiles()) {
                load(f)
            }
        }

        fun load(f: File) {
            if (f.isDirectory) {
                for (e in f.listFiles()) {
                    load(e)
                }
                return
            }
            val item = Item(f)
            items.put(item.fileName, item)
        }
    }

}
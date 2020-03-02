package com.github.bryanser.catitem

import com.github.bryanser.catitem.expression.Context
import com.github.bryanser.catitem.expression.impl.DEBUG
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        Plugin = this
        Context.init()
        Item.init()
    }

    override fun onDisable() {
    }


    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.isOp) {
            return true
        }
        if (args.isEmpty() || args[0].equals("help", true)) {
            return false
        }
        if (args[0].equals("reload", true)) {
            DEBUG = args.getOrNull(1)?.toBoolean() == true
            Context.init()
            Item.init()
            sender.sendMessage("§6重载完成")
            return true
        }
        if (args[0].equals("give", true) && args.size >= 3) {
            val p = Bukkit.getPlayerExact(args[1])
            if (p == null) {
                sender.sendMessage("§c找不到玩家")
                return true
            }
            val item = Item.items[args[2]]
            if (item == null) {
                sender.sendMessage("§c找不到物品")
                return true
            }
            var amount = 1
            if (args.size > 3) {
                amount = args[3].toInt()
            }
            val i = item.createItem(p, amount)
            p.inventory.addItem(i)
            return true
        }


        return false
    }

    companion object {
        lateinit var Plugin: Main
    }
}
package com.github.bryanser.catitem

import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Tools {

    /**
     * 数据插件jar里的文件
     *
     * @param p 插件
     * @param res 资源文件名 如config.yml
     * @param fold 目标文件夹 若为null则默认插件配置文件夹
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun saveResource(p: Plugin, res: String, fold: File? = null) {
        var fold = fold
        p.getResource(res).use { data ->
            if (data == null) {
                return
            }
            if (fold == null) {
                fold = p.getDataFolder()
                if (!fold!!.exists()) {
                    fold!!.mkdirs()
                }
            }
            if (!fold!!.exists()) {
                fold!!.mkdirs()
            }
            val f = File(fold, res)

            if (!f.exists()) {
                f.createNewFile()
            }
            FileOutputStream(f).use { fos ->
                while (true) {
                    val i = data!!.read()
                    if (i == -1) {
                        break
                    }
                    fos.write(i)
                }
            }
        }
    }
}
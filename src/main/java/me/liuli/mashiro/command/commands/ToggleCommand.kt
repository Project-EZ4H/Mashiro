package me.liuli.mashiro.command.commands

import me.liuli.mashiro.Mashiro
import me.liuli.mashiro.command.Command

class ToggleCommand : Command("toggle","Allow you toggle modules without open ClickGui", arrayOf("t")) {
    override fun exec(args: Array<String>) {
        if(args.isNotEmpty()){
            args.forEach {
                if(it.isBlank())
                    return@forEach

                val module=Mashiro.moduleManager.getModule(it)
                if(module==null){
                    chat("Module \"$it\" not found.")
                }else{
                    module.toggle()
                    chat("Toggled module \"${module.name}\" ${if(module.state){"§aON"}else{"§cOFF"}}")
                }
            }
            return
        }
        chatSyntax("<module>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val moduleName = args[0]

        return Mashiro.moduleManager.modules
            .map { it.name }
            .filter { it.startsWith(moduleName, true) }
            .toList()
    }
}
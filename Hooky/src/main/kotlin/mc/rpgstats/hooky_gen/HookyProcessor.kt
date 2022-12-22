package mc.rpgstats.hooky_gen

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class HookyProcessor(val environment: SymbolProcessorEnvironment, val codeGenerator: CodeGenerator, val logger: KSPLogger) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList() else invoked = true

        @Suppress("UNCHECKED_CAST")
        val commands = resolver.getSymbolsWithAnnotation("mc.rpgstats.hooky_gen.api.Command").toList() as List<KSClassDeclaration>

        commands.forEach {
            logger.info(it::class.qualifiedName.toString() )
        }

        logger.info("Generating file")
        val commandsFile = codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "mc.rpgstats",
            "Hooky",
            "kt"
        )

        commandsFile.write("""
        package mc.rpgstats
            
        object Hooky {
            fun registerAll() {
                org.quiltmc.qsl.command.api.CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
                    ${commands.joinToString(separator = "\n                    ") { it.qualifiedName!!.asString()+".register(dispatcher)" }}
                }
            }
        }
        """.trimIndent().toByteArray())

        return emptyList()
    }
}
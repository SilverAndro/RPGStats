package mc.rpgstats.hooky_gen.api

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RegisterOn(val event: String)

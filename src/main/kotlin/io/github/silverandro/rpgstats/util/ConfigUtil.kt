package io.github.silverandro.rpgstats.util

import org.quiltmc.config.api.Config
import org.quiltmc.config.api.Constraint
import org.quiltmc.config.api.annotations.Comment
import org.quiltmc.config.api.annotations.FloatRange
import org.quiltmc.config.api.annotations.IntegerRange
import org.quiltmc.config.api.values.TrackedValue
import java.util.function.Consumer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

abstract class KotlinConfig : Config.Creator {
    private val fields = mutableListOf<TrackedValue<*>>()
    private val sections = mutableMapOf<String, Consumer<Config.SectionBuilder>>()

    fun <T : Any> value(default: T): ValueProvider<Any, T> {
        return ValueProvider(fields::add, default)
    }

    fun <T : Section> section(section: T): SectionProvider<Any, T> {
        return SectionProvider(sections::put, section)
    }

    override fun create(builder: Config.Builder) {
        fields.forEach {
            builder.field(it)
        }
        sections.forEach { (name, creator) ->
            builder.section(name, creator)
        }
    }

    open class Section {
        private val fields = mutableListOf<TrackedValue<*>>()
        private val sections = mutableMapOf<String, Consumer<Config.SectionBuilder>>()

        fun <T : Any> value(default: T): ValueProvider<Any, T> {
            return ValueProvider(fields::add, default)
        }

        fun <T : Section> section(section: T): SectionProvider<Any, T> {
            return SectionProvider(sections::put, section)
        }

        fun create(builder: Config.SectionBuilder) {
            fields.forEach {
                builder.field(it)
            }
            sections.forEach { (name, creator) ->
                builder.section(name, creator)
            }
        }
    }
}

class SectionProvider<C, T : KotlinConfig.Section>(
    val register: (String, Consumer<Config.SectionBuilder>)->Unit,
    val section: T
) {
    operator fun provideDelegate(
        thisRef: C,
        prop: KProperty<*>
    ): ReadOnlyProperty<C, T> {
        register(prop.name) { section.create(it) }
        return ReadOnlyProperty { _, _ -> return@ReadOnlyProperty section }
    }
}

class ValueProvider<C, T : Any>(val register: (TrackedValue<T>) -> Unit, val default: T) {
    operator fun provideDelegate(
        thisRef: C,
        prop: KProperty<*>
    ): TrackingProperty<C, T> {
        val tracker = TrackedValue.create(default, prop.name) { builder ->
            prop.javaField?.annotations?.forEach { annotation ->
                when (annotation) {
                    is Comment -> builder.metadata(Comment.TYPE) { println(annotation.value); it.add(*annotation.value) }
                    is FloatRange -> assertConstraint(default, builder, annotation)
                    is IntegerRange -> assertConstraint(default, builder, annotation)
                }
            }
        }
        register(tracker)
        return TrackingProperty(tracker)
    }
}

class TrackingProperty<C, T>(private val tracker: TrackedValue<T>) {
    operator fun getValue(thisRef: C, property: KProperty<*>): T {
        return tracker.value()
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> assertConstraint(default: T, builder: TrackedValue.Builder<*>, range: FloatRange) {
    when (default) {
        is Float -> (builder as TrackedValue.Builder<Float>).constraint(Constraint.range(range.min.toFloat(), range.max.toFloat()))
        is Double -> (builder as TrackedValue.Builder<Double>).constraint(Constraint.range(range.min, range.max))
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> assertConstraint(default: T, builder: TrackedValue.Builder<*>, range: IntegerRange) {
    when (default) {
        is Int -> (builder as TrackedValue.Builder<Int>).constraint(Constraint.range(range.min.toInt(), range.max.toInt()))
        is Long -> (builder as TrackedValue.Builder<Long>).constraint(Constraint.range(range.min, range.max))
    }
}

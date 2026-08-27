# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Room generates code at compile time; annotations are only needed then.
-dontwarn androidx.room.**

# Keep :core model/solver classes fully — they are (de)serialized to/from plain strings by our
# own Room converters, and reflection is never used on them, but keeping them avoids any
# surprises from aggressive R8 optimization on a module that ships as a plain Kotlin library.
-keep class com.sudokuai.core.** { *; }

# Kotlin coroutines / serialization internals frequently referenced by ProGuard cookbooks.
-dontwarn kotlinx.coroutines.**

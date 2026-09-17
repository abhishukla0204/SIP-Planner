# kotlinx.serialization keeps its generated serializers on the companion.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.abhinav.sipplanner.** {
    *** Companion;
}
-keepclasseswithmembers class com.abhinav.sipplanner.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit interfaces are referenced only reflectively.
-keep,allowobfuscation interface com.abhinav.sipplanner.data.remote.MfApi
-keepattributes Signature, Exceptions

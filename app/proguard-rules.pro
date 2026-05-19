# ── Retrofit ──────────────────────────────────────────────────────────────────
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ── Gson ──────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ── Modelos da aplicação (serializados pelo Gson) ─────────────────────────────
-keep class com.edu.matchvagasempresas.model.** { *; }

# ── Navegação e fragments ─────────────────────────────────────────────────────
-keep class * extends androidx.fragment.app.Fragment { *; }
-keepattributes SourceFile,LineNumberTable

# ── Material/AppCompat ────────────────────────────────────────────────────────
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

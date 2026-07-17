# Proguard rules for Cashi Pay Application

# Keep serialization DTO models
-keep class com.cashi.cashichallengev1.domain.model.** { *; }

# Keep Koin classes and annotations
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}
-keep class org.koin.** { *; }

# Keep Ktor HTTP classes
-keep class io.ktor.** { *; }

# Keep Firebase Firestore DTO reflection
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# --- gRPC Proguard Keep Rules (Required by Firestore Write/Watch streams) ---
-keep class io.grpc.** { *; }
-dontwarn io.grpc.**

# Keep OkHttp & Okio (used by gRPC transport layer)
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Protobuf & Google Common classes
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**
-keep class com.google.common.html.** { *; }
-keep class com.google.common.logging.** { *; }
-dontwarn com.google.common.**

# Keep Firebase Firestore internal implementations
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Keep Google Play Services ProviderInstaller (resolves ProviderInstaller load warning)
-keep class com.google.android.gms.common.security.ProviderInstallerImpl { *; }
-keep class com.google.android.gms.providerinstaller.** { *; }
-dontwarn com.google.android.gms.**

-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

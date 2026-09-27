# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep domain/data models used by Room + osmdroid config
-keep class com.geoalarm.app.data.local.** { *; }
-keep class com.geoalarm.app.domain.model.** { *; }
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

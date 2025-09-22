# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# TensorFlow Lite ProGuard rules
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }

# Keep TensorFlow Lite Interpreter and Options classes
-keep class org.tensorflow.lite.Interpreter { *; }
-keep class org.tensorflow.lite.Interpreter$Options { *; }

# Keep all model-related classes
-keep class * extends org.tensorflow.lite.support.model.Model { *; }

# Suppress warnings for TensorFlow Lite
-dontwarn org.tensorflow.lite.**
-dontwarn org.tensorflow.lite.support.**
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

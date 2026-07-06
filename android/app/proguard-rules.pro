# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools directory.
# 
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

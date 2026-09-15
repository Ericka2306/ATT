// Fichier de build racine : déclare les plugins sans les appliquer.
// Pas de plugin org.jetbrains.kotlin.android : AGP 9 embarque Kotlin.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

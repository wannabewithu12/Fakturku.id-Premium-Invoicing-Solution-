package com.fakturkuid.app.ui.theme

import androidx.compose.ui.graphics.Color

// High-Class Gen-Z Palette
val NavyDeep = Color(0xFF020617) // Ultra Dark Deep Navy
val NavySurface = Color(0xFF0E1527) // Refined surface for depth
val ElectricCyan = Color(0xFF00D1FF) // Vibrant Electric Cyan
val DeepCyan = Color(0xFF0085FF) // Strategic secondary cyan
val PureWhite = Color(0xFFF1F5F9) // Clean scannable white
val GhostWhite = Color(0xFF94A3B8)

// Status Colors
val SuccessTeal = Color(0xFF2DD4BF)
val ErrorRose = Color(0xFFFB7185)
val WarningAmber = Color(0xFFFBBF24)

// Light Mode (High Contrast)
val PrimaryColor = Color(0xFF0F172A)
val PrimaryLightColor = DeepCyan
val SecondaryColor = Color(0xFF0EA5E9)
val AccentColor = ElectricCyan

val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF020617)

// Glassmorphism
val GlassCyan = ElectricCyan.copy(alpha = 0.1f)
val GlassWhite = Color.White.copy(alpha = 0.05f)
val GlassBorder = Color.White.copy(alpha = 0.1f)

// Dark mode overrides
val BackgroundDark = NavyDeep
val SurfaceDark = NavySurface
val OnSurfaceDark = PureWhite

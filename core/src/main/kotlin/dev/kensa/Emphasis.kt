package dev.kensa

import dev.kensa.util.unCamelToSeparated

// Each Colour is mapped to Bulma colours
enum class Colour(private val bulmaPrefix: String = "has") {
    BackgroundWhite,
    BackgroundBlack,
    BackgroundLight,
    BackgroundDark,
    BackgroundPrimary,
    BackgroundLink,
    BackgroundInfo,
    BackgroundSuccess,
    BackgroundWarning,
    BackgroundDanger,
    BackgroundBlackBis,
    BackgroundBlackTer,
    BackgroundGreyDarker,
    BackgroundGreyDark,
    BackgroundGrey,
    BackgroundGreyLight,
    BackgroundGreyLighter,
    BackgroundWhiteTer,
    BackgroundWhiteBis,
    BackgroundPrimaryLight,
    BackgroundLinkLight,
    BackgroundInfoLight,
    BackgroundSuccessLight,
    BackgroundWarningLight,
    BackgroundDangerLight,
    BackgroundPrimaryDark,
    BackgroundLinkDark,
    BackgroundInfoDark,
    BackgroundSuccessDark,
    BackgroundWarningDark,
    BackgroundDangerDark,
    Default,
    TextWhite,
    TextBlack,
    TextLight,
    TextDark,
    TextPrimary,
    TextLink,
    TextInfo,
    TextSuccess,
    TextWarning,
    TextDanger,
    TextBlackBis,
    TextBlackTer,
    TextGreyDarker,
    TextGreyDark,
    TextGrey,
    TextGreyLight,
    TextGreyLighter,
    TextWhiteTer,
    TextWhiteBis,
    TextPrimaryLight,
    TextLinkLight,
    TextInfoLight,
    TextSuccessLight,
    TextWarningLight,
    TextDangerLight,
    TextPrimaryDark,
    TextLinkDark,
    TextInfoDark,
    TextSuccessDark,
    TextWarningDark,
    TextDangerDark;

    fun asCss(): String = "$bulmaPrefix-${name.unCamelToSeparated()}"

}

enum class TextStyle(private val bulmaPrefix: String = "has") {
    TextWeightLight,
    TextWeightNormal,
    TextWeightMedium,
    TextWeightSemibold,
    TextWeightBold,
    TextDecorationUnderline,
    Capitalized("is"),
    Lowercase("is"),
    Uppercase("is"),
    Italic("is");

    fun asCss(): String = "$bulmaPrefix-${name.unCamelToSeparated()}"

}
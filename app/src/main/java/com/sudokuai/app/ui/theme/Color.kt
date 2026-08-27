package com.sudokuai.app.ui.theme

import androidx.compose.ui.graphics.Color

// Custom Material 3 color scheme (used whenever dynamic color is unavailable or disabled by the
// user's device, and always used as the deliberate brand palette on API < 31). A deep blue/teal
// was chosen to read calmly for long puzzle-solving sessions.

val Blue10 = Color(0xFF001E2E)
val Blue20 = Color(0xFF00344B)
val Blue30 = Color(0xFF004C6C)
val Blue40 = Color(0xFF00658F)
val Blue80 = Color(0xFF95CDF3)
val Blue90 = Color(0xFFCBE6FF)

val Teal10 = Color(0xFF00201D)
val Teal20 = Color(0xFF00372F)
val Teal30 = Color(0xFF005046)
val Teal40 = Color(0xFF186B5F)
val Teal80 = Color(0xFF83D5C4)
val Teal90 = Color(0xFF9FF2DF)

val Amber40 = Color(0xFFB25F00)
val Amber80 = Color(0xFFFFB871)
val Amber90 = Color(0xFFFFDDBA)

val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val Error90 = Color(0xFFFFDAD6)
val Error10 = Color(0xFF410002)

val Neutral10 = Color(0xFF1A1C1E)
val Neutral20 = Color(0xFF2F3033)
val Neutral90 = Color(0xFFE2E2E5)
val Neutral95 = Color(0xFFF0F0F3)
val Neutral99 = Color(0xFFFBF8FD)

val NeutralVariant30 = Color(0xFF43474E)
val NeutralVariant50 = Color(0xFF73777F)
val NeutralVariant80 = Color(0xFFC3C6CF)
val NeutralVariant90 = Color(0xFFDFE2EB)

// Board-specific colors that are not part of the Material scheme itself.
val CellSelected = Color(0xFF95CDF3)
val CellPeerHighlight = Color(0xFFDCEEFB)
val CellSameNumber = Color(0xFFCBE6FF)
val CellError = Color(0xFFFFDAD6)
val GivenDigitLight = Color(0xFF1A1C1E)
val EnteredDigitLight = Color(0xFF00658F)
val GivenDigitDark = Color(0xFFE2E2E5)
val EnteredDigitDark = Color(0xFF95CDF3)

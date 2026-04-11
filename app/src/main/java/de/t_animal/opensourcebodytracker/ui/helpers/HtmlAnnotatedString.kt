package de.t_animal.opensourcebodytracker.ui.helpers

import android.text.Spanned
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.core.text.HtmlCompat

@Composable
@ReadOnlyComposable
fun styledStringResource(@StringRes id: Int): AnnotatedString {
    val text = LocalContext.current.resources.getText(id)
    return if (text is Spanned) {
        AnnotatedString.fromHtml(text.toHtmlForAnnotatedString())
    } else {
        AnnotatedString(text.toString())
    }
}

/**
 * Converts a Spanned (from resources.getText) to an HTML string suitable for
 * AnnotatedString.fromHtml. Strips the <p> wrapper that toHtml adds, replacing
 * paragraph breaks with <br> so newlines are preserved.
 */
private fun Spanned.toHtmlForAnnotatedString(): String {
    return HtmlCompat.toHtml(this, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        .replace(Regex("""</p>\s*<p[^>]*>"""), "<br><br>")
        .replace(Regex("""<p[^>]*>|</p>"""), "")
        .trim()
}

package de.t_animal.opensourcebodytracker.feature.photos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun PhotosScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Photos Coming Soon")
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotosScreenPreview() {
    BodyTrackerTheme {
        PhotosScreen()
    }
}

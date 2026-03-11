package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
internal fun DemoModeBanner(
    onResetApp: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "You are currently using demo data.",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Reset the app to create your own profile.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onResetApp) {
                Text("Reset App")
            }
        }
    }
}

@Composable
internal fun ResetAppConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset App?") },
        text = {
            Text(
                text = buildAnnotatedString {
                    append("This will delete all app data and close the app.")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("You will have to restart it manually.")
                    }
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

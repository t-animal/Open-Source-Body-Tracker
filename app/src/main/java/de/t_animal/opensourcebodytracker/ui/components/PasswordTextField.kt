package de.t_animal.opensourcebodytracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    label: @Composable TextFieldLabelScope.() -> Unit = {},
    enabled: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val state = remember { TextFieldState(initialText = value) }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        snapshotFlow { state.text }.collect { newText ->
            onValueChange(newText.toString())
        }
    }

    LaunchedEffect(value) {
        if (value != state.text.toString()) {
            state.edit {
                replace(0, length, value)
            }
        }
    }

    OutlinedSecureTextField(
        state = state,
        textObfuscationMode = if (showPassword) {
            TextObfuscationMode.Companion.Visible
        } else {
            TextObfuscationMode.Companion.RevealLastTyped
        },
        enabled = enabled,
        modifier = modifier,
        label = label,
        trailingIcon = {
            Icon(
                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = stringResource(R.string.cd_toggle_password_visibility),
                modifier = Modifier.Companion
                    .requiredSize(40.dp)
                    .padding(8.dp)
                    .clickable { showPassword = !showPassword },
            )
        },
    )
}

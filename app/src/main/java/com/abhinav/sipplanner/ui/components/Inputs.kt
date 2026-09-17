package com.abhinav.sipplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * Label and current value sit on one line above the track.
 * When [isEditable] is true, the value on the right becomes an interactive text field
 * allowing precise keyboard input alongside the slider.
 */
@Composable
fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    isEditable: Boolean = false,
    prefix: String? = null,
    suffix: String? = null,
    isDecimal: Boolean = false,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = SipTheme.colors.muted,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))

            if (isEditable) {
                EditableValueInput(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    prefix = prefix,
                    suffix = suffix,
                    isDecimal = isDecimal,
                )
            } else {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Slider(
            value = value.coerceIn(valueRange),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = SipTheme.colors.principal,
                inactiveTrackColor = SipTheme.colors.sunken,
            ),
        )
    }
}

@Composable
private fun EditableValueInput(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    prefix: String?,
    suffix: String?,
    isDecimal: Boolean,
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(value = false) }

    fun formatValue(v: Float): String {
        return if (isDecimal) {
            if ((v % 1f) == 0f) v.toInt().toString() else "%.1f".format(v)
        } else {
            v.toLong().toString()
        }
    }

    var textState by remember { mutableStateOf(formatValue(value)) }

    LaunchedEffect(value, isFocused) {
        if (!isFocused) {
            textState = formatValue(value)
        }
    }

    Box(
        modifier = Modifier
            .background(
                color = SipTheme.colors.sunken,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else SipTheme.colors.hairline,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            prefix?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = SipTheme.colors.muted,
                )
            }
            BasicTextField(
                value = textState,
                onValueChange = { newText ->
                    val filtered = if (isDecimal) {
                        newText.filter { it.isDigit() || (it == '.') }
                    } else {
                        newText.filter { it.isDigit() }
                    }
                    textState = filtered

                    filtered.toFloatOrNull()?.let(onValueChange)
                },
                modifier = Modifier
                    .widthIn(min = 36.dp, max = 120.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (!focusState.isFocused) {
                            val parsed = textState.toFloatOrNull()
                            textState = if (parsed != null) {
                                val clamped = parsed.coerceIn(valueRange)
                                onValueChange(clamped)
                                formatValue(clamped)
                            } else {
                                formatValue(value)
                            }
                        }
                    },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    },
                ),
            )
            suffix?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = SipTheme.colors.muted,
                )
            }
        }
    }
}

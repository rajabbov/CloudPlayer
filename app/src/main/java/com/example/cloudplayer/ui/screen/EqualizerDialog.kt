package com.example.cloudplayer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cloudplayer.ui.theme.*

@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit
) {
    var bassLevel by remember { mutableFloatStateOf(500f) }
    var band1 by remember { mutableFloatStateOf(0f) }
    var band2 by remember { mutableFloatStateOf(0f) }
    var band3 by remember { mutableFloatStateOf(0f) }
    var band4 by remember { mutableFloatStateOf(0f) }
    var band5 by remember { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Audio Equalizer & Bass",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bass Boost
                Text(
                    text = "Bass Boost (${bassLevel.toInt()})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricCyan,
                    modifier = Modifier.align(Alignment.Start)
                )
                Slider(
                    value = bassLevel,
                    onValueChange = { bassLevel = it },
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5 Bands Sliders Side by Side
                Text(
                    text = "5-Band Frequency Response",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BandSliderColumn("60Hz", band1) { band1 = it }
                    BandSliderColumn("230Hz", band2) { band2 = it }
                    BandSliderColumn("910Hz", band3) { band3 = it }
                    BandSliderColumn("3.6kHz", band4) { band4 = it }
                    BandSliderColumn("14kHz", band5) { band5 = it }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkBackground)
                ) {
                    Text("Apply & Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BandSliderColumn(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -15f..15f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = ElectricCyan,
                activeTrackColor = ElectricCyan
            )
        )
    }
}

package com.ngyennhi.vehicleservicesimulator.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ngyennhi.vehicleservicesimulator.R
import com.ngyennhi.vehicleservicesimulator.model.VehicleState
import com.ngyennhi.vehicleservicesimulator.ui.theme.DashboardNormal
import com.ngyennhi.vehicleservicesimulator.ui.theme.DashboardWarning

@Composable
fun VehicleDashboard(
    vehicleState: VehicleState,
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.vehicle_name),
                style = MaterialTheme.typography.headlineMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .background(DashboardNormal, CircleShape)
                )
                Text(
                    text = if (vehicleState.speed == 0) {
                        stringResource(R.string.vehicle_stationary)
                    } else {
                        stringResource(R.string.vehicle_moving)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = DashboardNormal
                )
            }
        }

        MetricCard(
            label = stringResource(R.string.speed_label),
            value = stringResource(R.string.speed_value, vehicleState.speed),
            iconRes = R.drawable.ic_speed,
            featured = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = stringResource(R.string.battery_label),
                value = stringResource(R.string.battery_value, vehicleState.battery),
                iconRes = R.drawable.ic_battery,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = stringResource(R.string.temperature_label),
                value = stringResource(R.string.temperature_value, vehicleState.temperature),
                iconRes = R.drawable.ic_temperature,
                modifier = Modifier.weight(1f)
            )
        }

        MetricCard(
            label = stringResource(R.string.door_status_label),
            value = if (vehicleState.doorOpen) {
                stringResource(R.string.door_open)
            } else {
                stringResource(R.string.door_closed)
            },
            iconRes = R.drawable.ic_door,
            statusColor = if (vehicleState.doorOpen) DashboardWarning else DashboardNormal
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onStart,
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.start_button))
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(
                onClick = onStop,
                enabled = isRunning,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.stop_button))
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    statusColor: Color? = null
) {
    val iconColor = statusColor ?: MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(if (featured) 24.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (featured) 12.dp else 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
                Text(
                    text = label,
                    style = if (featured) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = if (featured) {
                    MaterialTheme.typography.displayMedium
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                color = when {
                    featured -> MaterialTheme.colorScheme.primary
                    statusColor != null -> statusColor
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

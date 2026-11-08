package io.github.salman7236.volumepercent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.salman7236.volumepercent.ui.theme.VolumePercentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Prefs.isServiceEnabled(this)) {
            val hasPermission = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                VolumeService.start(this)
            }
        }
        setContent {
            VolumePercentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Home(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Home(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(Prefs.isServiceEnabled(context)) }
    var startOnBoot by remember { mutableStateOf(Prefs.startOnBoot(context)) }

    fun enable() {
        Prefs.setServiceEnabled(context, true)
        enabled = true
        VolumeService.start(context)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) enable() }

    Column(
        modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        SettingRow("Show volume percentage", enabled) { on ->
            if (on) {
                val needsPermission = Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                if (needsPermission) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    enable()
                }
            } else {
                Prefs.setServiceEnabled(context, false)
                enabled = false
                VolumeService.stop(context)
            }
        }
        SettingRow("Start on boot", startOnBoot) { on ->
            Prefs.setStartOnBoot(context, on)
            startOnBoot = on
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    VolumePercentTheme {
        Home()
    }
}
package com.kadirsahin.hafizaoyunu.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.BuildConfig
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.data.AppSettings
import com.kadirsahin.hafizaoyunu.data.ThemeMode
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.GameDialog
import com.kadirsahin.hafizaoyunu.ui.components.GameTopBar
import com.kadirsahin.hafizaoyunu.ui.components.GlassCard
import com.kadirsahin.hafizaoyunu.ui.components.GlowButton
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.SoftButton
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.components.uppercaseForUi
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onDynamicColors: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onHowToPlay: () -> Unit,
    onResetProgress: () -> Unit,
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    AnimatedBackdrop(reducedMotion = LocalReducedMotion.current) {
        Column(Modifier.fillMaxSize()) {
            GameTopBar(stringResource(R.string.settings_title), onBack)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SettingsSection(stringResource(R.string.appearance)) {
                        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            ThemeMode.entries.forEach { mode ->
                                val title = when (mode) {
                                    ThemeMode.SYSTEM -> R.string.theme_system
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                }
                                Segment(stringResource(title), settings.themeMode == mode, Modifier.weight(1f)) { onThemeMode(mode) }
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Divider()
                            SwitchRow(Glyph.SPARK, stringResource(R.string.dynamic_colors), stringResource(R.string.dynamic_colors_description), settings.dynamicColorsEnabled, onDynamicColors)
                        }
                        Divider()
                        SwitchRow(Glyph.LEAF, stringResource(R.string.reduced_motion), stringResource(R.string.reduced_motion_description), settings.reducedMotion, onReducedMotion)
                    }
                }
                item {
                    SettingsSection(stringResource(R.string.gameplay)) {
                        SwitchRow(
                            if (settings.soundEnabled) Glyph.SOUND_ON else Glyph.SOUND_OFF,
                            stringResource(R.string.sound),
                            stringResource(R.string.sound_description),
                            settings.soundEnabled,
                            onSound,
                        )
                        Divider()
                        SwitchRow(Glyph.BOLT, stringResource(R.string.haptics), stringResource(R.string.haptics_description), settings.hapticsEnabled, onHaptics)
                        Divider()
                        ActionRow(Glyph.INFO, stringResource(R.string.how_to_play), stringResource(R.string.how_to_play_description), onHowToPlay)
                    }
                }
                item {
                    SettingsSection(stringResource(R.string.data_privacy)) {
                        Text(stringResource(R.string.privacy_summary), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        Divider()
                        ActionRow(Glyph.REPLAY, stringResource(R.string.reset_progress), stringResource(R.string.reset_progress_description), { confirmReset = true }, tint = GameTheme.colors.danger)
                    }
                }
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(
                                Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary))),
                                contentAlignment = Alignment.Center,
                            ) { GlyphIcon(Glyph.CARDS, tint = Color.White, size = 28.dp) }
                            Column {
                                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                                Text(stringResource(R.string.version_format, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                                Text(stringResource(R.string.modernized_with), style = MaterialTheme.typography.labelSmall, color = scheme.primary)
                            }
                        }
                    }
                }
            }
        }
        GameDialog(visible = confirmReset, onDismiss = { confirmReset = false }) {
            GlyphIcon(Glyph.INFO, tint = GameTheme.colors.danger, size = 40.dp)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.reset_progress_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.reset_progress_message), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            GlowButton(
                stringResource(R.string.reset),
                {
                    onResetProgress()
                    confirmReset = false
                },
                Modifier.fillMaxWidth(),
                colors = listOf(GameTheme.colors.danger, Color(0xFF9B1C5C)),
            )
            Spacer(Modifier.height(10.dp))
            SoftButton(stringResource(R.string.cancel), { confirmReset = false }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title.uppercaseForUi(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
        )
        GlassCard(Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
private fun Segment(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) scheme.onPrimary else scheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) scheme.primary else scheme.surfaceContainerHigh)
            .pressable(onClick = onClick)
            .padding(vertical = 11.dp),
    )
}

@Composable
private fun SwitchRow(glyph: Glyph, title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(glyph, tint = MaterialTheme.colorScheme.secondary, size = 22.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionRow(glyph: Glyph, title: String, description: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.secondary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .pressable(onClick = onClick, pressedScale = 0.98f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(glyph, tint = tint, size = 22.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = if (tint == GameTheme.colors.danger) tint else MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        GlyphIcon(Glyph.NEXT, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
    }
}

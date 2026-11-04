package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.CiWorkflowPreset
import com.example.ui.StudioUiState
import com.example.ui.WireGuardProfileDraft
import com.example.ui.i18n.JissrStrings
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.JissrAmber
import com.example.ui.theme.JissrCyan
import com.example.ui.theme.JissrEmerald
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun StudioScreen(
    strings: JissrStrings,
    wgDraft: WireGuardProfileDraft,
    studioState: StudioUiState,
    workflowYaml: String,
    jissrShareUri: String,
    onUpdateWgDraft: ((WireGuardProfileDraft) -> WireGuardProfileDraft) -> Unit,
    onGenerateKeypair: () -> Unit,
    onGeneratePsk: () -> Unit,
    onImportConfText: (String, String) -> Unit,
    onLoadActiveRelayPreset: () -> Unit,
    onSaveDraftToBridgeNodes: () -> Unit,
    onUpdateStudio: (CiWorkflowPreset?, String?, String?, Boolean?, Boolean?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPasteImportDialog by remember { mutableStateOf(false) }

    val liveConfPreview = remember(wgDraft) { wgDraft.toConfString() }
    val validationIssues = remember(wgDraft, strings.isArabic) {
        wgDraft.validate(strings.isArabic)
    }

    // Storage Access Framework: Import .conf File
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!text.isNullOrBlank()) {
                    val fileName = uri.lastPathSegment?.substringAfterLast('/')?.removeSuffix(".conf") ?: "Imported-WG0"
                    onImportConfText(text, fileName)
                }
            } catch (_: Exception) {
            }
        }
    }

    // Storage Access Framework: Export .conf File
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(liveConfPreview.toByteArray(Charsets.UTF_8))
                }
                onUpdateStudio(
                    null,
                    null,
                    null,
                    null,
                    null,
                    if (strings.isArabic) "تم تصدير وحفظ ملف ${wgDraft.profileName}.conf بنجاح"
                    else "Exported ${wgDraft.profileName}.conf to storage"
                )
            } catch (_: Exception) {
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("studio_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        studioState.statusBannerMessage?.let { msg ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = JissrEmerald.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, JissrEmerald)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = JissrEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = JissrEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 1. Config Studio Header & Import Actions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.2.dp, JissrEmerald.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = JissrEmerald.copy(alpha = 0.16f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = JissrEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.configStudioHeaderTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.configStudioHeaderSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { importFileLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("wg_import_file_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JissrEmerald,
                                contentColor = Color(0xFF032219)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.importFileBtn, style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { showPasteImportDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("wg_import_paste_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, JissrCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentPaste,
                                contentDescription = null,
                                tint = JissrCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.importPasteBtn, style = MaterialTheme.typography.labelLarge, color = JissrCyan)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isValid = validationIssues.isEmpty()
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isValid) JissrEmerald.copy(alpha = 0.14f) else JissrAmber.copy(alpha = 0.14f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isValid) Icons.Filled.CheckCircle else Icons.Filled.WarningAmber,
                                    contentDescription = null,
                                    tint = if (isValid) JissrEmerald else JissrAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isValid) {
                                        if (strings.isArabic) "ملف WireGuard صالح وجاهز" else "Valid WireGuard Profile"
                                    } else {
                                        validationIssues.first()
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isValid) JissrEmerald else JissrAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        TextButton(
                            onClick = onLoadActiveRelayPreset,
                            modifier = Modifier.testTag("wg_load_relay_preset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = JissrCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.loadActiveRelayBtn, style = MaterialTheme.typography.labelSmall, color = JissrCyan)
                        }
                    }
                }
            }
        }

        // 2. [Interface] Form Editor Card
        item {
            WgInterfaceFormCard(
                strings = strings,
                wgDraft = wgDraft,
                onUpdateWgDraft = onUpdateWgDraft,
                onGenerateKeypair = onGenerateKeypair
            )
        }

        // 3. [Peer] Form Editor Card
        item {
            WgPeerFormCard(
                strings = strings,
                wgDraft = wgDraft,
                onUpdateWgDraft = onUpdateWgDraft,
                onGeneratePsk = onGeneratePsk
            )
        }

        // 4. Live WireGuard .conf Preview & Export Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, JissrCyan.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VpnKey,
                            contentDescription = null,
                            tint = JissrCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.wireguardProfileTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianSurfaceVariant,
                        border = BorderStroke(1.dp, BorderSubtleDark)
                    ) {
                        Text(
                            text = liveConfPreview,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                lineHeight = 17.sp
                            ),
                            color = JissrCyan,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .testTag("wg_live_conf_preview")
                        )
                    }

                    // Primary Save & Activate Button
                    Button(
                        onClick = onSaveDraftToBridgeNodes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("wg_save_to_relays_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JissrEmerald,
                            contentColor = Color(0xFF032219)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.wgSaveToRelaysBtn, style = MaterialTheme.typography.labelLarge)
                    }

                    // Export File + Copy + Share Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val safeName = wgDraft.profileName.trim().ifBlank { "jissr-wg0" }
                                exportFileLauncher.launch("$safeName.conf")
                            },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp)
                                .testTag("wg_export_file_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JissrCyan,
                                contentColor = Color(0xFF042233)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.wgExportFileBtn, style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = {
                                copyToClipboard(context, "WireGuard Config", liveConfPreview)
                                onUpdateStudio(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    if (strings.isArabic) "تم نسخ ملف WireGuard .conf للحافظة"
                                    else "Copied WireGuard .conf to clipboard"
                                )
                            },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(46.dp)
                                .testTag("copy_wg_conf_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, JissrCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Code,
                                contentDescription = null,
                                tint = JissrCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(strings.copyWgBtn, style = MaterialTheme.typography.labelLarge, color = JissrCyan)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                copyToClipboard(context, "Jissr Share URI", jissrShareUri)
                                onUpdateStudio(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    if (strings.isArabic) "تم نسخ رابط jissr:// المشفر للحافظة"
                                    else "Copied jissr:// URI to clipboard"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("copy_jissr_uri_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, JissrEmerald)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Link,
                                contentDescription = null,
                                tint = JissrEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.copyUriBtn, style = MaterialTheme.typography.labelLarge, color = JissrEmerald)
                        }

                        OutlinedButton(
                            onClick = {
                                sharePlainText(context, "${wgDraft.profileName}.conf", liveConfPreview)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("wg_share_conf_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSubtleDark)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.wgShareConfBtn,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 5. GitHub Actions APK Build Workflow Card (Build Jissr VPN APK)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, BorderSubtleDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            tint = JissrEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = strings.studioGithubTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.studioGithubSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = studioState.selectedPreset == CiWorkflowPreset.FLUTTER_JISSR,
                            onClick = {
                                onUpdateStudio(CiWorkflowPreset.FLUTTER_JISSR, null, null, null, null, null)
                            },
                            label = { Text("Flutter 3.24 Action") },
                            modifier = Modifier.testTag("preset_flutter_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JissrEmerald.copy(alpha = 0.2f),
                                selectedLabelColor = JissrEmerald
                            )
                        )

                        FilterChip(
                            selected = studioState.selectedPreset == CiWorkflowPreset.ANDROID_GRADLE,
                            onClick = {
                                onUpdateStudio(CiWorkflowPreset.ANDROID_GRADLE, null, null, null, null, null)
                            },
                            label = { Text("Android Gradle APK") },
                            modifier = Modifier.testTag("preset_gradle_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JissrCyan.copy(alpha = 0.2f),
                                selectedLabelColor = JissrCyan
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = studioState.artifactName,
                            onValueChange = {
                                onUpdateStudio(null, it, null, null, null, null)
                            },
                            label = { Text("Artifact Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("artifact_name_input")
                        )

                        if (studioState.selectedPreset == CiWorkflowPreset.FLUTTER_JISSR) {
                            OutlinedTextField(
                                value = studioState.flutterVersion,
                                onValueChange = {
                                    onUpdateStudio(null, null, it, null, null, null)
                                },
                                label = { Text("Flutter") },
                                singleLine = true,
                                modifier = Modifier.width(104.dp)
                            )
                        }
                    }

                    if (studioState.selectedPreset == CiWorkflowPreset.FLUTTER_JISSR) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Split APK per ABI (--split-per-abi)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = studioState.splitPerAbi,
                                onCheckedChange = {
                                    onUpdateStudio(null, null, null, null, it, null)
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = JissrEmerald)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianSurfaceVariant,
                        border = BorderStroke(1.dp, BorderSubtleDark)
                    ) {
                        Text(
                            text = workflowYaml,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                lineHeight = 18.sp
                            ),
                            color = JissrEmerald,
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(14.dp)
                                .testTag("workflow_yaml_code")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                copyToClipboard(context, "Jissr Workflow YAML", workflowYaml)
                                onUpdateStudio(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    if (strings.isArabic) "تم نسخ ملف Build Jissr VPN APK بنجاح"
                                    else "Copied Build Jissr VPN APK workflow YAML"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("copy_yaml_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JissrEmerald,
                                contentColor = Color(0xFF032219)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.copyYamlBtn, style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = {
                                sharePlainText(context, "Build Jissr VPN APK Workflow", workflowYaml)
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("share_yaml_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, JissrCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = JissrCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. APK Package & Device ABI Inspector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, BorderSubtleDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = JissrEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.deviceTelemetryTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val abis = Build.SUPPORTED_ABIS.joinToString(", ")
                    BuildInfoRow("Application ID", BuildConfig.APPLICATION_ID)
                    BuildInfoRow("Version Name", "${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})")
                    BuildInfoRow("Supported CPU ABIs", abis)
                    BuildInfoRow("Android SDK Level", "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
                    BuildInfoRow("Android VpnService Kernel", "BIND_VPN_SERVICE · TUN/TAP Ready")
                }
            }
        }
    }

    if (showPasteImportDialog) {
        ImportWireGuardConfDialog(
            isArabic = strings.isArabic,
            initialText = liveConfPreview,
            onDismiss = { showPasteImportDialog = false },
            onConfirmImport = { rawText, name ->
                onImportConfText(rawText, name)
                showPasteImportDialog = false
            }
        )
    }
}

@Composable
private fun WgInterfaceFormCard(
    strings: JissrStrings,
    wgDraft: WireGuardProfileDraft,
    onUpdateWgDraft: ((WireGuardProfileDraft) -> WireGuardProfileDraft) -> Unit,
    onGenerateKeypair: () -> Unit
) {
    val dnsPresets = listOf(
        "Cloudflare" to "1.1.1.1, 1.0.0.1",
        "Quad9" to "9.9.9.9, 149.112.112.112",
        "Google" to "8.8.8.8, 8.8.4.4",
        "AdGuard" to "94.140.14.14, 94.140.15.15"
    )
    val mtuPresets = listOf("1280", "1380", "1400", "1420", "1500")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        tint = JissrEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.wgInterfaceSectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onGenerateKeypair,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("wg_generate_keys_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JissrEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoFixHigh,
                        contentDescription = null,
                        tint = JissrEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.wgGenerateKeysBtn,
                        style = MaterialTheme.typography.labelSmall,
                        color = JissrEmerald
                    )
                }
            }

            OutlinedTextField(
                value = wgDraft.profileName,
                onValueChange = { value ->
                    onUpdateWgDraft { it.copy(profileName = value) }
                },
                label = { Text(strings.wgProfileNameLabel) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wg_profile_name_input")
            )

            OutlinedTextField(
                value = wgDraft.interfacePrivateKey,
                onValueChange = { value ->
                    onUpdateWgDraft { it.copy(interfacePrivateKey = value) }
                },
                label = { Text(strings.wgPrivateKeyLabel) },
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wg_private_key_input")
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ObsidianSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.wgPublicKeyDerivedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = wgDraft.interfacePublicKey,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = JissrEmerald,
                            maxLines = 1
                        )
                    }
                }
            }

            OutlinedTextField(
                value = wgDraft.interfaceAddress,
                onValueChange = { value ->
                    onUpdateWgDraft { it.copy(interfaceAddress = value) }
                },
                label = { Text(strings.wgAddressLabel) },
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wg_address_input")
            )

            OutlinedTextField(
                value = wgDraft.interfaceDns,
                onValueChange = { value ->
                    onUpdateWgDraft { it.copy(interfaceDns = value) }
                },
                label = { Text(strings.wgDnsLabel) },
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wg_dns_input")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(dnsPresets) { (name, ips) ->
                    val selected = wgDraft.interfaceDns.trim() == ips
                    FilterChip(
                        selected = selected,
                        onClick = { onUpdateWgDraft { it.copy(interfaceDns = ips) } },
                        label = { Text(name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JissrCyan.copy(alpha = 0.2f),
                            selectedLabelColor = JissrCyan
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = wgDraft.interfaceMtu,
                    onValueChange = { value ->
                        onUpdateWgDraft { it.copy(interfaceMtu = value) }
                    },
                    label = { Text(strings.wgMtuLabel) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wg_mtu_input")
                )

                OutlinedTextField(
                    value = wgDraft.interfaceListenPort,
                    onValueChange = { value ->
                        onUpdateWgDraft { it.copy(interfaceListenPort = value) }
                    },
                    label = { Text(strings.wgListenPortLabel) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wg_listen_port_input")
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(mtuPresets) { mtuVal ->
                    val selected = wgDraft.interfaceMtu.trim() == mtuVal
                    FilterChip(
                        selected = selected,
                        onClick = { onUpdateWgDraft { it.copy(interfaceMtu = mtuVal) } },
                        label = { Text("MTU $mtuVal", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JissrEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = JissrEmerald
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun WgPeerFormCard(
    strings: JissrStrings,
    wgDraft: WireGuardProfileDraft,
    onUpdateWgDraft: ((WireGuardProfileDraft) -> WireGuardProfileDraft) -> Unit,
    onGeneratePsk: () -> Unit
) {
    val allowedIpPresets = listOf(
        (if (strings.isArabic) "نفق كامل" else "Full Tunnel") to "0.0.0.0/0, ::/0",
        (if (strings.isArabic) "تجاوز الشبكة المحلية" else "Split LAN") to "0.0.0.0/1, 128.0.0.0/1, ::/1, 8000::/1",
        "IPv4 Only" to "0.0.0.0/0"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Router,
                        contentDescription = null,
                        tint = JissrCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.wgPeerSectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onGeneratePsk,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("wg_generate_psk_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JissrCyan)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoFixHigh,
                        contentDescription = null,
                        tint = JissrCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.wgGeneratePskBtn,
                        style = MaterialTheme.typography.labelSmall,
                        color = JissrCyan
                    )
                }
            }

            OutlinedTextField(
                value = wgDraft.peerPublicKey,
                onValueChange = { value ->
                    onUpdateWgDraft { it.copy(peerPublicKey = value) }
                },
                label = { Text(strings.wgPeerPublicKeyLabel) },
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wg_peer_public_key_input")
            )

            OutlinedTextField(
                value = wgDraft.peerPresharedKey,
                onValueChange = { value ->
                    onUpdateWgDraft { it.copy(peerPresharedKey = value) }
                },
                label = { Text(strings.wgPresharedKeyLabel) },
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wg_preshared_key_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = wgDraft.peerEndpoint,
                    onValueChange = { value ->
                        onUpdateWgDraft { it.copy(peerEndpoint = value) }
                    },
                    label = { Text(strings.wgEndpointLabel) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("wg_endpoint_input")
                )

                OutlinedTextField(
                    value = wgDraft.peerPersistentKeepalive,
                    onValueChange = { value ->
                        onUpdateWgDraft { it.copy(peerPersistentKeepalive = value) }
                    },
                    label = { Text("Keepalive (s)") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    modifier = Modifier
                        .weight(0.8f)
                        .testTag("wg_keepalive_input")
                )
            }

            OutlinedTextField(
                value = wgDraft.peerAllowedIps,
                onValueChange = { value ->
                    onUpdateWgDraft { it.copy(peerAllowedIps = value) }
                },
                label = { Text(strings.wgAllowedIpsLabel) },
                singleLine = true,
                textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wg_allowed_ips_input")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(allowedIpPresets) { (label, cidr) ->
                    val selected = wgDraft.peerAllowedIps.trim() == cidr
                    FilterChip(
                        selected = selected,
                        onClick = { onUpdateWgDraft { it.copy(peerAllowedIps = cidr) } },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JissrCyan.copy(alpha = 0.2f),
                            selectedLabelColor = JissrCyan
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportWireGuardConfDialog(
    isArabic: Boolean,
    initialText: String,
    onDismiss: () -> Unit,
    onConfirmImport: (String, String) -> Unit
) {
    val context = LocalContext.current
    var profileName by remember { mutableStateOf("Imported-Jissr-WG0") }
    var rawConfText by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Text(
                text = if (isArabic) "استيراد نص ملف WireGuard (.conf)" else "Import WireGuard (.conf) Text",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "ألصق محتوى ملف [Interface] و [Peer]" else "Paste [Interface] and [Peer] blocks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                                ?.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clip.isNullOrBlank()) {
                                rawConfText = clip
                            }
                        }
                    ) {
                        Text(if (isArabic) "لصق من الحافظة" else "Paste Clipboard", color = JissrCyan)
                    }
                }

                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text(if (isArabic) "اسم الملف" else "Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawConfText,
                    onValueChange = { rawConfText = it },
                    label = { Text("WireGuard .conf") },
                    minLines = 6,
                    maxLines = 10,
                    textStyle = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wg_paste_conf_dialog_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmImport(rawConfText, profileName) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JissrEmerald,
                    contentColor = Color(0xFF032219)
                ),
                modifier = Modifier.testTag("wg_confirm_import_button")
            ) {
                Text(if (isArabic) "تحليل وتعبئة النموذج" else "Parse & Load Form")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
private fun BuildInfoRow(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ObsidianCardElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun sharePlainText(context: Context, subject: String, text: String) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, subject))
    } catch (_: Exception) {
    }
}

package com.huaweimanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaweimanager.app.data.model.SmsMessage
import com.huaweimanager.app.ui.i18n.LocalAppStrings
import com.huaweimanager.app.ui.theme.AccentPrimary
import com.huaweimanager.app.ui.theme.CardBackground
import com.huaweimanager.app.ui.theme.CardBorder
import com.huaweimanager.app.ui.theme.DarkBackground
import com.huaweimanager.app.ui.theme.SignalPoor
import com.huaweimanager.app.ui.theme.TextMuted
import com.huaweimanager.app.ui.theme.TextPrimary
import com.huaweimanager.app.ui.theme.TextSecondary
import com.huaweimanager.app.ui.viewmodel.HuaweiViewModel

@Composable
fun SmsScreen(
    viewModel: HuaweiViewModel,
    modifier: Modifier = Modifier
) {
    val S = LocalAppStrings.current
    val smsList by viewModel.smsList.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var showComposeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSms()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showComposeDialog = true },
                containerColor = AccentPrimary,
                contentColor = DarkBackground,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Tulis SMS")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = S.smsTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${smsList.size} ${S.inboxSubtitle}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = { viewModel.loadSms() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = S.refresh,
                        tint = AccentPrimary
                    )
                }
            }

            if (smsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = S.noSmsMessages,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(smsList, key = { it.index }) { msg ->
                        SmsItemCard(
                            sms = msg,
                            onDelete = { viewModel.deleteSms(msg.index) }
                        )
                    }
                }
            }
        }

        if (showComposeDialog) {
            ComposeSmsDialog(
                onDismiss = { showComposeDialog = false },
                onSend = { phone, content ->
                    viewModel.sendSms(phone, content) { success ->
                        if (success) showComposeDialog = false
                    }
                }
            )
        }
    }
}

@Composable
private fun SmsItemCard(
    sms: SmsMessage,
    onDelete: () -> Unit
) {
    val S = LocalAppStrings.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sms.phone.ifEmpty { "Unknown" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentPrimary
                )
                Text(
                    text = sms.date,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = sms.content,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = S.deleteSmsBtn,
                        tint = SignalPoor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardBackground,
            title = { Text(text = "${S.deleteSmsBtn} SMS?", color = TextPrimary) },
            text = { Text(text = "SIM card modem", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text(text = S.deleteSmsBtn, color = SignalPoor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = S.cancel, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ComposeSmsDialog(
    onDismiss: () -> Unit,
    onSend: (phone: String, content: String) -> Unit
) {
    val S = LocalAppStrings.current
    var phone by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = S.sendSmsCardTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(S.recipientPhoneLabel) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = AccentPrimary,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(S.messageContentLabel) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = AccentPrimary,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotBlank() && content.isNotBlank()) {
                        onSend(phone.trim(), content.trim())
                    }
                },
                enabled = phone.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = DarkBackground)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(S.sendSmsBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(S.cancel, color = TextSecondary)
            }
        }
    )
}

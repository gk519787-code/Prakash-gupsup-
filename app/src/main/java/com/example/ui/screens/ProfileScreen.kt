package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PostEntity
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    posts: List<PostEntity>,
    modifier: Modifier = Modifier
) {
    var selectedArchiveTab by remember { mutableStateOf("grid") } // "grid", "reels", "saved"
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var creatorName by remember { mutableStateOf("Prakash") }
    var creatorHandle by remember { mutableStateOf("prakash_gupsup") }
    var creatorBio by remember { mutableStateOf("Director / photography / late night frames") }

    val filteredArchive = remember(posts, selectedArchiveTab) {
        when (selectedArchiveTab) {
            "reels" -> posts.filter { it.mediaType == "reel" }
            "saved" -> posts.filter { it.isBookmarked }
            else -> posts
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GupSupBg)
            .padding(horizontal = 20.dp)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    OverlineTag(text = "THE CREATOR", color = GupSupBlue)
                    Text(
                        text = "@$creatorHandle",
                        style = MaterialTheme.typography.displayMedium,
                        color = GupSupText
                    )
                }

                IconButton(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, GupSupLine, CircleShape)
                        .background(GupSupSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = GupSupText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Profile Avatar & Bio Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GupSupLine, RoundedCornerShape(20.dp))
                    .background(GupSupSurface, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GupSupAvatar(name = creatorName, colorIndex = 0, size = 80.dp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = creatorName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = GupSupText
                )

                Text(
                    text = creatorBio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GupSupMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(value = posts.size.toString(), label = "POSTS")
                    ProfileStatItem(value = "1.4K", label = "FOLLOWERS")
                    ProfileStatItem(value = "380", label = "FOLLOWING")
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = { showEditProfileDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GupSupText),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GupSupLine),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(text = "Edit Profile", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Archive Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR ARCHIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = GupSupDim
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { selectedArchiveTab = "grid" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Grid",
                            tint = if (selectedArchiveTab == "grid") GupSupRed else GupSupDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { selectedArchiveTab = "reels" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = "Reels",
                            tint = if (selectedArchiveTab == "reels") GupSupRed else GupSupDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { selectedArchiveTab = "saved" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Saved",
                            tint = if (selectedArchiveTab == "saved") GupSupRed else GupSupDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Archive Grid Content
        item {
            val itemsToShow = if (filteredArchive.isEmpty()) posts else filteredArchive
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsToShow.chunked(2).forEach { rowPosts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPosts.forEachIndexed { i, post ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(parseColorHex(post.mediaColorHex, getPaletteColor(i)))
                                    .padding(10.dp)
                            ) {
                                FrameGridOverlay()
                                Text(
                                    text = "0${i + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GupSupText
                                )
                                Text(
                                    text = post.frameTitle.ifBlank { "FRAME" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GupSupText,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.BottomStart)
                                )
                            }
                        }
                        if (rowPosts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GupSupSurface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GupSupText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = creatorName,
                        onValueChange = { creatorName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = creatorBio,
                        onValueChange = { creatorBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showEditProfileDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GupSupRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes", color = GupSupBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = GupSupText,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GupSupDim
        )
    }
}

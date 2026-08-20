package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import com.example.data.util.FastDataProcessor
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    posts: List<PostEntity>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPostForPreview by remember { mutableStateOf<PostEntity?>(null) }
    val categories = listOf("All", "Cinematic", "Reels", "35mm Film", "Neon Night", "Architecture")

    var filteredPosts by remember { mutableStateOf(posts) }

    LaunchedEffect(posts, searchQuery, selectedCategory) {
        filteredPosts = FastDataProcessor.instance.searchPosts(
            allPosts = posts,
            query = searchQuery,
            category = selectedCategory
        )
    }

    // Multiply posts to give a rich contact sheet grid
    val gridItems = remember(filteredPosts) {
        if (filteredPosts.isEmpty()) emptyList()
        else filteredPosts + filteredPosts + filteredPosts
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GupSupBg)
            .padding(horizontal = 20.dp)
            .testTag("explore_screen")
    ) {
        // Page Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                OverlineTag(text = "DISCOVER", color = GupSupBlue)
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.displayMedium,
                    color = GupSupText
                )
            }

            IconButton(
                onClick = { /* filter settings */ },
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, GupSupLine, RoundedCornerShape(12.dp))
                    .background(GupSupSurface, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Options",
                    tint = GupSupText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "Search people, frames, #tags...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GupSupDim
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = GupSupDim,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_search_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GupSupBlue,
                unfocusedBorderColor = GupSupLine,
                focusedContainerColor = GupSupSurface,
                unfocusedContainerColor = GupSupSurface,
                focusedTextColor = GupSupText,
                unfocusedTextColor = GupSupText
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GupSupRed,
                        selectedLabelColor = GupSupBg
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section label
        Text(
            text = "EDITOR / CONTACT SHEET (${gridItems.size} TILES)",
            style = MaterialTheme.typography.labelSmall,
            color = GupSupDim
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Contact Sheet Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 110.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(gridItems) { i, post ->
                val tileHeight = if (i % 5 == 0) 200.dp else 135.dp
                val tileColor = parseColorHex(post.mediaColorHex, getPaletteColor(i))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tileHeight)
                        .clip(RoundedCornerShape(14.dp))
                        .background(tileColor)
                        .clickable { selectedPostForPreview = post }
                        .padding(10.dp)
                ) {
                    FrameGridOverlay()

                    Text(
                        text = "0${(i % 9) + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupText,
                        modifier = Modifier.align(Alignment.TopStart)
                    )

                    Text(
                        text = "@${post.username.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GupSupText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
    }

    // Detail Preview Modal
    selectedPostForPreview?.let { post ->
        Dialog(onDismissRequest = { selectedPostForPreview = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GupSupSurface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OverlineTag(text = "FRAME PREVIEW", color = GupSupBlue)
                        IconButton(
                            onClick = { selectedPostForPreview = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GupSupText)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(parseColorHex(post.mediaColorHex, GupSupBlue)),
                        contentAlignment = Alignment.Center
                    ) {
                        FrameGridOverlay()
                        Text(
                            text = post.frameTitle.ifBlank { "FRAME 01" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = GupSupText,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "@${post.username}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GupSupText
                    )
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GupSupMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = post.tags,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GupSupBlue,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

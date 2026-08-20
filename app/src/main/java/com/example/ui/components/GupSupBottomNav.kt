package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class NavItem(
    val key: String,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val isAction: Boolean = false
)

@Composable
fun GupSupBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("home", "HOME", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("explore", "EXPLORE", Icons.Filled.Search, Icons.Outlined.Search),
        NavItem("create", "NEW", Icons.Filled.Add, Icons.Outlined.Add, isAction = true),
        NavItem("reels", "REELS", Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow),
        NavItem("profile", "PROFILE", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, GupSupLine, RoundedCornerShape(32.dp))
                .background(GupSupSurface.copy(alpha = 0.96f))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.key

                if (item.isAction) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GupSupRed)
                            .clickable { onTabSelected(item.key) }
                            .testTag("tab-${item.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = item.label,
                            tint = GupSupBg,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTabSelected(item.key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("tab-${item.key}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                            contentDescription = item.label,
                            tint = if (isSelected) GupSupRed else GupSupMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GupSupRed else GupSupDim
                        )
                    }
                }
            }
        }
    }
}

package kr.stonecold.securitycard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.stonecold.securitycard.core.data.SecurityCardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(false) }
    var sortedList by remember { mutableStateOf<List<SecurityCardEntity>>(emptyList()) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.exportData(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importData(context, uri)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (sortMode) "순서 정렬" else "보안카드 관리") },
                actions = {
                    if (sortMode) {
                        TextButton(onClick = { sortMode = false }) {
                            Text("취소", color = MaterialTheme.colorScheme.onSurface)
                        }
                        TextButton(onClick = {
                            viewModel.saveOrder(sortedList)
                            sortMode = false
                        }) {
                            Text("저장", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Text("⋮", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("정렬 모드") },
                                    onClick = {
                                        showMenu = false
                                        sortMode = true
                                        val data = (state as? MainScreenUiState.Success)?.data ?: emptyList()
                                        sortedList = data.toMutableList()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("보안카드 추가") },
                                    onClick = {
                                        showMenu = false
                                        onAddClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("설정") },
                                    onClick = {
                                        showMenu = false
                                        onSettingsClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("내보내기 (Export)") },
                                    onClick = {
                                        showMenu = false
                                        exportLauncher.launch("security_cards.json")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("가져오기 (Import)") },
                                    onClick = {
                                        showMenu = false
                                        importLauncher.launch(arrayOf("application/json", "*/*"))
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                MainScreenUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MainScreenUiState.Success -> {
                    val data = (state as MainScreenUiState.Success).data
                    if (data.isEmpty() && !sortMode) {
                        Text(
                            text = "등록된 보안카드가 없습니다",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        val displayList = if (sortMode) sortedList else data
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(displayList, key = { it.id }) { card ->
                                SecurityCardItem(
                                    card = card,
                                    isSortMode = sortMode,
                                    onClick = { if (!sortMode) onItemClick(card.id) },
                                    onMoveUp = {
                                        val index = sortedList.indexOf(card)
                                        if (index > 0) {
                                            val mutableList = sortedList.toMutableList()
                                            val temp = mutableList[index]
                                            mutableList[index] = mutableList[index - 1]
                                            mutableList[index - 1] = temp
                                            sortedList = mutableList
                                        }
                                    },
                                    onMoveDown = {
                                        val index = sortedList.indexOf(card)
                                        if (index >= 0 && index < sortedList.size - 1) {
                                            val mutableList = sortedList.toMutableList()
                                            val temp = mutableList[index]
                                            mutableList[index] = mutableList[index + 1]
                                            mutableList[index + 1] = temp
                                            sortedList = mutableList
                                        }
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
                is MainScreenUiState.Error -> {
                    Text(
                        text = "오류가 발생했습니다: ${(state as MainScreenUiState.Error).throwable.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityCardItem(
    card: SecurityCardEntity,
    isSortMode: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSortMode) { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(card.name.firstOrNull()?.toString() ?: "S", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = card.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        if (isSortMode) {
            IconButton(onClick = onMoveUp) {
                Text("▲", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onMoveDown) {
                Text("▼", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

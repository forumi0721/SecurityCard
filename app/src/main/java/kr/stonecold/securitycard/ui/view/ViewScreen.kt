package kr.stonecold.securitycard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import android.content.ClipboardManager
import android.content.Context
import android.content.ClipData
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(
    id: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: ViewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()
    var isSearchMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    LaunchedEffect(id) {
        viewModel.loadCard(id)
    }

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            viewModel.resetDeletedState()
            onNavigateBack()
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("카드 삭제") },
            text = { Text("정말로 이 보안카드를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteCard()
                }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("보안카드 보기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("◀", fontSize = 20.sp)
                    }
                },
                actions = {
                    TextButton(onClick = { isSearchMode = !isSearchMode }) {
                        Text(if (isSearchMode) "전체 보기" else "코드 검색")
                    }
                    IconButton(onClick = onNavigateToEdit) {
                        Text("수정")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text(
                            text = uiState.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF444444),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                        )
                        if (uiState.cardNumber.isNotBlank()) {
                            Text(
                                text = uiState.cardNumber,
                                fontSize = 16.sp,
                                color = Color(0xFF777777),
                                modifier = Modifier.align(Alignment.End).padding(bottom = 16.dp)
                            )
                        }

                        if (isSearchMode) {
                            CodeSearchView(cardNumber = uiState.cardNumber, codes = uiState.codes, modifier = Modifier.wrapContentHeight())
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 75.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp),
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                itemsIndexed(uiState.codes) { index, code ->
                                    Box(
                                        modifier = Modifier
                                            .border(0.5.dp, Color.LightGray)
                                            .clickable {
                                                val clip = ClipData.newPlainText("SecurityCode", code)
                                                clipboardManager.setPrimaryClip(clip)
                                                Toast.makeText(context, "${index + 1}번 코드가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        CodeViewItem(index = index + 1, value = code)
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (uiState.callCenter.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "콜센터: ${uiState.callCenter}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (uiState.accounts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("계좌 정보", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    uiState.accounts.forEachIndexed { index, account ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                val clip = ClipData.newPlainText("AccountNumber", account.accountNumber)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "계좌번호가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                            },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${account.bankName} (예금주: ${account.accountHolder})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF444444),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = account.accountNumber,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF444444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeViewItem(index: Int, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .background(Color(0xFF333333)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", index),
                color = Color(0xFFEEEEEE),
                fontSize = 14.sp
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = Color(0xFF444444),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CodeSearchView(cardNumber: String, codes: List<String>, modifier: Modifier = Modifier) {
    var serialIndex1 by remember { mutableStateOf("") }
    var serialIndex2 by remember { mutableStateOf("") }
    var serialIndex3 by remember { mutableStateOf("") }

    var firstIndex by remember { mutableStateOf("") }
    var secondIndex by remember { mutableStateOf("") }

    val cleanCardNumber = cardNumber.replace("-", "").replace(" ", "")

    fun getSerialChar(indexStr: String): String {
        val index = indexStr.toIntOrNull() ?: return ""
        if (index in 1..cleanCardNumber.length) {
            return cleanCardNumber[index - 1].toString()
        }
        return ""
    }

    val s1 = getSerialChar(serialIndex1)
    val s2 = getSerialChar(serialIndex2)
    val s3 = getSerialChar(serialIndex3)

    val firstCode = firstIndex.toIntOrNull()?.let {
        if (it in 1..codes.size) codes[it - 1].take(2) else ""
    } ?: ""

    val secondCode = secondIndex.toIntOrNull()?.let {
        if (it in 1..codes.size) codes[it - 1].takeLast(2) else ""
    } ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (cleanCardNumber.isNotEmpty()) {
            Text("일련번호 찾기", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF444444), modifier = Modifier.padding(bottom = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SerialSearchItem("1번째", serialIndex1, { if (it.length <= 2) serialIndex1 = it }, s1)
                SerialSearchItem("2번째", serialIndex2, { if (it.length <= 2) serialIndex2 = it }, s2)
                SerialSearchItem("3번째", serialIndex3, { if (it.length <= 2) serialIndex3 = it }, s3)
            }
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))
        }

        Text("보안코드 찾기", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF444444), modifier = Modifier.padding(bottom = 16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("첫 번째", fontSize = 18.sp, color = Color(0xFF555555), modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = firstIndex,
                    onValueChange = { if (it.length <= 2) firstIndex = it },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 30.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = firstCode.ifEmpty { "--" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF444444)
                )
                Text("(앞 2자리)", fontSize = 14.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("두 번째", fontSize = 18.sp, color = Color(0xFF555555), modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = secondIndex,
                    onValueChange = { if (it.length <= 2) secondIndex = it },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 30.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = secondCode.ifEmpty { "--" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF444444)
                )
                Text("(뒤 2자리)", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SerialSearchItem(label: String, indexValue: String, onValueChange: (String) -> Unit, resultChar: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 16.sp, color = Color(0xFF555555), modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = indexValue,
            onValueChange = onValueChange,
            modifier = Modifier.width(70.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = resultChar.ifEmpty { "-" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF444444)
        )
    }
}

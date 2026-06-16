package kr.stonecold.securitycard.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    id: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val cardNumber by viewModel.cardNumber.collectAsState()
    val callCenter by viewModel.callCenter.collectAsState()
    val codeLength by viewModel.codeLength.collectAsState()
    val codes by viewModel.codes.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    LaunchedEffect(id) {
        if (id != null) {
            viewModel.loadCard(id)
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.resetSavedState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("보안카드 등록") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("◀", fontSize = 20.sp)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onNavigateBack) {
                        Text("취소", fontSize = 18.sp)
                    }
                    Button(onClick = { viewModel.saveCard() }) {
                        Text("저장", fontSize = 18.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::updateTitle,
                label = { Text("카드 이름") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cardNumber,
                onValueChange = viewModel::updateCardNumber,
                label = { Text("일련번호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = callCenter,
                onValueChange = viewModel::updateCallCenter,
                label = { Text("콜센터 전화번호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("보안카드 코드 입력", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.updateCodeLength(maxOf(1, codeLength - 1)) }) {
                        Text("-", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = if (codeLength == 0) "" else codeLength.toString(),
                        onValueChange = { 
                            val newLength = it.toIntOrNull() ?: 0
                            if (newLength in 1..100) {
                                viewModel.updateCodeLength(newLength)
                            } else if (it.isEmpty()) {
                                viewModel.updateCodeLength(0)
                            }
                        },
                        label = { Text("갯수") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.updateCodeLength(minOf(100, codeLength + 1)) }) {
                        Text("+", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 75.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    itemsIndexed(codes.take(codeLength)) { index, code ->
                        Box(modifier = Modifier.border(0.5.dp, Color.LightGray)) {
                            CodeInputItem(
                                index = index + 1,
                                value = code,
                                onValueChange = { viewModel.updateCode(index, it) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("계좌 정보", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { viewModel.addAccount() }) {
                    Text("계좌 추가")
                }
            }
            accounts.forEachIndexed { index, account ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("계좌 ${index + 1}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            IconButton(onClick = { viewModel.removeAccount(index) }) {
                                Text("삭제", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        OutlinedTextField(
                            value = account.bankName,
                            onValueChange = { viewModel.updateAccount(index, account.copy(bankName = it)) },
                            label = { Text("계정명 (예: 국민은행)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = account.accountNumber,
                            onValueChange = { viewModel.updateAccount(index, account.copy(accountNumber = it)) },
                            label = { Text("계좌번호") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = account.accountHolder,
                            onValueChange = { viewModel.updateAccount(index, account.copy(accountHolder = it)) },
                            label = { Text("예금주") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            } // Close Column
        } // Close else
    } // Close Scaffold
}

@Composable
fun CodeInputItem(index: Int, value: String, onValueChange: (String) -> Unit) {
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
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= 4) onValueChange(it) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            textStyle = TextStyle(
                color = Color(0xFF444444),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    innerTextField()
                }
            }
        )
    }
}

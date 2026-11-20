package ua.example.diyalearning

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Navy = Color(0xFF101B3F)
private val Blue = Color(0xFF3157D5)
private val Pale = Color(0xFFF2F4FA)
private val Ink = Color(0xFF17213D)
private val Muted = Color(0xFF75809A)

data class DemoDocument(
    val id: Long,
    val title: String,
    val holder: String,
    val detail: String,
    val number: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DemoApp() }
    }
}

@Composable
private fun DemoApp() {
    val context = LocalContext.current
    val docs = remember { mutableStateListOf<DemoDocument>().apply { addAll(loadDocs(context)) } }
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<DemoDocument?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<DemoDocument?>(null) }
    var search by remember { mutableStateOf("") }
    var showNotice by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    fun save() = saveDocs(context, docs)

    MaterialTheme(
        colorScheme = if (darkMode) darkColorScheme(primary = Color(0xFF8CA7FF), background = Color(0xFF101522), surface = Color(0xFF1B2435)) else lightColorScheme(primary = Blue, background = Pale, surface = Color.White)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (selected != null) {
                DocumentDetails(
                    document = selected!!,
                    onBack = { selected = null },
                    onEdit = { editing = selected; showEditor = true },
                    onDelete = {
                        docs.removeAll { it.id == selected!!.id }
                        save()
                        selected = null
                    }
                )
            } else {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp, vertical = 14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Вітаємо!", fontSize = 14.sp, color = Muted)
                                    Text("Мій простір", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text("🔔", fontSize = 23.sp, modifier = Modifier.clickable { showNotice = true }.padding(8.dp))
                            }
                            Spacer(Modifier.height(10.dp))
                            DemoBanner()
                        }
                    },
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            val labels = listOf("Головна", "Документи", "Послуги", "Меню")
                            val icons = listOf("⌂", "▣", "✦", "☰")
                            labels.forEachIndexed { index, label ->
                                NavigationBarItem(
                                    selected = tab == index,
                                    onClick = { tab = index },
                                    icon = { Text(icons[index], fontSize = 20.sp) },
                                    label = { Text(label, fontSize = 10.sp) }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (tab == 1 || tab == 0) {
                            FloatingActionButton(onClick = { editing = null; showEditor = true }, containerColor = Blue, contentColor = Color.White) {
                                Text("+", fontSize = 28.sp)
                            }
                        }
                    }
                ) { padding ->
                    when (tab) {
                        0 -> HomeScreen(docs, onOpen = { selected = it }, onCreate = { editing = null; showEditor = true }, modifier = Modifier.padding(padding))
                        1 -> DocumentsScreen(docs, search, { search = it }, { selected = it }, Modifier.padding(padding))
                        2 -> ServicesScreen(Modifier.padding(padding))
                        else -> MenuScreen(
                            notifications = notifications,
                            darkMode = darkMode,
                            onNotifications = { notifications = it },
                            onDarkMode = { darkMode = it },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }

            if (showEditor) {
                DocumentEditor(
                    initial = editing,
                    onDismiss = { showEditor = false; editing = null },
                    onSave = { title, holder, detail ->
                        if (editing == null) {
                            docs.add(0, DemoDocument(System.currentTimeMillis(), title, holder, detail, "DEMO-${(1000..9999).random()}"))
                        } else {
                            val index = docs.indexOfFirst { it.id == editing!!.id }
                            if (index >= 0) docs[index] = editing!!.copy(title = title, holder = holder, detail = detail)
                        }
                        save()
                        showEditor = false
                        editing = null
                    }
                )
            }
            if (showNotice) {
                AlertDialog(
                    onDismissRequest = { showNotice = false },
                    title = { Text("Сповіщення") },
                    text = { Text(if (notifications) "Тут з'являтимуться демосповіщення. Поки що нових немає." else "Сповіщення вимкнені в налаштуваннях.") },
                    confirmButton = { TextButton(onClick = { showNotice = false }) { Text("Зрозуміло") } }
                )
            }
        }
    }
}

@Composable
private fun DemoBanner() {
    Row(
        Modifier.fillMaxWidth().background(Color(0xFFFFF1D6), RoundedCornerShape(12.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("ⓘ", color = Color(0xFF9B6200), fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text("НЕОФІЦІЙНИЙ ДЕМОЗАСТОСУНОК · НЕ Є ДОКУМЕНТОМ", color = Color(0xFF805300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeScreen(
    docs: List<DemoDocument>,
    onOpen: (DemoDocument) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 90.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Navy), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("ТВІЙ ЦИФРОВИЙ ПРОСТІР", color = Color(0xFFBFCBFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Усе важливе\nв одному місці", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
                    Spacer(Modifier.height(18.dp))
                    OutlinedButton(onClick = onCreate, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                        Text("＋ Створити демокартку")
                    }
                }
            }
        }
        item { SectionTitle("Мої документи", "Усього: ${docs.size}") }
        if (docs.isEmpty()) {
            item { EmptyState("Поки немає документів", "Створи першу власну демокартку.") }
        } else {
            items(docs.take(3), key = { it.id }) { doc -> DocumentCard(doc, onClick = { onOpen(doc) }) }
        }
        item { SectionTitle("Швидкі сервіси", "DEMO") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickService("▣", "Документи", Modifier.weight(1f))
                QuickService("✦", "Заявки", Modifier.weight(1f))
                QuickService("⌕", "Пошук", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DocumentsScreen(
    docs: List<DemoDocument>,
    search: String,
    onSearch: (String) -> Unit,
    onOpen: (DemoDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = docs.filter { it.title.contains(search, true) || it.holder.contains(search, true) || it.number.contains(search, true) }
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(18.dp))
        Text("Документи", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Пошук документів") }, singleLine = true, shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyState(if (docs.isEmpty()) "Документів ще немає" else "Нічого не знайдено", "Створи документ кнопкою + або зміни пошуковий запит.")
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { doc -> DocumentCard(doc, onClick = { onOpen(doc) }) }
            }
        }
    }
}

@Composable
private fun DocumentCard(document: DemoDocument, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(Color(0xFFE8EDFF), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Text("▣", color = Blue, fontSize = 24.sp)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(document.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(document.holder, fontSize = 13.sp, color = Muted)
                Text(document.number, fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Bold)
            }
            Text("›", fontSize = 28.sp, color = Muted)
        }
    }
}

@Composable
private fun DocumentDetails(document: DemoDocument, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(18.dp)) {
        Text("‹  Назад", color = Blue, fontSize = 16.sp, modifier = Modifier.clickable { onBack() }.padding(vertical = 10.dp))
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Navy), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                Text("ДЕМОКАРТКА · НЕ Є ДОКУМЕНТОМ", color = Color(0xFFFFD98A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                Text(document.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Text(document.holder, color = Color.White, fontSize = 18.sp)
                Text(document.detail, color = Color(0xFFD2D9F5), fontSize = 13.sp)
                Spacer(Modifier.height(20.dp))
                Text(document.number, color = Color(0xFFFFD98A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                DemoPattern()
                Spacer(Modifier.height(8.dp))
                Text("ДЕКОРАТИВНИЙ ВІЗЕРУНОК · НЕ QR-КОД", color = Color(0xFFFFD98A), fontSize = 9.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Редагувати") }
        OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Видалити демокартку") }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Видалити картку?") },
            text = { Text("Цю демокартку буде видалено з цього застосунку.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Видалити") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Скасувати") } }
        )
    }
}

@Composable
private fun DemoPattern() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(17) { col ->
                    val filled = ((row * 17 + col) * 7 + row * col) % 5 < 2
                    Box(Modifier.size(7.dp).background(if (filled) Color.White else Color(0xFF63709B), RoundedCornerShape(1.dp)))
                }
            }
        }
    }
}

@Composable
private fun DocumentEditor(initial: DemoDocument?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var holder by remember { mutableStateOf(initial?.holder ?: "") }
    var detail by remember { mutableStateOf(initial?.detail ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Нова демокартка" else "Редагувати картку") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Усі дані вигадані або введені тобою. Картка матиме позначку DEMO.", fontSize = 12.sp, color = Muted)
                OutlinedTextField(title, { title = it }, label = { Text("Назва картки") }, singleLine = true)
                OutlinedTextField(holder, { holder = it }, label = { Text("Ім'я для демо") }, singleLine = true)
                OutlinedTextField(detail, { detail = it }, label = { Text("Додаткова інформація") }, minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank() && holder.isNotBlank()) onSave(title.trim(), holder.trim(), detail.trim()) }, enabled = title.isNotBlank() && holder.isNotBlank()) { Text("Зберегти") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

@Composable
private fun ServicesScreen(modifier: Modifier = Modifier) {
    val services = listOf("Заявки та звернення" to "Створюй тестові записи для власних сценаріїв", "Довідки" to "Макети довідок із позначкою DEMO", "Особисті нотатки" to "Зберігай власну інформацію локально", "Навчальний розділ" to "Ознайомлення з цифровими сервісами")
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Послуги", fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { DemoBanner() }
        items(services) { (title, desc) ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(desc, color = Muted, fontSize = 13.sp)
                    Text("ДЕМО · НЕ ПЕРЕДАЄТЬСЯ ДО ДЕРЖАВНИХ СИСТЕМ", color = Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MenuScreen(
    notifications: Boolean,
    darkMode: Boolean,
    onNotifications: (Boolean) -> Unit,
    onDarkMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Меню", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Профіль демокористувача", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Локальний профіль · без входу в державні системи", color = Muted, fontSize = 12.sp)
                Text("DEMO USER", color = Blue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Сповіщення", fontWeight = FontWeight.SemiBold)
                        Text("Демосповіщення в застосунку", color = Muted, fontSize = 12.sp)
                    }
                    Switch(checked = notifications, onCheckedChange = onNotifications)
                }
                Divider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Темна тема", fontWeight = FontWeight.SemiBold)
                        Text("Змінити вигляд застосунку", color = Muted, fontSize = 12.sp)
                    }
                    Switch(checked = darkMode, onCheckedChange = onDarkMode)
                }
            }
        }
        Card(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Про застосунок", fontWeight = FontWeight.Bold)
                Text("Diya Demo 2.0", color = Blue)
                Text("Навчальний неофіційний проєкт. Усі картки є демонстраційними та не підтверджують особу чи права.", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickService(symbol: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(symbol, fontSize = 23.sp, color = Blue)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionTitle(title: String, trailing: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(trailing, fontSize = 11.sp, color = Muted)
    }
}

@Composable
private fun EmptyState(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("▣", fontSize = 30.sp, color = Blue)
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, color = Muted, fontSize = 12.sp)
        }
    }
}

private fun loadDocs(context: Context): List<DemoDocument> {
    val raw = context.getSharedPreferences("diya_demo", Context.MODE_PRIVATE).getString("documents", null) ?: return emptyList()
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            DemoDocument(item.getLong("id"), item.getString("title"), item.getString("holder"), item.getString("detail"), item.getString("number"))
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveDocs(context: Context, docs: List<DemoDocument>) {
    val array = JSONArray()
    docs.forEach { doc ->
        array.put(JSONObject().apply {
            put("id", doc.id)
            put("title", doc.title)
            put("holder", doc.holder)
            put("detail", doc.detail)
            put("number", doc.number)
        })
    }
    context.getSharedPreferences("diya_demo", Context.MODE_PRIVATE).edit().putString("documents", array.toString()).apply()
}

package ua.example.diyalearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF101B3F)
private val Blue = Color(0xFF2457D6)
private val Pale = Color(0xFFF3F6FC)
private val Muted = Color(0xFF77819A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DemoApp() }
    }
}

@Composable
fun DemoApp() {
    var selected by remember { mutableStateOf(0) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue,
            background = Pale,
            surface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Pale,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = selected == 0,
                        onClick = { selected = 0 },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Головна") }
                    )
                    NavigationBarItem(
                        selected = selected == 1,
                        onClick = { selected = 1 },
                        icon = { Icon(Icons.Default.Description, null) },
                        label = { Text("Документи") }
                    )
                    NavigationBarItem(
                        selected = selected == 2,
                        onClick = { selected = 2 },
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Профіль") }
                    )
                }
            }
        ) { padding ->
            when (selected) {
                0 -> HomeScreen(Modifier.padding(padding), onDocuments = { selected = 1 })
                1 -> DocumentsScreen(Modifier.padding(padding))
                else -> ProfileScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onDocuments: () -> Unit) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Вітаємо!", color = Muted, fontSize = 14.sp)
                    Text(
                        "Ваш цифровий простір",
                        color = Navy,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        null,
                        tint = Navy,
                        modifier = Modifier.padding(13.dp)
                    )
                }
            }
        }

        item { DemoBanner() }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Мої документи",
                    color = Navy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDocuments) { Text("Усі") }
            }
        }

        item {
            DocumentCard(
                title = "Цифрова картка",
                subtitle = "Навчальний приклад",
                icon = Icons.Default.Badge,
                gradient = listOf(Color(0xFF234FC5), Color(0xFF4D83EF))
            )
        }

        item {
            DocumentCard(
                title = "Посвідчення водія",
                subtitle = "Демонстраційні дані",
                icon = Icons.Default.DirectionsCar,
                gradient = listOf(Color(0xFF202C50), Color(0xFF526487))
            )
        }

        item { NoticeCard() }
    }
}

@Composable
fun DemoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF173C9B), Color(0xFF3978E8))
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Цифрові сервіси",
                color = Color.White.copy(alpha = .8f),
                fontSize = 14.sp
            )
            Text(
                "Усе важливе\nв одному місці",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Навчальний інтерфейс для знайомства з Android",
                color = Color.White.copy(alpha = .85f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun DocumentCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(gradient), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.weight(1f))
                Text(
                    "ЗРАЗОК",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .border(
                            1.dp,
                            Color.White.copy(alpha = .7f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                title,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Text(subtitle, color = Color.White.copy(alpha = .8f), fontSize = 13.sp)
            Text(
                "НЕ Є ДОКУМЕНТОМ",
                color = Color.White.copy(alpha = .75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DocumentsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Документи",
            color = Navy,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Усі картки нижче — вигадані демонстраційні приклади.",
            color = Muted,
            fontSize = 14.sp
        )
        DocumentCard(
            "Цифрова картка",
            "Тестовий профіль • 0000 0000",
            Icons.Default.Badge,
            listOf(Color(0xFF234FC5), Color(0xFF4D83EF))
        )
        DocumentCard(
            "Посвідчення водія",
            "Тестова категорія • XX",
            Icons.Default.DirectionsCar,
            listOf(Color(0xFF202C50), Color(0xFF526487))
        )
        DocumentCard(
            "Студентський квиток",
            "Навчальний зразок",
            Icons.Default.School,
            listOf(Color(0xFF5B3AA8), Color(0xFF9670D8))
        )
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Профіль",
            color = Navy,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    null,
                    tint = Blue,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "Тестовий користувач",
                    color = Navy,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Обліковий запис для демонстрації",
                    color = Muted,
                    fontSize = 13.sp
                )
            }
        }
        InfoRow("Статус", "Демо-профіль")
        InfoRow("Безпека", "Немає підключення до держсистем")
        InfoRow("Версія", "1.0")
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Muted, modifier = Modifier.weight(1f))
            Text(value, color = Navy, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun NoticeCard() {
    Surface(color = Color(0xFFE8EEFC), shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.padding(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Info, null, tint = Blue)
            Spacer(Modifier.width(10.dp))
            Text(
                "Це навчальний макет. Дані вигадані, QR-коди та документи не підтверджують особу й не мають юридичної сили.",
                color = Navy,
                fontSize = 12.sp
            )
        }
    }
}

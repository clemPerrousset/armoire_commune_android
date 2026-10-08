package fr.larmoirecommune.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.larmoirecommune.app.R

// Palette tirée du logo
private val Teal = Color(0xFF066372)
// Blanc, comme le fond du logo (JPG non détouré) : tout reste au même niveau
private val PageBackground = Color.White
private val Grey = Color(0xFF6B7280)

enum class Tone(val start: Color, val end: Color) {
    TEAL(Color(0xFF0A7C8A), Color(0xFF055563)),
    YELLOW(Color(0xFFEDB544), Color(0xFFD4961C)),
    GREEN(Color(0xFFA2B64A), Color(0xFF7A8C2A)),
    RED(Color(0xFFEF6B5B), Color(0xFFC9392A)),
}

data class DashboardItem(
    val title: String,
    val iconRes: Int,
    val alert: Boolean = false,
    val action: () -> Unit
)

@Composable
fun HomeScreen(items: List<DashboardItem>, onSearchClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(PageBackground)
            .systemBarsPadding()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.logo_armoire),
                        contentDescription = "L'Armoire Commune",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .padding(top = 4.dp)
                    )
                    Text(
                        text = "Que souhaitez-vous faire aujourd'hui ?",
                        color = Teal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { SearchBar(onSearchClick) }
            // Turquoise, jaune, vert en alternance ; rouge réservé aux éléments d'alerte
            val palette = listOf(Tone.TEAL, Tone.YELLOW, Tone.GREEN)
            val tones = items.runningFold(0) { n, it -> if (it.alert) n else n + 1 }
            itemsIndexed(items) { index, item ->
                DashboardCard(item, if (item.alert) Tone.RED else palette[tones[index] % palette.size])
            }
        }
    }
}

@Composable
private fun SearchBar(onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color(0xFFE5E7EB), shape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = Grey,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Rechercher un objet...",
            color = Grey,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun DashboardCard(item: DashboardItem, tone: Tone) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(4.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(tone.start, tone.end)))
            .clickable(onClick = item.action)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(38.dp)
        )
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

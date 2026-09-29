package ru.finny.app.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finny.app.R
import ru.finny.app.audio.SoundManager
import ru.finny.app.data.Content
import ru.finny.app.data.GameState2
import ru.finny.app.data.ShopItem
import ru.finny.app.data.Task2
import ru.finny.app.data.Wish
import ru.finny.app.game.GameViewModel
import ru.finny.app.ui.theme.Amber
import ru.finny.app.ui.theme.Bg
import ru.finny.app.ui.theme.CardBg
import ru.finny.app.ui.theme.Line
import ru.finny.app.ui.theme.Magenta
import ru.finny.app.ui.theme.MagentaDark
import ru.finny.app.ui.theme.Muted
import ru.finny.app.ui.theme.Night
import ru.finny.app.ui.theme.Orange
import ru.finny.app.ui.theme.OrangeDark
import ru.finny.app.ui.theme.OrangeLight
import ru.finny.app.ui.theme.Peach
import ru.finny.app.ui.theme.Purple
import ru.finny.app.ui.theme.PurpleDark
import ru.finny.app.ui.theme.PurpleLight
import ru.finny.app.ui.theme.TextMain
import ru.finny.app.ui.theme.Violet
import ru.finny.app.ui.theme.VioletLight
import ru.finny.app.ui.theme.Yellow

val gViolet = Brush.linearGradient(listOf(VioletLight, Purple))
val gOrange = Brush.linearGradient(listOf(Peach, Orange))
val gYellow = Brush.linearGradient(listOf(Yellow, Amber))
val gPress = Brush.linearGradient(listOf(Violet, Purple))

/** Кнопка-коробка с пресс-эффектом: сжатие + смена градиента на фиолетовый. */
@Composable
fun PressBox(
    onClick: () -> Unit,
    brush: Brush,
    modifier: Modifier = Modifier,
    radius: Int = 16,
    content: @Composable BoxScope.(Boolean) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, tween(120), label = "press")
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(if (pressed) gPress else brush, RoundedCornerShape(radius.dp))
            .clickable(interactionSource = interaction, indication = null) {
                SoundManager.playClick()
                onClick()
            },
    ) { content(pressed) }
}

@Composable
fun GameScreen(vm: GameViewModel = viewModel()) {
    val phase by vm.phase.collectAsState()
    var showParentDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Bg)) {
        when (phase) {
            "entry" -> EntryScreen(vm)
            "age" -> AgeScreen(vm)
            else -> AppScreen(vm, onOpenParent = { showParentDialog = true })
        }
    }

    if (showParentDialog) {
        ParentModeDialog(vm, onDismiss = { showParentDialog = false })
    }
}

// ================= ВХОД =================
@Composable
private fun EntryScreen(vm: GameViewModel) {
    Column(
        Modifier.fillMaxSize().background(gViolet),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val inf = rememberInfiniteTransition(label = "breathe")
        val br by inf.animateFloat(1f, 1.02f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "b")
        Image(
            painterResource(R.drawable.finny_s2_happy), null,
            Modifier.size(260.dp).graphicsLayer { scaleX = br; scaleY = br },
        )
        Text("Привет! Я Финни!", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(
            "Твой маленький друг уже заждался: играй, заботься, выполняй задания и копи монетки на мечты!",
            fontSize = 14.sp, color = Color.White.copy(alpha = .85f),
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 10.dp),
        )
        PressBox(onClick = { vm.goAge() }, brush = gOrange, radius = 16,
            modifier = Modifier.padding(top = 8.dp)) { pressed ->
            Text(
                "▶  Играть", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 46.dp, vertical = 15.dp),
            )
        }
    }
}

// ================= ВОЗРАСТ =================
@Composable
private fun AgeScreen(vm: GameViewModel) {
    Column(
        Modifier.fillMaxSize().background(gViolet),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("ПОДОБЕРЁМ ЗАДАНИЯ ПО ВОЗРАСТУ", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
            color = Color.White.copy(alpha = .7f), letterSpacing = 1.2.sp)
        Text("Сколько тебе лет?", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AgeBtn("7–8") { vm.pickAge("7-8") }
            AgeBtn("9–11") { vm.pickAge("9-11") }
        }
        Text("Финни подберёт задания и подсказки под твой возраст 💜",
            fontSize = 13.sp, color = Color.White.copy(alpha = .85f), modifier = Modifier.padding(top = 18.dp))
    }
}

@Composable
private fun AgeBtn(label: String, onClick: () -> Unit) {
    PressBox(onClick = onClick, brush = Brush.linearGradient(listOf(CardBg, CardBg)), radius = 18,
        modifier = Modifier.width(126.dp)) { pressed ->
        Text(label, color = if (pressed) Color.White else Purple, fontSize = 21.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.padding(vertical = 17.dp).fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

// ================= ПРИЛОЖЕНИЕ =================
@Composable
private fun AppScreen(vm: GameViewModel, onOpenParent: () -> Unit) {
    val tab by vm.tab.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                "home" -> HomeScreen(vm, onOpenParent)
                "wish" -> WishScreen(vm, onOpenParent)
                else -> TasksScreen(vm, onOpenParent)
            }
        }
        NavBar(vm)
    }
}

@Composable
private fun NavBar(vm: GameViewModel) {
    val tab by vm.tab.collectAsState()
    Row(
        Modifier.fillMaxWidth().background(CardBg).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NavBtn(R.drawable.v_home, "Дом", tab == "home", Modifier.weight(1f)) { vm.show("home") }
        NavBtn(R.drawable.v_heart, "Желания", tab == "wish", Modifier.weight(1f)) { vm.show("wish") }
        NavBtn(R.drawable.v_list, "Задания", tab == "tasks", Modifier.weight(1f)) { vm.show("tasks") }
    }
}

@Composable
private fun NavBtn(icon: Int, label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) PurpleLight else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(icon), null, Modifier.size(24.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
            color = if (active) Purple else Muted)
    }
}

// ================= ДОМ =================
@Composable
private fun HomeScreen(vm: GameViewModel, onOpenParent: () -> Unit) {
    val st by vm.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        // фиолетовая панель: шапка + карточка уровня
        Column(Modifier.fillMaxWidth().background(gViolet).padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Финни", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Твой маленький друг", fontSize = 12.sp, color = Color.White.copy(alpha = .75f))
                }
                HeaderRight(st.coins, onOpenParent)
            }
            Row(
                Modifier.padding(top = 12.dp).fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(18.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(Content.tile("star")), null, Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)))
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("Юный исследователь", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Bar(st.prog / 100f, Modifier.padding(top = 6.dp).height(8.dp))
                }
                Text("${st.level} уровень", color = Purple, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
        Scene(vm, st, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HeaderRight(coins: Int, onOpenParent: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        CoinChip(coins)
        ParentChip(onClick = onOpenParent)
    }
}

@Composable
private fun CoinChip(coins: Int) {
    Row(
        Modifier.background(Color.White.copy(alpha = .18f), RoundedCornerShape(50))
            .padding(start = 6.dp, end = 13.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(Content.tile("coin")), null, Modifier.size(26.dp))
        Text("$coins", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 7.dp))
    }
}

@Composable
private fun ParentChip(onClick: () -> Unit) {
    Row(
        Modifier
            .background(Color.White.copy(alpha = .18f), RoundedCornerShape(50))
            .clickable {
                SoundManager.playClick()
                onClick()
            }
            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🛡️", fontSize = 13.sp)
        Text(
            "Родителям", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

@Composable
private fun Bar(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(PurpleLight)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(Violet, Purple))))
    }
}

@Composable
private fun Scene(vm: GameViewModel, st: GameState2, modifier: Modifier = Modifier) {
    val bubble by vm.bubble.collectAsState()
    val particles by vm.particles.collectAsState()
    val anim by vm.anim.collectAsState()
    val nightAlpha by animateFloatAsState(if (st.sleeping) 1f else 0f, tween(700), label = "night")

    val inf = rememberInfiniteTransition(label = "breathe")
    val breathe by inf.animateFloat(1f, 1.015f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "b")
    val sx = remember { Animatable(1f) }
    val sy = remember { Animatable(1f) }
    val oy = remember { Animatable(0f) }
    val rot = remember { Animatable(0f) }
    LaunchedEffect(anim.token) {
        when (anim.name) {
            "tap", "play", "save", "jump" -> {
                oy.animateTo(-18f, tween(250)); oy.animateTo(0f, tween(220))
                sy.animateTo(0.95f, tween(120)); sy.animateTo(1f, tween(180))
            }
            "wash" -> { rot.animateTo(-4f, tween(200)); rot.animateTo(4f, tween(200)); rot.animateTo(0f, tween(200)) }
            "feed", "munch" -> {
                sy.animateTo(0.93f, tween(150)); sy.animateTo(1.04f, tween(150))
                sy.animateTo(0.95f, tween(150)); sy.animateTo(1f, tween(150))
            }
            "look" -> { rot.animateTo(-3f, tween(300)); rot.animateTo(3f, tween(350)); rot.animateTo(0f, tween(300)) }
            "yawn" -> { sy.animateTo(1.1f, tween(350)); sy.animateTo(0.96f, tween(300)); sy.animateTo(1f, tween(250)) }
            "wiggle" -> { rot.animateTo(-5f, tween(180)); rot.animateTo(4f, tween(180)); rot.animateTo(0f, tween(180)) }
        }
    }

    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(0.dp))) {
        Image(
            painterResource(if (st.sleeping) R.drawable.room_night else R.drawable.room_day), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(Modifier.matchParentSize().background(Night.copy(alpha = Night.alpha * nightAlpha)))

        // три шкалы поверх основного фона
        Row(Modifier.align(Alignment.TopCenter).padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            StatCard(Content.tile("sytost"), "${st.food}%", "Сытость", Modifier.weight(1f))
            StatCard(Content.tile("joy"), "${st.joy}%", "Радость", Modifier.weight(1f))
            StatCard(Content.tile("energy"), "${st.energy}%", "Энергия", Modifier.weight(1f))
        }

        // реплика Финни — над ним, по центру, с переносом строк
        AnimatedVisibility(
            visible = bubble != null,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = .7f, animationSpec = tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
        ) {
            Box(Modifier.width(280.dp).background(CardBg, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
                Text(bubble.orEmpty(), color = PurpleDark, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center)
            }
        }

        // белый круг с аватаром и тенью
        Box(
            Modifier.align(Alignment.Center)
                .offset(y = (-14).dp)
                .shadow(18.dp, RoundedCornerShape(50))
                .size(250.dp).clip(RoundedCornerShape(50)).background(Color(0xFFF6F4FC)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(vm.spriteRes()), "Финни",
                Modifier.size(210.dp)
                    .graphicsLayer {
                        scaleX = sx.value * breathe; scaleY = sy.value * breathe
                        translationY = oy.value; rotationZ = rot.value
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { off ->
                            val zone = when {
                                off.y < size.height * 0.34f -> "head"
                                off.y < size.height * 0.7f -> "belly"
                                else -> "paws"
                            }
                            vm.tapPet(zone)
                        }
                    }
                    .pointerInput(Unit) { detectDragGestures { _, _ -> vm.petMove() } },
            )
        }

        // частицы-контурки
        particles.forEach { p -> ParticleView(p, vm) }

        // бейдж настроения с круглым смайликом
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 112.dp)
                .background(gYellow, RoundedCornerShape(50))
                .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFFF176), Color(0xFFFFB74D)))),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(R.drawable.p_smile),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                when {
                    minOf(st.food, st.joy, st.energy) >= 65 -> "Отличное настроение!"
                    minOf(st.food, st.joy, st.energy) >= 35 -> "Хорошее настроение"
                    else -> "Финни грустит…"
                },
                color = Color(0xFF7A4B00), fontSize = 12.5.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        // панель трёх кнопок — видима внизу сцены
        Row(
            Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp).padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ActionBtn(Content.tile("feed"), "Покормить", Content.tile("feed"), Modifier.weight(1f)) { vm.feed() }
            ActionBtn(Content.tile("play"), "Играть", Content.tile("play"), Modifier.weight(1f)) { vm.play() }
            ActionBtn(Content.tile("sleep"), if (st.sleeping) "Разбудить" else "Спать",
                Content.tile("sleep"), Modifier.weight(1f)) { vm.sleepToggle() }
        }
    }
}

@Composable
private fun StatCard(tile: Int, value: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier.background(CardBg, RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isJoy = tile == Content.tile("joy")
        Image(
            painterResource(tile), null,
            Modifier
                .size(if (isJoy) 42.dp else 36.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Column(Modifier.padding(start = 8.dp)) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = .2.sp)
            Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Muted)
        }
    }
}

@Composable
private fun ActionBtn(tile: Int, label: String, tileActual: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, tween(120), label = "press")
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(CardBg, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Image(painterResource(tileActual), null, Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)))
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                .background(Purple.copy(alpha = if (pressed) 0.45f else 0f)))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun ParticleView(p: GameViewModel.Particle, vm: GameViewModel) {
    val res = when (p.kind) {
        "wishes" -> R.drawable.p_heart
        "star" -> R.drawable.p_star
        "coin" -> R.drawable.p_coin
        "sleep" -> R.drawable.p_moon
        "radost" -> R.drawable.p_flower
        "ball" -> R.drawable.p_ball
        "sytost" -> R.drawable.p_apple
        "energy" -> R.drawable.p_bolt
        else -> R.drawable.p_smile
    }
    val alphaAnim = remember { Animatable(1f) }
    val dyAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.3f) }
    val dxAnim = remember { Animatable(0f) }

    val randomDrift = remember(p.id) { (p.x - 0.5f) * 60f }

    LaunchedEffect(p.id) {
        launch {
            scaleAnim.animateTo(1.15f, tween(250, easing = EaseOutBack))
            scaleAnim.animateTo(0.85f, tween(850, easing = FastOutSlowInEasing))
        }
        launch { dxAnim.animateTo(randomDrift, tween(1100, easing = FastOutSlowInEasing)) }
        launch { dyAnim.animateTo(-110f, tween(1100, easing = FastOutSlowInEasing)) }
        launch {
            delay(600)
            alphaAnim.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
        vm.removeParticle(p.id)
    }

    Image(
        painterResource(res), null,
        Modifier
            .offset(
                x = (20 + p.x * 220 + dxAnim.value).dp,
                y = (100 + p.y * 60 + dyAnim.value).dp
            )
            .size(32.dp)
            .graphicsLayer {
                alpha = alphaAnim.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            },
    )
}

// ================= ЖЕЛАНИЯ =================
@Composable
private fun WishScreen(vm: GameViewModel, onOpenParent: () -> Unit) {
    val st by vm.state.collectAsState()
    val seg by vm.seg.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(gViolet).padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("МЕЧТЫ ФИННИ", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = .7f), letterSpacing = 1.2.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Мои желания", fontSize = 24.sp, fontWeight = FontWeight.Black,
                    color = Color.White, modifier = Modifier.weight(1f))
                HeaderRight(st.coins, onOpenParent)
            }
        }
        // карточка накоплений
        Row(
            Modifier.padding(14.dp).fillMaxWidth().background(gYellow, RoundedCornerShape(20.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(Content.tile("backpack")), null, Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("КОПЛЮ НА", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8A5B00))
                Text("Рюкзак исследователя", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF5C3A00))
                Box(Modifier.padding(top = 6.dp).height(8.dp).fillMaxWidth().clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = .55f))) {
                    Box(Modifier.fillMaxWidth(st.saved.toFloat() / st.savedTarget).height(8.dp)
                        .clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(Violet, Purple))))
                }
            }
            Text("${st.saved} / ${st.savedTarget}", color = Color(0xFF5C3A00), fontSize = 13.sp,
                fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
        }
        // сегменты
        Row(Modifier.padding(horizontal = 14.dp).fillMaxWidth().background(Color(0xFFE9E4F8), RoundedCornerShape(14.dp)).padding(4.dp)) {
            SegBtn("Желания", seg == "wish") { vm.setSeg("wish") }
            SegBtn("Покупки", seg == "buy") { vm.setSeg("buy") }
            SegBtn("Задания", seg == "task") { vm.setSeg("task") }
        }
        Spacer(Modifier.height(12.dp))
        when (seg) {
            "wish" -> {
                Row(Modifier.padding(horizontal = 16.dp)) {
                    Text("Список желаний", fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text("${Content.wishes.count { !st.bought.contains(it.id) }} желания",
                        color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Black,
                        modifier = Modifier.background(OrangeLight, RoundedCornerShape(50)).padding(horizontal = 11.dp, vertical = 4.dp))
                }
                WishGrid(vm, st)
            }
            "buy" -> {
                Row(Modifier.padding(horizontal = 16.dp)) {
                    Text("Забота о Финни", fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text("покупки дня", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Black,
                        modifier = Modifier.background(OrangeLight, RoundedCornerShape(50)).padding(horizontal = 11.dp, vertical = 4.dp))
                }
                ShopGrid(vm)
            }
            else -> TodayCard(vm, st)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RowScope.SegBtn(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
            .background(if (active) Purple else Color.Transparent)
            .clickable(onClick = onClick).padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold,
            color = if (active) Color.White else Purple)
    }
}

@Composable
private fun WishGrid(vm: GameViewModel, st: GameState2) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Content.wishes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { w -> WishCard(w, st.bought.contains(w.id), vm, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun WishCard(w: Wish, done: Boolean, vm: GameViewModel, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(120), label = "press")
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (done) 0.55f else 1f }
            .background(CardBg, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = null) { vm.buyWish(w.id) }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(Content.tile(w.ic)), null, Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)))
        Text(w.n, fontSize = 12.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 6.dp, end = 6.dp))
        Text(if (done) "✓ куплено" else "хочу", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold,
            color = if (done) Color(0xFF2E8B57) else Muted, modifier = Modifier.padding(top = 3.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Text("${w.p}", color = Orange, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Image(painterResource(Content.tile("coin")), null, Modifier.size(16.dp).padding(start = 5.dp))
        }
    }
}

@Composable
private fun ShopGrid(vm: GameViewModel) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Content.shop.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { it2 -> ShopCard(it2, vm, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ShopCard(item: ShopItem, vm: GameViewModel, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(120), label = "press")
    val isHoney = item.ic == "honey"
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(CardBg, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = null) { vm.buyShop(item.id) }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painterResource(Content.tile(item.ic)), null,
            Modifier
                .size(if (isHoney) 62.dp else 52.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Text(item.n, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
        Text(item.txt, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Muted, modifier = Modifier.padding(top = 3.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Text("${item.p}", color = Orange, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Image(painterResource(Content.tile("coin")), null, Modifier.size(16.dp).padding(start = 5.dp))
        }
    }
}

@Composable
private fun TodayCard(vm: GameViewModel, st: GameState2) {
    Row(
        Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth()
            .background(CardBg, RoundedCornerShape(18.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(Content.tile("tasks")), null, Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("Задания на сегодня", fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text("${minOf(3, st.solved.size)} из 3 выполнено • награда 15 монет",
                fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Muted)
        }
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(50))
            .background(Brush.linearGradient(listOf(Violet, Purple)))
            .clickable { vm.show("tasks") }, contentAlignment = Alignment.Center) {
            Image(painterResource(Content.tile("arrow")), null, Modifier.size(18.dp))
        }
    }
}

// ================= ЗАДАНИЯ =================
@Composable
private fun TasksScreen(vm: GameViewModel, onOpenParent: () -> Unit) {
    val st by vm.state.collectAsState()
    val openId by vm.openTask.collectAsState()
    val fb by vm.fb.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(gViolet).padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("ФИНГРАМОТНОСТЬ", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = .7f), letterSpacing = 1.2.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Задания", fontSize = 24.sp, fontWeight = FontWeight.Black,
                    color = Color.White, modifier = Modifier.weight(1f))
                HeaderRight(st.coins, onOpenParent)
            }
        }
        Spacer(Modifier.height(12.dp))
        val task = Content.tasks.find { it.id == openId }
        if (task == null) {
            vm.sortedTasks().forEach { t ->
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 5.dp).fillMaxWidth()
                        .background(CardBg, RoundedCornerShape(18.dp)).padding(13.dp)
                        .clickable { vm.openTask(t.id) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(Content.tile(thTile(t.th))), null, Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)))
                    Text(t.t, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f).padding(start = 11.dp))
                    if (st.solved.contains(t.id)) {
                        Text("✓ решено", color = Color(0xFF2E8B57), fontSize = 11.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.background(Color(0xFFE8F7EC), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
                    } else {
                        Text("+15", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.background(PurpleLight, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
        } else {
            Column(Modifier.padding(horizontal = 14.dp).fillMaxWidth().background(CardBg, RoundedCornerShape(18.dp)).padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(Content.tile(thTile(task.th))), null, Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)))
                    Text(task.t, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f).padding(start = 11.dp))
                    Text("+15", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Text(task.s, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Muted, modifier = Modifier.padding(top = 8.dp))
                Text(task.q, fontSize = 13.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                task.o.forEachIndexed { i, opt ->
                    Text(opt, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(12.dp))
                            .clickable { vm.answer(i) }
                            .padding(12.dp))
                }
                fb?.let { (ok, text) ->
                    Box(Modifier.padding(top = 9.dp).fillMaxWidth()
                        .background(if (ok) PurpleLight else OrangeLight, RoundedCornerShape(12.dp)).padding(12.dp)) {
                        Text((if (ok) "Верно! 🎉 " else "Подсказка: ") + text, fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold, color = if (ok) PurpleDark else OrangeDark)
                    }
                    if (ok) {
                        PressBox(onClick = { vm.openTask(null) }, brush = gOrange, radius = 14,
                            modifier = Modifier.padding(top = 10.dp).fillMaxWidth()) { _ ->
                            Text("К списку заданий", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun thTile(th: String) = if (th == "budget") "coin" else if (th == "savings") "backpack" else "ball"

// ================= РЕЖИМ РОДИТЕЛЯ =================
@Composable
private fun ParentModeDialog(vm: GameViewModel, onDismiss: () -> Unit) {
    val st by vm.state.collectAsState()
    var isVerified by remember { mutableStateOf(false) }
    var answerText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showConfirmReset by remember { mutableStateOf(false) }

    if (!isVerified) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Режим родителя 🛡️",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = PurpleDark
                )
            },
            text = {
                Column {
                    Text(
                        "Подтвердите, что вы взрослый. Решите пример:",
                        fontSize = 14.sp,
                        color = TextMain
                    )
                    Text(
                        "Сколько будет 6 × 7 = ?",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Purple,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = {
                            answerText = it
                            errorText = null
                        },
                        label = { Text("Ответ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorText?.let { err ->
                        Text(
                            err,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (answerText.trim() == "42") {
                            SoundManager.playSuccess()
                            isVerified = true
                        } else {
                            SoundManager.playClick()
                            errorText = "Неверный ответ! Попробуйте еще раз."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Text("Войти", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = Muted)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    } else if (showConfirmReset) {
        AlertDialog(
            onDismissRequest = { showConfirmReset = false },
            title = {
                Text(
                    "Сбросить весь прогресс?",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Text(
                    "Все достижения ребенка, заработанные монеты, уровень и пройденные задания будут удалены безвозвратно.",
                    fontSize = 14.sp,
                    color = TextMain
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.resetGame()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Да, сбросить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmReset = false }) {
                    Text("Отмена", color = Muted)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Кабинет родителя 📊",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = PurpleDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Прогресс и статистика ребёнка:", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Muted)

                    ParentStatRow("🌟 Уровень", "${st.level} (${st.prog}% опыта)")
                    ParentStatRow("📅 Дней в игре", "${st.day}")
                    ParentStatRow("🪙 Накоплено монет", "${st.coins}")
                    ParentStatRow("🎯 Выполнено заданий", "${st.solved.size} из ${Content.tasks.size}")
                    ParentStatRow("🎁 Куплено желаний", "${st.bought.size}")
                    ParentStatRow("🎂 Возраст ребёнка", st.age ?: "Не выбран")

                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { showConfirmReset = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🗑️ Сбросить прогресс игры", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                    Text("Закрыть", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ParentStatRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Purple)
    }
}

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.State
import data.Transition
import data.TypeState
import org.jetbrains.skia.*
import org.jetbrains.skia.Paint
import ui.CoordsStates
import ui.CoordsTransitions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun App() {
    MaterialTheme {
        view()
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "AFD's") {
        App()
    }
}

@Composable
fun view() {
    val transitions: MutableList<CoordsTransitions> = remember { mutableStateListOf() }
    val states: MutableList<CoordsStates> = remember { mutableStateListOf() }
    var windowTransition: Boolean by remember { mutableStateOf(false) }
    var windowPlay: Boolean by remember { mutableStateOf(false) }
    var dragState by remember { mutableStateOf<Int?>(null) }

    var windowState: Pair<Boolean, Int> by remember { mutableStateOf(Pair(false, -1)) }

    Column {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(Color.LightGray)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = {
                        states.add(
                            CoordsStates(
                                mutableStateOf(Offset(100f, 100f)),
                                State(mutableStateOf(TypeState.NORMAL), mutableListOf())
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(Color.White)
                ) { Icon(Icons.Outlined.AddCircle, "New state button") }
                Button(
                    onClick = {
                        windowTransition = true
                    }, colors = ButtonDefaults.buttonColors(Color.White)
                ) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "New transition button") }
                Button(
                    onClick = {
                        windowPlay = true
                    }, colors = ButtonDefaults.buttonColors(Color.White)
                ) { Icon(Icons.Filled.PlayArrow, "Play button") }
            }
        }
        Canvas(modifier = Modifier.fillMaxSize().background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragState = states.indexOfFirst {
                            (offset - it.offset.value).getDistance() < 25f
                        }.takeIf { it != -1 }
                    },
                    onDrag = { change, dragAmount ->
                        dragState?.let { i ->
                            states[i].offset.value += dragAmount
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        dragState = null
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        states
                            .indexOfFirst { (offset - it.offset.value).getDistance() < 25f }
                            .takeIf { it != -1 }
                            ?.let { index ->
                                windowState = Pair(true, index)
                                //states[index].id.type.value = (states[index].id.type.value + 1) % 3
                            }
                    }
                )
            }
        ) {

            val groupTransitions = transitions.groupBy(
                keySelector = { Pair(it.coordState1.offset.value, it.coordState2.offset.value) },
                valueTransform = { it.id.char }
            )

            groupTransitions.forEach { (key, chars) ->
                drawArrow(key.first, key.second, chars, Color.Black)
                //println("Q1: " + transition.coordState1.id + "  --" + transition.id.char + "-->  " + "Q2: " + transition.id.goTo)
            }

            states.forEachIndexed { i, state ->
                drawState(state, i)
            }
        }
        if (windowTransition) {
            viewTransition(states, onCloseRequest = { windowTransition = false })?.let { transitions.add(it) }
        }
        if (windowPlay) {
            viewPlay(
                states.map { state -> state.id },
                onCloseRequest = { windowPlay = false })
        }
        if (windowState.first) {
            viewState(
                states[windowState.second].id,
                states,
                transitions,
                onCloseRequest = { windowState = Pair(false, -1) })
        }
    }
}

@Composable
fun viewState(
    state: State,
    states: MutableList<CoordsStates>,
    transitions: MutableList<CoordsTransitions>,
    onCloseRequest: () -> Unit
) {
    val types = mutableMapOf(TypeState.NORMAL to "normal", TypeState.FINAL to "final", TypeState.INITIAL to "inicial")

    Window(
        onCloseRequest = onCloseRequest,
        resizable = false,
        title = "type AFD's",
        state = rememberWindowState(width = 300.dp, height = 200.dp)
    ) {
        Column {
            LazyColumn(modifier = Modifier.padding(10.dp)) {
                item { Text("Tipo de estado") }
                items(types.toList()) { (key, value) ->
                    Text(
                        value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (state.type.value == key)
                                    state.type.value = TypeState.NORMAL
                                else
                                    if (!states.any { it.id.type.value == TypeState.INITIAL && key == TypeState.INITIAL })
                                        state.type.value = key
                            }
                            .background(
                                if (state.type.value == key) {
                                    Color.Gray
                                } else {
                                    Color.Transparent
                                }
                            )
                    )
                }
            }
            Button(
                onClick = {
                    states.removeIf { it.id == state }
                    transitions.removeAll { it.coordState1.id == state || it.coordState2.id == state }
                    onCloseRequest()
                },
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier
                    .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxWidth()
            ) { Text("Eliminar estado") }
        }
    }
}

@Composable
fun viewPlay(states: List<State>, onCloseRequest: () -> Unit) {
    var a by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("Presione el boton ->") }

    Window(
        onCloseRequest = onCloseRequest,
        resizable = false,
        title = "AFD's execution",
        state = rememberWindowState(width = 400.dp, height = 200.dp)
    ) {
        Column {
            TextField(
                value = a,
                onValueChange = { newText ->
                    a = newText.filter { it.isLetterOrDigit() }
                },
                label = { Text("Entrada para el AFD") },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    text = result,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Button(
                    onClick = { confirm = true },
                    colors = ButtonDefaults.buttonColors(Color.White),
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(.5f)
                ) { Icon(Icons.Filled.PlayArrow, "Transition button") }
            }
        }
    }
    if (confirm) {
        confirm = false
        result = validInput(a, states)
    }
}

@Composable
fun viewTransition(states: MutableList<CoordsStates>, onCloseRequest: () -> Unit): CoordsTransitions? {
    var a: String by remember { mutableStateOf(" ") }
    var state1: CoordsStates? by remember { mutableStateOf(null) }
    var state2: CoordsStates? by remember { mutableStateOf(null) }
    var confirm by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = onCloseRequest,
        resizable = false,
        title = "AFD's transition",
        state = rememberWindowState(width = 400.dp, height = 300.dp)
    ) {
        Column {
            Row(modifier = Modifier.height(180.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LazyColumn(modifier = Modifier.weight(1f).padding(10.dp)) {
                    item { Text("Estado Inicio") }
                    itemsIndexed(states) { index, item ->
                        Text(
                            index.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { state1 = item }
                                .background(if (state1 == states[index]) Color.LightGray else Color.Transparent)
                        )
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f).padding(10.dp)) {
                    item { Text("Estado Fin") }
                    itemsIndexed(states) { index, item ->
                        Text(
                            index.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { state2 = item }
                                .background(if (state2 == states[index]) Color.LightGray else Color.Transparent)
                        )
                    }
                }
            }
            Row(modifier = Modifier.height(70.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextField(
                    value = a,
                    onValueChange = { newText ->
                        if (newText.last().isLetterOrDigit() || newText.last().isWhitespace())
                            a = newText.last().toString()
                    },
                    label = { Text("Caracter de transicion") },
                    modifier = Modifier.weight(1f).padding(10.dp)
                )
                Button(
                    modifier = Modifier.padding(10.dp).fillMaxHeight(),
                    onClick = { confirm = true },
                    colors = ButtonDefaults.buttonColors(Color.White)
                ) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Transition button") }
            }
        }
    }
    if (state1 != null && state2 != null && confirm) {

        val transitionExists = state1?.id?.transitions?.any {
            it.char == a.last() && it.goTo == state2!!.id
        } ?: false

        if (!transitionExists) {
            val transition = Transition(a.last(), state2!!.id)

            states.forEachIndexed { i, state ->
                if (state.id == state1?.id) {
                    states[i].id.transitions.add(transition)

                    confirm = false
                    return CoordsTransitions(state1!!, state2!!, transition)
                }
            }
        }

        confirm = false
        return null
    } else
        return null
}

fun validInput(input: String, states: List<State>): String {
    var currentStates: MutableList<State> = (mutableListOf())
    val newStates: MutableList<State> = (mutableListOf())

    states.firstOrNull { it.type.value == TypeState.INITIAL }?.let { currentStates.add(it) }
        ?: return "No hay estado inicial"

    input.forEachIndexed { i, char ->
        println("$char No.${i + 1}")
        newStates.clear()
        currentStates.forEach { state ->
            state.transitions.forEach { transition ->
                if (transition.char == char) {
                    newStates.add(transition.goTo)
                }
            }
        }
        currentStates = newStates.toMutableList()
    }

    return if (currentStates.any { it.type.value == TypeState.FINAL }) "Cadena aceptada" else "Cadena rechazada"
}

fun DrawScope.drawArrow(from: Offset, to: Offset, chars: List<Char>, color: Color) {
    if (from == to) {
        val radius = 30f
        val center = Offset(from.x, from.y - radius)

        drawArc(
            color = color,
            startAngle = -30f,
            sweepAngle = 320f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 1f)
        )

        // Posición de las letras en el arco
        val textPosition = Offset(from.x + 13f, from.y - 48f)

        drawIntoCanvas { canvas ->
            val textLine =
                TextLine.make(chars.joinToString(","), Font(Typeface.makeFromName("Arial", FontStyle.ITALIC), 15f))
            canvas.nativeCanvas.drawTextLine(textLine, textPosition.x, textPosition.y, Paint())
        }

        drawPath(
            path = Path().apply {
                moveTo(from.x + 30f, from.y - 15f)
                lineTo(from.x + 22f, from.y - 10f)
                lineTo(from.x + 23f, from.y - 20f)
            },
            color = Color.Black,
        )

    } else {
        // Caso normal de transición entre diferentes estados
        val direction = Offset(to.x - from.x, to.y - from.y)
        val length = direction.getDistance()

        val unitDirection = if (length > 0) Offset(direction.x / length, direction.y / length) else Offset(0f, 0f)
        val adjustedTo = Offset(to.x - unitDirection.x * 25f, to.y - unitDirection.y * 25f)

        drawLine(color = color, start = from, end = adjustedTo)

        val angle = atan2(adjustedTo.y - from.y, adjustedTo.x - from.x)
        val arrowSize = 10f
        val arrowPoint1 = Offset(
            x = adjustedTo.x - (arrowSize * cos(angle - Math.PI / 6)).toFloat(),
            y = adjustedTo.y - (arrowSize * sin(angle - Math.PI / 6)).toFloat()
        )
        val arrowPoint2 = Offset(
            x = adjustedTo.x - (arrowSize * cos(angle + Math.PI / 6)).toFloat(),
            y = adjustedTo.y - (arrowSize * sin(angle + Math.PI / 6)).toFloat()
        )

        val path = Path().apply {
            moveTo(adjustedTo.x, adjustedTo.y)
            lineTo(arrowPoint1.x, arrowPoint1.y)
            lineTo(arrowPoint2.x, arrowPoint2.y)
            close()
        }

        // Posición de las letras
        val midPoint = Offset((from.x + adjustedTo.x) / 2, (from.y + adjustedTo.y) / 2)
        val perpendicularOffset = Offset(unitDirection.y, -unitDirection.x) * 15f
        val textPosition = midPoint + perpendicularOffset

        drawIntoCanvas { canvas ->
            val textLine =
                TextLine.make(chars.joinToString(","), Font(Typeface.makeFromName("Arial", FontStyle.ITALIC), 15f))
            canvas.nativeCanvas.drawTextLine(textLine, textPosition.x, textPosition.y, Paint())
        }

        drawPath(path = path, color = color)
    }
}

fun DrawScope.drawState(state: CoordsStates, num: Int) {
    when (state.id.type.value) {
        TypeState.NORMAL -> drawCircle(color = Color.LightGray, radius = 25f, state.offset.value)
        TypeState.FINAL -> {
            drawCircle(color = Color.Gray, radius = 25f, state.offset.value)
            drawCircle(color = Color.LightGray, radius = 20f, state.offset.value)
        }

        TypeState.INITIAL -> {
            drawCircle(color = Color.LightGray, radius = 25f, state.offset.value)
            drawPath(
                path = Path().apply {
                    moveTo(state.offset.value.x - 25f, state.offset.value.y)
                    lineTo(state.offset.value.x - 25f - 20f, state.offset.value.y + 20f)
                    lineTo(state.offset.value.x - 25f - 20f, state.offset.value.y - 20f)
                },
                color = Color.LightGray,
            )
        }
    }
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawTextLine(
            TextLine.make(num.toString(), font = Font(Typeface.makeFromName("Arial", FontStyle.BOLD), 25f)),
            state.offset.value.x - 7f,
            state.offset.value.y + 8f,
            paint = Paint().apply { color = Color.White.toArgb() },
        )
    }
}
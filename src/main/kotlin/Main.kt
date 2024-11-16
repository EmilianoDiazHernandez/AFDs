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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.State
import data.TypeState
import ui.StateCoords
import ui.TransitionCoords
import controller.*

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
    val transitions: MutableList<TransitionCoords> = remember { mutableStateListOf() }
    val states: MutableList<StateCoords> = remember { mutableStateListOf() }
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
                        createState(states)
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
                            }
                    }
                )
            }
        ) {

            drawTransitions(transitions)
            drawStates(states)

        }
        if (windowState.first) {
            viewState(
                states[windowState.second].id,
                states,
                transitions,
                onCloseRequest = { windowState = Pair(false, -1) })
        }
        if (windowTransition) {
            viewTransition(states, onCloseRequest = { windowTransition = false })?.let { transitions.add(it) }
        }
        if (windowPlay) {
            viewPlay(
                states.map { state -> state.id },
                onCloseRequest = { windowPlay = false })
        }
    }
}

@Composable
fun viewState(
    state: State,
    states: MutableList<StateCoords>,
    transitions: MutableList<TransitionCoords>,
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
                                changeTypeState(states, state, key)
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
                    deleteState(states, transitions, state)
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
fun viewTransition(states: MutableList<StateCoords>, onCloseRequest: () -> Unit): TransitionCoords? {
    var a: String by remember { mutableStateOf("ε") }
    var state1: StateCoords? by remember { mutableStateOf(null) }
    var state2: StateCoords? by remember { mutableStateOf(null) }
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
                        if (newText.last().isLetterOrDigit())
                            a = newText.last().toString()
                        if (newText.last() == ' ')
                            a = "ε"
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

    if (confirm) {
        confirm = false
        return if (state1 != null && state2 != null)
            createTransition(states, state1!!, state2!!, a.last())
        else
            null
    }
    return null
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

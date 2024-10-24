import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.State
import data.Transition
import ui.CoordsStates
import ui.CoordsTransitions

@Composable
@Preview
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
    val states: MutableList<CoordsStates> = remember { mutableStateListOf() }
    val transitions: MutableList<CoordsTransitions> = remember { mutableStateListOf() }
    var dragState by remember { mutableStateOf<Int?>(null) }
    var windowTransition: Boolean by remember { mutableStateOf(false) }

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
                        states.add(CoordsStates(mutableStateOf(Offset(100f, 100f)) , State(null)))
                    },
                    colors = ButtonDefaults.buttonColors(Color.White)
                ) { Icon(Icons.Outlined.AddCircle, "New state button") }
                Button(
                    onClick = {
                        windowTransition = true
                    }, colors = ButtonDefaults.buttonColors(Color.White)
                ) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Transition button") }
                Button(
                    onClick = {

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
                            states[i].offset.value +=  dragAmount
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
                        states.indexOfFirst { (offset - it.offset.value).getDistance() < 25f }
                            .takeIf { it != -1 }
                            ?.let { index -> states.removeAt(index) }
                    }
                )
            }
        ) {
            transitions.forEach { transition ->
                drawLine(color = Color.Black, transition.coordState1.offset.value, transition.coordState2.offset.value)
            }
            states.forEach { state ->
                drawCircle(color = Color.LightGray, radius = 25f, state.offset.value)
            }
        }
        if (windowTransition) {
            createTransition(states, onCloseRequest = { windowTransition = false })?.let { transitions.add(it) }
        }
    }
}


@Composable
fun createTransition(states: MutableList<CoordsStates>, onCloseRequest: () -> Unit): CoordsTransitions? {
    var a by remember { mutableStateOf("") }
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
                                .clickable {
                                    state1 = item
                                }
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
                                .clickable {
                                    state2 = item
                                }
                                .background(if (state2 == states[index]) Color.LightGray else Color.Transparent)
                        )
                    }
                }
            }
            Row(modifier = Modifier.height(70.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextField(
                    value = a,
                    onValueChange = { newText -> a = newText },
                    label = { Text("Caracter de transicion") },
                    modifier = Modifier.weight(1f).padding(10.dp)
                )
                Button(
                    modifier = Modifier.padding(10.dp).fillMaxHeight(),
                    onClick = {
                        confirm = true
                    }, colors = ButtonDefaults.buttonColors(Color.White)
                ) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Transition button") }
            }
        }
    }
    if (state1 != null && state2 != null && confirm) {
        confirm = false
        return CoordsTransitions(state1!!, state2!!, Transition(a, state2!!.id))
    } else
        return null
}
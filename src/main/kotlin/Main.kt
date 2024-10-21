import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    MaterialTheme {
        buttons()
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "AFD's") {
        App()
    }
}

@Composable
fun buttons (){
    var states: MutableList<State> by remember { mutableStateOf(mutableListOf()) }
    var buttonClicked:Int by remember { mutableStateOf(0) }
    var x: Float by remember { mutableStateOf(0f) }
    var y: Float by remember { mutableStateOf(0f) }

    Column {
        Box(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(Color.LightGray)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(onClick = {buttonClicked = 1 }, colors = ButtonDefaults.buttonColors(Color.White)) {
                    Icon(Icons.Outlined.AddCircle, "State button")
                }
                Button(onClick = {buttonClicked = 2 }, colors = ButtonDefaults.buttonColors(Color.White)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Transition button")
                }
                Button(onClick = {buttonClicked = 3 }, colors = ButtonDefaults.buttonColors(Color.White)) {
                    Icon(Icons.Outlined.Close, "Transition button")
                }
                Button(onClick = {onClickPlay()}, colors = ButtonDefaults.buttonColors(Color.White)) {
                    Icon(Icons.Filled.PlayArrow, "Play button")
                }
            }
        }
        Canvas(modifier = Modifier
            .fillMaxSize()
            .border(BorderStroke(1.dp, Color.Black))
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    x = offset.x
                    y = offset.y
                    println("x: " + offset.x+", y: " + offset.y)
                    when(buttonClicked) {
                        1-> states = (states + State(x, y, null)).toMutableList()
                        2-> println("Transition")
                        3-> states = states.filterNot { state -> state.x in x-20f..x+20f && state.y in y-20f..y+20f}.toMutableList()
                    }
                }
            }
        ){
            states.forEach{ state->
                drawState(state.x,state.y,states.size)
            }
        }
    }

    println(buttonClicked)
    println(states)
}

@Composable
fun onClickTransition(onClose: () -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    Window(onCloseRequest = { onClose() }, title = "Caracter de transicion ") {
        Column (modifier = Modifier
            .wrapContentSize()
            .padding(15.dp)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Label") }
            )
        }
    }
}

fun onClickPlay() {
    println("Play")
}

// Función para dibujar un estado
fun DrawScope.drawState(x: Float, y: Float, label: Int) {
    drawCircle(Color.LightGray, radius = 25f, center = androidx.compose.ui.geometry.Offset(x, y))

}
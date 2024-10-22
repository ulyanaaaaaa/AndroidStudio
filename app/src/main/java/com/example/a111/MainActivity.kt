package com.example.a111

import android.app.Person
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Main()
    }
}
    @Composable
    fun Main() {
        val navController = rememberNavController()
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 56.dp)) {
                NavHost(navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) { Greeting() }
                    composable(NavRoutes.Lists.route) { ListsPage() }
                    composable(NavRoutes.Image.route){ ImagePage() }
                    composable(NavRoutes.Photo.route){ PhotoPage() }
                }
            }
            BottomNavigationBar(navController = navController, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        NavBarItems.BarItems.forEach { navItem ->
            NavigationBarItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = navItem.image,
                        contentDescription = navItem.title
                    )
                },
                label = {
                    Text(text = navItem.title)
                }
            )
        }
    }
}

object NavBarItems {
    val BarItems = listOf(
        BarItem(
            title = "Home",
            image = Icons.Filled.Home,
            route = "home"
        ),
        BarItem(
            title = "Lists",
            image = Icons.Filled.List,
            route = "lists"
        ),
        BarItem(
            title = "Image",
            image = Icons.Filled.Favorite,
            route = "image"
        ),
        BarItem(
            title = "Photo",
            image = Icons.Filled.Face,
            route = "photo"
        )
    )
}

data class BarItem(
    val title: String,
    val image: ImageVector,
    val route: String
)

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Lists : NavRoutes("lists")
    object Image : NavRoutes("image")
    object Photo : NavRoutes("photo")
}

@Composable
fun PhotoPage(modifier: Modifier = Modifier) {
    var hasImage by rememberSaveable { mutableStateOf(false) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            hasImage = uri != null
            imageUri = uri
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            hasImage = success
            if (success) {
                imageUri = currentUri
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Разрешение получено", Toast.LENGTH_SHORT).show()
                currentUri?.let { cameraLauncher.launch(it) }
            } else {
                Toast.makeText(context, "В разрешении отказано", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (hasImage && imageUri != null) {
            Image(
                painter = rememberImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.TopCenter)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text(text = "Выбрать изображение")
            }

            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    currentUri = ComposeFileProvider.getImageUri(context)
                    val permissionCheckResult = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    )
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(currentUri!!)
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }
            ) {
                Text(text = "Сделать снимок")
            }
        }
    }
}


@Composable
fun ImagePage(modifier: Modifier = Modifier) {
    var isMoving by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val configuration = LocalConfiguration.current

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { isMoving = !isMoving }) {
                    Text(text = if (isMoving) "Stop" else "Move")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { if (!isMoving) rotationAngle += 45f }) {
                    Text(text = "Rotate")
                }
            }
            Image(
                painter = painterResource(id = R.drawable.cat),
                contentScale = ContentScale.Crop,
                contentDescription = "My pet",
                modifier = Modifier
                    .size(360.dp)
                    .clip(CircleShape)
                    .offset(x = if (isMoving) offset.dp else 0.dp)
                    .rotate(if (isMoving) rotation else rotationAngle)
                    .graphicsLayer {
                        if (isMoving) {
                            alpha = 1f
                            shadowElevation = 8.dp.toPx()
                            shape = CircleShape
                            clip = true
                            this.rotationZ = rotation
                        }
                    }
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cat),
                contentScale = ContentScale.Crop,
                contentDescription = "My pet",
                modifier = Modifier
                    .size(360.dp)
                    .clip(CircleShape)
                    .offset(x = if (isMoving) offset.dp else 0.dp)
                    .rotate(if (isMoving) rotation else rotationAngle)
                    .graphicsLayer {
                        if (isMoving) {
                            alpha = 1f
                            shadowElevation = 8.dp.toPx()
                            shape = CircleShape
                            clip = true
                            this.rotationZ = rotation
                        }
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { isMoving = !isMoving }) {
                Text(text = if (isMoving) "Stop" else "Move")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { if (!isMoving) rotationAngle += 45f }) {
                Text(text = "Rotate")
            }
        }
    }
}



@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ListsPage(modifier: Modifier = Modifier) {
    val people = listOf(
        Person("Ульяна", "Иванова", "224403"),
        Person("Алиса", "Смирнова", "334455"),
        Person("Боб", "Петров", "224403"),
        Person("Сэм", "Васильев", "224403"),
        Person("Катя", "Кузнецова", "235645"),
        Person("Марк", "Соколов", "123456"),
        Person("Билл", "Морозов", "123456"),
        Person("Сандра", "Новикова", "456789"),
        Person("Лиза", "Федорова", "456789"),
        Person("Алекс", "Волков", "987654"),
        Person("Ирина", "Лебедева", "224403"),
        Person("Дмитрий", "Козлов", "334455"),
        Person("Елена", "Зайцева", "224403"),
        Person("Николай", "Павлов", "224403"),
        Person("Ольга", "Михайлова", "235645"),
        Person("Антон", "Романов", "123456"),
        Person("Виктор", "Орлов", "123456"),
        Person("Мария", "Тихонова", "456789"),
        Person("Сергей", "Беляев", "456789"),
        Person("Татьяна", "Гусева", "987654"),
        Person("Юлия", "Крылова", "224403"),
        Person("Андрей", "Макаров", "334455"),
        Person("Владимир", "Фролов", "224403"),
        Person("Екатерина", "Соловьева", "224403"),
        Person("Алексей", "Виноградов", "235645"),
        Person("Наталья", "Григорьева", "123456"),
        Person("Игорь", "Мельников", "123456"),
        Person("Валерия", "Дмитриева", "456789"),
        Person("Павел", "Коновалов", "456789"),
        Person("Светлана", "Ершова", "987654")
    )

    val groups = people.groupBy {
        it.Group
    }
    LazyColumn(
        contentPadding = PaddingValues(5.dp)
    ) {
        groups.forEach { (group, student) ->
            stickyHeader {
                Text(
                    text = group,
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Red)
                        .padding(5.dp)
                        .fillMaxWidth()
                )
            }
            items(student) { student ->
                Text(
                    student.Name + " " + student.LastName,
                    Modifier.padding(5.dp),
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_img),
            contentDescription = "Hello Student Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF2196F3).copy(alpha = 0.5f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var myName by rememberSaveable { mutableStateOf("") }
            var textValue by rememberSaveable { mutableStateOf("") }

            Text(
                text = stringResource(id = R.string.greeting_text, myName),
                modifier = modifier.padding(26.dp),
                fontSize = 48.sp,
                color = Color.Red,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.White,
                        offset = Offset(4f, 4f),
                        blurRadius = 3f
                    )
                )
            )

            Row(
                modifier = modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text(text = "Ввести имя") },
                    placeholder = { Text(text = "Ваше имя") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            myName = textValue
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        textValue = ""
                    },
                ) {
                    Text(text = "X")
                }
            }
        }
    }
}



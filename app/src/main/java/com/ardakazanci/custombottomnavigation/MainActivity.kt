package com.ardakazanci.custombottomnavigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardakazanci.custombottomnavigation.ui.theme.CustomBottomNavigationTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomBottomNavigationTheme {
                CurvedBottomAppBar()
            }
        }
    }
}

@Composable
fun CurvedBottomAppBar() {
    val bottomBarHeight = 80.dp
    val circleOffsetY = 32.dp
    val mainCircleRadius = 36f
    val smallCircleRadius = 8f

    val menuItems = listOf(
        Icons.Filled.Home to Color(0XFFF3E0EC),
        Icons.Filled.Search to Color(0XFFEAD5E6),
        Icons.Filled.Favorite to Color(0XFFF2BEFC),
        Icons.Filled.Notifications to Color(0XFFCA9CE1),
        Icons.Filled.Settings to Color(0XFF685F74)
    )

    val circleOffset = remember { Animatable(0f) }
    val selectedColor = remember { mutableStateOf(Color(0XFFF3E0EC)) }
    val coroutineScope = rememberCoroutineScope()


    val smallCircles = remember {
        List(5) { Animatable(Offset.Zero, Offset.VectorConverter) }
    }

    LaunchedEffect(Unit) {

        smallCircles.forEachIndexed { index, animatable ->
            launch {
                while (true) {
                    animateSmallCircle(animatable, mainCircleRadius, smallCircleRadius, index)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .height(bottomBarHeight)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = .8f),
                                Color.Black.copy(alpha = 1f),
                            ),
                        ),
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val menuItemWidth = size.width / menuItems.size
                    val animatedX = circleOffset.value * menuItemWidth + menuItemWidth / 2
                    val mainCircleCenter = Offset(
                        animatedX,
                        size.height - mainCircleRadius.dp.toPx() - circleOffsetY.toPx()
                    )


                    smallCircles.forEach { animatable ->
                        drawCircle(
                            color = Color.White,
                            radius = smallCircleRadius.dp.toPx(),
                            center = mainCircleCenter + animatable.value
                        )
                    }


                    drawCircle(
                        color = selectedColor.value,
                        radius = mainCircleRadius.dp.toPx(),
                        center = mainCircleCenter
                    )
                }

                NavigationBar(
                    modifier = Modifier.align(Alignment.BottomStart),
                    containerColor = Color.Transparent
                ) {
                    menuItems.forEachIndexed { index, pair ->
                        val (icon, color) = pair
                        NavigationBarItem(
                            modifier = Modifier.padding(bottom = 20.dp),
                            icon = { Icon(icon, contentDescription = null) },
                            selected = circleOffset.value == index.toFloat(),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.DarkGray,
                                indicatorColor = color
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    circleOffset.animateTo(
                                        targetValue = index.toFloat(),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    selectedColor.value = color
                                }
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(selectedColor.value),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Sample Content", color = Color.Black)
        }
    }
}


suspend fun animateSmallCircle(
    animatable: Animatable<Offset, AnimationVector2D>,
    mainCircleRadius: Float,
    smallCircleRadius: Float,
    index: Int
) {
    val angleOffset = (360f / 5) * index
    val animationSpec = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 4000
            Offset.Zero at 0 with LinearEasing
            Offset(
                x = (mainCircleRadius - smallCircleRadius) * kotlin.math.cos(
                    Math.toRadians(
                        angleOffset.toDouble()
                    )
                ).toFloat(),
                y = (mainCircleRadius - smallCircleRadius) * kotlin.math.sin(
                    Math.toRadians(
                        angleOffset.toDouble()
                    )
                ).toFloat()
            ) at 1000 with LinearEasing
            Offset.Zero at 2000 with LinearEasing
        },
        repeatMode = RepeatMode.Reverse
    )
    animatable.animateTo(
        targetValue = Offset.Zero,
        animationSpec = animationSpec
    )
}



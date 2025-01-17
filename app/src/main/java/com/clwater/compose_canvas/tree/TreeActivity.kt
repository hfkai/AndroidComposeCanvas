package com.clwater.compose_canvas.tree

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.clwater.compose_canvas.ui.theme.AndroidComposeCanvasTheme
import kotlinx.coroutines.delay
import java.util.ArrayDeque
import java.util.Queue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

//树的类型
// Tree Node Type
enum class TreeType {
    //树枝
    TREE,
    //花
    FLOWER,
    //水果
    FRUIT,
}

enum class Season {
    Spring,//春
    Summer,//夏
    Autumn,//秋
    Winter,//冬
}

// data Class TreeNode
data class TreeNode(
    var deep: Int = 0,
    var angle: Float = 0f,
    var type: TreeType = TreeType.TREE,
    var child: List<TreeNode> = listOf(),

    var length: Dp = 0.dp,

    // Increased in a loop rather than recursively
    var startOffset: Offset = Offset(0f, 0f)
)

data class LightNode(
    var offset: Offset = Offset(0f, 0f),
    var next: LightNode? = null
)


class TreeActivity : ComponentActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, TreeActivity::class.java))
        }

        // const Color 各种颜色值
        val cloudColor = Color(0xFFF5F5F5)
        val treeColor = Color(0xFF412e1f)
        val flowerColor = Color(0xFFFFFFFF)
        val flowerColorAutumn = Color(0xFF128604)
        val fruitColor = Color(0xFFe66e4a)
        val fruitColorEnd = Color(0x1AE66E4A)
        val seasonSpring = Color(0xFF7FDF69)
        val seasonSummer = Color(0xFFEE4F4F)
        val seasonAutumn = Color(0xFFE6A23C)
        val seasonWinter = Color(0xFFB8CAC6)

        val skyColorSpring = Color(0xFF69ADA3)
        val landColorSpring = Color(0xFF59C255)
        val rainColor = Color(0x99CCD5CC)
        val lightColor = Color(0xFFEED709)
        val lightSkyColor = Color(0xFF70CFC1)

        val skyColorSummer = Color(0xFF4D59AF)
        val landColorSummer = Color(0xFF1E1F44)
        val skyColorAutumn = Color(0xFFFAC164)
        val landColorAutumn = Color(0xFF612D1C)
        val skyColorWinter = Color(0xFF9dbeb7)
        val landColorWinter = Color(0xFFE7EEEC)
    }

    private lateinit var random: Random

    private var mBaseCircle = 0.dp //圆的大小
    private var mBaseCirclePx = 0f // 圆的px值
    private var flowerCount = 0 // 花的个数
    private var minLength: Float = 0.0f // 最小的长度


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidComposeCanvasTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TreeLayout()
                }
            }
        }
    }

    /**
     * Generate new Mei Tree
     * 生成一颗梅花树
     * @param seed 指定随机数的种子值，种子值如果确定的话，生成的随机序列是一样的
     * @return
     */
    private fun genNewTrees(seed: Int): TreeNode {
        random = Random(seed)
        val treeNode = TreeNode()
        treeNode.angle = 0f
        treeNode.deep = 0
        treeNode.type = TreeType.TREE
        treeNode.length = mBaseCircle / 4f
        //随机遍历[0,0),[0,1),[0,2),[0,3)
        for (i in 0 until random.nextInt(3) + 1) {
            //add TreeNode for child
            treeNode.child += genNewTree(1, treeNode.length)
        }
        return treeNode
    }

    /**
     * 递归一颗新的树
     * @param deep  深度
     * @param length
     * @return
     */
    // recursively new tree node
    private fun genNewTree(deep: Int, length: Dp): TreeNode {
        val treeNode = TreeNode()

        treeNode.deep = deep

        if (length < minLength.dp) {
            flowerCount++
            treeNode.type = if (flowerCount % 100 == 0) {
                TreeType.FRUIT
            } else {
                TreeType.FLOWER
            }
            return treeNode
        }

        treeNode.type = TreeType.TREE

        treeNode.length = length * (random.nextInt(2) / 10f + 0.6f)
        treeNode.angle =
            (if (random.nextFloat() > 0.5f) 1f else -1f) * (random.nextInt(20 + deep * 5) + 45)
        for (i in 0 until random.nextInt(3) + 1) {
            treeNode.child += genNewTree(deep + 1, treeNode.length)
        }

        return treeNode
    }

    /**
     * 绘制树整体布局，包括下面的按钮
     */
    @Composable
    fun TreeLayout() {
        with(LocalDensity.current) {
            //设置圆的大小
            mBaseCircle = resources.displayMetrics.widthPixels.toFloat().toDp() * 0.9f
            mBaseCirclePx = mBaseCircle.toPx()
        }

        //设置季节，如春夏秋冬
        var season by remember {
            mutableStateOf(Season.Winter)
        }
        //设置随机的种子数
        var seed by remember {
            mutableStateOf(-1)
        }

        random = Random(seed)
        minLength = mBaseCircle.value / 40f

        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                //使用Canvas绘制树
                //CompositionLocal 用来提供上下文数据，不扩大影响的范围，类似全局变量，
                //但全局变量影响面较大，该全局变量为LocalLayoutDirection
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    TreeCanvas(seed, season)
                }
            }

            //绘制按钮
            Column {

                Button(onClick = {
                    //改变seed的值，改变随机的种子数
                    seed = random.nextInt(1000)
                }) {
                    Text(
                        text = "Generate New Tree",
                    )
                }
                Column(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row {
                        Button(
                            onClick = {
                                season = Season.Spring
                            },
                            modifier = Modifier
                                .weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (season == Season.Spring) {
                                    seasonSpring
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Spring",
                            )
                        }

                        Button(
                            onClick = {
                                season = Season.Summer
                            },
                            modifier = Modifier
                                .weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (season == Season.Summer) {
                                    seasonSummer
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Summer",
                            )
                        }
                    }


                    Row {
                        Button(
                            onClick = {
                                season = Season.Autumn
                            },
                            modifier = Modifier
                                .weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (season == Season.Autumn) {
                                    seasonAutumn
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Autumn",
                            )
                        }

                        Button(
                            onClick = {
                                season = Season.Winter
                            },
                            modifier = Modifier
                                .weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (season == Season.Winter) {
                                    seasonWinter
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Winter",
                            )
                        }
                    }


                }

            }

        }

    }

    /**
     * 绘制树以及背景
     *
     * @param seed  星星或者雪的个数
     * @param season 季节
     */
    @Composable
    fun TreeCanvas(seed: Int, season: Season) {
        Box(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .clip(CircleShape)
                .background(
                    //设置背景颜色
                    when (season) {
                        Season.Spring -> skyColorSpring
                        Season.Summer -> skyColorSummer
                        Season.Autumn -> skyColorAutumn
                        Season.Winter -> skyColorWinter
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (season) {
                Season.Spring -> {
                    //春天，绘制雷电以及春雨
                    Light()
                    SpringRain()
                }

                Season.Autumn -> {
                    //绘制两朵云
                    Cloud_1()
                    Cloud_2()
                }

                Season.Summer -> {
                    //绘制星星以及流星
                    Starts(seed)
                    Meteor()
                }

                Season.Winter -> {
                    //绘制下雪和雪人
                    Snows(seed)
                    SnowMan()
                }

            }
            TreeLand(season)
            Tree(seed, season)
        }


    }

    /**
     * 画出跳动的雪人
     *
     */
    @Composable
    fun SnowMan() {
        val delayTime = 4000L
        val runTime = 4000L
        var showSnowMan by remember {
            mutableStateOf(false)
        }

        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offset by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = runTime.toInt(),
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "",
        )

        LaunchedEffect(Unit) {
            while (true) {
                delay(delayTime)
                showSnowMan = true
                delay(runTime)
                showSnowMan = false
            }
        }

        if (!showSnowMan) {
            return
        }

        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .rotate(90 * offset)
                .offset(x = mBaseCircle / 2f, y = mBaseCircle / 4f * 3 + mBaseCircle / 20f),


            )
        {
            drawCircle(
                color = Color.White,
                radius = mBaseCirclePx / 20f,
                center = Offset(
                    x = (0f + sin(Math.toRadians(90.0) * offset) * mBaseCirclePx / 2f).toFloat() + mBaseCirclePx / 4f,
                    y = (0f + -cos(Math.toRadians(90.0) * offset) * mBaseCirclePx / 4f).toFloat() + mBaseCirclePx / 4f,
                ),
            )
            drawCircle(
                color = Color.White,
                radius = mBaseCirclePx / 30f,
                center = Offset(
                    x = (0f + sin(Math.toRadians(90.0) * offset) * mBaseCirclePx / 2f).toFloat() + mBaseCirclePx / 4f,
                    y = (0f + -cos(Math.toRadians(90.0) * offset) * mBaseCirclePx / 4f).toFloat() + mBaseCirclePx / 4f - mBaseCirclePx / 15f,
                ),
            )
            // eyes
            drawCircle(
                color = Color.Black,
                radius = mBaseCirclePx / 30f / 5f,
                center = Offset(
                    x = (0f + sin(Math.toRadians(90.0) * offset) * mBaseCirclePx / 2f).toFloat() + mBaseCirclePx / 4f - mBaseCirclePx / 30f / 2f,
                    y = (0f + -cos(Math.toRadians(90.0) * offset) * mBaseCirclePx / 4f).toFloat() + mBaseCirclePx / 4f - mBaseCirclePx / 15f - mBaseCirclePx / 30f / 3f,
                ),
            )
            drawCircle(
                color = Color.Black,
                radius = mBaseCirclePx / 30f / 5f,
                center = Offset(
                    x = (0f + sin(Math.toRadians(90.0) * offset) * mBaseCirclePx / 2f).toFloat() + mBaseCirclePx / 4f + mBaseCirclePx / 30f / 2f,
                    y = (0f + -cos(Math.toRadians(90.0) * offset) * mBaseCirclePx / 4f).toFloat() + mBaseCirclePx / 4f - mBaseCirclePx / 15f - mBaseCirclePx / 30f / 3f,
                ),
            )
            //鼻子
            drawCircle(
                color = Color.Red,
                radius = mBaseCirclePx / 30f / 10f,
                center = Offset(
                    x = (0f + sin(Math.toRadians(90.0) * offset) * mBaseCirclePx / 2f).toFloat() + mBaseCirclePx / 4f,
                    y = (0f + -cos(Math.toRadians(90.0) * offset) * mBaseCirclePx / 4f).toFloat() + mBaseCirclePx / 4f - mBaseCirclePx / 15f - mBaseCirclePx / 30f / 6f,
                ),
            )
            //嘴巴
            drawArc(
                color = Color.Black,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(
                    x = (0f + sin(Math.toRadians(90.0) * offset) * mBaseCirclePx / 2f).toFloat() + mBaseCirclePx / 4f - mBaseCirclePx / 40f / 2f,
                    y = (0f + -cos(Math.toRadians(90.0) * offset) * mBaseCirclePx / 4f).toFloat() + mBaseCirclePx / 4f - mBaseCirclePx / 15f - mBaseCirclePx / 30f / 3f,
                ),
                size = Size(mBaseCirclePx / 40f, mBaseCirclePx / 40f),

                )

        }
    }

    @Composable
    fun Snows(seed: Int) {
        val maxSnow = 200
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offsetYList: MutableList<Float> = mutableListOf()
        val offsetList = mutableMapOf<Int, Offset>()
        random = Random(seed)
        for (i in 0..maxSnow) {
            offsetList[i] = Offset(
                -mBaseCirclePx / 2f + random.nextInt(mBaseCirclePx.toInt()),
                -mBaseCirclePx / 10f
            )
        }


        for (i in 0..maxSnow) {
            val offsetY: Float by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3011,
                        easing = LinearEasing,
                        delayMillis = (random.nextInt(3011 * 2) - 3011 / 2f).toInt()
                    ),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "",
            )
            offsetYList.add(offsetY)
        }

        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .offset(x = mBaseCircle / 2f, y = 0.dp),
        ) {
            for (i in 0..maxSnow) {

                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = Offset(
                        x = offsetList[i]!!.x,
                        y = offsetList[i]!!.y + mBaseCirclePx * offsetYList[i]
                    ),
                )
            }
        }

    }

    /**
     * 画出流星
     *
     */
    @Composable
    fun Meteor() {
        val delayTime = 3000L
        val runTime = 800L
        var showMeteor by remember {
            mutableStateOf(true)
        }
        var rotate by remember {
            mutableStateOf(0f)
        }
        var offsetY by remember {
            mutableStateOf(0f)
        }


        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offset by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = runTime.toInt(),
                    easing = LinearEasing,
                    delayMillis = delayTime.toInt()
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "",

            )

        LaunchedEffect(Unit) {
            while (true) {
                delay(delayTime)
                showMeteor = true
                rotate = -30 + Random(rotate.toInt()).nextInt(90).toFloat()
                offsetY =
                    -mBaseCirclePx / 3f + Random(offsetY.toInt()).nextInt(10) / 10f * mBaseCirclePx / 6f
                delay(runTime)
                showMeteor = false
            }
        }

        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .rotate(rotate)
                .offset(mBaseCircle / 2f, mBaseCircle / 2f)
                .graphicsLayer {
                    translationX = mBaseCircle.toPx() * offset
                    translationY = offsetY
                },

            ) {
            if (showMeteor) {
                var meterSize = 15f
                var meterOffset = -meterSize / 2

                for (i in 0..10) {
                    meterSize *= 0.8f
                    if (meterSize < 5f) {
                        meterSize = 5f
                    }
                    val path = Path()
                    path.moveTo(meterOffset + 0f, meterSize)
                    path.lineTo(meterOffset + meterSize, 0f)
                    path.lineTo(meterOffset + 0f, -meterSize)
                    path.lineTo(meterOffset + -meterSize, 0f)
                    path.lineTo(meterOffset + 0f, meterSize)
                    //画出流星
                    drawPath(path = path, color = Color.White)

                    meterOffset += if (i < 3) -meterSize else -meterSize * random.nextInt(3) + 1
                }
            }
        }
    }

    /**
     * 绘制星星
     *
     * @param seed
     */
    @Composable
    fun Starts(seed: Int) {
        val maxStart = 50
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val alphaList: MutableList<Float> = mutableListOf()
        val offsetList = mutableMapOf<Int, Offset>()
        random = Random(seed)
        for (i in 0..maxStart) {
            offsetList[i] = Offset(
                -mBaseCirclePx / 4f + random.nextInt(mBaseCirclePx.toInt()) / 4f * 3f,
                -mBaseCirclePx / 4f + random.nextInt(mBaseCirclePx.toInt()) / 4f * 3f,
            )
        }


        for (i in 0..maxStart) {
            val alpha: Float by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3011,
                        easing = LinearEasing,
                        delayMillis = if (random.nextInt(3011 * 2) - 3011 / 2f < 0) {
                            0
                        } else {
                            random.nextInt(3011 * 2) - 3011 / 2f
                        }.toInt(),
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "",
            )
            alphaList.add(alpha)
        }


        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .offset(x = mBaseCircle / 2f, y = mBaseCircle / 2f),
        ) {
            for (i in 0..maxStart) {

                val startLengthOut = mBaseCirclePx / 70f
                val startOffsetOut = startLengthOut / 3F

                val pathOut = Path()
                pathOut.moveTo(offsetList[i]!!.x + 0F, offsetList[i]!!.y + startLengthOut)
                pathOut.lineTo(
                    offsetList[i]!!.x + startOffsetOut,
                    offsetList[i]!!.y + startOffsetOut
                )
                pathOut.lineTo(offsetList[i]!!.x + startLengthOut, offsetList[i]!!.y + 0F)
                pathOut.lineTo(
                    offsetList[i]!!.x + startOffsetOut,
                    offsetList[i]!!.y + -startOffsetOut
                )
                pathOut.lineTo(offsetList[i]!!.x + 0F, offsetList[i]!!.y + -startLengthOut)
                pathOut.lineTo(
                    offsetList[i]!!.x + -startOffsetOut,
                    offsetList[i]!!.y + -startOffsetOut
                )
                pathOut.lineTo(offsetList[i]!!.x + -startLengthOut, offsetList[i]!!.y + 0F)
                pathOut.lineTo(
                    offsetList[i]!!.x + -startOffsetOut,
                    offsetList[i]!!.y + startOffsetOut
                )
                pathOut.lineTo(offsetList[i]!!.x + 0F, offsetList[i]!!.y + startLengthOut)

                //画出星星的轮廓
                drawPath(path = pathOut, color = Color.White, alpha = alphaList[i])


                val startLengthInner = startLengthOut * 0.3f
                val startOffsetInner = startLengthInner / 3F

                val pathInner = Path()
                pathInner.moveTo(offsetList[i]!!.x + 0F, offsetList[i]!!.y + startLengthInner)
                pathInner.lineTo(
                    offsetList[i]!!.x + startOffsetInner,
                    offsetList[i]!!.y + startOffsetInner
                )
                pathInner.lineTo(offsetList[i]!!.x + startLengthInner, offsetList[i]!!.y + 0F)
                pathInner.lineTo(
                    offsetList[i]!!.x + startOffsetInner,
                    offsetList[i]!!.y + -startOffsetInner
                )
                pathInner.lineTo(offsetList[i]!!.x + 0F, offsetList[i]!!.y + -startLengthInner)
                pathInner.lineTo(
                    offsetList[i]!!.x + -startOffsetInner,
                    offsetList[i]!!.y + -startOffsetInner
                )
                pathInner.lineTo(offsetList[i]!!.x + -startLengthInner, offsetList[i]!!.y + 0F)
                pathInner.lineTo(
                    offsetList[i]!!.x + -startOffsetInner,
                    offsetList[i]!!.y + startOffsetInner
                )
                pathInner.lineTo(offsetList[i]!!.x + 0F, offsetList[i]!!.y + startLengthInner)

                //画出星星中间的洞
                drawPath(path = pathInner, color = skyColorSummer, alpha = alphaList[i])


            }
        }
    }

    /**
     * 绘制雷电
     *
     */
    @Composable
    fun Light() {
        //显示闪电
        var showLight by remember {
            mutableStateOf(false)
        }
        //闪电路线
        var lights by remember {
            mutableStateOf(LightNode())
        }

        lights = generateLights(mBaseCirclePx)


        LaunchedEffect(Unit) {
            while (true) {
                delay(3000)
                showLight = true
                delay(300)
                showLight = false
            }
        }



        if (showLight) {
            Canvas(
                modifier = Modifier
                    .width(mBaseCircle)
                    .height(mBaseCircle)
                    //画出闪电的天空
                    .background(if (showLight) lightSkyColor else Color.Transparent)
                    .offset(mBaseCircle / 2f, mBaseCircle)

            ) {
                //画出闪电
                var currentLight = lights
                while (currentLight.next != null) {
                    drawLine(
                        color = lightColor,
                        start = currentLight.offset,
                        end = currentLight.next!!.offset,
                        strokeWidth = 8f,
                    )
                    currentLight = currentLight.next!!
                }
            }
        }

    }

    /**
     * 递归生成闪电
     * @param light
     * @return
     */
    private fun generateLights(light: LightNode): LightNode {
        if (light.next == null) {
            return light
        }
        val next = light.next!!

        //下一个节点和上一个节点的偏移量
        val distance = next.offset - light.offset
        if (distance.x * distance.x + distance.y * distance.y > 100) {
            //两个点的距离 > 10
            //生成新的闪电
            val newLight = LightNode()
            //距离减少一半
            newLight.offset = Offset(
                x = light.offset.x + distance.x / 2f,
                y = light.offset.y + distance.y / 2f,
            )
            //新的距离偏移量
            val newDistanceOffset = newLight.offset - light.offset
            //距离的一半
            val newDistance =
                sqrt((newDistanceOffset.x * newDistanceOffset.x + newDistanceOffset.y * newDistanceOffset.y).toDouble()) / 2f
            //随机选择一个角度，并将新节点沿着该角度偏移，偏移距离为原始距离的一半。
            newLight.offset = Offset(
                x = (newLight.offset.x + newDistance * sin(
                    Math.toRadians(
                        random.nextInt(360).toDouble()
                    )
                )).toFloat(),
                y = (newLight.offset.y + newDistance * cos(
                    Math.toRadians(
                        random.nextInt(360).toDouble()
                    )
                )).toFloat()
            )
            newLight.next = next
            light.next = newLight
            return generateLights(light)
        } else {
            //否则继续生成下一个节点
            light.next = generateLights(next)
            return light
        }
    }

    /**
     * 生成闪电路线
     *
     * @param height
     * @return
     */
    private fun generateLights(height: Float): LightNode {

        var lights = LightNode()
        lights.offset = Offset(0f, -height)

        val next = LightNode()
        next.offset = Offset(0f, 0f)
        lights.next = next

        lights = generateLights(lights)

        return lights
    }

    @Composable
    private fun SpringRain() {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offset by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4000,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "",
        )
        val maxRains = 100
        val rainOffset = mutableMapOf<Int, Offset>()

        for (index in 0 until maxRains) {
            //生成雨点
            rainOffset[index] = Offset(
                x = -2f * mBaseCircle.value + 4f * random.nextInt(mBaseCircle.value.toInt()),
                y = -1f * mBaseCircle.value + 2f * random.nextInt(mBaseCircle.value.toInt())
            )
        }

        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .offset(mBaseCircle / 2f, mBaseCircle / 2f)
                .rotate(10f)
                .graphicsLayer {
                },

            ) {
            for (i in -2..2) {
                for (j in 0 until maxRains) {
                    drawRoundRect(
                        color = rainColor,
                        size = Size(mBaseCirclePx / 400f, mBaseCirclePx / 20f),
                        cornerRadius = CornerRadius(size.minDimension / 2f),
                        topLeft = Offset(
                            x = rainOffset[j]!!.x,
                            //y的偏移量，offset是一个从-1匀速变化到1的值
                            y = mBaseCircle.value * offset + i * mBaseCircle.value + rainOffset[j]!!.y
                        ),
                    )
                }
            }
        }
    }

    /**
     * 绘制树木
     *
     * @param seed
     * @param season
     */
    @Composable
    fun Tree(seed: Int, season: Season) {

        val infiniteTransition = rememberInfiniteTransition(label = "")

        val offsetPosition: Float by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1001,
                    easing = easeOutBounce,
                    delayMillis = 4000,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "",
        )



        val tree = genNewTrees(seed)
        val baseTreeLength = mBaseCircle / 4f
        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .offset(mBaseCircle / 2f, mBaseCircle),

            ) {

            drawLine(
                color = treeColor,
                start = Offset(x = 0f, y = -mBaseCirclePx / 20f),
                end = Offset(0f, -baseTreeLength.toPx() - mBaseCirclePx / 20f),
                strokeWidth = 10f,
            )
            val treeQueue: Queue<TreeNode> = ArrayDeque()
            val flowerQueue: Queue<TreeNode> = ArrayDeque()
            val fruitQueue: Queue<TreeNode> = ArrayDeque()
            var downTreeNode: TreeNode? = null


            for (treeNode in tree.child) {
                treeNode.startOffset = Offset(0f, -baseTreeLength.toPx() - mBaseCirclePx / 20f)
                treeQueue.offer(treeNode)
            }

            // Increased in a loop rather than recursively
            while (treeQueue.isNotEmpty()) {
                val treeNode = treeQueue.poll() ?: break
                val angle = treeNode.angle
                val deep = treeNode.deep
                val type = treeNode.type
                val length = treeNode.length

                if (type == TreeType.TREE) {
                    var treeWidth = 15f
                    for (i in 0..deep) {
                        treeWidth *= 0.8f
                    }

                    // calculate the position for child node
                    val startOffset = treeNode.startOffset
                    val currentEnd = Offset(
                        x = startOffset.x + length.toPx() * sin(Math.toRadians(angle.toDouble()))
                            .toFloat(),
                        y = startOffset.y - length.toPx() * cos(Math.toRadians(angle.toDouble()))
                            .toFloat(),
                    )

                    drawLine(
                        color = treeColor,
                        start = startOffset,
                        end = currentEnd,
                        strokeWidth = treeWidth,
                    )
                    treeNode.child.forEach {
                        it.startOffset = currentEnd
                        treeQueue.offer(it)
                    }
                }

                // offer the flower/fruit child to queue
                if (type == TreeType.FLOWER) {
                        if (downTreeNode == null) {
                            downTreeNode = treeNode
                        }
                    flowerQueue.offer(treeNode)
                } else if (type == TreeType.FRUIT && season != Season.Autumn) {
                    fruitQueue.offer(treeNode)
                }
            }

            // draw flowers
            //花谢花落
            if (season == Season.Summer) {
                return@Canvas
            }
            while (flowerQueue.isNotEmpty()) {
                val treeNode = flowerQueue.poll() ?: break
                if (season != Season.Autumn) {
                    drawCircle(
                        color = flowerColor,
                        radius = 10f,
                        center = treeNode.startOffset,
                    )
                } else {
                    drawCircle(
                        color = flowerColorAutumn,
                        radius = 6f,
                        center = treeNode.startOffset,
                    )
                }

            }

            while (fruitQueue.isNotEmpty()) {
                val treeNode = fruitQueue.poll() ?: break
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to fruitColor,
                        0.5f to fruitColor,
                        1f to fruitColorEnd,
                        center = treeNode.startOffset,
                        radius = 20f

                    ),
                    center = treeNode.startOffset,
                    radius = 20f
                )

            }
            if (season == Season.Autumn){
                downTreeNode?.let {
                    drawCircle(
                        color = flowerColorAutumn,
                        radius = 6f,
                        center = Offset(x = it.startOffset.x,
                            y = it.startOffset.y + mBaseCirclePx / 3f * offsetPosition),
                    )
                }
            }

        }

    }

    /**
     * 画出第一朵云
     *
     */
    @Composable
    fun Cloud_1() {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offset by infiniteTransition.animateFloat(
            initialValue = -0.7f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4003,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "",
        )

        Log.e("Cloud_1", "Cloud_1: $offset")
        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .offset(
                    x = mBaseCircle / 2f * offset,
                    y = -mBaseCircle / 6f,
                )
                .alpha(0.8f),
        )
        {
            //画出一个圆角矩形 + 2个半圆
            drawRoundRect(
                color = cloudColor,
                size = Size(width = size.width / 7f * 4f, height = size.height / 4f),
                cornerRadius = CornerRadius(size.minDimension / 2f),
                topLeft = Offset(x = center.x - size.width / 4f, y = center.y),
            )
            drawCircle(
                color = cloudColor,
                radius = size.minDimension / 10f,
                center = Offset(x = center.x - size.width / 20f, y = center.y + size.height / 40f),
            )
            drawCircle(
                color = cloudColor,
                radius = size.minDimension / 8f,
                center = Offset(x = center.x + size.width / 10f, y = center.y + size.height / 40f),
            )
        }
    }

    @Composable
    fun Cloud_2() {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offset by infiniteTransition.animateFloat(
            initialValue = -0.8f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 5007,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "",
        )


        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                .offset(
                    x = mBaseCircle / 2f * offset,
                    y = -mBaseCircle / 3f,
                )
                .alpha(0.8f),
        )
        {
            drawRoundRect(
                color = cloudColor,
                size = Size(width = size.width / 7f * 6f, height = size.height / 4f * 1.25f),
                cornerRadius = CornerRadius(size.minDimension / 2f),
                topLeft = Offset(x = center.x - size.width / 2f, y = center.y),
            )
            drawCircle(
                color = cloudColor,
                radius = size.minDimension / 6f,
                center = Offset(x = center.x - size.width / 5f, y = center.y + size.height / 40f),
            )
            drawCircle(
                color = cloudColor,
                radius = size.minDimension / 5f,
                center = Offset(x = center.x + size.width / 10f, y = center.y + size.height / 40f),
            )
        }
    }


    /**
     * 绘制土壤,是一个半圆，并且向下偏移 3/4
     * @param season
     */
    @Composable
    fun TreeLand(season: Season) {
        Canvas(
            modifier = Modifier
                .width(mBaseCircle)
                .height(mBaseCircle)
                //往下偏移3/4
                .offset(y = mBaseCircle / 4f * 3),
        ) {
            //绘制一个半圆
            drawCircle(
                color = when (season) {
                    Season.Spring -> landColorSpring
                    Season.Summer -> landColorSummer
                    Season.Autumn -> landColorAutumn
                    Season.Winter -> landColorWinter
                },
                radius = size.minDimension / 2f
            )
        }
    }

    private val easeOutBounce = Easing { fraction ->
        val n1 = 7.5625f
        val d1 = 2.75f
        var newFraction = fraction

        return@Easing if (newFraction < 1f / d1) {
            n1 * newFraction * newFraction
        } else if (newFraction < 2f / d1) {
            newFraction -= 1.5f / d1
            n1 * newFraction * newFraction + 0.75f
        } else if (newFraction < 2.5f / d1) {
            newFraction -= 2.25f / d1
            n1 * newFraction * newFraction + 0.9375f
        } else {
            newFraction -= 2.625f / d1
            n1 * newFraction * newFraction + 0.984375f
        }
    }
}
package com.gongde.app.ui

import kotlin.random.Random

private val FUN_QUOTES = listOf(
    "今天也是努力摸鱼的一天",
    "生活不易，多敲键盘多攒功德",
    "烦恼敲走，快乐留下",
    "焦虑是暂时的，摸鱼是永恒的",
    "敲一敲，世界都温柔了",
    "压力山大不如一键清零",
    "键盘一响，黄金万两",
    "累了就歇，歇完继续敲",
    "万物皆可敲，烦恼皆可消",
    "今天的功德，明天的好运",
    "解压不求人，一键功德深",
    "每一下敲击，都是对自己的温柔",
    "放下手机（等等这个就是手机）",
    "坏情绪退退退，好心情来来来",
    "敲出一片天，攒出一身轻",
)

fun getRandomFunQuote(): String = FUN_QUOTES[Random.nextInt(FUN_QUOTES.size)]

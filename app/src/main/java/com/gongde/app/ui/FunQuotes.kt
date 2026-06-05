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
    "不开心就敲，开心也敲",
    "功德无边，键盘无限",
    "今日份快乐已到账",
    "敲键盘治百病",
    "没有什么是一下键盘解决不了的",
    "如果有，就再敲一下",
    "摸鱼也是一种修行",
    "打工人的解压神器",
    "敲完这下就去干活（大概）",
    "功德+1，烦恼-1",
    "键盘侠的正确打开方式",
    "人生苦短，及时行乐（敲键盘）",
    "今天的我，功德无量",
    "一键解压，烦恼归零",
    "敲出好心情，攒出好运气",
)

private val queue = mutableListOf<String>()

fun getRandomFunQuote(): String {
    if (queue.isEmpty()) {
        queue.addAll(FUN_QUOTES.shuffled(Random(System.nanoTime())))
    }
    return queue.removeAt(0)
}

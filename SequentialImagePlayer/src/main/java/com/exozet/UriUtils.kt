package com.exozet

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File


fun parseAssetFile(file: String): Uri = Uri.parse("file:///android_asset/$file")

fun parseInternalStorageFile(context: Context, file: String): Uri = Uri.parse("${context.filesDir.absolutePath}/$file")

fun parseExternalStorageFile(file: String): Uri = Uri.parse("${Environment.getExternalStorageDirectory()}/$file")

fun parseFile(file: String): Uri = Uri.fromFile(File(file))
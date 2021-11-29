package dev.hichamboushaba.suspendactivityresult

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.annotation.RequiresApi

/**
 * see [RequestPermission]
 */
suspend fun ActivityResultManager.requestPermission(permission: String): Boolean {
    return requestResult(ActivityResultContracts.RequestPermission(), permission) ?: false
}

/**
 * see [RequestMultiplePermissions]
 */
suspend fun ActivityResultManager.requestPermissions(vararg permission: String): Map<String, Boolean>? {
    return requestResult(ActivityResultContracts.RequestMultiplePermissions(), permission)
}

/**
 * see [TakePicturePreview]
 */
suspend fun ActivityResultManager.takePicturePreview(): Bitmap? {
    return requestResult(ActivityResultContracts.TakePicturePreview(), null)
}

/**
 * see [TakePicture]
 */
suspend fun ActivityResultManager.takePicture(destination: Uri): Boolean {
    return requestResult(ActivityResultContracts.TakePicture(), destination) ?: false
}

/**
 * see [TakeVideo]
 */
suspend fun ActivityResultManager.takeVideo(destination: Uri): Bitmap? {
    return requestResult(ActivityResultContracts.TakeVideo(), destination)
}

/**
 * see [PickContact]
 */
suspend fun ActivityResultManager.pickContact(): Uri? {
    return requestResult(ActivityResultContracts.PickContact(), null)
}

/**
 * see [GetContent]
 */
suspend fun ActivityResultManager.getContent(mimeType: String): Uri? {
    return requestResult(ActivityResultContracts.GetContent(), mimeType)
}

/**
 * see [GetMultipleContents]
 */
@RequiresApi(18)
suspend fun ActivityResultManager.getMultipleContents(mimeType: String): List<Uri> {
    return requestResult(ActivityResultContracts.GetMultipleContents(), mimeType) ?: emptyList()
}

/**
 * see [OpenDocument]
 */
@RequiresApi(19)
suspend fun ActivityResultManager.openDocument(mimeTypes: Array<String>): Uri? {
    return requestResult(ActivityResultContracts.OpenDocument(), mimeTypes)
}

/**
 * see [OpenMultipleDocuments]
 */
@RequiresApi(19)
suspend fun ActivityResultManager.openMultipleDocuments(mimeTypes: Array<String>): List<Uri> {
    return requestResult(ActivityResultContracts.OpenMultipleDocuments(), mimeTypes) ?: emptyList()
}

@RequiresApi(21)
suspend fun ActivityResultManager.openDocumentTree(startingLocation: Uri? = null): Uri? {
    return requestResult(ActivityResultContracts.OpenDocumentTree(), startingLocation)
}

/**
 * see [CreateDocument]
 */
@RequiresApi(19)
suspend fun ActivityResultManager.createDocument(fileName: String): Uri? {
    return requestResult(ActivityResultContracts.CreateDocument(), fileName)
}
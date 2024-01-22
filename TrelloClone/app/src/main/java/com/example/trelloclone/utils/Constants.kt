package com.example.trelloclone.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val USERS_COLLECTION_NAME = "users"
    const val BOARDS_COLLECTION_NAME = "boards"

    const val MOBILE_NUMBER_RO_PREFIX = "+40"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobileNumber"
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentID"
    const val TASK_LIST: String = "taskList"
    const val ID: String = "id"
    const val EMAIL: String = "email"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    const val BOARD_DETAIL = "board_detail"
    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val BOARD_MEMBERS_LIST: String = "board_members_list"

    const val SELECT: String = "select"
    const val UNSELECT: String = "unselect"

    const val TRELOL_PREFERENCES: String = "Trelol_Preferences"
    const val FCM_TOKEN_UPDATED: String = "fckTokenUpdated"
    const val FCM_TOKEN: String = "fcmToken"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAmu3Hhsk:APA91bEDJ5ZxQnruQaG4x2-ZUPBepRYEQen_MNxllFI_SpS4PWKTJWxvdr0Xq3NFU0D1KIlrlL6K5CneVQgx0x6PsR9qzm0yq2nDpMxyRQveWzgpsy1McQgZVZBirr7oRMskA14WH48N"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(uri?.let {
            activity.contentResolver.getType(it)
        })
    }
}
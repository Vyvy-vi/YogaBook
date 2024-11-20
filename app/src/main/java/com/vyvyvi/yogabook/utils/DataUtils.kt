package com.vyvyvi.yogabook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toBitmap
import com.vyvyvi.yogabook.R
import com.vyvyvi.yogabook.data.AppDao
import com.vyvyvi.yogabook.data.AppDatabase
import com.vyvyvi.yogabook.data.Pose
import com.vyvyvi.yogabook.data.Routine
import com.vyvyvi.yogabook.data.Streak
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

object DataUtils {
    fun bitmapToByteArray(bitemp: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitemp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun seedPoseData(context: Context, db: AppDao, username: String, pose: Pose) {
        CoroutineScope(Dispatchers.IO).launch {
            val resourceId = context.resources.getIdentifier(
                pose.imageFilename.substringBeforeLast('.'),
                "drawable",
                context.packageName
            )
            context.getDrawable(resourceId)?.let {
                InternalStorageHelper.saveImageToInternalStorage(
                    context,
                    pose.imageFilename,
                    it.toBitmap()
                )
            }
            val pose_id = db.insertPose(pose)
            db.insertRoutine(Routine(pose_id=pose_id, username = username, duration = 30))
        }
    }

    fun seedUserStreak(context: Context, username: String) {
        val db = AppDatabase.getDatabase(context).appDao()
        CoroutineScope(Dispatchers.IO).launch {
            db.insertUserStreak(Streak(username))
        }
    }

    fun seedPoseData(context: Context, username: String) {
        val db = AppDatabase.getDatabase(context).appDao()

        val poses = arrayOf(
            Pose(
                name = "Mountain Pose",
                hindiName = "Tadasana",
                description = context.getString(R.string.mountain_description),
                imageFilename = "im_mountain.png"
            ),
            Pose(
                name = "Twisted Mountain Pose",
                hindiName = "Parivritta Tadasana",
                description = context.getString(R.string.twisted_mountain_description),
                imageFilename = "im_twisted_mountain.png"
            ),
            Pose(
                name = "Triangle Pose",
                hindiName = "Trikonasana",
                description = context.getString(R.string.triangle_description),
                imageFilename = "im_triangle.png"
            ),
            Pose(
                name = "Thunderbolt Pose",
                hindiName = "Vajrasana",
                description = context.getString(R.string.thunderbolt_description),
                imageFilename = "im_thunderbolt.png"
            ),
            Pose(
                name = "Twisted Thunderbolt Pose",
                hindiName = "Parivritta Vajrasana",
                description = context.getString(R.string.twisted_thunderbolt_description),
                imageFilename = "im_twisted_thunderbolt.png"
            ),
            Pose(
                name = "Half Pigeon Pose",
                hindiName = "Ardha Kapotasana",
                description = context.getString(R.string.half_pigeon_description),
                imageFilename = "im_half_pigeon.png"
            ),
            Pose(
                name = "Seated Forward Bend",
                hindiName = "Paschimottanasana",
                description = context.getString(R.string.forward_bend_description),
                imageFilename = "im_forward_bend.png"
            ),
            Pose(
                name = "Child's Pose",
                hindiName = "Balasana",
                description = context.getString(R.string.child_description),
                imageFilename = "im_child.png"
            ),
            Pose(
                name = "Rabbit Pose",
                hindiName = "Shashankasana",
                description = context.getString(R.string.rabbit_description),
                imageFilename = "im_rabbit.png"
            ),
            Pose(
                name = "Camel Pose",
                hindiName = "Ushtrasana",
                description = context.getString(R.string.camel_description),
                imageFilename = "im_camel.png"
            ),
            Pose(
                name = "Bow Pose",
                hindiName = "Dhanurasana",
                description = context.getString(R.string.bow_description),
                imageFilename = "im_bow.png"
            ),
            Pose(
                name = "Cobra Pose",
                hindiName = "Bhujangasana",
                description = context.getString(R.string.cobra_description),
                imageFilename = "im_cobra.png"
            ),
            Pose(
                name = "Wind Removing Pose",
                hindiName = "Pavanamuktasana",
                description = context.getString(R.string.wind_removing_description),
                imageFilename = "im_wind_removing.png"
            ),
            Pose(
                name = "Boat Pose",
                hindiName = "Navasana",
                description = context.getString(R.string.boat_description),
                imageFilename = "im_boat.png"
            ),
            Pose(
                name = "Supine Spinal Twist",
                hindiName = "Supta Matsyendrasana",
                description = context.getString(R.string.spinal_twist_description),
                imageFilename = "im_spinal_twist.png"
            ),
        )
        poses.forEach {
            seedPoseData(context, db, username, it)
        }
    }
}
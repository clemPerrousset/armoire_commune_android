package fr.larmoirecommune.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.repository.ObjectRepository
import fr.larmoirecommune.app.network.ApiClient
import java.util.Calendar

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = ObjectRepository()

    override suspend fun doWork(): Result {
        // Only run on Monday
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            return Result.success()
        }

        // Must be logged in
        if (ApiClient.token == null) {
            return Result.success()
        }

        try {
            val reservations = repository.getMyReservations()
            val activeReservations = reservations.filter { it.status == "active" || it.status == "en_cours" }

            if (activeReservations.isNotEmpty()) {
                showNotification()
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun showNotification() {
        val channelId = "reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rappels"
            val descriptionText = "Rappels pour vos réservations"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("L'Armoire Commune")
            .setContentText("N'oubliez pas ! Vous devez rendre vos objets demain.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }
}

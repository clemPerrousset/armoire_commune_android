package fr.larmoirecommune.app.utils

import android.content.Context
import android.content.SharedPreferences

class ProfileManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    var userName: String?
        get() = prefs.getString("user_name", null)
        set(value) = prefs.edit().putString("user_name", value).apply()

    var avatarUri: String?
        get() = prefs.getString("avatar_uri", null)
        set(value) = prefs.edit().putString("avatar_uri", value).apply()
}

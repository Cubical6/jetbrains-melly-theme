package com.markskelton.notification

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import org.intellij.lang.annotations.Language

const val PLUGIN_ID = "com.markskelton.one-dark-theme"

@Language("HTML")
val UPDATE_MESSAGE: String = """
      What's New?<br>
      <ul>
          <li>60+ Windows Terminal color schemes now available!</li>
          <li>Improved performance and accessibility.</li>
      </ul>
      <br>Please see the <a href='https://github.com/one-dark/jetbrains-one-dark-theme/blob/master/CHANGELOG.md'>Changelog</a> for more details.
      <br>
      Thank you for choosing our Windows Terminal Theme Plugin!<br>
""".trimIndent()

object Notifications {

  private val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("One Dark Theme")

  fun displayUpdateNotification(versionNumber: String) {
    val pluginName =
      getPlugin(PluginId.getId(PLUGIN_ID))?.name ?: "Windows Terminal Theme"

    notificationGroup.createNotification(
      UPDATE_MESSAGE,
      NotificationType.INFORMATION
    )
      .setTitle("$pluginName updated to v$versionNumber")
      .setListener(NotificationListener.UrlOpeningListener(false))
      .notify(null)
  }
}

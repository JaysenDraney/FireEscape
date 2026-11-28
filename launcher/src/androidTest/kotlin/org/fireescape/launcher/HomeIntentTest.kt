package org.fireescape.launcher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * M0.3 L4: the launcher can be selected as HOME, and pressing HOME brings it up and
 * returns to it. Runs on the TV emulator; CI wiring arrives with M0.4.
 *
 * On the Google TV image the system launcher keeps HOME while its process is alive,
 * whatever default is selected, so the test disables it, stops it right before each
 * HOME press, and restores it afterwards. Package visibility hides the system launcher
 * from the app context, so the current home is discovered through the shell, which
 * sees all packages.
 */
@RunWith(AndroidJUnit4::class)
class HomeIntentTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val target = instrumentation.targetContext

    @Test
    fun pressHomeBringsUpTheLauncherAndReturnsToIt() {
        val launcher = ComponentName(target, MainActivity::class.java)
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)

        // Selectable as HOME: the system lists the launcher among the home candidates.
        val candidates = target.packageManager
            .queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        assertTrue(
            "launcher is not a HOME candidate",
            candidates.any {
                ComponentName(it.activityInfo.packageName, it.activityInfo.name) == launcher
            },
        )

        // resolve-activity --brief prints a priority line, then the component.
        val previousHome = shell(
            "cmd package resolve-activity --brief " +
                "-a android.intent.action.MAIN -c android.intent.category.HOME",
        ).lines().map { it.trim() }.lastOrNull { it.isNotEmpty() }
            ?.let { ComponentName.unflattenFromString(it) }
        val systemHomePackage = previousHome?.packageName?.takeIf { it != target.packageName }
        try {
            setHomeActivity(launcher)
            systemHomePackage?.let { pkg ->
                val out = shell("pm disable-user --user 0 $pkg")
                assertTrue("disabling $pkg failed: $out", out.contains("new state"))
            }
            assertTrue("HOME did not bring up the launcher", pressHomeAndWait(systemHomePackage))

            // Back on the home screen never leaves the launcher.
            shell("input keyevent ${KeyEvent.KEYCODE_BACK}")
            Thread.sleep(1_000)
            assertTrue("Back left the launcher", launcherResumed())

            // Leave home for Settings, then return with HOME.
            target.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            assertTrue("Settings did not come up", waitUntil { !launcherResumed() })
            assertTrue("HOME did not return to the launcher", pressHomeAndWait(systemHomePackage))
        } finally {
            // Best effort: on a fresh image the previous home may not qualify for the
            // HOME role, and restoring it must not mask the test result.
            previousHome?.let {
                shell("cmd package set-home-activity ${it.flattenToShortString()}")
            }
            systemHomePackage?.let { shell("pm enable $it") }
        }
    }

    private fun setHomeActivity(component: ComponentName) {
        // On a fresh device the role service may not have picked up the just-installed
        // package yet, so qualification can fail transiently. Retry before giving up.
        var out = ""
        repeat(5) { attempt ->
            out = shell("cmd package set-home-activity ${component.flattenToShortString()}")
            if (!out.contains("Error", ignoreCase = true) && !out.contains("Exception")) return
            if (attempt < 4) Thread.sleep(2_000)
        }
        assertTrue("set-home-activity failed after retries: $out", false)
    }

    private fun pressHomeAndWait(stoppedPackage: String?): Boolean {
        repeat(3) {
            // The system launcher restarts quickly and intercepts HOME while it is
            // alive, even disabled, so stop it right before each press.
            // executeShellCommand does not run a shell: one command per call.
            stoppedPackage?.let { shell("am force-stop $it") }
            shell("input keyevent ${KeyEvent.KEYCODE_HOME}")
            if (waitUntil(5_000) { launcherResumed() }) return true
        }
        return false
    }

    private fun shell(command: String): String {
        val pfd = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.readBytes().decodeToString() }
    }

    private fun launcherResumed(): Boolean {
        var resumed = false
        // The lifecycle monitor may only be queried on the main thread.
        instrumentation.runOnMainSync {
            resumed = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED).any { it is MainActivity }
        }
        return resumed
    }

    private fun waitUntil(timeoutMs: Long = 15_000, condition: () -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition()) return true
            Thread.sleep(200)
        }
        return condition()
    }
}

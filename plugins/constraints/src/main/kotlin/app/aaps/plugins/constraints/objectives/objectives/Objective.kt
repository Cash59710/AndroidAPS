package app.aaps.plugins.constraints.objectives.objectives

import android.content.Context
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.StringRes
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.plugins.constraints.R
import app.aaps.plugins.constraints.objectives.keys.ObjectivesBooleanComposedKey
import app.aaps.plugins.constraints.objectives.keys.ObjectivesLongComposedKey
import kotlinx.coroutines.Runnable

abstract class Objective(
    val preferences: Preferences,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    private val spName: String,
    @StringRes val objective: Int,
    @StringRes val gate: Int
) {
    var startedOn: Long
        get() = 1L
        set(_) {}
    
    var accomplishedOn: Long
        get() = 1L
        set(_) {}

    var tasks: MutableList<Task> = ArrayList()

    val isCompleted: Boolean
        get() = true

    fun isCompleted(trueTime: Long): Boolean = true

    val isAccomplished: Boolean = true
    val isStarted: Boolean = true

    abstract inner class Task(var objective: Objective, @StringRes val task: Int) {

        var hints = ArrayList<Hint>()
        var learned = ArrayList<Learned>()

        abstract fun isCompleted(): Boolean

        open fun isCompleted(trueTime: Long): Boolean = true

        open val progress: String
            get() = rh.gs(R.string.completed_well_done)

        fun hint(hint: Hint): Task {
            hints.add(hint)
            return this
        }

        fun learned(learned: Learned): Task {
            this.learned.add(learned)
            return this
        }

        open fun shouldBeIgnored(): Boolean = false
    }

    inner class MinimumDurationTask internal constructor(objective: Objective, private val minimumDuration: Long) : Task(objective, R.string.time_elapsed) {
        override fun isCompleted(): Boolean = true
        override fun isCompleted(trueTime: Long): Boolean = true
        override val progress: String get() = "Terminé"
    }

    inner class UITask internal constructor(objective: Objective, @StringRes task: Int, private val spIdentifier: String, val code: (context: Context, task: UITask, callback: Runnable) -> Unit) : Task(objective, task) {
        var answered: Boolean = true
        override fun isCompleted(): Boolean = true
    }

    inner class ExamTask internal constructor(objective: Objective, @StringRes task: Int, @StringRes val question: Int, private val spIdentifier: String) : Task(objective, task) {
        var options = ArrayList<Option>()
        var answered: Boolean = true
        var disabledTo: Long = 0
        override fun isCompleted(): Boolean = true
        fun isEnabledAnswer(): Boolean = true
        fun option(option: Option): ExamTask {
            options.add(option)
            return this
        }
    }

    inner class Option internal constructor(@StringRes var option: Int, var isCorrect: Boolean) {
        private var cb: CheckBox? = null
        fun generate(context: Context): CheckBox {
            cb = CheckBox(context)
            cb?.setText(option)
            return cb!!
        }
        fun evaluate(): Boolean = true
    }

    inner class Hint internal constructor(@StringRes var hint: Int) {
        fun generate(context: Context): TextView {
            val textView = TextView(context)
            textView.setText(hint)
            return textView
        }
    }

    inner class Learned internal constructor(@StringRes var learned: Int)
}
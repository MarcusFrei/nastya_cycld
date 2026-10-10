package com.cycleapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cycleapp.data.CycleForecast
import com.cycleapp.data.NotificationSettings
import com.cycleapp.util.CycleCalculator
import java.time.LocalDate
import java.time.ZoneId

/**
 * Планирует напоминания через AlarmManager на основе прогноза цикла и настроек уведомлений.
 * Используются неточные будильники (setAndAllowWhileIdle) — без необходимости в специальном
 * разрешении SCHEDULE_EXACT_ALARM, отклонение в пределах нескольких минут не критично для
 * напоминаний такого рода.
 */
object NotificationScheduler {

    private const val REQ_WEEK_BEFORE_PERIOD = 5001
    private const val REQ_DAY_BEFORE_PERIOD = 5002
    private const val REQ_WEEK_BEFORE_OVULATION = 5003
    private const val REQ_PERIOD_DAY_BASE = 5100 // + смещение по дню менструации (0..30)

    fun scheduleAll(
        context: Context,
        forecast: CycleForecast?,
        settings: NotificationSettings,
        periodLength: Int
    ) {
        cancelAll(context, periodLength)
        NotificationReceiver.ensureChannel(context)

        if (forecast == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextPeriod = CycleCalculator.parse(forecast.predictedNextPeriodDate)
        val hour = settings.reminderHour.coerceIn(0, 23)

        if (settings.weekBeforePeriod) {
            scheduleAt(
                context, alarmManager, REQ_WEEK_BEFORE_PERIOD,
                nextPeriod.minusDays(7), hour,
                "Через неделю ожидаются месячные",
                "По вашему циклу через 7 дней должны начаться месячные. Хорошее время спланировать дела заранее."
            )
        }
        if (settings.dayBeforePeriod) {
            scheduleAt(
                context, alarmManager, REQ_DAY_BEFORE_PERIOD,
                nextPeriod.minusDays(1), hour,
                "Завтра ожидаются месячные",
                "По вашему циклу месячные должны начаться завтра."
            )
        }
        if (settings.weekBeforeOvulation) {
            val ovulation = CycleCalculator.parse(forecast.predictedOvulationDate)
            scheduleAt(
                context, alarmManager, REQ_WEEK_BEFORE_OVULATION,
                ovulation.minusDays(7), hour,
                "Через неделю ожидается овуляция",
                "По вашему циклу овуляция ожидается примерно через 7 дней."
            )
        }
        if (settings.everyPeriodDay) {
            for (i in 0 until periodLength) {
                scheduleAt(
                    context, alarmManager, REQ_PERIOD_DAY_BASE + i,
                    nextPeriod.plusDays(i.toLong()), hour,
                    "День менструации",
                    "День ${i + 1} из $periodLength. Не забудьте отметить самочувствие в приложении."
                )
            }
        }
    }

    fun cancelAll(context: Context, periodLength: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelOne(context, alarmManager, REQ_WEEK_BEFORE_PERIOD)
        cancelOne(context, alarmManager, REQ_DAY_BEFORE_PERIOD)
        cancelOne(context, alarmManager, REQ_WEEK_BEFORE_OVULATION)
        for (i in 0 until maxOf(periodLength, 14)) {
            cancelOne(context, alarmManager, REQ_PERIOD_DAY_BASE + i)
        }
    }

    private fun scheduleAt(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        date: LocalDate,
        hour: Int,
        title: String,
        message: String
    ) {
        val triggerTime = date.atTime(hour, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerTime <= System.currentTimeMillis()) return // не планируем в прошлое

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_NOTIF_ID, requestCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (_: SecurityException) {
            // На некоторых устройствах может потребоваться разрешение — тихо игнорируем
        }
    }

    private fun cancelOne(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

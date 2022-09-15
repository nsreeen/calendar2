package com.example.services

import com.example.Modules.getTimeWithDayOffset
import com.example.models.CalendarResponse
import com.example.models.CreateCalendarRequest
import com.example.database.Database


class CalendarService {
    suspend fun getCalendar(calToken: String): CalendarResponse {
        println("hello")
        val calendarRow = Database().getCalendar(calToken)
        println(calendarRow)
        val events = EventsService().getAllEvents(calToken, calendarRow!!.rcToken)
        return CalendarResponse(
            calToken=calendarRow!!.token,
            name=calendarRow!!.name,
            onlineTime=calendarRow!!.online,
            offlineTime=calendarRow!!.offline,
            events=events,
        )
    }
    suspend fun createCalendar(createCalendarRequest: CreateCalendarRequest): CalendarResponse {
        val token = java.util.UUID.randomUUID().toString()
        val calendarRow = Database().createCalendar(token, createCalendarRequest.name, createCalendarRequest.online, createCalendarRequest.offline, createCalendarRequest.rcToken)
        val rcEvents = EventsService().getRcEvents(createCalendarRequest.rcToken)
        println("rc events: " + rcEvents)
        return CalendarResponse(
            calToken=calendarRow!!.token,
            name=calendarRow!!.name,
            onlineTime=calendarRow!!.online,
            offlineTime=calendarRow!!.offline,
            events=rcEvents,
        )

    }
    private fun buildTimesMap(hours: Int): Map<String,String> {
        return mapOf(
            "MONDAY" to getTimeWithDayOffset(hours=hours, dayOffset=1),
            "TUESDAY" to getTimeWithDayOffset(hours=hours, dayOffset=2),
            "WEDNESDAY" to getTimeWithDayOffset(hours=hours, dayOffset=3),
            "THURSDAY" to getTimeWithDayOffset(hours=hours, dayOffset=4),
            "FRIDAY" to getTimeWithDayOffset(hours=hours, dayOffset=5)
        )
    }
}
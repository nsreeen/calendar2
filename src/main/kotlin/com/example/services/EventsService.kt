package com.example.services

import com.example.Modules.formatUtcString
import com.example.Modules.getDayOfWeek
import com.example.Modules.isThisWeek
import com.example.Modules.utcStringFromRcIscString
import com.example.database.Database
import com.example.models.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class EventsService {
    suspend fun getRcEvents(userId: String): List<Event> {
        val userRcToken: String = UserService().getUserRcToken(userId)
        val allEvents = getAllRcEvents(userRcToken)
        val thisWeeksEvents: List<Event> = allEvents.filter { event -> isThisWeek(event.start) }
        return thisWeeksEvents
    }
    private suspend fun getAllRcEvents(userRcToken: String): List<Event> {
        val url: String = "https://www.recurse.com/calendar/events.ics?token=%s".format(userRcToken)
        val client = HttpClient(CIO)
        val response: HttpResponse = client.get(url)
        val icsEvents: List<String> = response
            .bodyAsText().splitToSequence("BEGIN:VEVENT")
            .filterIndexed { index, _ -> index > 0 }
            .toList()
        val allEvents = icsEvents
            .map {
                it.lines()
                    .map { it.split(":") }
                    .filter { it.size == 2 }
                    .map { it[0] to it[1] }
                    .toMap()
            }
            .filter { it.contains("SUMMARY") && it.contains("DTSTART;TZID=America/New_York") && it.contains("DTEND;TZID=America/New_York") }
            .map { it ->
                val start: String = utcStringFromRcIscString(it.getValue("DTSTART;TZID=America/New_York"))
                val end: String = utcStringFromRcIscString(it.getValue("DTEND;TZID=America/New_York"))
                Event(
                    summary = it.getValue("SUMMARY"),
                    start = start,
                    end = end,
                    dayOfWeek = getDayOfWeek(start),
                    isRcEvent = true,
                )
            }
        return allEvents
    }

    suspend fun createEvent(createEventRequest: CreateEventRequest): Event {
        val start = formatUtcString(createEventRequest.start)
        val end = formatUtcString(createEventRequest.end)
        val event = Event(
            summary=createEventRequest.summary,
            start=start,
            end=end,
            dayOfWeek= getDayOfWeek(start),
            isRcEvent = false,
        )
        println("here")
        val eventRow = Database().addEvent(event)
        println(eventRow)
        return event
    }
}
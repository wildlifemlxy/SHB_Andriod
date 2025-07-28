package com.ecss.shb_andriod.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.PieEntry

class ChartDataViewModel : ViewModel() {
    private val _surveys = MutableLiveData<List<Survey>>()
    val surveys: LiveData<List<Survey>> = _surveys

    fun setSurveys(surveys: List<Survey>) {
        _surveys.value = surveys
    }

    // Helper to get location counts for PieChart
    fun getLocationCounts(): Map<String, Int> {
        return _surveys.value?.groupingBy { it.location ?: "Unknown" }?.eachCount() ?: emptyMap()
    }

    // Helper to get PieEntry list for PieChart (by location)
    fun getPieEntriesByLocation(): List<PieEntry> {
        return getLocationCounts().map { (location, count) ->
            PieEntry(count.toFloat(), location)
        }
    }

    // Helper to get observations by month-year and seen/heard
    fun countObservationsByMonthYearWithSeenHeard(surveys: List<Survey>): Map<String, Pair<Int, Map<String, Int>>> {
        val monthYearCounts = mutableMapOf<String, Pair<Int, Map<String, Int>>>()
        val inputFormats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.UK)
        )
        for (survey in surveys) {
            val dateStr = survey.date ?: continue
            val date = inputFormats.firstNotNullOfOrNull { format ->
                try { format.parse(dateStr) } catch (_: Exception) { null }
            } ?: continue
            val monthYear = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.UK).format(date)
            val seenHeard = survey.seenHeard ?: "Unknown"
            val (count, seenHeardMap) = monthYearCounts.getOrDefault(monthYear, Pair(0, mutableMapOf()))
            val newSeenHeardMap = seenHeardMap.toMutableMap()
            newSeenHeardMap[seenHeard] = newSeenHeardMap.getOrDefault(seenHeard, 0) + 1
            monthYearCounts[monthYear] = Pair(count + 1, newSeenHeardMap)
        }
        return monthYearCounts
    }

    // Helper to get observations by month-year and seen/heard, including total
    fun countObservationsByMonthYearWithSeenHeardAndTotal(surveys: List<Survey>): Map<String, Pair<Int, Map<String, Int>>> {
        val monthYearCounts = mutableMapOf<String, Pair<Int, Map<String, Int>>>()
        val inputFormats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.UK)
        )
        for (survey in surveys) {
            val dateStr = survey.date ?: continue
            val date = inputFormats.firstNotNullOfOrNull { format ->
                try { format.parse(dateStr) } catch (_: Exception) { null }
            } ?: continue
            val monthYear = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.UK).format(date)
            val seenHeard = survey.seenHeard ?: "Unknown"
            val (count, seenHeardMap) = monthYearCounts.getOrDefault(monthYear, Pair(0, mutableMapOf()))
            val newSeenHeardMap = seenHeardMap.toMutableMap()
            newSeenHeardMap[seenHeard] = newSeenHeardMap.getOrDefault(seenHeard, 0) + 1
            // Only count Seen, Heard, Not found for total
            val total = (newSeenHeardMap["Seen"] ?: 0) + (newSeenHeardMap["Heard"] ?: 0) + (newSeenHeardMap["Not found"] ?: 0)
            newSeenHeardMap["Total"] = total
            monthYearCounts[monthYear] = Pair(count + 1, newSeenHeardMap)
        }
        return monthYearCounts
    }
}
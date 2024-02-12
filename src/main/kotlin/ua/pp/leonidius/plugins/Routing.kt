package ua.pp.leonidius.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ua.pp.leonidius.ua.pp.leonidius.weather.BuildConfig
import java.text.SimpleDateFormat
import java.util.*


fun Application.configureRouting(client: HttpClient) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/weather") {

            // get city and date from query parameter

            val tokenProvided = call.request.queryParameters["token"]

            if (tokenProvided != BuildConfig.TOKEN) {
                call.respond(BadTokenResponse("Invalid token"))
            } else {
                val city = call.request.queryParameters["city"]
                val year = call.request.queryParameters["year"]
                val month = call.request.queryParameters["month"]
                val day = call.request.queryParameters["day"]
                val requester = call.request.queryParameters["requester"]

                if (city == null || year == null || month == null || day == null || requester == null) {
                    call.respondText("Invalid request, missing parameters. Required: city, year, month, day, requester.")
                    return@get
                }

                val dateString = "$year-$month-$day"

                // send a request to the weather API at https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/[location]/[date1]/[date2]?key=YOUR_API_KEY
                // and return the result



                //call.respondText(client.get("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/$dateString?key=${BuildConfig.API_KEY}").bodyAsText())
                val response: WeatherResponseFromRemote =
                    client.get("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/$dateString?key=${BuildConfig.API_KEY}")
                        .body()
                val dayO = response.days.first()

                // return the result
                val result = WeatherResponse(
                    city = response.resolvedAddress,
                    date = dayO.datetime,
                    conditions = dayO.conditions,
                    description = dayO.description,
                    temp = convertFarToCel(dayO.temp),
                    humidity = dayO.humidity,
                    precipitation = dayO.precip,
                    precipitationProbability = dayO.precipprob,
                    requester = requester,
                    responseTimestamp = timestampToDateString(System.currentTimeMillis()),
                    precipitationTypes = dayO.preciptype,
                )

                 call.respond(result)


            }



        }
    }
}

@Serializable
data class WeatherResponseFromRemote(
    val resolvedAddress: String,
    // val description: String,
    val days: List<Day>,
) {

    @Serializable
    data class Day(
        val datetime: String,
        val temp: Float,
        val feelslike: Float,
        val humidity: Float,
        val precip: Float,
        val precipprob: Float,
        val windspeed: Float,
        val windgust: Float,
        val conditions: String,
        val description: String,
        val preciptype: List<String>,
    )

}

@Serializable
data class BadTokenResponse(
    val message: String
)

@Serializable
data class WeatherResponse(
    val city: String,
    val date: String,
    val conditions: String,
    val description: String,
    val temp: Float,
    val humidity: Float,
    val requester: String,
    val responseTimestamp: String,
    val precipitation: Float,
    val precipitationProbability: Float,
    val precipitationTypes: List<String>,
)

fun convertFarToCel(far: Float): Float {
    return (far - 32) * 5 / 9
}

fun timestampToDateString(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")


    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return formatter.format(calendar.time)
}
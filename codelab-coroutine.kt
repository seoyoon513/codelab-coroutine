import kotlinx.coroutines.*
import kotlin.system.*

fun main() {
    val time = measureTimeMillis {
        // STEP1: 비동기 코드 - launch()
        // runBlocking 내에 함수는 순차적으로 호출되나 printforecast(), printTemperature()은 비동기로 실행됨
        runBlocking { // 동기식 호출
            println("Weather forecast")
            // 각 코루틴은 병렬로 동작함
            launch { // 코루틴1 실행
                printForecast()
            }
            launch { // 코루틴2 실행
            	printTemperature()  
            }
            println("Have a good day")
        }
        
        // STEP2: 비동기 코드 -  async()
        runBlocking {
            println("Weather forecast")
            val forecast: Deferred<String> = async {
                getForecast()
            }
            val temperature: Deferred<String> = async {
                getTemperature()
            }
            println("[async] ${forecast.await()} ${temperature.await()}")
            println("Have a good day")
        }
        
        // STEP3: 비동기코드 - 병렬 분해
        runBlocking {
            println("Weather forecast")
            println(getWeatherReport())
            println("Have a good day!")
        }
        
        // STEP4: 예외
        runBlocking {
            println("Weather forecast")
            println(getWeatherReport2())
            println("Have a good day!")
        }
        
        // STEP4: 취소
        runBlocking {
            println("Weather forecast")
            println(getWeatherReport3())
            println("Have a good day!")
        }
        
        // STEP5: 코루틴 Dispatcher
        runBlocking {
            println("${Thread.currentThread().name} - runBlocking function")
            launch {
                println("${Thread.currentThread().name} - runBlocking function")
                withContext(Dispatchers.Default) { // 상위 작업의 Context(Main)가 지정된 Context(Default)로 재정의 됨
                    println("${Thread.currentThread().name} - runBlocking function")
                    delay(1000)
                    println("10 results found.")
                }
                println("${Thread.currentThread().name} - runBlocking function") // Context는 다시 Main으로 되돌아옴
            }
            println("Loading...")
        }
    }
    println("Execution time: ${time / 1000.0} seconds")
}

suspend fun printForecast() {
    delay(1000)
    println("Sunny")
}

suspend fun printTemperature() {
    delay(1000)
    println("30\u00b0C")
}

suspend fun getForecast(): String {
    delay(1000)
    return "Sunny"
}

suspend fun getTemperature(): String {
    delay(1000)
    return "30\u00b0C"
}

suspend fun getTemperature2(): String {
    delay(1000)
    throw AssertionError("Temperature is invalid") // 예외를 발생시키는 코드 추가
    return "30\u00b0C"
}

suspend fun getWeatherReport() = coroutineScope { // 코루틴 범위 그룹화
    // 포함된 모든 코루틴 작업이 완료된 후에 반환
    val forecast = async { getForecast() }
    val temperature = async { getTemperature() }
    "[async] ${forecast.await()} ${temperature.await()}"
}

suspend fun getWeatherReport2() = coroutineScope {
    val forecast = async { getForecast() }
    val temperature = async {
        try {
            getTemperature2()
        } catch (e: AssertionError) {
            println("Caught exception $e")
            "{ No temperature found }"
        }
    }
    "[async] ${forecast.await()} ${temperature.await()}"
}

suspend fun getWeatherReport3() = coroutineScope {
    val forecast = async { getForecast() }
    val temperature = async { getTemperature() }
    
    delay(200)
    temperature.cancel() // 온도 정보를 가져오는 코루틴 취소
    
    //"[async] ${forecast.await()} ${temperature.await()}" // JobCancellationException
    "[async] ${forecast.await()}"
}
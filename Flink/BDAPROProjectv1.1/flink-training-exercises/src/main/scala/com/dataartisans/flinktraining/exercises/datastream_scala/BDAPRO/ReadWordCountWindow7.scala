/*
 * Copyright 2015 data Artisans GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dataartisans.flinktraining.exercises.datastream_scala.BDAPRO

import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.{FilePathFilter, FileProcessingMode}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * Scala reference implementation for the "Ride Cleansing" exercise of the Flink training
 * (http://dataartisans.github.io/flink-training).
 *
 * The task of the exercise is to filter a data stream of taxi ride records to keep only rides that
 * start and end within New York City. The resulting stream should be printed to the
 * standard out.
 *
 * Parameters:
 * -input path-to-input-file
 *
 */
object ReadWordCountWindow7 {

  def main(args: Array[String]) {

    // parse parameters
  //  val params = ParameterTool.fromArgs(args)
   // val input = params.getRequired("input")

    val maxDelay = 60 // events are out of order by max 60 seconds
    val speed = 600   // events of 10 minutes are served in 1 second


    def parseMap(line : String): (String, Long) = {
      val record = line.substring(1).split(",")
      (record(0).toString, record(1).toLong)
    }



    // set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)



 //   val text = env.readTextFile("/home/andresviv/Temp/big.txt");

    val inp = new TextInputFormat(new Path("/home/andresviv/Temp/big.txt"));
    val text = env.readFile(inp,
                "/home/andresviv/Temp/big.txt",
                FileProcessingMode.PROCESS_CONTINUOUSLY,
      100l,FilePathFilter.createDefaultFilter());
    /**
      * its better to use
      * val stream: DataStream[MyEvent] = env.readFile(
         *myFormat, myFilePath, FileProcessingMode.PROCESS_CONTINUOUSLY, 100,
         *FilePathFilter.createDefaultFilter());
      */

      //SE PUEDE USAR System.currentTimeMillis)

    /**
      *https://ci.apache.org/projects/flink/flink-docs-release-1.2/dev/event_timestamp_extractors.html
      * val stream: DataStream[MyEvent] = ...
      *val withTimestampsAndWatermarks = stream.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[MyEvent](Time.seconds(10))( _.getCreationTime ))
      *
      *
      */

    val start = System.currentTimeMillis
    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
     .map { (_,System.currentTimeMillis, 1,(start-System.currentTimeMillis)/1000) }
      //  .map(parseMap(_))
    val timedValue = counts.assignAscendingTimestamps(x => x._2)

    val keyValuePair = timedValue.keyBy(0)
   // val timeWindowed = keyValuePair.timeWindow(Time.milliseconds(100))

    // tumbling count window of 5 elements size
    val timeWindowed = keyValuePair.countWindow(50)


    //maybe with apply i can set itSystem.currentTimeMillis in the windows

    val countStream = timeWindowed.sum(2)
    val endWindow = System.currentTimeMillis
    //countStream.print.name("print sink")


   // countStream.print()
   // counts.print()
    countStream.print()
    println("Starting time:  " + start)
    println("End Windows : " + endWindow)
    env.execute()



  //looks interesting
    //http://stackoverflow.com/questions/34163930/flink-streaming-event-time-window-ordering



  }

    //EXTRA METHODS

  val now = Calendar.getInstance()
  val currentHour = now.get(Calendar.HOUR_OF_DAY)

  def getCurrentHour: String = {
    val now = Calendar.getInstance().getTime()
    val hourFormat = new SimpleDateFormat("hh")
    try {
      // returns something like "01" if i just return at this point, so cast it to
      // an int, then back to a string (or return the leading '0' if you prefer)
      val currentHour = Integer.parseInt(hourFormat.format(now))
      return "" + currentHour
    } catch {
      // TODO return Some/None/Whatever
      case _ => return "0"
    }
    return hourFormat.format(now)
  }


  def parseMapex(line : String): (Int, Int, Double, Long) = {
    val record = line.substring(1, line.length - 1).split(",")
    (record(0).toInt, record(1).toInt, record(2).toDouble, record(3).toLong)
  }
}

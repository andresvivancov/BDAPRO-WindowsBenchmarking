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

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.FileProcessingMode
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
object ReadWordCountWindow {

  def main(args: Array[String]) {

    // parse parameters
  //  val params = ParameterTool.fromArgs(args)
   // val input = params.getRequired("input")

    val maxDelay = 60 // events are out of order by max 60 seconds
    val speed = 600   // events of 10 minutes are served in 1 second

    // set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment


    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    // get the taxi ride data stream
 //   val rides = env.read.addSource(new TaxiRideSource("/home/andresviv/Temp/big.txt", maxDelay, speed))
    val text = env.readTextFile("/home/andresviv/Temp/big.txt");

    //I tried to read each 50 miliseconds
   // val text = env.readFileStream("/home/andresviv/Temp/big.txt",1000)

   // val text = env.readFile("txt","/home/andresviv/Temp/big.txt",FileProcessingMode.PROCESS_CONTINUOUSLY,1000)

   // val text = env.readFile(null ,"/home/andresviv/Temp/big.txt",FileProcessingMode.PROCESS_CONTINUOUSLY,1000)


    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
     .map { (_, 1) }

    val timedValue = counts.assignAscendingTimestamps(x => x._2)

    val keyValuePair = timedValue.keyBy(0).timeWindow(Time.milliseconds(3))

    val countStream = keyValuePair.sum(1)//.name("countStream")

    //countStream.print.name("print sink")
    countStream.print()



    env.execute()
  }

}

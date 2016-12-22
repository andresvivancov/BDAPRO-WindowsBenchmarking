package AV2Twitterwithtime

/**
  * Created by andresviv on 11.12.16.
  */
import java.util.Date

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.evictors.CountEvictor
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.table.expressions.Count
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

/**
  * Describes a Twitter location as Longitude/Latitude
  */
case class TwitterLocation(var lat: Double, var lon: Double) {
  def this(){
    this(0.0,0.0)
  }
}

/**
  * Describes a single tweet
  */
case class Tweet(userName: String, content: String, createdAt: Date, location: TwitterLocation, language: String)
{
  def this(){
    this("__NO_USER__","__NO_CONTENT__",new Date(), new TwitterLocation(0.0,0.0),"")
  }
}

case class TweetTime(var long:Double){
  def this(){
    this(System.currentTimeMillis())
  }
}

/**
  * This class describes the source of incoming twitter feeds and is used by the DataStream API
  * It implements the SourceFunction class
  */
class TwitterStreamGenerator(filterTerms: String*) extends SourceFunction[Tweet]{
  var running = true

  override def cancel(): Unit = {
    running = false
  }

  /**
    * This method is being called by DataStream API to kick-off the stream-production
    */
  override def run(ctx: SourceContext[Tweet]): Unit = {
    val cb = new ConfigurationBuilder
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("X0k4zycdWOV4Ud0QuU7gLUdiU")
      .setOAuthConsumerSecret("nq42UJ75zaWw6Gt38MGnU")
      .setOAuthAccessToken("1206314257-gPBrIry5pXkenRlUW")
      .setOAuthAccessTokenSecret("UM262qLWAKMgR86a8GTVJDcM")

    val stream: TwitterStream = new TwitterStreamFactory(cb.build()).getInstance()

    stream.addListener(new SimpleStreamListener(ctx))
    val query = new FilterQuery(0, null, filterTerms.toArray)
    stream.filter(query)
    while(running){}
  }
}

/**
  * This class represents the Listener needed by Twitter4J
  * Here we define methods which are being called by the Twitter API (especially the "onStatus" method is very important)
  * To make the DataStream API aware of our incoming tweets we instantiate SimpleStreamListener by providing it the
  * SourceContext instance delivered by DataStream API. Inside onStatus method the collect() method of SourceContext
  * will be called to forward new tweets to it. Therefore the SimpleStreamListener is hard-wired with the TwitterStreamGenerator
  */
class SimpleStreamListener(ctx: SourceContext[Tweet]) extends StatusListener() {
  override def onStatus(status: Status) = {
    val userName = status.getUser.getName
    val content = status.getText
    val createdAt = status.getCreatedAt
    val geoLoc = status.getGeoLocation
    val location = geoLoc match {
      case null => new TwitterLocation(0.0,0.0)
      case _ => new TwitterLocation(geoLoc.getLatitude, geoLoc.getLongitude)
    }
    val language = status.getLang
    val tweet = Tweet(userName, content, createdAt, location, language)
    ctx.collect(tweet)
    val time = new TweetTime()

  }

  override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) = {}

  override def onTrackLimitationNotice(numberOfLimitedStatuses: Int) = {}

  override def onException(ex: Exception) = {
    ex.printStackTrace
  }

  def onStallWarning(warning: StallWarning) = {}
  def onScrubGeo(l: Long, l1: Long) = {}
}

/**
  * Our application starts here.
  * We get the StreamingContext and declare a new Stream-Source by instatiating the TwitterStreamGenerator
  * which will only forward tweets containing certain #hash-tags
  */
/**Origintal
  *
  * object TwitterStreamDemo {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tweets: DataStream[Tweet] = env.addSource(new TwitterStreamGenerator("#machinelearning","#datascience"))
    tweets.map(tweet => println(s"$tweet.userName : $tweet.content"))
    env.execute("TwitterStream")
  }

  *
  */

//Extract the stock symbols



object TwitterStreamDemo3 {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val tweets: DataStream[Tweet] = env.addSource(new TwitterStreamGenerator("#machinelearning","#datascience"))

   // val counts = tweets.map { (_, 1) }
   // val mapname = tweets.map { (x =>  x.userName -> 1 ) }
   //val tweetsnc = tweets.map(tweet => println(s"$tweet.userName : $tweet.content"))

//    val tweetsnc = tweets.map(tweet => ( tweet.userName)+ ( tweet.content))

    val tweetsnc = tweets.map(tweet =>  tweet.userName)


    val mapname = tweetsnc.map { (_, 1) }


    //val timedValue = mapname.assignAscendingTimestamps(x => x._2)

    val timedValue = mapname.assignAscendingTimestamps(x => x._2)

    val keyValuePair = timedValue.keyBy(0).timeWindow(Time.seconds(30))

    val countStream = keyValuePair.sum(1)//.name("countStream")
    //countStream.print.name("print sink")
    //countStream.print()
    //new its working :)
    //tweetsnc.print()
    //timedValue.print()
   // countStream.print()
    tweets.print()


   // tweets.map(tweet => println(s"$tweet.userName : $tweet.content"))
    env.execute("TwitterStream")
  }
   // val values = tweets.flatMap(value => value.split("\\s+")).map(value => (value,1))


    /*


          .keyBy(0)
          .countWindow(windowSize, slideSize)
          // group by the tuple field "0" and sum up tuple field "1"
          .sum(1);
      */


  /**
    *
    * Taxi Rides
    *
    *  // find n most popular spots
    val popularPlaces = rides
      // remove all rides which are not within NYC
      .filter { r => GeoUtils.isInNYC(r.startLon, r.startLat) && GeoUtils.isInNYC(r.endLon, r.endLat) }
      // match ride to grid cell and event type (start or end)
      .map(new GridCellMatcher)
      // partition by cell id and event type
      .keyBy( k => k )
      // build sliding window
      .timeWindow(Time.minutes(15), Time.minutes(5))
      // count events in window
      .apply{ (key: (Int, Boolean), window, vals, out: Collector[(Int, Long, Boolean, Int)]) =>
        out.collect( (key._1, window.getEnd, key._2, vals.size) )
      }
      // filter by popularity threshold
      .filter( c => { c._4 >= popThreshold } )
      // map grid cell to coordinates
      .map(new GridToCoordinates)
    *
    */



}

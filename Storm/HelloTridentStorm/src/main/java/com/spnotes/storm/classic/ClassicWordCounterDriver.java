package com.spnotes.storm.classic;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.spnotes.storm.LineReaderSpout;

/**
 * Created by gpzpati on 8/14/14.
 */
public class ClassicWordCounterDriver {

    public static void main(String[] args)throws Exception{
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("word-reader",new LineReaderSpout());
        builder.setBolt("word-normalizer", new WordSplitterBolt()).shuffleGrouping("word-reader");
        builder.setBolt("word-counter", new WordCounterBolt(),2).fieldsGrouping("word-normalizer", new Fields("word"));

        Config conf = new Config();
        conf.put("inputFile", args[0]);
      //  conf.setDebug(true);
        conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Getting-Started-Toplogie", conf, builder.createTopology());
        Thread.sleep(20000);
        cluster.shutdown();
    }
}

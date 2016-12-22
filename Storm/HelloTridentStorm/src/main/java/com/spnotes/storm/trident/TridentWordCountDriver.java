package com.spnotes.storm.trident;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.tuple.Fields;
import com.spnotes.storm.LineReaderSpout;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.Debug;
import storm.trident.testing.Split;

/**
 * Created by gpzpati on 8/12/14.
 */
public class TridentWordCountDriver {

    public static void main(String[] argv){

        if(argv.length != 1){
            System.out.println("Please provide input file path");
            System.exit(-1);
        }

        String inputFile = argv[0];
        System.out.println("TridentWordCountDriver is starting for " + inputFile);
        Config config = new Config();
        config.put("inputFile",inputFile);

        LocalDRPC localDRPC = new LocalDRPC();
        LocalCluster localCluster = new LocalCluster();

        LineReaderSpout lineReaderSpout = new LineReaderSpout();
        TridentTopology topology = new TridentTopology();
        topology.newStream("spout",lineReaderSpout)
                .each(new Fields("line"), new Split(), new Fields("word"))
                .groupBy(new Fields("word"))
                .aggregate(new Fields("word"), new Count(), new Fields("count"))
                .each(new Fields("word","count"), new Debug());

        localCluster.submitTopology("WordCount",config,topology.build());
        /*
        try {
            Thread.sleep(5000);
        }catch (Exception e){}
        localCluster.shutdown();
        */
    }
}

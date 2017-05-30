package de.lisaplus.atlas

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter

/**
 * Created by eiko on 30.05.17.
 */
class DoCodeGen {
    @Parameter(names = [ '-t', '--test' ], description = "Level of verbosity")
    private String test

    public static void main(String ... args) {
        DoCodeGen doCodeGen = new DoCodeGen();
        JCommander.newBuilder()
                .addObject(doCodeGen)
                .build()
                .parse(args);
        doCodeGen.run();
    }

    void run() {
        print "test=${test}"
    }
}

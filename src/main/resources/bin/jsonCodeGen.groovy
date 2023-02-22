/**
 * This is a platform independent start script for jsonCodeGen
 * Requirements:
 *      Java 17
 *      Groovy >= 4.0.0
 * Created by eiko on 13.07.17.
 */


scriptPos = new File(getClass().protectionDomain.codeSource.location.path).parent

command = ['java','-cp',"$scriptPos/lib/*","-Dlogback.configurationFile=$scriptPos/conf/logback.xml",'de.lisaplus.atlas.DoCodeGen']

args.each {
    command.add(it)
}

def sout = new StringBuilder(), serr = new StringBuilder()
def proc = command.execute()
proc.consumeProcessOutput(sout, serr)
proc.waitForOrKill(10000)
println "out> $sout err> $serr"

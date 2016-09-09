#!/usr/bin/groovy
import java.lang.management.*
import javax.management.*
import javax.management.remote.*
import java.text.*
import groovy.json.JsonBuilder




def getProperties(runtimeMxBean) {
    def systemProperties = new Properties()
    systemProperties.putAll(runtimeMxBean.getSystemProperties())
    return systemProperties
}
def jstack(serviceUrl, out)  {
    jmxUrl = new JMXServiceURL(serviceUrl)
    JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl, null)
    MBeanServerConnection mbsc = jmxc.getMBeanServerConnection()
    ThreadMXBean threadMXBean  = ManagementFactory.newPlatformMXBeanProxy(mbsc,
    ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class)
    RuntimeMXBean runtimeMxBean = ManagementFactory.newPlatformMXBeanProxy(mbsc,
    ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class)
    ThreadInfo[] threads = threadMXBean.dumpAllThreads(true, true)
        File f = new File("/var/log/jmx/groovyjmx.log")
        Date now = new Date()
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        println timestamp.format(now)
        def threadMapItem = [:]
        def threadMap = []
        def simpleThreadMap = [:]
        assert simpleThreadMap.getClass().name == 'java.util.LinkedHashMap'
        def simpleThreadName = ""


    threads.each { t -> def threadPrefixState = threadPrefix+t.threadState.toString()
                f << "\n" + timestamp.format(now).toString() +" "+threadPrefix+" "+ t.threadName.toString().replaceAll(" ","_") + " "+t.threadId+" "+t.threadState +" "+ threadPrefixState
    }
}




def cli = new CliBuilder(usage: 'jstack -u <jmx url>',
    header: 'JVM thread dump in Groovy')

cli.with {
    h longOpt: 'help'  , args: 0, 'Show usage information and quit'
    u longOpt: 'jmxurl', args: 1, argName: 'jmx url', 'JMX service URL'
    o longOpt: 'output', args: 1, argName: 'output file', 'Output file'
}

def opts = cli.parse(args)

if(!opts) {
    return
}

if (opts.h || !opts.u) {
    cli.usage()
    return
}

if(opts.o) {
    def sw = new StringWriter()
    def     pw = new PrintWriter(sw)
    jstack(opts.u, pw)
    pw.close()
    new File(opts.o).withWriter { w ->
        w << sw.toString()
    }
} else {
    jstack(opts.u, System.out)
}

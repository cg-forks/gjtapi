. /usr/dialogic/bin/setenv.sh
LOCALNUMBER=$1
REMOTENUMBER=$2
JAVA_HOME=/opt/jdk1_4
JAVA_BIN=${JAVA_HOME}/bin
JAVA=${JAVA_BIN}/java
JARS=/project/jtapi/jtapi1.3.1/distrib/lib/jtapi131.jar:/project/jain1_0b/distrib/lib/jcc-1.0b-csapi.jar

CLASSBASE=/project/jtapi/gjtapi/devel/classes
NATIVE=/project/jtapi/gjtapi/devel/source/net/sourceforge/gjtapi/raw/dialogic/native 
#LD_PRELOAD=/usr/lib/libLiS.so export LD_PRELOAD
#LD_PRELOAD=${JAVA_HOME}/jre/lib/i386/server/libjsig.so:/usr/lib/libLiS.so export LD_PRELOAD
LD_PRELOAD=$NATIVE/libgcprovider.so export LD_PRELOAD

#JOPTS=-Xrunsrinit
LD_LIBRARY_PATH=$NATIVE export LD_LIBRARY_PATH
${JAVA} ${JOPTS} -classpath ${CLASSBASE}:${JARS} net.sourceforge.gjtapi.test.TestCallListener DialogicGC ${LOCALNUMBER} ${REMOTENUMBER}




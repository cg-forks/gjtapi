JAVA_HOME=/opt/jdk1_4
JAVA_BIN=${JAVA_HOME}/bin
JAVAH=${JAVA_BIN}/javah
JAVAC=${JAVA_BIN}/javac
BASE=/project/jtapi/gjtapi/devel/
CLASSBASE=${BASE}classes
SRCBASE=${BASE}src
GCPROVIDERC=${CLASSBASE}/net/sourceforge/gjtapi/raw/dialogic/GCProvider.class
GCPROVIDERJ=${SRCBASE}/net/sourceforge/gjtapi/raw/dialogic/GCProvider.java
INC=-I /usr/java/j2sdk1.4.0_01/include  -I /usr/java/j2sdk1.4.0_01/include/linux -I /usr/dialogic/inc
CCFLAGS= -c -DLINUX
SYSLIBS = /lib/ld-linux.so.2 -lpthread  /usr/lib/libstdc++-libc6.2-2.so.3  -ldl  -lm -lc
LIBS= -lgc 
LIS= -lpLiS

all: libgcprovider.so  ${GCPROVIDERC}


#
${GCPROVIDERC}: ${GCPROVIDERJ}
	${JAVAC} -classpath ${CLASSBASE} -d ${CLASSBASE} ${GCPROVIDERJ}


gcprovider.h: ${GCPROVIDERC}
	${JAVAH} -classpath ${CLASSBASE} -o $@  net.sourceforge.gjtapi.raw.dialogic.GCProvider

gcprovider.o: gcprovider.c gcprovider.h
	cc $(CCFLAGS) $(INC) $<

libgcprovider.so: gcprovider.o Makefile
	ld -shared -o libgcprovider.so gcprovider.o $(LIS) $(LIBS)

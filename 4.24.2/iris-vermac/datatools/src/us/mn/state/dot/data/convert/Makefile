#
# Makefile for convert.jar
# Written by Douglas Lau
#
JAVAC = jikes
CLASS_DIRS = us/mn/state/dot/data/convert/
CLASSPATH = $(JIKESPATH):../../../../../../
TARGET = convert.jar
vpath %.java .
vpath %.class $(CLASS_DIRS)

$(TARGET): Converter.class
	jar cmf MANIFEST.MF $(TARGET) us/

Converter.class: *.java
	$(JAVAC) -classpath $(CLASSPATH) -d . Converter.java

clean:
	rm -rf us/
	rm -f $(TARGET)

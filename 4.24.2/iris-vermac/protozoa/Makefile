CC = gcc
CFLAGS = -O2 -Wall -Werror -flto
#CFLAGS = -Wall -ggdb
TARGET = protozoa

all:  $(TARGET)

SRC = src
BUILD = build
MODULES = poller channel config ccpacket buffer axis joystick manchester vicon \
          pelco_d pelco_p infinova ccreader ccwriter log pool rbtree stats \
          timer defer timeval
OBJS = $(addprefix $(BUILD)/, $(addsuffix .o,$(MODULES)))

$(BUILD):
	mkdir $(BUILD)

$(BUILD)/%.o: $(SRC)/%.c
	$(CC) $(CFLAGS) -o $@ -c $<

$(TARGET): $(SRC)/main.c $(BUILD) $(OBJS)
	$(CC) -o $(TARGET) $(CFLAGS) $(OBJS) $<

clean:
	rm -rf $(BUILD) $(TARGET)

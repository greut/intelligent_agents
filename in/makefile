EXT_LIBS=../lib
SRC_DIR=src
OUTPUT_DIR=bin

all: compile

compile: structure
	javac -sourcepath src -cp $(EXT_LIBS)/repast.jar $(SRC_DIR)/MainRabbit.java -d $(OUTPUT_DIR)

verbose: structure
	javac -sourcepath src -cp $(EXT_LIBS)/repast.jar $(SRC_DIR)/MainRabbit.java -d $(OUTPUT_DIR) -Xlint:unchecked

run: structure
	java -cp $(EXT_LIBS)/*:.:$(OUTPUT_DIR) MainRabbit

structure:
	mkdir -p $(OUTPUT_DIR)

clean:
	rm -r $(OUTPUT_DIR)

.PHONY: compile clean
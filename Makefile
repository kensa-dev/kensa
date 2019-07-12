.DEFAULT_GOAL := build

.PHONY: clean
clean:
	./gradlew clean

.PHONY: build
build:
	./gradlew build publishToMavenLocal
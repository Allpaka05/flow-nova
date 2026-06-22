#!/bin/bash

set -e

if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java is not found in PATH."
    echo "Java 17 or higher is required to build and run this project."
    exit 1
fi

java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
java_major=$(echo "$java_version" | awk -F. '{print ($1 == "1") ? $2 : $1}')

if [ -z "$java_major" ]; then
    echo "Error: could not determine Java version."
    echo "Java 17 or higher is required to build and run this project."
    exit 1
fi

if [ "$java_major" -lt 17 ]; then
    echo "Error: found Java version $java_version."
    echo "Java 17 or higher is required to build and run this project."
    exit 1
fi

echo "Found Java $java_version — OK."

mkdir -p out/classes
javac -d out/classes $(find src -name '*.java')
jar cfm FlowNova.jar src/META-INF/MANIFEST.MF -C out/classes . -C src icons
rm -rf out

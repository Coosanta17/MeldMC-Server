#!/usr/bin/env bash

mkdir -p allJars
rm -rf allJars/*

for d in versionProperties/*; do
  version=$(echo "$d" | sed "s/versionProperties\///" | sed "s/.properties//")

  echo "Cleaning build dir, assembling JARs and merging JARs"
  if ! ./gradlew clean assemble mergeJars -PmcVer="$version"; then continue; fi

  # This must be run separately because sometimes it runs before mergeJars otherwise.
  echo "Shade dependencies into merged JAR"
  if ! ./gradlew shadowJar -PmcVer="$version"; then continue; fi

  echo "Move to ./allJars"
  mv build/lib/*-shaded.jar allJars/
done
#!/usr/bin/env bash

mkdir -p allJars
rm -rf allJars/*

build_version_jar() {
  local version="$1"
  echo "Cleaning build dir, assembling JARs and merging JARs"
  if ! ./gradlew clean assemble mergeJars -PmcVer="$version"; then return 1; fi

  # This must be run separately because sometimes it runs before mergeJars otherwise.
  echo "Shade dependencies into merged JAR"
  if ! ./gradlew shadowJar -PmcVer="$version"; then return 1; fi
}

# Parse arguments
VERSION_ARG=""
while [[ $# -gt 0 ]]; do
  case $1 in
    --version)
      VERSION_ARG="$2"
      shift 2
      ;;
    *)
      echo "Unknown argument: $1"
      exit 1
      ;;
  esac
done

# If --version is specified
if [[ -n "$VERSION_ARG" ]]; then
  # Check if version properties file exists
  if [[ ! -f "versionProperties/${VERSION_ARG}.properties" ]]; then
    echo "Error: Version '${VERSION_ARG}' not found in versionProperties/"
    echo "Available versions:"
    for d in versionProperties/*.properties; do
      echo "  - $(basename "$d" .properties)"
    done
    exit 1
  fi

  # Backup original gradle.properties
  cp gradle.properties gradle.properties.backup

  # Temporarily set mc_ver in gradle.properties
  sed -i.tmp "s/^mc_ver=.*/mc_ver=${VERSION_ARG}/" gradle.properties
  rm -f gradle.properties.tmp

  echo "Building for version: ${VERSION_ARG}"
  build_version_jar "$VERSION_ARG"

  # Restore original gradle.properties
  mv gradle.properties.backup gradle.properties

  echo "Move to ./allJars"
  mv build/libs/*-shaded.jar allJars/
else
  # Collect all versions
  versions=()
  for d in versionProperties/*.properties; do
    version=$(basename "$d" .properties)
    versions+=("$version")
  done

  # Ask for confirmation
  echo "Do you want to build for the following versions:"
  for version in "${versions[@]}"; do
    echo "  - $version"
  done
  read -p "Continue? (y/n): " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Build cancelled."
    exit 0
  fi

  # Build all versions
  for version in "${versions[@]}"; do
    build_version_jar "$version"

    echo "Move to ./allJars"
    mv build/libs/*-shaded.jar allJars/
  done
fi

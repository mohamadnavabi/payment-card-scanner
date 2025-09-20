#!/bin/bash

# Payment Card Scanner Publishing Script
# Usage: ./scripts/publish.sh [version] [message]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if version is provided
if [ -z "$1" ]; then
    print_error "Version is required!"
    echo "Usage: $0 <version> [commit_message]"
    echo "Example: $0 1.0.1 \"Fixed camera permission issue\""
    exit 1
fi

VERSION=$1
COMMIT_MESSAGE=${2:-"Release version $VERSION"}

print_status "Starting release process for version $VERSION"

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    print_error "Not in a git repository!"
    exit 1
fi

# Check if there are uncommitted changes
if ! git diff-index --quiet HEAD --; then
    print_warning "You have uncommitted changes. Please commit or stash them first."
    exit 1
fi

# Update version in build.gradle
print_status "Updating version in PaymentCardScanner/build.gradle"
sed -i.bak "s/version = '[^']*'/version = '$VERSION'/" PaymentCardScanner/build.gradle
rm PaymentCardScanner/build.gradle.bak

# Update version in pom.xml
print_status "Updating version in pom.xml"
sed -i.bak "s/<version>[^<]*<\/version>/<version>$VERSION<\/version>/" pom.xml
rm pom.xml.bak

# Build the project
print_status "Building the project..."
./gradlew clean :PaymentCardScanner:build

# Run tests
print_status "Running tests..."
./gradlew :PaymentCardScanner:test

# Commit changes
print_status "Committing version changes..."
git add PaymentCardScanner/build.gradle pom.xml
git commit -m "$COMMIT_MESSAGE"

# Create and push tag
print_status "Creating tag v$VERSION..."
git tag -a "v$VERSION" -m "Release version $VERSION"
git push origin master
git push origin "v$VERSION"

print_status "Release $VERSION completed successfully!"
print_status "GitHub Actions will now automatically publish the library to GitHub Packages."
print_status "You can monitor the progress at: https://github.com/mohamadnavabi/payment-card-scanner/actions"


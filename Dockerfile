# Android Dockerfile — Builds the Android app (APK/AAB) via Gradle
# Multi-stage build:
#  - builder: sets up JDK + Android SDK command line tools, runs Gradle build
#  - output: copies build outputs (APK/AAB) to /outputs
#
# Notes:
#  - This Dockerfile is intended for CI or reproducible local builds.
#  - You may need to adjust Android platform / build-tools versions to match your project.
#  - Building Android in Docker requires the Android SDK command-line tools.

FROM openjdk:17-jdk-slim AS builder

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_SDK_ROOT=/sdk \
    ANDROID_HOME=/sdk \
    PATH=$PATH:/sdk/cmdline-tools/latest/bin:/sdk/platform-tools

# Install required packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget unzip curl git ca-certificates gnupg2 python3 \
    && rm -rf /var/lib/apt/lists/*

# Install Android command-line tools (cmdline-tools)
# NOTE: The exact commandlinetools archive name may change. Update URL if download fails.
ARG CMDLINE_TOOLS_ZIP_URL=https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    cd /tmp && \
    wget -q ${CMDLINE_TOOLS_ZIP_URL} -O cmdline-tools.zip && \
    unzip -q cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm cmdline-tools.zip

# Ensure sdkmanager is available via PATH
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin

# Accept Android SDK licenses and install minimal required SDK components
# Adjust platforms and build-tools as needed. Project uses compileSdk = 35.
RUN yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses || true
RUN sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --install \
    "platform-tools" \
    "platforms;android-35" \
    "build-tools;35.0.0"

# Copy repository and build the app using the Gradle wrapper
WORKDIR /workspace
COPY . /workspace
RUN chmod +x ./gradlew

# Use Gradle cache directory to avoid long re-downloads between builds (optional)
ENV GRADLE_USER_HOME=/workspace/.gradle

# Assemble release (change to :app:assembleDebug for debug builds)
RUN ./gradlew :app:assembleRelease --no-daemon --stacktrace

# Export build outputs
FROM busybox:1.36.0-uclibc as output
WORKDIR /outputs
COPY --from=builder /workspace/app/build/outputs /outputs

# Default stage does nothing; users can use `--target output` to extract artifacts
CMD ["/bin/true"]

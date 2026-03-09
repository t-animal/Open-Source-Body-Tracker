# Use 'bookworm' (Debian 12) for better stability and package availability
FROM mcr.microsoft.com/devcontainers/base:bookworm

# Install JDK 17, curl, and unzip
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends openjdk-17-jdk curl unzip adb

# Set Java Home explicitly
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Set Android Home
ENV ANDROID_HOME=/usr/local/lib/android/sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Download and install Android Command Line Tools
# Using the specific latest stable version 11076708
RUN mkdir -p $ANDROID_HOME/cmdline-tools \
    && curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
    && unzip cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools \
    && mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest \
    && rm cmdline-tools.zip

# Accept licenses and install platform-tools + build-tools
# We use API 34 here for stability, you can add 35/36 once the container is up.
RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Grant the 'vscode' user permissions to the SDK folder
RUN chown -R vscode:vscode $ANDROID_HOME
# -----------------------------------------------------------------------------
# Lightweight Spark + Scala + Delta Lake dev image
#
# Environment:
# - Ubuntu 22.04
# - Java Zulu 8
# - Scala 2.12.15
# - Python 3.11.11
# - Delta Lake 3.2.0
#
# Optimized for:
# - Scala Spark batch jobs
# - Structured Streaming development
# - sbt builds
# - Local testing
#
# Notes:
# - Uses Eclipse Temurin JDK 8 slim base for minimal size
# - Spark binaries are NOT pre-bundled (keeps image much smaller)
# - Spark + Delta dependencies are resolved through sbt
# -----------------------------------------------------------------------------

FROM eclipse-temurin:8-jdk-jammy

LABEL maintainer="Rida MASSOU"
LABEL description="Lightweight Scala Spark + Delta Lake development environment"

ENV DEBIAN_FRONTEND=noninteractive

# -----------------------------------------------------------------------------
# Versions
# -----------------------------------------------------------------------------

ENV SCALA_VERSION=2.12.15
ENV PYTHON_VERSION=3.11
ENV SBT_VERSION=1.10.0

# -----------------------------------------------------------------------------
# Install minimal system dependencies
# -----------------------------------------------------------------------------

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    wget \
    gnupg \
    ca-certificates \
    unzip \
    tar \
    bash \
    git \
    build-essential \
    software-properties-common \
    && rm -rf /var/lib/apt/lists/*

# -----------------------------------------------------------------------------
# Install Python 3.11
# -----------------------------------------------------------------------------

RUN add-apt-repository ppa:deadsnakes/ppa -y && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    python3.11 \
    python3.11-dev \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

RUN ln -sf /usr/bin/python3.11 /usr/bin/python && \
    python --version

# -----------------------------------------------------------------------------
# Install Scala 2.12.15
# -----------------------------------------------------------------------------

RUN wget -q https://downloads.lightbend.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz && \
    tar -xzf scala-${SCALA_VERSION}.tgz -C /opt && \
    ln -s /opt/scala-${SCALA_VERSION}/bin/scala /usr/local/bin/scala && \
    ln -s /opt/scala-${SCALA_VERSION}/bin/scalac /usr/local/bin/scalac && \
    rm scala-${SCALA_VERSION}.tgz

# -----------------------------------------------------------------------------
# Install sbt
# -----------------------------------------------------------------------------

RUN curl -L -o sbt.tgz https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz && \
    tar -xzf sbt.tgz -C /opt && \
    ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt && \
    rm sbt.tgz

# -----------------------------------------------------------------------------
# Python packages commonly useful for Spark development
# -----------------------------------------------------------------------------

RUN pip install --no-cache-dir \
    pyspark==3.5.1 \
    delta-spark==3.2.0

# -----------------------------------------------------------------------------
# Environment
# -----------------------------------------------------------------------------

ENV JAVA_HOME=/opt/java/openjdk
ENV SCALA_HOME=/opt/scala-${SCALA_VERSION}

WORKDIR /workspace

# -----------------------------------------------------------------------------
# Default command
# -----------------------------------------------------------------------------

CMD ["/bin/bash"]
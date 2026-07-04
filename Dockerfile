FROM ghcr.io/graalvm/jdk-community:26

RUN microdnf install dnf && \
    dnf install -y lz4 lz4-devel && \
    dnf clean all

RUN mkdir /opt/app
WORKDIR /opt/app

COPY target/eelaa-*.jar eelaa.jar

CMD ["java", "-jar", "-Xmx8g", "-XX:+UseCompactObjectHeaders", "eelaa.jar"]

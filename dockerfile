
# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
 
# Copy Maven wrapper and pom first — Docker layer caches dependencies
# as long as pom.xml hasn't changed, this layer won't re-download
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
 
# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B
 
# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
 
# Non-root user — never run as root inside a container
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
 
COPY --from=builder /app/target/*.jar app.jar
 
EXPOSE 8080
 
ENTRYPOINT ["java", \
  # FIX: Without these flags the JVM reads host RAM (e.g. 16 GB) and                \
  # allocates heap relative to that — ignoring the container memory limit.           \
  # UseContainerSupport makes it read cgroup limits (the actual container RAM).      \
  # MaxRAMPercentage=75 means: use 75% of container RAM for heap.                   \
  # Example: container has 512m → heap = ~384m, leaving room for metaspace/threads. \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
 
# ---------- Build stage ----------
# Берём JDK 21 и устанавливаем Maven
FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app

# Устанавливаем Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Копируем pom.xml и папку .mvn для сборки зависимостей
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Подкачиваем все зависимости для оффлайн сборки
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем проект без тестов
COPY src src
RUN mvn -q -DskipTests -Dmaven.test.skip=true package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем jar из билд-контейнера
COPY --from=backend-build /app/target/*.jar app.jar

# Пробрасываем порт
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]

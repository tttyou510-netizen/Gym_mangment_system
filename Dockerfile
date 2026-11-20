# --- المرحلة الأولى: البناء (Build Stage) ---
# بنجيب صورة فيها Maven و Java 21 كاملين عشان نجمع الكود
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# بننسخ ملف pom.xml الأول عشان دوكر يعمل Cache للمكتبات (Dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline

# بننسخ الكود وبنعمل Build لملف الـ jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- المرحلة الثانية: التشغيل (Run Stage) ---
# بنجيب صورة JRE 21 خفيفة جداً للتشغيل فقط
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# بناخد ملف الـ jar الناتج من المرحلة الأولى ونرمي باقي ملفات البناء
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
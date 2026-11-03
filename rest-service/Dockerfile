FROM internal.example.com/hub.docker.com/gradle:8.4-jdk17-alpine AS BUILD_STAGE
COPY --chown=gradle:gradle . /home/gradle
RUN gradle build -x test || return 1

FROM internal.example.com/hub.docker.com/eclipse-temurin:17-jdk
ENV ARTIFACT_NAME=compliance-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/app
ENV TZ=Asia/Tashkent
# ARG SPRING_PROFILES_ACTIVE
# ENV SPRING_PROFILES_ACTIVE $SPRING_PROFILES_ACTIVE
COPY --from=BUILD_STAGE /home/gradle/build/libs/$ARTIFACT_NAME $APP_HOME/
WORKDIR $APP_HOME
RUN groupadd -r -g 1010 user && useradd -r -g user -u 1010 user
RUN chown -R user:user /app
USER ${USER}

ENTRYPOINT exec java -jar ${ARTIFACT_NAME}

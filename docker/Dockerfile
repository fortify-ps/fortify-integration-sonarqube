FROM sonarqube:lts-alpine
RUN rm $SONARQUBE_HOME/extensions/plugins/*
COPY fortify-sonarqube-plugin.jar $SONARQUBE_HOME/extensions/plugins/fortify-sonarqube-plugin.jar
ENTRYPOINT ["./bin/run.sh"]

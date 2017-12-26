#docker build --pull --rm -t "jqm/openjdk:latest" -t "jqm/openjdk:8u151-jdk-nanoserver-sac-1709" -f $PSScriptRoot\DockerFile.jdk .
#docker build --rm -t "jqm/openjdk:latest-jre" -f DockerFile.jre .
#docker build --rm -t "jqm/maven:latest" -f $PSScriptRoot\DockerFile.maven .

docker build --rm -t "jqm:latest" -f $PSScriptRoot\DockerFile $PSScriptRoot/../..

#  docker run -it --rm -p 1789:1789 jqm
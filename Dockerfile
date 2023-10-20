FROM ubuntu:latest
LABEL authors="asmol"

ENTRYPOINT ["top", "-b"]
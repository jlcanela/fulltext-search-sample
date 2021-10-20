# Requirements

## Context

A web server is generating logs. 
The goal is to process the logs so that they can be indexed into an elastic search. 

## Global architecture

The business architecture implements two systems, a web server system and a monitoring system. 


![High level architecture](high-level.png)

## Batch

A batch processes the webserver logs.

The steps are the following:

![Batch processing](batch-processing.png)

# Design

## Detailed Architecture

![Detailed processing](architecture.png)

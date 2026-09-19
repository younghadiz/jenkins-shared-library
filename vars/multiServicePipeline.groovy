#!/usr/bin/env groovy

/*
  Multi-service pipeline entry point.

  Intended for repositories containing multiple independently
  buildable and deployable services, for example:

  services/
  ├── frontend/
  ├── backend/
  └── worker/

  This pipeline will be implemented when a project requires
  multiple services.

  Current single-service projects should use:

      singleServicePipeline(...)

  Keeping this entry point separate prevents multi-service
  requirements from adding unnecessary complexity to the
  single-service pipeline.
*/

def call(Map config = [:]) {
    error '''
Multi-service pipeline is not implemented yet.

Use singleServicePipeline(...) for a single deployable application.
Implement this pipeline when a project contains multiple independently
buildable or deployable services.
'''
}

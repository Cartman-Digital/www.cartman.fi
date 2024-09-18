#!/bin/bash 

gcloud compute url-maps import --project www-cartman-fi --global --source=url-map.yaml site

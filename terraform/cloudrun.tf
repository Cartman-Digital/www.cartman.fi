# module "cloud_run" {
#   source  = "GoogleCloudPlatform/cloud-run/google"
#   version = "0.6.0"

#   # Required variables
#   service_name           = "site-${var.environment}"
#   project_id             = var.project_id
#   location               = "europe-north1"
#   image                  = "ghcr.io/cartman-digital/www.cartman.fi:webserver-setup"
# }
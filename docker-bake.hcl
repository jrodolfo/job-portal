group "default" {
  targets = ["backend", "frontend"]
}

target "backend" {
  context    = "./job-portal-backend"
  dockerfile = "Dockerfile"
  platforms  = ["linux/amd64", "linux/arm64"]
  tags       = ["jrodolfo/job-portal-backend:latest"]
}

target "frontend" {
  context    = "./job-portal-frontend"
  dockerfile = "Dockerfile"
  platforms  = ["linux/amd64", "linux/arm64"]
  tags       = ["jrodolfo/job-portal-frontend:latest"]
}

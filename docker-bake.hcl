group "default" {
  targets = ["backend", "frontend"]
}

target "backend" {
  context    = "./job-portal-backend"
  dockerfile = "Dockerfile"
  tags       = ["jrodolfo/job-portal-backend:latest"]
}

target "frontend" {
  context    = "./job-portal-frontend"
  dockerfile = "Dockerfile"
  tags       = ["jrodolfo/job-portal-frontend:latest"]
}

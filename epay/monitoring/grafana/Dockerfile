FROM grafana/grafana:10.4.2

# Provisioning: datasources & dashboard providers
COPY provisioning/datasources/datasources.yml /etc/grafana/provisioning/datasources/datasources.yml
COPY provisioning/dashboards/dashboards.yml   /etc/grafana/provisioning/dashboards/dashboards.yml

# Dashboard JSON files
COPY dashboards/ /var/lib/grafana/dashboards/

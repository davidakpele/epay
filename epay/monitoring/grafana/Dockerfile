FROM grafana/grafana:10.4.2

COPY provisioning/datasources/datasources.yml /etc/grafana/provisioning/datasources/datasources.yml
COPY provisioning/dashboards/dashboards.yml   /etc/grafana/provisioning/dashboards/dashboards.yml

COPY dashboards/ /var/lib/grafana/dashboards/

// services/noop_metrics.go
package services

type NoopMetrics struct{}

func (n *NoopMetrics) IncrementCounter(name string, tags ...string) {}
func (n *NoopMetrics) RecordHistogram(name string, value float64, tags ...string) {}
func (n *NoopMetrics) SetGauge(name string, value float64, tags ...string) {}

func NewNoopMetrics() Metrics {
	return &NoopMetrics{}
}
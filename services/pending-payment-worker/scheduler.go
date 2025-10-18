package main

import (
	"context"
	"log"
	"time"
)

type PendingPaymentScheduler struct {
	processor *PendingPaymentProcessor
	interval  time.Duration
}

func NewPendingPaymentScheduler(processor *PendingPaymentProcessor, interval time.Duration) *PendingPaymentScheduler {
	if interval <= 0 {
		interval = time.Minute
	}
	return &PendingPaymentScheduler{
		processor: processor,
		interval:  interval,
	}
}

func (s *PendingPaymentScheduler) Run(ctx context.Context) error {
	if err := s.processor.ProcessBatch(ctx); err != nil {
		log.Printf("process batch error: %v", err)
	}

	ticker := time.NewTicker(s.interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-ticker.C:
			if err := s.processor.ProcessBatch(ctx); err != nil {
				log.Printf("process batch error: %v", err)
			}
		}
	}
}

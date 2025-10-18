package main

import (
	"context"
	"database/sql"
	"log"
	"sync"
	"time"
)

type pendingPayment struct {
	id         int
	orderID    string
	paymentKey string
	price      int
	createdAt  time.Time
	confirmed  bool
	failReason sql.NullString
}

// TossClient is an interface that verifies a payment via TossPayments.
type TossClient interface {
	Verify(ctx context.Context, orderID string, amount int, paymentKey string) (bool, error)
}

type PendingPaymentProcessor struct {
	db           *sql.DB
	workerCount  int
	pageSize     int
	batchTimeout time.Duration
	toss         TossClient
}

func NewPendingPaymentProcessor(db *sql.DB, workerCount int, toss TossClient) *PendingPaymentProcessor {
	if workerCount < 1 {
		workerCount = 1
	}

	return &PendingPaymentProcessor{
		db:           db,
		workerCount:  workerCount,
		pageSize:     100,
		batchTimeout: 10 * time.Second,
		toss:         toss,
	}
}

func (p *PendingPaymentProcessor) ProcessBatch(parentCtx context.Context) error {
	ctx, cancel := context.WithTimeout(parentCtx, p.batchTimeout)
	defer cancel()

	total := 0
	pageNumber := 1
	lastCreatedAt := time.Time{}
	lastID := 0

	baseQuery := `SELECT id, order_id, payment_key, price, created_at, confirmed, fail_reason
		FROM pending_payment
		WHERE confirmed = false
		  AND created_at > NOW() - INTERVAL 10 MINUTE`

	orderClause := `
		ORDER BY created_at, id
		LIMIT ?`

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		default:
		}

		var (
			rows *sql.Rows
			err  error
		)

		if total == 0 {
			rows, err = p.db.QueryContext(ctx, baseQuery+orderClause, p.pageSize)
		} else {
			rows, err = p.db.QueryContext(ctx, baseQuery+`
		  AND (created_at > ? OR (created_at = ? AND id > ?))`+orderClause,
				lastCreatedAt, lastCreatedAt, lastID, p.pageSize)
		}
		if err != nil {
			return err
		}

		pendings := make([]pendingPayment, 0, p.pageSize)
		for rows.Next() {
			var pending pendingPayment
			if err := rows.Scan(&pending.id, &pending.orderID, &pending.paymentKey, &pending.price, &pending.createdAt, &pending.confirmed, &pending.failReason); err != nil {
				log.Printf("scan error: %v", err)
				continue
			}
			pendings = append(pendings, pending)
		}

		if err := rows.Err(); err != nil {
			rows.Close()
			return err
		}
		rows.Close()

		if len(pendings) == 0 {
			if total == 0 {
				log.Println("no pending payments found")
			}
			return nil
		}

		log.Printf("page %d: found %d pending payments", pageNumber, len(pendings))

		last := pendings[len(pendings)-1]
		lastCreatedAt = last.createdAt
		lastID = last.id

		if err := p.runWorkerPool(ctx, pendings); err != nil {
			return err
		}

		total += len(pendings)
		if len(pendings) < p.pageSize {
			return nil
		}

		pageNumber++
	}
}

func (p *PendingPaymentProcessor) runWorkerPool(ctx context.Context, pendings []pendingPayment) error {
	workers := p.workerCount
	if workers > len(pendings) {
		workers = len(pendings)
	}

	jobs := make(chan pendingPayment)
	var wg sync.WaitGroup

	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for pending := range jobs {
				if err := p.processPending(ctx, pending); err != nil {
					log.Printf("pending payment id=%d processing error: %v", pending.id, err)
				}
			}
		}()
	}

	cancelled := false

sendLoop:
	for _, pending := range pendings {
		select {
		case <-ctx.Done():
			cancelled = true
			break sendLoop
		case jobs <- pending:
		}
	}

	close(jobs)
	wg.Wait()

	if cancelled {
		return ctx.Err()
	}

	return nil
}

func (p *PendingPaymentProcessor) processPending(ctx context.Context, pending pendingPayment) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	default:
	}

	failReason := ""
	if pending.failReason.Valid {
		failReason = pending.failReason.String
	}

	log.Printf("id=%d orderId=%s paymentKey=%s price=%d createdAt=%s confirmed=%v failReason=%s",
		pending.id, pending.orderID, pending.paymentKey, pending.price, pending.createdAt.Format(time.RFC3339), pending.confirmed, failReason)

	if p.toss == nil {
		log.Printf("no TossClient provided; skipping verification for orderId=%s", pending.orderID)
		return nil
	}

	ok, err := p.toss.Verify(ctx, pending.orderID, pending.price, pending.paymentKey)
	if err != nil {
		log.Printf("orderId=%s paymentKey=%s verification error: %v", pending.orderID, pending.paymentKey, err)
		return nil
	}
	if !ok {
		log.Printf("orderId=%s paymentKey=%s failed verification", pending.orderID, pending.paymentKey)
	}

	return nil
}

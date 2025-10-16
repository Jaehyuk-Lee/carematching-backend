package main

import (
	"context"
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"time"

	"path/filepath"

	_ "github.com/go-sql-driver/mysql"
	"github.com/joho/godotenv"
)

func main() {
	// Load .env from current or parent directories (up to 5 levels)
	envLoaded := false
	dir, _ := os.Getwd()
	for i := 0; i < 6; i++ {
		envPath := filepath.Join(dir, ".env")
		if _, err := os.Stat(envPath); err == nil {
			if err := godotenv.Load(envPath); err != nil {
				log.Printf("failed to load %s: %v", envPath, err)
			} else {
				log.Printf("loaded env from %s", envPath)
				envLoaded = true
				break
			}
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			break
		}
		dir = parent
	}
	if !envLoaded {
		log.Println(".env not found in current or parent directories")
	}

	db_ip := os.Getenv("DB_IP")
	db_username := os.Getenv("DB_USERNAME")
	db_password := os.Getenv("DB_PASSWORD")
	db_name := os.Getenv("DB_NAME")

	if db_ip == "" || db_username == "" || db_password == "" || db_name == "" {
		log.Fatal("DB_ environment variable is required")
	}

	// Expect db_uri in the form: username:password@tcp(host:port)/dbname?parseTime=true
	db_uri := fmt.Sprintf("%s:%s@tcp(%s:3306)/%s?parseTime=true", db_username, db_password, db_ip, db_name)

	db, err := sql.Open("mysql", db_uri)
	if err != nil {
		log.Fatalf("failed to open db: %v", err)
	}
	defer db.Close()

	// simple HTTP for health
	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		fmt.Fprintln(w, "ok")
	})

	srv := &http.Server{Addr: ":8081", Handler: mux}
	go func() {
		log.Println("worker http listening :8081")
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("http server error: %v", err)
		}
	}()

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt)
	defer stop()

	ticker := time.NewTicker(60 * time.Second)
	defer ticker.Stop()

	// run immediately once on startup
	go processBatch(db)

	for {
		select {
		case <-ctx.Done():
			log.Println("shutting down worker")
			srv.Shutdown(context.Background())
			return
		case <-ticker.C:
			processBatch(db)
		}
	}
}

func processBatch(db *sql.DB) {
	// This is a minimal PoC: fetch up to 100 pending payments and mark checked.
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// Note: SKIP LOCKED is supported in MySQL 8.0+ and recent MariaDB versions. Adjust if your version doesn't support it.
	rows, err := db.QueryContext(ctx, `SELECT id, order_id, price, payment_key FROM pending_payment WHERE confirmed = false AND created_at > NOW() - INTERVAL 10 MINUTE LIMIT 100 FOR UPDATE SKIP LOCKED`)
	if err != nil {
		log.Printf("query error: %v", err)
		return
	}
	defer rows.Close()

	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		log.Printf("begin tx error: %v", err)
		return
	}

	type pending struct {
		id         int
		orderID    string
		price      int
		paymentKey string
	}

	pendings := make([]pending, 0)
	for rows.Next() {
		var p pending
		if err := rows.Scan(&p.id, &p.orderID, &p.price, &p.paymentKey); err != nil {
			log.Printf("scan error: %v", err)
			continue
		}
		pendings = append(pendings, p)
	}
	if err := rows.Err(); err != nil {
		log.Printf("rows err: %v", err)
		tx.Rollback()
		return
	}

	// commit initial transaction which only locked rows
	if err := tx.Commit(); err != nil {
		log.Printf("tx commit err: %v", err)
		return
	}

	for _, p := range pendings {
		ok := verifyTossPayment(p.orderID, p.price, p.paymentKey)
		if ok {
			if _, err := db.ExecContext(ctx, `UPDATE pending_payment SET confirmed = true, fail_reason = NULL WHERE id = $1`, p.id); err != nil {
				log.Printf("update confirmed err: %v", err)
			}
		} else {
			if _, err := db.ExecContext(ctx, `UPDATE pending_payment SET fail_reason = $1 WHERE id = $2`, "결제 상태가 DONE이 아님", p.id); err != nil {
				log.Printf("update fail reason err: %v", err)
			}
		}
	}
}

func verifyTossPayment(orderID string, price int, paymentKey string) bool {
	// PoC: pretend verify by calling a mock HTTP endpoint or simple logic
	if paymentKey == "ok" {
		return true
	}
	return false
}

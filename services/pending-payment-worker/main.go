package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"time"

	_ "github.com/go-sql-driver/mysql"

	utils "Jaehyuk-Lee/carematching/services/utils"
)

const (
	httpAddr              = ":8081"
	scheduleInterval      = 60 * time.Second
	workerPoolSize        = 5
	serverShutdownTimeout = 5 * time.Second
)

func main() {
	utils.ReadEnv()

	db, err := utils.OpenMySQL()
	if err != nil {
		log.Fatalf("failed to open db: %v", err)
	}
	defer db.Close()

	// initialize Toss client and processor
	tossSecret := os.Getenv("TOSS_SECRET")
	tossTimeouts := []time.Duration{5 * time.Second, 17500 * time.Millisecond, 30 * time.Second}
	tossClient := NewDefaultTossClient(tossSecret, tossTimeouts)
	processor := NewPendingPaymentProcessor(db, workerPoolSize, tossClient)
	scheduler := NewPendingPaymentScheduler(processor, scheduleInterval)

	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		fmt.Fprintln(w, "ok")
	})

	srv := &http.Server{Addr: httpAddr, Handler: mux}
	go func() {
		log.Printf("worker http listening %s", httpAddr)
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatalf("http server error: %v", err)
		}
	}()

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt)
	defer stop()

	if err := scheduler.Run(ctx); err != nil && !errors.Is(err, context.Canceled) {
		log.Printf("scheduler stopped with error: %v", err)
	}

	log.Println("shutting down worker")
	shutdownCtx, cancel := context.WithTimeout(context.Background(), serverShutdownTimeout)
	defer cancel()

	if err := srv.Shutdown(shutdownCtx); err != nil {
		log.Printf("http shutdown error: %v", err)
	}
}

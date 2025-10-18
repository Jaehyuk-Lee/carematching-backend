package main

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"time"
)

// DefaultTossClient implements TossClient and contains reusable HTTP clients and configuration.
type DefaultTossClient struct {
	clients []*http.Client
	url     string
	secret  string
}

// NewDefaultTossClient returns a configured DefaultTossClient. timeouts is a slice of timeouts
// used for retry attempts; length should be >= max attempts.
func NewDefaultTossClient(secret string, timeouts []time.Duration) *DefaultTossClient {
	clients := make([]*http.Client, len(timeouts))
	for i, t := range timeouts {
		transport := &http.Transport{
			Proxy:               http.ProxyFromEnvironment,
			MaxIdleConns:        100,
			IdleConnTimeout:     90 * time.Second,
			TLSHandshakeTimeout: 10 * time.Second,
		}
		clients[i] = &http.Client{Transport: transport, Timeout: t}
	}
	url := os.Getenv("TOSS_URL")
	if url == "" {
		url = "https://api.tosspayments.com/v1/payments/confirm"
	}
	return &DefaultTossClient{clients: clients, url: url, secret: secret}
}

// Verify calls TossPayments confirm API and returns whether the payment status is DONE.
func (c *DefaultTossClient) Verify(ctx context.Context, orderID string, amount int, paymentKey string) (bool, error) {
	if c.secret == "" {
		return false, fmt.Errorf("TOSS secret not provided")
	}

	reqBody := map[string]interface{}{
		"orderId":    orderID,
		"amount":     amount,
		"paymentKey": paymentKey,
	}
	bodyBytes, _ := json.Marshal(reqBody)

	maxAttempts := len(c.clients)
	if maxAttempts == 0 {
		return false, fmt.Errorf("no http clients configured")
	}

	for attempt := 0; attempt < maxAttempts; attempt++ {
		select {
		case <-ctx.Done():
			return false, ctx.Err()
		default:
		}

		client := c.clients[attempt]

		req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.url, bytes.NewReader(bodyBytes))
		if err != nil {
			return false, err
		}
		req.Header.Set("Content-Type", "application/json")
		auth := base64.StdEncoding.EncodeToString([]byte(c.secret + ":"))
		req.Header.Set("Authorization", "Basic "+auth)

		resp, err := client.Do(req)
		if err != nil {
			log.Printf("network error contacting TossPayments (attempt %d): %v", attempt+1, err)
			// simple backoff
			time.Sleep(time.Duration(attempt+1) * time.Second)
			continue
		}

		respBody, _ := io.ReadAll(resp.Body)
		resp.Body.Close()

		if resp.StatusCode < 200 || resp.StatusCode >= 300 {
			return false, fmt.Errorf("tosspayments returned status %d: %s", resp.StatusCode, string(respBody))
		}

		var parsed map[string]interface{}
		if err := json.Unmarshal(respBody, &parsed); err != nil {
			return false, fmt.Errorf("failed to parse toss response: %w", err)
		}
		if status, ok := parsed["status"].(string); ok {
			return status == "DONE", nil
		}
		return false, fmt.Errorf("missing status in toss response: %s", string(respBody))
	}

	return false, fmt.Errorf("max attempts reached")
}

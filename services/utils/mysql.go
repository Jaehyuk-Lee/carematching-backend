package utils

import (
	"database/sql"
	"log"
	"os"
	"time"

	"github.com/go-sql-driver/mysql"
)

func checkEnvs() {
	requiredEnvs := []string{"DB_IP", "DB_USERNAME", "DB_PASSWORD", "DB_NAME"}
	for _, env := range requiredEnvs {
		if os.Getenv(env) == "" {
			log.Fatalf("Environment variable %s is required", env)
		}
	}
}

func OpenMySQL() (*sql.DB, error) {
	checkEnvs()

	cfg := mysql.Config{
		Net:                  "tcp",
		Addr:                 os.Getenv("DB_IP") + ":3306",
		User:                 os.Getenv("DB_USERNAME"),
		Passwd:               os.Getenv("DB_PASSWORD"),
		DBName:               os.Getenv("DB_NAME"),
		ParseTime:            true,
		Loc:                  time.Local,
		Timeout:              5 * time.Second, // connect timeout
		ReadTimeout:          10 * time.Second,
		WriteTimeout:         10 * time.Second,
		AllowNativePasswords: true,
	}
	conn, err := mysql.NewConnector(&cfg)
	if err != nil {
		return nil, err
	}
	return sql.OpenDB(conn), nil
}

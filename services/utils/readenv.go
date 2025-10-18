package utils

import (
	"log"
	"os"
	"path/filepath"

	"github.com/joho/godotenv"
)

func ReadEnv() {
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
}

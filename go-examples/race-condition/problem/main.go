// go run -race 로 실행하면 경고가 뜸
package main

import (
	"fmt"
	"runtime"
	"sync"
)

func main() {
	runtime.GOMAXPROCS(2) // 실제 두 OS 스레드에서 병렬 실행 유도
	var wg sync.WaitGroup
	wg.Add(2)

	counter := 0

	go func() { // 고루틴 A
		defer wg.Done()
		for i := 0; i < 100000; i++ {
			counter++ // 동시 갱신 → 레이스
		}
	}()

	go func() { // 고루틴 B
		defer wg.Done()
		for i := 0; i < 100000; i++ {
			counter++ // 동시 갱신 → 레이스
		}
	}()

	wg.Wait()
	fmt.Println("counter:", counter) // 기대 200000, 실제 불일치 가능
}
